import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import * as api from './api'

type Alert = { id: string; title: string; detail: string; href: string; urgency: number }
const terminal = new Set(['COMPLETED', 'CANCELLED'])

export function OperationalAlertsPage() {
  const [orders, setOrders] = useState<api.ServiceOrder[]>([])
  const [products, setProducts] = useState<api.Product[]>([])
  const [branches, setBranches] = useState<api.Branch[]>([])
  const [balances, setBalances] = useState<api.StockBalance[]>([])
  const [expenses, setExpenses] = useState<api.Expense[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    let active = true
    Promise.all([api.listServiceOrders(), api.listProducts(), api.listBranches(), api.listExpenses()])
      .then(async ([orderRows, productRows, branchRows, expenseRows]) => {
        const stockRows = await Promise.all(branchRows.filter(branch => branch.active).map(branch => api.stock(branch.id).catch(() => [])))
        if (!active) return
        setOrders(orderRows); setProducts(productRows); setBranches(branchRows); setExpenses(expenseRows); setBalances(stockRows.flat())
      })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar las alertas') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [revision])

  const alerts = useMemo(() => {
    const now = Date.now()
    const result: Alert[] = []
    for (const order of orders) {
      if (terminal.has(order.status)) continue
      if (order.promisedAt && new Date(order.promisedAt).getTime() < now) {
        result.push({ id: `late:${order.id}`, title: 'Orden vencida', detail: `${order.summary} · compromiso ${new Date(order.promisedAt).toLocaleString('es-MX')}`, href: `/service-orders/${order.id}`, urgency: 0 })
      }
      if (order.status === 'QA') result.push({ id: `ready:${order.id}`, title: 'Lista para entrega', detail: order.summary, href: `/service-orders/${order.id}`, urgency: 1 })
    }
    for (const product of products) {
      const low = balances.filter(balance => balance.productId === product.id && balance.quantity <= Number(product.reorderLevel ?? 0))
      for (const balance of low) {
        const branch = branches.find(item => item.id === balance.branchId)
        result.push({ id: `stock:${balance.id}`, title: 'Existencia baja', detail: `${product.sku} · ${product.name}: ${balance.quantity} unidades en ${branch?.name ?? 'sucursal'}`, href: '/reports/inventory', urgency: 2 })
      }
    }
    for (const expense of expenses) {
      if (expense.paid === false && expense.dueAt && expense.dueAt < new Date().toISOString().slice(0, 10)) {
        result.push({ id: `expense:${expense.id}`, title: 'Gasto vencido', detail: `${expense.category} · ${expense.description ?? ''} · vence ${expense.dueAt}`, href: '/expenses', urgency: 0 })
      }
    }
    return result.sort((a, b) => a.urgency - b.urgency || a.title.localeCompare(b.title))
  }, [orders, products, balances, branches, expenses])

  return <BaseLayout title="Alertas operativas" description="Vencimientos de órdenes, entregas listas, stock bajo y gastos vencidos."><div className="space-y-4 px-4 lg:px-6">
    <Card><CardHeader><div className="flex items-center justify-between gap-3"><CardTitle>{alerts.length} alertas actuales</CardTitle><Button type="button" variant="outline" disabled={loading} onClick={() => { setLoading(true); setError(''); setRevision(value => value + 1) }}>Actualizar</Button></div></CardHeader><CardContent className="space-y-2">
      {loading && <p role="status">Actualizando alertas…</p>}{alerts.map(alert => <div key={alert.id} className="flex flex-wrap items-center justify-between gap-3 rounded border p-3"><div><p className="font-medium">{alert.title}</p><p className="text-sm text-muted-foreground">{alert.detail}</p></div><Button asChild size="sm" variant="outline"><Link to={alert.href}>Abrir</Link></Button></div>)}
      {!loading && !error && !alerts.length && <p className="text-sm text-muted-foreground">Sin alertas operativas en este momento.</p>}{error && <p role="alert" className="text-sm text-destructive">{error}</p>}
    </CardContent></Card>
  </div></BaseLayout>
}
