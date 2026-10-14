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
  const [orders, setOrders] = useState<api.ServiceOrder[]>([])
  const [expenses, setExpenses] = useState<api.Expense[]>([])
  const [customers, setCustomers] = useState<api.Customer[]>([])
  const [users, setUsers] = useState<api.TeamUser[]>([])
  const [loaded, setLoaded] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function generate() {
    if (!from || !to || from > to) return
    setBusy(true); setError('')
    try {
      const [allOrders, allExpenses, customerResult, allUsers] = await Promise.all([api.listServiceOrders(), api.listExpenses(), api.listCustomers().catch(() => []), api.listUsers().catch(() => [])])
      setCustomers(Array.isArray(customerResult) ? customerResult : customerResult.content)
      setUsers(allUsers)
      setOrders(allOrders.filter(order => {
        const date = (order.status === 'COMPLETED' ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? ''
        return date >= from && date <= to
      }))
      setExpenses(allExpenses.filter(expense => {
        const date = (expense.spentAt ?? expense.createdAt).slice(0, 10)
        return date >= from && date <= to
      }))
      setLoaded(true)
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo generar el reporte del taller') }
    finally { setBusy(false) }
  }

  const completed = orders.filter(order => order.status === 'COMPLETED')
  const open = orders.filter(order => !['COMPLETED', 'CANCELLED'].includes(order.status))
  const completedValue = completed.reduce((sum, order) => sum + Number(order.quoteTotal ?? 0), 0)
  const openValue = open.reduce((sum, order) => sum + Number(order.quoteTotal ?? 0), 0)
  const paidExpenses = expenses.filter(expense => expense.paid !== false)
  const paidCost = paidExpenses.reduce((sum, expense) => sum + Number(expense.amount), 0)
  const pendingCost = expenses.filter(expense => expense.paid === false).reduce((sum, expense) => sum + Number(expense.amount), 0)
  const averageTicket = completed.length ? completedValue / completed.length : 0
  const averageHours = completed.map(order => {
    const start = order.createdAt ? new Date(order.createdAt).getTime() : Number.NaN
    const end = order.deliveredAt ? new Date(order.deliveredAt).getTime() : Number.NaN
    return Number.isFinite(start) && Number.isFinite(end) && end >= start ? (end - start) / 3_600_000 : null
  }).filter((value): value is number => value !== null)
  const avgHours = averageHours.length ? averageHours.reduce((sum, value) => sum + value, 0) / averageHours.length : 0
  const byDay = completed.reduce<Record<string, { count: number; amount: number }>>((groups, order) => {
    const day = (order.deliveredAt ?? order.updatedAt ?? order.createdAt ?? '').slice(0, 10)
    const current = groups[day] ?? { count: 0, amount: 0 }
    groups[day] = { count: current.count + 1, amount: current.amount + Number(order.quoteTotal ?? 0) }
    return groups
  }, {})
  const byCategory = expenses.reduce<Record<string, number>>((groups, expense) => {
    groups[expense.category] = (groups[expense.category] ?? 0) + Number(expense.amount)
    return groups
  }, {})
  const byService = completed.reduce<Record<string, number>>((groups, order) => {
    const service = order.serviceName?.trim() || 'Sin categoría'
    groups[service] = (groups[service] ?? 0) + Number(order.quoteTotal ?? 0)
    return groups
  }, {})
  const byCustomer = completed.reduce<Record<string, { count: number; amount: number }>>((groups, order) => {
    const current = groups[order.customerId] ?? { count: 0, amount: 0 }
    groups[order.customerId] = { count: current.count + 1, amount: current.amount + Number(order.quoteTotal ?? 0) }
    return groups
  }, {})
  const customerNames = new Map(customers.map(customer => [customer.id, `${customer.firstName} ${customer.lastName}`.trim()]))
  const technicianCounts = completed.reduce<Record<string, number>>((groups, order) => {
    if (order.assignedUserId) groups[order.assignedUserId] = (groups[order.assignedUserId] ?? 0) + 1
    return groups
  }, {})

  function exportCsv() {
    const rows = [['fecha', 'tipo', 'referencia', 'estado', 'ingreso_cotizado', 'egreso', 'pagado'],
      ...orders.map(order => [(order.status === 'COMPLETED' ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? '', 'ORDEN', order.id, order.status, order.quoteTotal ?? 0, '', '']),
      ...expenses.map(expense => [(expense.spentAt ?? expense.createdAt).slice(0, 10), 'GASTO', expense.reference ?? expense.id, expense.paid === false ? 'PENDIENTE' : 'PAGADO', '', expense.amount, expense.paid !== false])]
    const csv = rows.map(row => row.map(value => `"${String(value).replaceAll('"', '""')}"`).join(',')).join('\r\n')
    const url = URL.createObjectURL(new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })); const link = document.createElement('a'); link.href = url; link.download = 'fixi-reporte-financiero.csv'; link.click(); URL.revokeObjectURL(url)
  }

  return <BaseLayout title="Finanzas del taller" description="Órdenes completadas, cotizaciones pendientes y gastos del periodo."><div className="space-y-5 px-4 lg:px-6">
    <div className="grid gap-3 sm:grid-cols-3"><Input aria-label="Desde" type="date" value={from} onChange={event => setFrom(event.target.value)}/><Input aria-label="Hasta" type="date" value={to} onChange={event => setTo(event.target.value)}/><div className="flex gap-2"><Button disabled={busy || !from || !to || from > to} onClick={() => void generate()}>{busy ? 'Generando…' : 'Generar reporte'}</Button><Button type="button" variant="outline" disabled={!loaded} onClick={exportCsv}>Exportar CSV</Button><Button type="button" variant="outline" disabled={!loaded} onClick={() => window.print()}>Imprimir / PDF</Button></div></div>
    {loaded && <><div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">{[['Cotizado en órdenes completadas', completedValue], ['Pendiente en órdenes abiertas', openValue], ['Gastos pagados', paidCost], ['Gastos por pagar', pendingCost]].map(([label, value]) => <Card key={String(label)}><CardHeader><CardDescription>{label}</CardDescription><CardTitle className="text-xl">{money.format(Number(value))}</CardTitle></CardHeader></Card>)}</div>
      <div className="grid gap-4 md:grid-cols-3"><Card><CardHeader><CardDescription>Órdenes completadas</CardDescription><CardTitle className="text-3xl">{completed.length}</CardTitle></CardHeader></Card><Card><CardHeader><CardDescription>Ticket promedio cotizado</CardDescription><CardTitle className="text-2xl">{money.format(averageTicket)}</CardTitle></CardHeader></Card><Card><CardHeader><CardDescription>Tiempo promedio de cierre</CardDescription><CardTitle className="text-2xl">{avgHours.toFixed(1)} h</CardTitle></CardHeader></Card></div>
      <div className="grid gap-4 lg:grid-cols-2"><Card><CardHeader><CardTitle>Órdenes completadas por día</CardTitle><CardDescription>Importes basados en cotización, no representan cobros confirmados.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(byDay).sort(([a], [b]) => a.localeCompare(b)).map(([day, value]) => <div className="flex justify-between rounded border p-3" key={day}><span>{day} · {value.count} órdenes</span><span>{money.format(value.amount)}</span></div>)}{!completed.length && <p className="text-sm text-muted-foreground">Sin órdenes completadas en este periodo.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Desglose de gastos</CardTitle></CardHeader><CardContent className="space-y-2">{Object.entries(byCategory).sort((a, b) => b[1] - a[1]).map(([category, amount]) => <div className="flex justify-between rounded border p-3" key={category}><span>{category}</span><span>{money.format(amount)}</span></div>)}{!expenses.length && <p className="text-sm text-muted-foreground">Sin gastos en este periodo.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Ingresos cotizados por servicio</CardTitle></CardHeader><CardContent className="space-y-2">{Object.entries(byService).sort((a,b)=>b[1]-a[1]).map(([service,amount])=><div className="flex justify-between rounded border p-3" key={service}><span>{service}</span><span>{money.format(amount)}</span></div>)}{!completed.length&&<p className="text-sm text-muted-foreground">Sin datos de servicios.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Clientes recurrentes</CardTitle><CardDescription>Top cinco por órdenes completadas en el periodo.</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(byCustomer).filter(([,value])=>value.count>1).sort((a,b)=>b[1].count-a[1].count).slice(0,5).map(([id,value])=><div className="flex justify-between rounded border p-3" key={id}><span>{customerNames.get(id)??id}</span><span>{value.count} órdenes · {money.format(value.amount)}</span></div>)}{!Object.values(byCustomer).some(value=>value.count>1)&&<p className="text-sm text-muted-foreground">No hay clientes con más de una orden completada en este periodo.</p>}</CardContent></Card><Card><CardHeader><CardTitle>Rendimiento por técnico</CardTitle></CardHeader><CardContent className="space-y-2">{Object.entries(technicianCounts).sort((a,b)=>b[1]-a[1]).map(([id,count])=><div className="flex justify-between rounded border p-3" key={id}><span>{users.find(user=>user.id===id)?.username??id}</span><span>{count} órdenes completadas</span></div>)}{!Object.keys(technicianCounts).length&&<p className="text-sm text-muted-foreground">Sin cierres asignados a técnicos.</p>}</CardContent></Card></div>
      <Card><CardHeader><CardTitle>Actividad del periodo</CardTitle></CardHeader><CardContent className="space-y-2">{orders.map(order => <div className="flex flex-wrap justify-between gap-2 rounded border p-3" key={order.id}><span>{order.summary} · {(order.status === 'COMPLETED' ? order.deliveredAt ?? order.updatedAt : order.createdAt)?.slice(0, 10) ?? 'Sin fecha'}</span><span>{order.status} · cotizado {money.format(Number(order.quoteTotal ?? 0))}</span></div>)}{!orders.length && <p className="text-sm text-muted-foreground">Sin órdenes en este periodo.</p>}</CardContent></Card>
    </>}
    {!loaded && !busy && <Card><CardContent className="pt-6"><p className="text-sm text-muted-foreground">Genera un reporte para consultar las órdenes y gastos del rango elegido.</p></CardContent></Card>}{error && <p role="alert" className="text-sm text-destructive">{error}</p>}
  </div></BaseLayout>
}
