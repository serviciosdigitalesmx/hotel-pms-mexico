import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import type { OrderStatus } from '../../api/types'
import { dashboardApi, ordersApi } from '../../api/endpoints'
import { errorMessage } from '../../api/http'
import { useSession } from '../../auth/session'
import { AreaChart } from '../../components/charts'
import { PriorityBadge, StatusBadge } from '../../components/domain'
import { AppIcon } from '../../components/layout/AppLayout'
import { Alert, Button, EmptyState, ErrorState, Loading, Table, Td, Th } from '../../components/ui'
import { amounts, dateOnly, initials, money, relative } from '../../lib/format'
import { OPEN_STATUSES } from '../../lib/labels'

const STATUS_COLORS: Record<OrderStatus, string> = {
  Received: '#4f9391',
  Diagnosing: '#7898b2',
  WaitingParts: '#d39a52',
  InProgress: '#bd8150',
  Testing: '#709d8b',
  Ready: '#559477',
  Delivered: '#72858f',
  Cancelled: '#b86662',
}

function revenueValue(summary: { converted?: number | null; byCurrency: { currency: string; amount: number }[] } | null | undefined, currency: string) {
  if (!summary) return '—'
  return summary.converted !== null && summary.converted !== undefined ? money(summary.converted, currency) : amounts(summary.byCurrency)
}

export function DashboardPage() {
  const { user, can, shops } = useSession()
  const navigate = useNavigate()
  const summary = useQuery({ queryKey: ['dashboard', 'summary'], queryFn: dashboardApi.summary, refetchInterval: 60_000 })
  const revenue = useQuery({ queryKey: ['dashboard', 'revenue', 30], queryFn: () => dashboardApi.revenue(30), enabled: can('reports') })
  const consolidated = useQuery({ queryKey: ['dashboard', 'consolidated'], queryFn: dashboardApi.consolidated, enabled: can('reports') && shops.length > 1 })
  const mine = useQuery({
    queryKey: ['orders', 'mine'],
    queryFn: () => ordersApi.search({ mine: true, onlyOpen: true, sortBy: 'promisedAt', sortDir: 'asc', take: 10 }),
    enabled: user?.role === 'Tech',
  })

  if (summary.isLoading) return <Loading label="Actualizando el pulso del taller…" />
  if (summary.isError) return <ErrorState error={errorMessage(summary.error)} onRetry={() => summary.refetch()} />

  const s = summary.data!
  const currency = s.reportingCurrency ?? 'ARS'
  const statusCounts = s.statusCounts ?? {}
  const openByStatus = OPEN_STATUSES.reduce((total, status) => total + (statusCounts[status] ?? 0), 0)
  const flowMax = Math.max(1, ...OPEN_STATUSES.map((status) => statusCounts[status] ?? 0))
  const revenueTotal = (revenue.data ?? []).reduce((total, point) => total + (point.converted ?? 0), 0)
  const technicians = s.technicians ?? []

  const focusItems = [
    {
      key: 'overdue',
      label: 'Órdenes atrasadas',
      note: 'Pasaron la fecha prometida',
      value: s.overdueOrders,
      priority: 'is-high',
      icon: 'clock',
      to: '/orders?onlyOverdue=true',
    },
    {
      key: 'ready',
      label: 'Listas sin retirar',
      note: 'Superaron el plazo de aviso configurado',
      value: s.readyNotPickedUp,
      priority: 'is-medium',
      icon: 'pickup',
      to: '/orders?status=Ready',
    },
    {
      key: 'quotes',
      label: 'Cotizaciones pendientes',
      note: 'Esperan decisión del cliente',
      value: s.quotesPendingDecision,
      priority: 'is-medium',
      icon: 'quote',
      to: '/orders?status=Diagnosing',
    },
    {
      key: 'stale',
      label: 'Órdenes estancadas',
      note: 'Sin cambios recientes de estado',
      value: s.staleOrders,
      priority: 'is-low',
      icon: 'pause',
      to: '/orders/board',
    },
    {
      key: 'stock',
      label: 'Refacciones con stock bajo',
      note: 'Requieren revisión de compra',
      value: s.lowStockItems,
      priority: 'is-medium',
      icon: 'inventory',
      to: '/inventory?lowStock=1',
    },
  ].filter((item) => item.value > 0)

  const summaryCells = [
    {
      label: 'Órdenes abiertas',
      value: s.openOrders,
      note: openByStatus > 0 ? `${openByStatus} distribuidas en el flujo` : 'Sin trabajo en curso',
      icon: 'orders',
      tone: '',
      onClick: () => navigate('/orders?onlyOpen=true'),
    },
    {
      label: 'Listas para retirar',
      value: s.readyOrders,
      note: s.readyNotPickedUp > 0 ? `${s.readyNotPickedUp} superaron el plazo de aviso` : 'Mostrador al día',
      icon: 'pickup',
      tone: s.readyNotPickedUp > 0 ? 'is-warn' : '',
      onClick: () => navigate('/orders?status=Ready'),
    },
    {
      label: 'Atrasadas',
      value: s.overdueOrders,
      note: s.overdueOrders > 0 ? 'Requieren atención hoy' : 'Nada fuera de fecha',
      icon: 'clock',
      tone: s.overdueOrders > 0 ? 'is-danger' : '',
      onClick: () => navigate('/orders?onlyOverdue=true'),
    },
    can('sales') || can('reports')
      ? {
          label: 'Cobrado hoy',
          value: revenueValue(s.today, currency),
          note: s.today && s.today.byCurrency.length > 1 ? amounts(s.today.byCurrency) : 'Pagos registrados hoy',
          icon: 'cash',
          tone: 'is-info',
          onClick: can('sales') ? () => navigate('/cash') : () => navigate('/reports?r=revenue'),
        }
      : {
          label: 'Cotizaciones sin respuesta',
          value: s.quotesPendingDecision,
          note: 'Pendientes de decisión',
          icon: 'quote',
          tone: s.quotesPendingDecision > 0 ? 'is-warn' : '',
          onClick: () => navigate('/orders?status=Diagnosing'),
        },
  ]

  return (
    <div className="fx-dashboard">
      <header className="fixi-page-header mb-0 flex flex-wrap items-end justify-between gap-4">
        <div className="min-w-0">
          <p className="fx-kicker">Centro de operaciones</p>
          <h1>Hola, {user?.displayName?.split(' ')[0] ?? ''}</h1>
          <p className="mt-1 text-xs text-slate-500">
            {user?.shopName} <span className="mx-1.5 text-slate-300">·</span>
            <span className="fx-live">actualizado {relative(s.generatedAtUtc)}</span>
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {can('sales') ? <Button variant="default" onClick={() => navigate('/pos')}>Abrir punto de venta</Button> : null}
          {can('orders.manage') ? <Button variant="primary" onClick={() => navigate('/orders/new')}>+ Recibir equipo</Button> : null}
        </div>
      </header>

      {can('sales') && !s.cashSessionOpen ? (
        <Alert tone="amber" title="La caja está cerrada">
          Ábrela antes de registrar cobros en efectivo. <Link to="/cash" className="font-semibold underline underline-offset-2">Abrir caja</Link>
        </Alert>
      ) : null}
      {s.missingRates && s.missingRates.length > 0 ? (
        <Alert tone="amber" title="Hay monedas sin tipo de cambio">
          No se suman al total en {currency} los importes de {s.missingRates.join(', ')}. Revisa cotizaciones en Facturas.
        </Alert>
      ) : null}

      <section className="fx-summary-strip" aria-label="Resumen operativo">
        {summaryCells.map((item) => (
          <button key={item.label} type="button" className="fx-summary-cell" onClick={item.onClick}>
            <span className={`fx-metric-icon ${item.tone}`}>
              <AppIcon name={item.icon} />
            </span>
            <span className="fx-metric-copy">
              <span className="fx-metric-label">{item.label}</span>
              <span className="fx-metric-value">{item.value}</span>
              <span className="fx-metric-note">{item.note}</span>
            </span>
          </button>
        ))}
      </section>

      <div className="fx-dashboard-main">
        <section className="fx-panel" aria-labelledby="flow-title">
          <div className="fx-panel-head">
            <div>
              <h2 id="flow-title" className="fx-panel-title">Pulso del flujo</h2>
              <p className="fx-panel-sub">Carga actual por etapa abierta, desde recepción hasta entrega.</p>
            </div>
            <Link to="/orders/board" className="fx-panel-link">Abrir tablero ↗</Link>
          </div>
          <div className="fx-flow-grid">
            {OPEN_STATUSES.map((status) => {
              const count = statusCounts[status] ?? 0
              return (
                <Link key={status} to={`/orders?status=${status}`} className={`fx-flow-stage ${count > 0 ? 'has-work' : ''}`}>
                  <span className="fx-flow-count">
                    {String(count).padStart(2, '0')} <small>órdenes</small>
                  </span>
                  <span className="fx-flow-name">{status === 'Received' ? 'Recibido' : status === 'Diagnosing' ? 'Diagnóstico' : status === 'WaitingParts' ? 'Esperando refacción' : status === 'InProgress' ? 'En reparación' : status === 'Testing' ? 'En pruebas' : 'Listo para retirar'}</span>
                  <span className="fx-stage-bar" aria-hidden="true">
                    <i style={{ width: `${count === 0 ? 0 : Math.max(8, (count / flowMax) * 100)}%` }} />
                  </span>
                </Link>
              )
            })}
          </div>
          <div className="fx-flow-foot">
            <span className="fx-pulse" />
            <span><strong>{s.openOrders}</strong> órdenes abiertas</span>
            <span className="ml-auto">{s.staleOrders > 0 ? `${s.staleOrders} requieren seguimiento` : 'El flujo se está moviendo'}</span>
          </div>
        </section>

        <section className="fx-panel" aria-labelledby="focus-title">
          <div className="fx-panel-head">
            <div>
              <h2 id="focus-title" className="fx-panel-title">Atención priorizada</h2>
              <p className="fx-panel-sub">Lo que conviene resolver primero con los datos actuales.</p>
            </div>
          </div>
          {focusItems.length > 0 ? (
            <div className="fx-focus-list">
              {focusItems.map((item) => (
                <Link key={item.key} to={item.to} className={`fx-focus-row ${item.priority}`}>
                  <span className="fx-focus-icon"><AppIcon name={item.icon} /></span>
                  <span className="fx-focus-copy"><b>{item.label}</b><small>{item.note}</small></span>
                  <span className="fx-focus-count">{item.value}</span>
                </Link>
              ))}
            </div>
          ) : (
            <p className="fx-focus-clear">No hay alertas operativas pendientes.</p>
          )}
        </section>
      </div>

      <div className="fx-dashboard-lower">
        {can('reports') ? (
          <section className="fx-panel" aria-labelledby="revenue-title">
            <div className="fx-panel-head">
              <div>
                <h2 id="revenue-title" className="fx-panel-title">Cobros de los últimos 30 días</h2>
                <p className="fx-panel-sub">Dinero registrado, no ventas cotizadas.</p>
              </div>
              <Link to="/reports?r=revenue" className="fx-panel-link">Ver reporte ↗</Link>
            </div>
            <div className="fx-chart-wrap">
              <div className="fx-chart-meta">
                <span className="fx-chart-total">{money(revenueTotal, currency)}</span>
                <span className="fx-chart-chip">30 días</span>
              </div>
              {revenue.isLoading ? (
                <Loading label="Cargando cobros…" />
              ) : revenue.isError ? (
                <ErrorState error={errorMessage(revenue.error)} onRetry={() => revenue.refetch()} />
              ) : (
                <>
                  <AreaChart
                    title="Cobros diarios de los últimos 30 días"
                    data={(revenue.data ?? []).map((point) => ({
                      label: dateOnly(point.date).slice(0, 5),
                      value: point.converted ?? 0,
                      hint: point.byCurrency.length > 1 ? amounts(point.byCurrency) : undefined,
                    }))}
                    format={(value) => money(value, currency).replace(/,00$/, '')}
                    height={205}
                  />
                  <p className="fx-chart-caption"><span>Importes convertidos a {currency}</span><span>Solo pagos recibidos</span></p>
                </>
              )}
            </div>
          </section>
        ) : user?.role === 'Tech' ? (
          <section className="fx-panel" aria-labelledby="mine-title">
            <div className="fx-panel-head">
              <div>
                <h2 id="mine-title" className="fx-panel-title">Mis órdenes abiertas</h2>
                <p className="fx-panel-sub">Trabajo asignado, ordenado por fecha prometida.</p>
              </div>
              <Link to="/orders/board?mine=true" className="fx-panel-link">Ver tablero ↗</Link>
            </div>
            <div className="p-4 pt-0">
              {mine.isLoading ? (
                <Loading />
              ) : mine.isError ? (
                <ErrorState error={errorMessage(mine.error)} onRetry={() => mine.refetch()} />
              ) : (mine.data?.items.length ?? 0) === 0 ? (
                <EmptyState title="No tienes órdenes asignadas" description="El trabajo nuevo aparecerá aquí cuando recepción lo asigne." />
              ) : (
                <Table>
                  <thead>
                    <tr><Th>Orden</Th><Th>Equipo</Th><Th>Estado</Th><Th>Promesa</Th></tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {mine.data!.items.map((order) => (
                      <tr key={order.id} className="cursor-pointer" onClick={() => navigate(`/orders/${order.id}`)}>
                        <Td className="font-semibold">{order.code} <PriorityBadge priority={order.priority} hideNormal /></Td>
                        <Td>{order.deviceLabel}</Td>
                        <Td><StatusBadge status={order.status} /></Td>
                        <Td className={order.isOverdue ? 'font-medium text-rose-700' : undefined}>{order.promisedAtUtc ? relative(order.promisedAtUtc) : '—'}</Td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </div>
          </section>
        ) : (
          <section className="fx-panel" aria-labelledby="next-title">
            <div className="fx-panel-head">
              <div>
                <h2 id="next-title" className="fx-panel-title">Siguiente paso</h2>
                <p className="fx-panel-sub">Accesos directos para mover la operación del día.</p>
              </div>
            </div>
            <div className="grid gap-2 p-4 pt-0 sm:grid-cols-2">
              <Link to="/orders?onlyOpen=true" className="fx-focus-row"><span className="fx-focus-icon"><AppIcon name="orders" /></span><span className="fx-focus-copy"><b>Revisar órdenes abiertas</b><small>{s.openOrders} en curso</small></span><span className="fx-focus-count">→</span></Link>
              <Link to="/inventory?lowStock=1" className="fx-focus-row"><span className="fx-focus-icon"><AppIcon name="inventory" /></span><span className="fx-focus-copy"><b>Revisar inventario</b><small>{s.lowStockItems} con stock bajo</small></span><span className="fx-focus-count">→</span></Link>
            </div>
          </section>
        )}

        <section className="fx-panel" aria-labelledby="status-title">
          <div className="fx-panel-head">
            <div>
              <h2 id="status-title" className="fx-panel-title">Distribución por estado</h2>
              <p className="fx-panel-sub">Composición de las órdenes abiertas en esta sucursal.</p>
            </div>
          </div>
          <div className="fx-status-summary">
            <div
              className="fx-donut"
              style={{
                background:
                  openByStatus > 0
                    ? `conic-gradient(${OPEN_STATUSES.map((status, index) => {
                        const before = OPEN_STATUSES.slice(0, index).reduce((sum, current) => sum + (statusCounts[current] ?? 0), 0)
                        const start = (before / openByStatus) * 100
                        const end = ((before + (statusCounts[status] ?? 0)) / openByStatus) * 100
                        return `${STATUS_COLORS[status]} ${start}% ${end}%`
                      }).join(',')})`
                    : undefined,
              }}
            >
              <div className="fx-donut-center"><strong>{openByStatus}</strong><span>abiertas</span></div>
            </div>
            <div className="fx-status-list">
              {OPEN_STATUSES.map((status) => (
                <Link key={status} to={`/orders?status=${status}`} className="fx-status-row">
                  <i className="fx-status-dot" style={{ background: STATUS_COLORS[status] }} />
                  <span className="fx-status-name"><StatusBadge status={status} /></span>
                  <span className="fx-status-value">{String(statusCounts[status] ?? 0).padStart(2, '0')}</span>
                </Link>
              ))}
            </div>
          </div>
        </section>
      </div>

      <div className="fx-dashboard-bottom">
        <section className="fx-panel" aria-labelledby="team-title">
          <div className="fx-panel-head">
            <div>
              <h2 id="team-title" className="fx-panel-title">Carga del equipo</h2>
              <p className="fx-panel-sub">Órdenes abiertas y atrasadas por técnico.</p>
            </div>
            <Link to="/orders/board" className="fx-panel-link">Ver tablero ↗</Link>
          </div>
          {technicians.length > 0 ? (
            <div className="fx-tech-grid">
              {technicians.slice(0, 6).map((technician) => (
                <Link key={technician.userId} to={`/orders?technicianId=${technician.userId}&onlyOpen=true`} className="fx-tech-card">
                  <span className="fx-tech-person">
                    <span className="fx-tech-avatar">{initials(technician.name)}</span>
                    <span className="min-w-0"><span className="fx-tech-name">{technician.name}</span><span className="fx-tech-role">Técnico</span></span>
                  </span>
                  <span className="fx-tech-stats"><span>Abiertas</span><b>{technician.openOrders}</b></span>
                  <span className="fx-tech-stats"><span>Atrasadas</span><b className={technician.overdueOrders > 0 ? 'is-alert' : ''}>{technician.overdueOrders}</b></span>
                </Link>
              ))}
            </div>
          ) : (
            <p className="fx-focus-clear">Todavía no hay órdenes asignadas al equipo.</p>
          )}
        </section>

        <section className="fx-panel" aria-labelledby="cash-title">
          <div className="fx-panel-head">
            <div>
              <h2 id="cash-title" className="fx-panel-title">Estado de caja</h2>
              <p className="fx-panel-sub">Control de cobros de la sucursal en este turno.</p>
            </div>
            {can('sales') ? <Link to="/cash" className="fx-panel-link">Ir a caja ↗</Link> : null}
          </div>
          <div className="fx-cash-body">
            <div className={`fx-cash-state ${s.cashSessionOpen ? '' : 'is-closed'}`}>
              <span className="fx-cash-icon"><AppIcon name={s.cashSessionOpen ? 'check' : 'cash'} /></span>
              {s.cashSessionOpen ? 'La caja está abierta' : 'La caja está cerrada'}
            </div>
            <p className="fx-cash-copy">
              {s.cashSessionOpen
                ? <>Los cobros del turno pueden registrarse. Hoy se registraron <strong>{revenueValue(s.today, currency)}</strong>.</>
                : <>Abre una sesión de caja para habilitar cobros en efectivo y conciliación de turno.</>}
            </p>
            {s.month ? <p className="fx-cash-copy">Acumulado del mes: <strong>{revenueValue(s.month, currency)}</strong></p> : null}
          </div>
        </section>
      </div>

      {consolidated.data && consolidated.data.branches.length > 1 ? (
        <section className="fx-panel" aria-labelledby="branches-title">
          <div className="fx-panel-head">
            <div>
              <h2 id="branches-title" className="fx-panel-title">Pulso por sucursal</h2>
              <p className="fx-panel-sub">Mes actual consolidado en {consolidated.data.reportingCurrency}.</p>
            </div>
          </div>
          <div className="fx-branch-grid">
            {consolidated.data.branches.map((branch) => (
              <div key={branch.shopId} className="fx-branch-card">
                <b>{branch.shopName}</b>
                <small>{branch.overdueOrders > 0 ? `${branch.overdueOrders} órdenes atrasadas` : 'Sin órdenes atrasadas'}</small>
                <div className="fx-branch-values">
                  <span>Abiertas<strong>{branch.openOrders}</strong></span>
                  <span>Listas<strong>{branch.readyOrders}</strong></span>
                  <span>Cobrado<strong>{revenueValue(branch.month, consolidated.data!.reportingCurrency)}</strong></span>
                </div>
              </div>
            ))}
            <div className="fx-branch-card">
              <b>Total del mes</b>
              <small>Conversión consolidada</small>
              <div className="fx-branch-values">
                <span>Moneda<strong>{consolidated.data.reportingCurrency}</strong></span>
                <span>Cobrado<strong>{money(consolidated.data.monthTotalConverted, consolidated.data.reportingCurrency)}</strong></span>
                <span>Faltantes<strong>{consolidated.data.missingRates.length}</strong></span>
              </div>
            </div>
          </div>
        </section>
      ) : null}
    </div>
  )
}
