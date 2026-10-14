import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const money = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })

export function ExpensesPage() {
  const [expenses, setExpenses] = useState<api.Expense[]>([])
  const [form, setForm] = useState({ expenseType: 'VARIABLE' as 'FIXED' | 'VARIABLE' | 'INVENTORY', category: '', subcategory: '', amount: '', description: '', reference: '', paid: true, spentAt: new Date().toISOString().slice(0, 10), dueAt: '' })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [revision, setRevision] = useState(0)
  const [view, setView] = useState<'ALL' | 'PAID' | 'PENDING'>('ALL')
  const [updatingId, setUpdatingId] = useState('')

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    void api.listExpenses().then(rows => { if (active) setExpenses(rows) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los gastos') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [revision])

  const paidTotal = expenses.filter(expense => expense.paid !== false).reduce((sum, expense) => sum + Number(expense.amount), 0)
  const pendingTotal = expenses.filter(expense => expense.paid === false).reduce((sum, expense) => sum + Number(expense.amount), 0)
  const visibleExpenses = expenses.filter(expense => view === 'ALL' || (view === 'PAID' ? expense.paid !== false : expense.paid === false))
  const grouped = expenses.reduce<Record<string, number>>((result, expense) => {
    result[expense.category] = (result[expense.category] ?? 0) + Number(expense.amount)
    return result
  }, {})

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError('')
    setMessage('')
    try {
      await api.createExpense({ expenseType: form.expenseType, category: form.category.trim(), subcategory: form.subcategory.trim() || undefined, amount: Number(form.amount), description: form.description.trim() || undefined, reference: form.reference.trim() || undefined, paid: form.paid, spentAt: form.spentAt || undefined, dueAt: form.dueAt || undefined })
      setForm({ expenseType: 'VARIABLE', category: '', subcategory: '', amount: '', description: '', reference: '', paid: true, spentAt: new Date().toISOString().slice(0, 10), dueAt: '' })
      setMessage('Gasto registrado')
      setRevision(value => value + 1)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo registrar el gasto')
    } finally {
      setSaving(false)
    }
  }

  async function markPaid(id: string) {
    setUpdatingId(id)
    setError('')
    try {
      await api.markExpensePaid(id)
      setRevision(value => value + 1)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo actualizar el gasto')
    } finally {
      setUpdatingId('')
    }
  }

  return <BaseLayout title="Gastos" description="Captura y consulta los egresos operativos del negocio.">
    <div className="grid gap-5 px-4 lg:grid-cols-3 lg:px-6">
      <Card className="lg:col-span-2"><CardHeader><CardTitle>Registrar gasto</CardTitle><CardDescription>Clasifica el egreso y distingue pagos realizados de cuentas pendientes.</CardDescription></CardHeader>
        <CardContent><form className="grid gap-3 md:grid-cols-2" onSubmit={submit}>
          <select aria-label="Tipo de gasto" className="h-10 rounded-md border bg-background px-3 text-sm" value={form.expenseType} onChange={event => setForm({ ...form, expenseType: event.target.value as typeof form.expenseType })}><option value="FIXED">Gasto fijo</option><option value="VARIABLE">Gasto variable</option><option value="INVENTORY">Compra de inventario</option></select>
          <Input required maxLength={120} placeholder="Categoría (renta, herramienta, servicio…)" value={form.category} onChange={event => setForm({ ...form, category: event.target.value })}/>
          <Input maxLength={120} placeholder="Subcategoría" value={form.subcategory} onChange={event => setForm({ ...form, subcategory: event.target.value })}/>
          <Input required type="number" min="0.01" step="0.01" placeholder="Monto" value={form.amount} onChange={event => setForm({ ...form, amount: event.target.value })}/>
          <Input maxLength={255} placeholder="Descripción" value={form.description} onChange={event => setForm({ ...form, description: event.target.value })}/>
          <Input maxLength={100} placeholder="Referencia / folio" value={form.reference} onChange={event => setForm({ ...form, reference: event.target.value })}/>
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.paid} onChange={event => setForm({ ...form, paid: event.target.checked })}/> Ya fue pagado</label>
          <label className="grid gap-1 text-sm text-muted-foreground">Fecha del gasto<Input type="date" value={form.spentAt} onChange={event => setForm({ ...form, spentAt: event.target.value })}/></label>
          {!form.paid && <label className="grid gap-1 text-sm text-muted-foreground">Vence el<Input type="date" value={form.dueAt} onChange={event => setForm({ ...form, dueAt: event.target.value })}/></label>}
          <Button disabled={saving}>{saving ? 'Guardando…' : 'Registrar gasto'}</Button>
        </form>{message && <p role="status" className="mt-3 text-sm text-emerald-700">{message}</p>}{error && <p role="alert" className="mt-3 text-sm text-destructive">{error}</p>}</CardContent>
      </Card>
      <Card><CardHeader><CardDescription>Pagado · Pendiente</CardDescription><CardTitle className="text-2xl">{money.format(paidTotal)}</CardTitle><CardDescription>{money.format(pendingTotal)} por pagar</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(grouped).sort((a,b)=>b[1]-a[1]).map(([category, amount])=><div className="flex justify-between text-sm" key={category}><span>{category}</span><span>{money.format(amount)}</span></div>)}</CardContent></Card>
      <Card className="lg:col-span-3"><CardHeader><CardTitle>Movimientos recientes</CardTitle><div className="flex flex-wrap gap-2 pt-2">{([['ALL','Todos'],['PAID','Realizados'],['PENDING','Pendientes']] as const).map(([key,label])=><Button key={key} type="button" size="sm" variant={view===key?'default':'outline'} onClick={()=>setView(key)}>{label} ({key==='ALL'?expenses.length:key==='PAID'?expenses.filter(e=>e.paid!==false).length:expenses.filter(e=>e.paid===false).length})</Button>)}</div></CardHeader><CardContent className="space-y-2">
        {loading && <p role="status" className="text-sm text-muted-foreground">Cargando gastos…</p>}
        {!loading && !error && visibleExpenses.map(expense => <div className="flex flex-wrap items-center justify-between gap-2 rounded border p-3" key={expense.id}><div><p className="font-medium">{expense.category}{expense.subcategory ? ` · ${expense.subcategory}` : ''}</p><p className="text-sm text-muted-foreground">{{FIXED:'Fijo',VARIABLE:'Variable',INVENTORY:'Inventario'}[expense.expenseType ?? 'VARIABLE']} · {expense.description || 'Sin descripción'}{expense.reference ? ` · ${expense.reference}` : ''}</p><p className="text-xs text-muted-foreground">{expense.paid === false ? `Pendiente${expense.dueAt ? ` · vence ${expense.dueAt}` : ''}` : 'Pagado'}{expense.spentAt ? ` · ${expense.spentAt}` : ''}</p></div><div className="flex items-center gap-3">{expense.paid === false&&<Button size="sm" variant="outline" disabled={updatingId===expense.id} onClick={()=>void markPaid(expense.id)}>{updatingId===expense.id?'Actualizando…':'Marcar pagado'}</Button>}<div className="text-right"><p className="font-semibold">{money.format(Number(expense.amount))}</p><p className="text-xs text-muted-foreground">{new Date(expense.createdAt).toLocaleString('es-MX')}</p></div></div></div>)}
        {!loading && !error && expenses.length === 0 && <p className="text-sm text-muted-foreground">Aún no hay gastos registrados.</p>}
        {error && <div className="flex items-center justify-between gap-3"><p role="alert" className="text-sm text-destructive">{error}</p><Button variant="outline" onClick={() => setRevision(value => value + 1)}>Reintentar</Button></div>}
      </CardContent></Card>
    </div>
  </BaseLayout>
}
