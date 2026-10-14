import { useState } from 'react'
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
  const [from, setFrom] = useState(monthStart())
  const [to, setTo] = useState(today())
  const [period, setPeriod] = useState<'TODAY' | 'WEEK' | 'MONTH' | 'CUSTOM'>('MONTH')
  const [orders, setOrders] = useState<api.ServiceOrder[]>([])
  const [expenses, setExpenses] = useState<api.Expense[]>([])
  const [purchases, setPurchases] = useState<api.PurchaseOrder[]>([])
  const [topConsumedProducts, setTopConsumedProducts] = useState<api.ProductConsumption[]>([])
  const [customers, setCustomers] = useState<api.Customer[]>([])
  const [users, setUsers] = useState<api.TeamUser[]>([])
  const [loaded, setLoaded] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function generate(rangeStart = from, rangeEnd = to) {
    if (!rangeStart || !rangeEnd || rangeStart > rangeEnd) return
    setBusy(true); setError('')
    try {
      const [allOrders, allExpenses, allPurchases, allCustomers, allUsers, consumedProducts] = await Promise.all([api.listAllServiceOrders(), api.listExpenses(), api.listPurchases(), api.listAllCustomers().catch(() => []), api.listUsers().catch(() => []), api.listTopConsumedProducts(rangeStart, rangeEnd)])
      setCustomers(allCustomers)
      setUsers(allUsers)
      setTopConsumedProducts(consumedProducts)
      setOrders(allOrders.filter(order => {
        const date = (order.status === 'COMPLETED' ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? ''
        return date >= rangeStart && date <= rangeEnd
      }))
      setExpenses(allExpenses.filter(expense => {
        const date = (expense.spentAt ?? expense.createdAt).slice(0, 10)
        return date >= rangeStart && date <= rangeEnd
      }))
      setPurchases(allPurchases.filter(purchase => {
        const date = purchase.createdAt?.slice(0, 10) ?? ''
        return date >= rangeStart && date <= rangeEnd && purchase.status !== 'CANCELLED'
      }))
      setLoaded(true)
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo generar el reporte del taller') }
    finally { setBusy(false) }
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
  const collectedForOrder = (order: api.ServiceOrder) => Number(order.paidAmount ?? (
    order.paymentStatus === 'PAID'
      ? Math.max(Number(order.quoteTotal ?? 0), Number(order.depositAmount ?? 0))
      : Number(order.depositAmount ?? 0)
  ))
  const outstandingForOrder = (order: api.ServiceOrder) => order.paymentStatus === 'PENDING'
    ? Math.max(0, Number(order.quoteTotal ?? 0) - collectedForOrder(order)) : 0
  const collectedRevenue = completed.reduce((sum, order) => sum + collectedForOrder(order), 0)
  const pendingRevenue = completed.reduce((sum, order) => sum + outstandingForOrder(order), 0)
  const openValue = open.reduce((sum, order) => sum + Number(order.quoteTotal ?? 0), 0)
  const paidExpenses = expenses.filter(expense => expense.paid !== false)
  const paidCost = paidExpenses.reduce((sum, expense) => sum + Number(expense.amount), 0)
  const pendingCost = expenses.filter(expense => expense.paid === false).reduce((sum, expense) => sum + Number(expense.amount), 0)
  const purchaseValue = purchases.reduce((sum, purchase) => sum + purchase.lines.reduce((lineSum, line) => lineSum + line.quantityReceived * line.unitCost, 0), 0)
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
    groups[day] = { count: current.count + 1, amount: current.amount + Number(order.quoteTotal ?? 0), collected: current.collected + collectedForOrder(order), pending: current.pending + outstandingForOrder(order) }
    return groups
  }, {})
  const byCategory = expenses.reduce<Record<string, number>>((groups, expense) => {
    groups[expense.category] = (groups[expense.category] ?? 0) + Number(expense.amount)
    return groups
  }, {})
  const expenseSettlementByCategory = expenses.reduce<Record<string, { paid: number; pending: number }>>((groups, expense) => {
    const totals = groups[expense.category] ?? { paid: 0, pending: 0 }
    if (expense.paid === false) totals.pending += Number(expense.amount)
    else totals.paid += Number(expense.amount)
    groups[expense.category] = totals
    return groups
  }, {})
  const byService = completed.reduce<Record<string, { count: number; quoted: number; collected: number; pending: number }>>((groups, order) => {
    const service = order.serviceName?.trim() || 'Sin categoría'
    const current = groups[service] ?? { count: 0, quoted: 0, collected: 0, pending: 0 }
    groups[service] = { count: current.count + 1, quoted: current.quoted + Number(order.quoteTotal ?? 0), collected: current.collected + collectedForOrder(order), pending: current.pending + outstandingForOrder(order) }
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
      groups[order.assignedUserId] = { count: current.count + 1, quoted: current.quoted + Number(order.quoteTotal ?? 0), collected: current.collected + collectedForOrder(order), pending: current.pending + outstandingForOrder(order) }
    }
    return groups
  }, {})

  function exportCsv() {
    const rows = [['fecha', 'tipo', 'referencia', 'estado', 'ingreso_cotizado', 'ingreso_cobrado_marcado', 'saldo_pendiente', 'egreso', 'pagado'],
      ...orders.map(order => [(order.status === 'COMPLETED' ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? '', 'ORDEN', order.id, order.paymentStatus ?? order.status, order.quoteTotal ?? 0, collectedForOrder(order), outstandingForOrder(order), '', order.paymentStatus === 'PAID']),
      ...expenses.map(expense => [(expense.spentAt ?? expense.createdAt).slice(0, 10), 'GASTO', expense.reference ?? expense.id, expense.paid === false ? 'PENDIENTE' : 'PAGADO', '', '', '', expense.amount, expense.paid !== false]),
      ...purchases.map(purchase => [purchase.createdAt.slice(0, 10), 'COMPRA_INVENTARIO', purchase.id, purchase.status, '', '', '', purchase.lines.reduce((sum, line) => sum + line.quantityReceived * line.unitCost, 0), 'NO ES PAGO'])]
    const csv = rows.map(row => row.map(api.csvCell).join(',')).join('\r\n')
    const url = URL.createObjectURL(new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })); const link = document.createElement('a'); link.href = url; link.download = 'fixi-reporte-financiero.csv'; link.click(); URL.revokeObjectURL(url)
  }

  return <BaseLayout title="Finanzas del taller" description="Órdenes completadas, cotizaciones pendientes y gastos del periodo."><div className="space-y-5 px-4 lg:px-6">
    <div className="space-y-3"><div className="flex flex-wrap gap-2">{([['TODAY','Hoy'],['WEEK','Semana'],['MONTH','Mes'],['CUSTOM','Rango personalizado']] as const).map(([key,label])=><Button key={key} type="button" size="sm" variant={period===key?'default':'outline'} onClick={() => key === 'CUSTOM' ? setPeriod('CUSTOM') : choosePeriod(key)}>{label}</Button>)}</div><div className="grid gap-3 sm:grid-cols-3"><Input aria-label="Desde" type="date" disabled={period!=='CUSTOM'} value={from} onChange={event => setFrom(event.target.value)}/><Input aria-label="Hasta" type="date" disabled={period!=='CUSTOM'} value={to} onChange={event => setTo(event.target.value)}/><div className="flex gap-2"><Button disabled={busy || !from || !to || from > to} onClick={() => void generate()}>{busy ? 'Generando…' : 'Generar reporte'}</Button><Button type="button" variant="outline" disabled={!loaded} onClick={exportCsv}>Exportar CSV</Button><Button type="button" variant="outline" disabled={!loaded} onClick={() => window.print()}>Imprimir / PDF</Button></div></div></div>
    {loaded && <><div className="grid gap-4 md:grid-cols-2 xl:grid-cols-7">{[['Cobros marcados como recibidos', collectedRevenue], ['Saldo pendiente de órdenes entregadas', pendingRevenue], ['Cotizado en órdenes completadas', completedValue], ['Pendiente en órdenes abiertas', openValue], ['Gastos pagados', paidCost], ['Gastos por pagar', pendingCost], ['Inventario recibido', purchaseValue]].map(([label, value]) => <Card key={String(label)}><CardHeader><CardDescription>{label}</CardDescription><CardTitle className="text-xl">{money.format(Number(value))}</CardTitle></CardHeader></Card>)}</div>
      <div className="grid gap-4 lg:grid-cols-2"><Card><CardHeader><CardTitle>Compras e inventario</CardTitle><CardDescription>Órdenes creadas en el periodo · importe recibido calculado con las cantidades recibidas actuales. Es costo de inventario, no confirma pago ni se suma a gastos.</CardDescription></CardHeader><CardContent className="space-y-2">{purchases.map(purchase => { const received = purchase.lines.reduce((sum, line) => sum + line.quantityReceived * line.unitCost, 0); const ordered = purchase.lines.reduce((sum, line) => sum + line.quantityOrdered * line.unitCost, 0); return <div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={purchase.id}><span>{purchase.id.slice(0, 8)} · {purchase.status} · {purchase.createdAt.slice(0, 10)}</span><span>Recibido {money.format(received)} · Ordenado {money.format(ordered)}</span></div> })}{!purchases.length&&<p className="text-sm text-muted-foreground">Sin compras en el periodo.</p>}{purchases.length>0&&<p className="text-sm font-medium">Valor ordenado {money.format(orderedPurchaseValue)} · valor recibido {money.format(purchaseValue)}</p>}</CardContent></Card><Card><CardHeader><CardTitle>Top 5 refacciones consumidas</CardTitle><CardDescription>Unidades vinculadas a órdenes entregadas en el rango seleccionado; no son ventas monetarias.</CardDescription></CardHeader><CardContent className="space-y-2">{topConsumedProducts.map((product,index)=><div className="flex justify-between rounded border p-3" key={product.productId}><span>{index+1}. {product.name}{product.sku?` · ${product.sku}`:''}</span><span>{product.quantity} pzas.</span></div>)}{!topConsumedProducts.length&&<p className="text-sm text-muted-foreground">No hay refacciones consumidas en el periodo.</p>}</CardContent></Card></div>
      <div className="grid gap-4 md:grid-cols-3"><Card><CardHeader><CardDescription>Órdenes completadas</CardDescription><CardTitle className="text-3xl">{completed.length}</CardTitle></CardHeader></Card><Card><CardHeader><CardDescription>Ticket promedio cotizado</CardDescription><CardTitle className="text-2xl">{money.format(averageTicket)}</CardTitle></CardHeader></Card><Card><CardHeader><CardDescription>Tiempo promedio de cierre</CardDescription><CardTitle className="text-2xl">{avgHours.toFixed(1)} h</CardTitle></CardHeader></Card></div>
      <div className="grid gap-4 lg:grid-cols-2"><Card><CardHeader><CardTitle>Órdenes completadas por día</CardTitle><CardDescription>Cotización, cobros marcados y saldo pendiente según la fecha de entrega.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(byDay).sort(([a], [b]) => a.localeCompare(b)).map(([day, value]) => <div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={day}><span>{day} · {value.count} órdenes</span><span>Cotizado {money.format(value.amount)} · Cobrado {money.format(value.collected)} · Pendiente {money.format(value.pending)}</span></div>)}{!completed.length && <p className="text-sm text-muted-foreground">Sin órdenes completadas en este periodo.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Desglose de gastos</CardTitle></CardHeader><CardContent className="space-y-2">{Object.entries(byCategory).sort((a, b) => b[1] - a[1]).map(([category, amount]) => <div className="flex justify-between rounded border p-3" key={category}><span>{category}</span><span>{money.format(amount)}</span></div>)}{!expenses.length && <p className="text-sm text-muted-foreground">Sin gastos en este periodo.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Ingresos por servicio</CardTitle><CardDescription>Importes cotizados y cobros marcados como recibidos.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(byService).sort((a,b)=>b[1].collected-a[1].collected).map(([service,value])=><div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={service}><span>{service} · {value.count} órdenes</span><span>Cotizado {money.format(value.quoted)} · Cobrado {money.format(value.collected)} · Pendiente {money.format(value.pending)}</span></div>)}{!completed.length&&<p className="text-sm text-muted-foreground">Sin datos de servicios.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Clientes recurrentes</CardTitle><CardDescription>Top cinco por órdenes completadas en el periodo.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(byCustomer).filter(([,value])=>value.count>1).sort((a,b)=>b[1].count-a[1].count).slice(0,5).map(([id,value])=><div className="flex justify-between rounded border p-3" key={id}><span>{customerNames.get(id)??id}</span><span>{value.count} órdenes · {money.format(value.amount)}</span></div>)}{!Object.values(byCustomer).some(value=>value.count>1)&&<p className="text-sm text-muted-foreground">No hay clientes con más de una orden completada en este periodo.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Rendimiento por técnico</CardTitle><CardDescription>Actividad e ingreso cobrado marcado por órdenes entregadas.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(technicianPerformance).sort((a,b)=>b[1].collected-a[1].collected).map(([id,value])=><div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={id}><span>{users.find(user=>user.id===id)?.username??id} · {value.count} órdenes</span><span>Cotizado {money.format(value.quoted)} · Cobrado {money.format(value.collected)} · Pendiente {money.format(value.pending)}</span></div>)}{!Object.keys(technicianPerformance).length&&<p className="text-sm text-muted-foreground">Sin cierres asignados a técnicos.</p>}</CardContent></Card></div>
      <Card><CardHeader><CardTitle>Gastos por categoría y estado</CardTitle><CardDescription>Separación entre egresos liquidados y compromisos de pago.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(expenseSettlementByCategory).sort((a,b)=>(b[1].paid+b[1].pending)-(a[1].paid+a[1].pending)).map(([category,totals])=><div className="flex flex-wrap items-center justify-between gap-3 rounded border p-3" key={category}><span className="font-medium">{category}</span><div className="flex gap-4 text-sm"><span>Pagado {money.format(totals.paid)}</span><span>Pendiente {money.format(totals.pending)}</span></div></div>)}{!expenses.length&&<p className="text-sm text-muted-foreground">Sin gastos en este periodo.</p>}</CardContent></Card>
      <Card><CardHeader><CardTitle>Actividad del periodo</CardTitle></CardHeader><CardContent className="space-y-2">{orders.map(order => <div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={order.id}><span>{order.summary} · {(order.status === 'COMPLETED' ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? 'Sin fecha'}</span><span>{order.status} · cotizado {money.format(Number(order.quoteTotal ?? 0))}</span></div>)}{!orders.length && <p className="text-sm text-muted-foreground">Sin órdenes en este periodo.</p>}</CardContent></Card>
    </>}
    {!loaded && !busy && <Card><CardContent className="pt-6"><p className="text-sm text-muted-foreground">Genera un reporte para consultar las órdenes y gastos del rango elegido.</p></CardContent></Card>}{error && <p role="alert" className="text-sm text-destructive">{error}</p>}
  </div></BaseLayout>
}
