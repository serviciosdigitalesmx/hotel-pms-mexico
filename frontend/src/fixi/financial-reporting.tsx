import { useEffect, useRef, useState } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const money = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })
const localDate = (date: Date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
const monthStart = () => { const now = new Date(); return localDate(new Date(now.getFullYear(), now.getMonth(), 1)) }
const today = () => localDate(new Date())

export function FixiFinancialReportingPage() {
  const generation = useRef(0)
  const [from, setFrom] = useState(monthStart())
  const [to, setTo] = useState(today())
  const [period, setPeriod] = useState<'TODAY' | 'WEEK' | 'MONTH' | 'CUSTOM'>('MONTH')
  const [branchId, setBranchId] = useState('ALL')
  const [branches, setBranches] = useState<api.Branch[]>([])
  const [orders, setOrders] = useState<api.ServiceOrder[]>([])
  const [paymentOrders, setPaymentOrders] = useState<api.ServiceOrder[]>([])
  const [expenses, setExpenses] = useState<api.Expense[]>([])
  const [purchases, setPurchases] = useState<api.PurchaseOrder[]>([])
  const [topConsumedProducts, setTopConsumedProducts] = useState<api.ProductConsumption[]>([])
  const [customers, setCustomers] = useState<api.Customer[]>([])
  const [users, setUsers] = useState<api.TeamUser[]>([])
  const [loaded, setLoaded] = useState(false)
  const [appliedFilters, setAppliedFilters] = useState<{ from: string; to: string; branchId: string } | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    api.listBranches().then(rows => { if (active) setBranches(rows) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar las sucursales') })
    return () => { active = false }
  }, [])

  async function generate(rangeStart = from, rangeEnd = to) {
    if (!rangeStart || !rangeEnd || rangeStart > rangeEnd) return
    const requestId = ++generation.current
    const selectedBranchId = branchId
    setBusy(true); setError(''); setLoaded(false)
    try {
      const [allOrders, reportExpenses, allPurchases, branchRows, allCustomers, allUsers, consumedProducts] = await Promise.all([api.listAllServiceOrders(), api.listExpensesForReport(rangeStart, rangeEnd), api.listPurchases(), api.listBranches(), api.listAllCustomers().catch(() => []), api.listUsers().catch(() => []), api.listTopConsumedProducts(rangeStart, rangeEnd, selectedBranchId === 'ALL' ? undefined : selectedBranchId)])
      if (requestId !== generation.current) return
      setBranches(branchRows)
      setCustomers(allCustomers)
      setUsers(allUsers)
      setTopConsumedProducts(consumedProducts)
      setPaymentOrders(allOrders.filter(order =>
        selectedBranchId === 'ALL' || order.branchId === selectedBranchId))
      setOrders(allOrders.filter(order => {
        const date = (order.status === 'COMPLETED' ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? ''
        return (selectedBranchId === 'ALL' || order.branchId === selectedBranchId) && date >= rangeStart && date <= rangeEnd
      }))
      setExpenses(reportExpenses)
      setPurchases(allPurchases.filter(purchase => {
        const receiptEvents = purchase.receiptEvents ?? []
        const hasReceiptInPeriod = receiptEvents.length
          ? receiptEvents.some(event => event.receivedAt.slice(0, 10) >= rangeStart
            && event.receivedAt.slice(0, 10) <= rangeEnd)
          : (() => { const date = (purchase.lastReceivedAt ?? purchase.createdAt)?.slice(0, 10) ?? ''; return purchase.lines.some(line => line.quantityReceived > 0) && date >= rangeStart && date <= rangeEnd })()
        return (selectedBranchId === 'ALL' || purchase.branchId === selectedBranchId)
          && hasReceiptInPeriod && purchase.status !== 'CANCELLED'
      }))
      setAppliedFilters({ from: rangeStart, to: rangeEnd, branchId: selectedBranchId })
      setLoaded(true)
    } catch (reason) { if (requestId === generation.current) setError(reason instanceof Error ? reason.message : 'No se pudo generar el reporte del taller') }
    finally { if (requestId === generation.current) setBusy(false) }
  }

  function choosePeriod(next: 'TODAY' | 'WEEK' | 'MONTH') {
    const now = new Date()
    const end = localDate(now)
    const start = next === 'TODAY' ? end : next === 'MONTH'
      ? localDate(new Date(now.getFullYear(), now.getMonth(), 1))
      : localDate(new Date(now.getFullYear(), now.getMonth(), now.getDate() - ((now.getDay() + 6) % 7)))
    setPeriod(next); setFrom(start); setTo(end)
    void generate(start, end)
  }

  const completed = orders.filter(order => order.status === 'COMPLETED')
  const open = orders.filter(order => !['COMPLETED', 'CANCELLED'].includes(order.status))
  const completedValue = completed.reduce((sum, order) => sum + Number(order.quoteTotal ?? 0), 0)
  const collectedForOrder = api.serviceOrderAmountReceived
  const collectedInPeriod = (order: api.ServiceOrder) => {
    const events = order.paymentEvents ?? []
    if (events.length) return events.reduce((sum, event) => {
      const date = event.recordedAt.slice(0, 10)
      return date >= (appliedFilters?.from ?? from) && date <= (appliedFilters?.to ?? to)
        ? sum + Number(event.amount) : sum
    }, 0)
    const activityDate = (order.status === 'COMPLETED'
      ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? ''
    return activityDate >= (appliedFilters?.from ?? from)
      && activityDate <= (appliedFilters?.to ?? to) ? collectedForOrder(order) : 0
  }
  const outstandingForOrder = (order: api.ServiceOrder) => order.paymentStatus === 'PENDING' ? api.serviceOrderBalance(order) : 0
  const isExpenseOverdueAtPeriodEnd = (expense: api.Expense) => expense.paid === false && typeof expense.dueAt === 'string' && expense.dueAt < (appliedFilters?.to ?? to)
  const collectedRevenue = paymentOrders.reduce((sum, order) => sum + collectedInPeriod(order), 0)
  const pendingRevenue = completed.reduce((sum, order) => sum + outstandingForOrder(order), 0)
  const openValue = open.reduce((sum, order) => sum + Number(order.quoteTotal ?? 0), 0)
  const paidExpenses = expenses.filter(expense => expense.paid !== false)
  const paidCost = paidExpenses.reduce((sum, expense) => sum + Number(expense.amount), 0)
  const pendingCost = expenses.filter(expense => expense.paid === false).reduce((sum, expense) => sum + Number(expense.amount), 0)
  const overdueExpenseCost = expenses.filter(isExpenseOverdueAtPeriodEnd).reduce((sum, expense) => sum + Number(expense.amount), 0)
  const purchaseReceiptValue = (purchase: api.PurchaseOrder) => {
    const events = purchase.receiptEvents ?? []
    if (events.length) return events.reduce((sum, event) => {
      const date = event.receivedAt.slice(0, 10)
      return date >= (appliedFilters?.from ?? from) && date <= (appliedFilters?.to ?? to)
        ? sum + Number(event.quantity) * Number(event.unitCost) : sum
    }, 0)
    return purchase.lines.reduce((sum, line) => sum + line.quantityReceived * line.unitCost, 0)
  }
  const purchaseValue = purchases.reduce((sum, purchase) => sum + purchaseReceiptValue(purchase), 0)
  const orderedPurchaseValue = purchases.reduce((sum, purchase) => sum + purchase.lines.reduce((lineSum, line) => lineSum + line.quantityOrdered * line.unitCost, 0), 0)
  const averageTicket = completed.length ? completedValue / completed.length : 0
  const averageHours = completed.map(order => {
    const start = order.createdAt ? new Date(order.createdAt).getTime() : Number.NaN
    const end = order.deliveredAt ? new Date(order.deliveredAt).getTime() : Number.NaN
    return Number.isFinite(start) && Number.isFinite(end) && end >= start ? (end - start) / 3_600_000 : null
  }).filter((value): value is number => value !== null)
  const avgHours = averageHours.length ? averageHours.reduce((sum, value) => sum + value, 0) / averageHours.length : 0
  const byDay = completed.reduce<Record<string, { count: number; amount: number; collected: number; pending: number }>>((groups, order) => {
    const day = (order.deliveredAt ?? order.updatedAt ?? order.createdAt ?? '').slice(0, 10)
    const current = groups[day] ?? { count: 0, amount: 0, collected: 0, pending: 0 }
    groups[day] = { count: current.count + 1, amount: current.amount + Number(order.quoteTotal ?? 0), collected: current.collected + collectedInPeriod(order), pending: current.pending + outstandingForOrder(order) }
    return groups
  }, {})
  const byCategory = expenses.reduce<Record<string, number>>((groups, expense) => {
    groups[expense.category] = (groups[expense.category] ?? 0) + Number(expense.amount)
    return groups
  }, {})
  const byExpenseType = expenses.reduce<Record<'FIXED' | 'VARIABLE' | 'INVENTORY', { paid: number; pending: number; count: number }>>((groups, expense) => {
    const type = expense.expenseType ?? 'VARIABLE'
    const totals = groups[type] ?? { paid: 0, pending: 0, count: 0 }
    if (expense.paid === false) totals.pending += Number(expense.amount)
    else totals.paid += Number(expense.amount)
    totals.count += 1
    groups[type] = totals
    return groups
  }, { FIXED: { paid: 0, pending: 0, count: 0 }, VARIABLE: { paid: 0, pending: 0, count: 0 }, INVENTORY: { paid: 0, pending: 0, count: 0 } })
  const expenseSettlementByCategory = expenses.reduce<Record<string, { paid: number; pending: number; overdue: number }>>((groups, expense) => {
    const totals = groups[expense.category] ?? { paid: 0, pending: 0, overdue: 0 }
    if (expense.paid === false) totals.pending += Number(expense.amount)
    else totals.paid += Number(expense.amount)
    if (isExpenseOverdueAtPeriodEnd(expense)) totals.overdue += Number(expense.amount)
    groups[expense.category] = totals
    return groups
  }, {})
  const byService = completed.reduce<Record<string, { count: number; quoted: number; collected: number; pending: number }>>((groups, order) => {
    const service = order.serviceName?.trim() || 'Sin categoría'
    const current = groups[service] ?? { count: 0, quoted: 0, collected: 0, pending: 0 }
    groups[service] = { count: current.count + 1, quoted: current.quoted + Number(order.quoteTotal ?? 0), collected: current.collected + collectedInPeriod(order), pending: current.pending + outstandingForOrder(order) }
    return groups
  }, {})
  const byCustomer = completed.reduce<Record<string, { count: number; amount: number }>>((groups, order) => {
    const current = groups[order.customerId] ?? { count: 0, amount: 0 }
    groups[order.customerId] = { count: current.count + 1, amount: current.amount + Number(order.quoteTotal ?? 0) }
    return groups
  }, {})
  const customerNames = new Map(customers.map(customer => [customer.id, `${customer.firstName} ${customer.lastName}`.trim()]))
  const technicianPerformance = completed.reduce<Record<string, { count: number; quoted: number; collected: number; pending: number }>>((groups, order) => {
    if (order.assignedUserId) {
      const current = groups[order.assignedUserId] ?? { count: 0, quoted: 0, collected: 0, pending: 0 }
      groups[order.assignedUserId] = { count: current.count + 1, quoted: current.quoted + Number(order.quoteTotal ?? 0), collected: current.collected + collectedInPeriod(order), pending: current.pending + outstandingForOrder(order) }
    }
    return groups
  }, {})
  const reportIsStale = loaded && (!appliedFilters || appliedFilters.from !== from || appliedFilters.to !== to || appliedFilters.branchId !== branchId)

  function exportCsv() {
    const purchaseRows = purchases.flatMap(purchase => {
      const events = (purchase.receiptEvents ?? []).filter(event => {
        const date = event.receivedAt.slice(0, 10)
        return date >= (appliedFilters?.from ?? from) && date <= (appliedFilters?.to ?? to)
      })
      if (events.length) return events.map(event => [event.receivedAt.slice(0, 10),
        event.kind === 'CORRECTION' ? 'AJUSTE_RECEPCION_COMPRA' : 'RECEPCION_COMPRA',
        purchase.id, event.productId, '', '', '', Number(event.quantity) * Number(event.unitCost), 'NO ES PAGO', ''])
      const date = (purchase.lastReceivedAt ?? purchase.createdAt).slice(0, 10)
      const legacyReceived = purchase.lines.reduce((sum, line) => sum + line.quantityReceived * line.unitCost, 0)
      return legacyReceived > 0 ? [[date, 'RECEPCION_COMPRA_HISTORICA', purchase.id, 'FECHA_APROXIMADA', '', '', '',
        legacyReceived, 'NO ES PAGO', '']] : []
    })
    const paymentRows = paymentOrders.flatMap(order => {
      const events = (order.paymentEvents ?? []).filter(event => {
        const date = event.recordedAt.slice(0, 10)
        return date >= (appliedFilters?.from ?? from) && date <= (appliedFilters?.to ?? to)
      })
      if (events.length) return events.map(event => [event.recordedAt.slice(0, 10), 'COBRO_ORDEN', order.id,
        event.kind, '', event.amount, '', '', 'SI', event.method])
      const activityDate = (order.status === 'COMPLETED'
        ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? ''
      const received = collectedForOrder(order)
      return received > 0 && activityDate >= (appliedFilters?.from ?? from)
        && activityDate <= (appliedFilters?.to ?? to)
        ? [[activityDate, 'COBRO_ORDEN_HISTORICO', order.id, 'FECHA_APROXIMADA', '', received, '', '', 'SI', order.paymentMethod ?? '']]
        : []
    })
    const rows = [['rango_desde', from], ['rango_hasta', to], ['sucursal', branchId === 'ALL' ? 'Todas' : branches.find(branch => branch.id === branchId)?.name ?? branchId], [], ['fecha', 'tipo', 'referencia', 'estado', 'ingreso_cotizado', 'ingreso_cobrado_marcado', 'saldo_pendiente', 'egreso', 'pagado', 'metodo_pago'],
      ...orders.map(order => [(order.status === 'COMPLETED' ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? '', 'ORDEN', order.id, order.paymentStatus ?? order.status, order.quoteTotal ?? 0, '', outstandingForOrder(order), '', order.paymentStatus === 'PAID', order.paymentMethod ?? '']),
      ...paymentRows,
      ...expenses.map(expense => [(expense.spentAt ?? expense.createdAt).slice(0, 10), 'GASTO', expense.reference ?? expense.id, expense.paid === false ? 'PENDIENTE' : 'PAGADO', '', '', '', expense.amount, expense.paid !== false, expense.paymentMethod ?? '']),
      ...purchaseRows]
    const csv = rows.map(row => row.map(api.csvCell).join(',')).join('\r\n')
    const url = URL.createObjectURL(new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })); const link = document.createElement('a'); link.href = url; link.download = `fixi-reporte-financiero-${from}-${to}.csv`; link.click(); URL.revokeObjectURL(url)
  }

  return <BaseLayout title="Finanzas del taller" description="Órdenes completadas, cotizaciones pendientes y gastos del periodo."><style>{'@media print{@page{margin:15mm}aside,nav,header,[data-sidebar]{display:none!important}.print-only{display:block!important}.print-hidden{display:none!important}}'}</style><div className="space-y-5 px-4 lg:px-6">
    {loaded && <div className="print-only hidden"><h1 className="text-2xl font-semibold">Reporte financiero del taller</h1><p>Periodo: {from} a {to} · Sucursal: {branchId === 'ALL' ? 'Todas' : branches.find(branch => branch.id === branchId)?.name ?? branchId}</p><p>Generado: {new Date().toLocaleString('es-MX')}</p></div>}
    {reportIsStale && <p role="status" className="rounded border border-amber-500/40 bg-amber-50 p-3 text-sm text-amber-900 print-hidden">Cambiaste el periodo o la sucursal. El reporte visible corresponde a la selección anterior; genera de nuevo antes de exportar o imprimir.</p>}
    {loaded && <Card><CardHeader><CardTitle>Gastos por tipo</CardTitle><CardDescription>Separación entre gastos fijos, variables y egresos de inventario pagados o pendientes. Las órdenes de compra del módulo Compras se muestran aparte y no se suman a estos gastos; evita registrar la misma recepción en ambos flujos.</CardDescription></CardHeader><CardContent className="grid gap-3 md:grid-cols-3">{([['FIXED','Fijos'],['VARIABLE','Variables'],['INVENTORY','Inventario']] as const).map(([type,label])=><div className="rounded border p-3" key={type}><p className="font-medium">{label} · {byExpenseType[type].count}</p><p className="text-sm text-muted-foreground">Pagado {money.format(byExpenseType[type].paid)}</p><p className="text-sm text-muted-foreground">Pendiente {money.format(byExpenseType[type].pending)}</p></div>)}</CardContent></Card>}
    <div className="space-y-3 print-hidden"><div className="flex flex-wrap gap-2">{([['TODAY','Hoy'],['WEEK','Semana'],['MONTH','Mes'],['CUSTOM','Rango personalizado']] as const).map(([key,label])=><Button key={key} type="button" size="sm" variant={period===key?'default':'outline'} onClick={() => key === 'CUSTOM' ? setPeriod('CUSTOM') : choosePeriod(key)}>{label}</Button>)}</div><div className="grid gap-3 sm:grid-cols-3"><Input aria-label="Desde" type="date" disabled={period!=='CUSTOM'} value={from} onChange={event => setFrom(event.target.value)}/><Input aria-label="Hasta" type="date" disabled={period!=='CUSTOM'} value={to} onChange={event => setTo(event.target.value)}/><div className="flex gap-2"><Button disabled={busy || !from || !to || from > to} onClick={() => void generate()}>{busy ? 'Generando…' : 'Generar reporte'}</Button><Button type="button" variant="outline" disabled={!loaded || reportIsStale} onClick={exportCsv}>Exportar CSV</Button><Button type="button" variant="outline" disabled={!loaded || reportIsStale} onClick={() => window.print()}>Imprimir / PDF</Button></div></div></div>
    <div className="print-hidden"><select aria-label="Filtrar reporte por sucursal" className="h-10 w-full max-w-sm rounded-md border bg-background px-3 text-sm" value={branchId} onChange={event=>setBranchId(event.target.value)}><option value="ALL">Todas las sucursales</option>{branches.filter(branch=>branch.active).map(branch=><option key={branch.id} value={branch.id}>{branch.name}</option>)}</select><p className="mt-1 text-xs text-muted-foreground">El filtro aplica a órdenes, compras y consumo de refacciones. Los gastos se muestran a nivel negocio; pulsa Generar reporte para aplicar la selección.</p></div>
    {loaded && <><div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">{[['Cobros registrados en órdenes del periodo', collectedRevenue], ['Saldo pendiente de órdenes entregadas', pendingRevenue], ['Cotizado en órdenes completadas', completedValue], ['Pendiente en órdenes abiertas', openValue], ['Gastos pagados', paidCost], ['Gastos por pagar', pendingCost], ['Gastos vencidos', overdueExpenseCost], ['Costo recibido por órdenes de compra', purchaseValue]].map(([label, value]) => <Card key={String(label)}><CardHeader><CardDescription>{label}</CardDescription><CardTitle className="text-xl">{money.format(Number(value))}</CardTitle></CardHeader></Card>)}</div>
      <div className="grid gap-4 lg:grid-cols-2"><Card><CardHeader><CardTitle>Compras e inventario</CardTitle><CardDescription>Valor neto recibido en el periodo por fecha de recepción y corrección. Es costo de inventario; no confirma pago ni se suma a gastos.</CardDescription></CardHeader><CardContent className="space-y-2">{purchases.map(purchase => { const received = purchaseReceiptValue(purchase); const ordered = purchase.lines.reduce((sum, line) => sum + line.quantityOrdered * line.unitCost, 0); const eventDates = purchase.receiptEvents?.filter(event=>event.receivedAt.slice(0,10)>=(appliedFilters?.from??from)&&event.receivedAt.slice(0,10)<=(appliedFilters?.to??to)).map(event=>event.receivedAt.slice(0,10)); return <div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={purchase.id}><span>{purchase.id.slice(0, 8)} · {purchase.status} · {eventDates?.length?eventDates.join(', '):(purchase.lastReceivedAt??purchase.createdAt).slice(0,10)}</span><span>Recibido en periodo {money.format(received)} · Ordenado {money.format(ordered)}</span></div> })}{!purchases.length&&<p className="text-sm text-muted-foreground">Sin compras en el periodo.</p>}{purchases.length>0&&<p className="text-sm font-medium">Valor ordenado {money.format(orderedPurchaseValue)} · valor recibido neto {money.format(purchaseValue)}</p>}</CardContent></Card><Card><CardHeader><CardTitle>Top 5 refacciones consumidas</CardTitle><CardDescription>Unidades vinculadas a órdenes entregadas en el rango seleccionado; no son ventas monetarias.</CardDescription></CardHeader><CardContent className="space-y-2">{topConsumedProducts.map((product,index)=><div className="flex justify-between rounded border p-3" key={product.productId}><span>{index+1}. {product.name}{product.sku?` · ${product.sku}`:''}</span><span>{product.quantity} pzas.</span></div>)}{!topConsumedProducts.length&&<p className="text-sm text-muted-foreground">No hay refacciones consumidas en el periodo.</p>}</CardContent></Card></div>
      <div className="grid gap-4 md:grid-cols-3"><Card><CardHeader><CardDescription>Órdenes completadas</CardDescription><CardTitle className="text-3xl">{completed.length}</CardTitle></CardHeader></Card><Card><CardHeader><CardDescription>Ticket promedio cotizado</CardDescription><CardTitle className="text-2xl">{money.format(averageTicket)}</CardTitle></CardHeader></Card><Card><CardHeader><CardDescription>Tiempo promedio de cierre</CardDescription><CardTitle className="text-2xl">{avgHours.toFixed(1)} h</CardTitle></CardHeader></Card></div>
      <div className="grid gap-4 lg:grid-cols-2"><Card><CardHeader><CardTitle>Órdenes completadas por día</CardTitle><CardDescription>Cotización, cobros marcados y saldo pendiente según la fecha de entrega.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(byDay).sort(([a], [b]) => a.localeCompare(b)).map(([day, value]) => <div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={day}><span>{day} · {value.count} órdenes</span><span>Cotizado {money.format(value.amount)} · Cobrado {money.format(value.collected)} · Pendiente {money.format(value.pending)}</span></div>)}{!completed.length && <p className="text-sm text-muted-foreground">Sin órdenes completadas en este periodo.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Desglose de gastos</CardTitle></CardHeader><CardContent className="space-y-2">{Object.entries(byCategory).sort((a, b) => b[1] - a[1]).map(([category, amount]) => <div className="flex justify-between rounded border p-3" key={category}><span>{category}</span><span>{money.format(amount)}</span></div>)}{!expenses.length && <p className="text-sm text-muted-foreground">Sin gastos en este periodo.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Ingresos por servicio</CardTitle><CardDescription>Importes cotizados y cobros marcados como recibidos.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(byService).sort((a,b)=>b[1].collected-a[1].collected).map(([service,value])=><div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={service}><span>{service} · {value.count} órdenes</span><span>Cotizado {money.format(value.quoted)} · Cobrado {money.format(value.collected)} · Pendiente {money.format(value.pending)}</span></div>)}{!completed.length&&<p className="text-sm text-muted-foreground">Sin datos de servicios.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Clientes recurrentes</CardTitle><CardDescription>Top cinco por órdenes completadas en el periodo.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(byCustomer).filter(([,value])=>value.count>1).sort((a,b)=>b[1].count-a[1].count).slice(0,5).map(([id,value])=><div className="flex justify-between rounded border p-3" key={id}><span>{customerNames.get(id)??id}</span><span>{value.count} órdenes · {money.format(value.amount)}</span></div>)}{!Object.values(byCustomer).some(value=>value.count>1)&&<p className="text-sm text-muted-foreground">No hay clientes con más de una orden completada en este periodo.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Rendimiento por técnico</CardTitle><CardDescription>Actividad e ingreso cobrado marcado por órdenes entregadas.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(technicianPerformance).sort((a,b)=>b[1].collected-a[1].collected).map(([id,value])=><div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={id}><span>{users.find(user=>user.id===id)?.username??id} · {value.count} órdenes</span><span>Cotizado {money.format(value.quoted)} · Cobrado {money.format(value.collected)} · Pendiente {money.format(value.pending)}</span></div>)}{!Object.keys(technicianPerformance).length&&<p className="text-sm text-muted-foreground">Sin cierres asignados a técnicos.</p>}</CardContent></Card></div>
      <Card><CardHeader><CardTitle>Gastos por categoría y estado</CardTitle><CardDescription>Separación entre egresos liquidados, pendientes y vencidos.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(expenseSettlementByCategory).sort((a,b)=>(b[1].paid+b[1].pending)-(a[1].paid+a[1].pending)).map(([category,totals])=><div className="flex flex-wrap items-center justify-between gap-3 rounded border p-3" key={category}><span className="font-medium">{category}</span><div className="flex flex-wrap gap-4 text-sm"><span>Pagado {money.format(totals.paid)}</span><span>Pendiente {money.format(totals.pending)}</span><span className={totals.overdue?'text-destructive':''}>Vencido {money.format(totals.overdue)}</span></div></div>)}{!expenses.length&&<p className="text-sm text-muted-foreground">Sin gastos en este periodo.</p>}</CardContent></Card>
      <Card><CardHeader><CardTitle>Actividad del periodo</CardTitle></CardHeader><CardContent className="space-y-2">{orders.map(order => <div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={order.id}><span>{order.summary} · {(order.status === 'COMPLETED' ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? 'Sin fecha'}</span><span>{order.status} · cotizado {money.format(Number(order.quoteTotal ?? 0))}</span></div>)}{!orders.length && <p className="text-sm text-muted-foreground">Sin órdenes en este periodo.</p>}</CardContent></Card>
    </>}
    {!loaded && !busy && <Card><CardContent className="pt-6"><p className="text-sm text-muted-foreground">Genera un reporte para consultar las órdenes y gastos del rango elegido.</p></CardContent></Card>}{error && <p role="alert" className="text-sm text-destructive">{error}</p>}
  </div></BaseLayout>
}
