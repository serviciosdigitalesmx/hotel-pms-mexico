import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import * as api from './api'

type Alert = { id: string; event: keyof api.NotificationPreferences; title: string; detail: string; href: string; urgency: number; branchId?: string }
const terminal = new Set(['COMPLETED', 'CANCELLED'])
const localDateKey = (date: Date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
const localDateOf = (value?: string) => {
  if (!value) return ''
  return /^\d{4}-\d{2}-\d{2}$/.test(value) ? value : localDateKey(new Date(value))
}

export function OperationalAlertsPage() {
  const [orders, setOrders] = useState<api.ServiceOrder[]>([])
  const [products, setProducts] = useState<api.Product[]>([])
  const [branches, setBranches] = useState<api.Branch[]>([])
  const [balances, setBalances] = useState<api.StockBalance[]>([])
  const [expenses, setExpenses] = useState<api.Expense[]>([])
  const [preferences, setPreferences] = useState<api.NotificationPreferences>({})
  const [branchFilter, setBranchFilter] = useState('ALL')
  const [eventFilter, setEventFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    let active = true
    Promise.allSettled([api.listAllServiceOrders(), api.listProducts(), api.listBranches(), api.listExpenses(), api.getNotificationPreferences()])
      .then(async ([orderResult, productResult, branchResult, expenseResult, preferenceResult]) => {
        if (!active) return
        const issues: string[] = []
        const orderRows = orderResult.status === 'fulfilled' ? orderResult.value : []
        const productRows = productResult.status === 'fulfilled' ? productResult.value : []
        const branchRows = branchResult.status === 'fulfilled' ? branchResult.value : []
        const expenseRows = expenseResult.status === 'fulfilled' ? expenseResult.value : []
        if (orderResult.status === 'rejected') issues.push('órdenes')
        if (productResult.status === 'rejected') issues.push('inventario')
        if (branchResult.status === 'rejected') issues.push('sucursales')
        if (expenseResult.status === 'rejected') issues.push('gastos')
        if (preferenceResult.status === 'fulfilled') setPreferences(preferenceResult.value)
        else issues.push('preferencias de notificación')
        setOrders(orderRows); setProducts(productRows); setBranches(branchRows); setExpenses(expenseRows)
        const activeBranches = branchResult.status === 'fulfilled' ? branchRows.filter(branch => branch.active) : []
        const stockResults = await Promise.allSettled(activeBranches.map(branch => api.stock(branch.id)))
        if (!active) return
        const stockRows: api.StockBalance[] = []
        stockResults.forEach((result, index) => {
          if (result.status === 'fulfilled') stockRows.push(...result.value)
          else issues.push(`existencias de ${activeBranches[index].name}`)
        })
        setBalances(stockRows)
        setError(issues.length ? `Algunas alertas pueden estar incompletas; no se cargaron: ${issues.join(', ')}.` : '')
      })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar las alertas') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [revision])

  const alerts = useMemo(() => {
    const now = Date.now()
    const today = localDateKey(new Date())
    const result: Alert[] = []
    for (const order of orders) {
      if (order.status === 'COMPLETED' && order.paymentStatus === 'PENDING' && order.quoteTotal != null) {
        const balance = api.serviceOrderBalance(order)
        if (balance > 0) result.push({ id: `order-payment:${order.id}`, event: 'PAYMENT_DUE', title: 'Saldo de orden pendiente', detail: `${order.summary} · ${new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(balance)} por cobrar`, href: `/service-orders/${order.id}`, urgency: 0, branchId: order.branchId })
      }
      if (terminal.has(order.status)) continue
      if (order.assignedUserId && localDateOf(order.createdAt) === today) {
        result.push({ id: `assigned:${order.id}`, event: 'ORDER_ASSIGNED', title: 'Orden nueva con técnico asignado', detail: `${order.summary} · creada hoy y con responsable asignado`, href: `/service-orders/${order.id}`, urgency: 1, branchId: order.branchId })
      }
      if (order.promisedAt && new Date(order.promisedAt).getTime() < now) {
        result.push({ id: `late:${order.id}`, event: 'DELIVERY_DUE', title: 'Orden vencida', detail: `${order.summary} · compromiso ${new Date(order.promisedAt).toLocaleString('es-MX')}`, href: `/service-orders/${order.id}`, urgency: 0, branchId: order.branchId })
      }
      if (order.status === 'QA') result.push({ id: `ready:${order.id}`, event: 'ORDER_READY', title: 'Lista para entrega', detail: order.summary, href: `/service-orders/${order.id}`, urgency: 1, branchId: order.branchId })
    }
    for (const product of products) {
      const low = balances.filter(balance => balance.productId === product.id && balance.quantity <= Number(product.reorderLevel ?? 0))
      for (const balance of low) {
        const branch = branches.find(item => item.id === balance.branchId)
        result.push({ id: `stock:${balance.id}`, event: 'STOCK_LOW', title: 'Existencia baja', detail: `${product.sku} · ${product.name}: ${balance.quantity} unidades en ${branch?.name ?? 'sucursal'}`, href: '/reports/inventory', urgency: 2, branchId: balance.branchId })
      }
    }
    const completedToday = orders.filter(order => order.status === 'COMPLETED' && localDateOf(order.deliveredAt ?? order.updatedAt ?? order.createdAt) === today)
    if (completedToday.length) {
      result.push({ id: `summary:${today}`, event: 'DAY_SUMMARY', title: 'Cierre del día disponible', detail: `${completedToday.length} órdenes completadas hoy`, href: '/reports/finance', urgency: 3 })
    }
    for (const expense of expenses) {
      if (expense.paid === false && expense.dueAt && expense.dueAt <= today) {
        const dueToday = expense.dueAt === today
        result.push({ id: `expense:${expense.id}`, event: 'PAYMENT_DUE', title: dueToday ? 'Pago vence hoy' : 'Pago vencido', detail: `${expense.category} · ${expense.description ?? ''} · vence ${expense.dueAt}`, href: '/expenses', urgency: dueToday ? 1 : 0 })
      }
    }
    return result.filter(alert => preferences[alert.event]?.WEB !== false)
      .filter(alert => eventFilter === 'ALL' || alert.event === eventFilter)
      .filter(alert => branchFilter === 'ALL' || !alert.branchId || alert.branchId === branchFilter)
      .sort((a, b) => a.urgency - b.urgency || a.title.localeCompare(b.title))
  }, [orders, products, balances, branches, expenses, preferences, eventFilter, branchFilter])

  return <BaseLayout title="Alertas operativas" description="Órdenes vencidas o con saldo pendiente, entregas listas, stock bajo y pagos de gastos vencidos."><div className="space-y-4 px-4 lg:px-6">
    <div className="grid gap-2 sm:grid-cols-2"><select aria-label="Filtrar alertas por sucursal" className="h-10 rounded-md border bg-background px-3 text-sm" value={branchFilter} onChange={event => setBranchFilter(event.target.value)}><option value="ALL">Todas las sucursales</option>{branches.filter(branch => branch.active).map(branch => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select><select aria-label="Filtrar alertas por tipo" className="h-10 rounded-md border bg-background px-3 text-sm" value={eventFilter} onChange={event => setEventFilter(event.target.value)}><option value="ALL">Todos los tipos</option><option value="ORDER_ASSIGNED">Órdenes nuevas asignadas</option><option value="STOCK_LOW">Existencia baja</option><option value="DELIVERY_DUE">Fecha compromiso vencida</option><option value="PAYMENT_DUE">Pagos pendientes</option><option value="ORDER_READY">Listas para entregar</option><option value="DAY_SUMMARY">Resumen del día</option></select></div>
    <Card><CardHeader><div className="flex items-center justify-between gap-3"><CardTitle>{alerts.length} alertas actuales</CardTitle><Button type="button" variant="outline" disabled={loading} onClick={() => { setLoading(true); setError(''); setRevision(value => value + 1) }}>Actualizar</Button></div></CardHeader><CardContent className="space-y-2">
      {loading && <p role="status">Actualizando alertas…</p>}{alerts.map(alert => <div key={alert.id} className="flex flex-wrap items-center justify-between gap-3 rounded border p-3"><div><p className="font-medium">{alert.title}</p><p className="text-sm text-muted-foreground">{alert.detail}</p></div><Button asChild size="sm" variant="outline"><Link to={alert.href}>Abrir</Link></Button></div>)}
      {!loading && !alerts.length && <p className="text-sm text-muted-foreground">No hay alertas para los filtros seleccionados.</p>}{error && <p role="alert" className="text-sm text-amber-700">{error}</p>}
    </CardContent></Card>
  </div></BaseLayout>
}
