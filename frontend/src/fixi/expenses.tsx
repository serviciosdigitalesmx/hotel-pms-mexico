import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const money = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })

export function ExpensesPage() {
  const [expenses, setExpenses] = useState<api.Expense[]>([])
  const [form, setForm] = useState({ category: '', amount: '', description: '', reference: '' })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    void api.listExpenses().then(rows => { if (active) setExpenses(rows) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los gastos') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [revision])

  const total = expenses.reduce((sum, expense) => sum + Number(expense.amount), 0)
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
      await api.createExpense({ category: form.category.trim(), amount: Number(form.amount), description: form.description.trim() || undefined, reference: form.reference.trim() || undefined })
      setForm({ category: '', amount: '', description: '', reference: '' })
      setMessage('Gasto registrado')
      setRevision(value => value + 1)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo registrar el gasto')
    } finally {
      setSaving(false)
    }
  }

  return <BaseLayout title="Gastos" description="Captura y consulta los egresos operativos del negocio.">
    <div className="grid gap-5 px-4 lg:grid-cols-3 lg:px-6">
      <Card className="lg:col-span-2"><CardHeader><CardTitle>Registrar gasto</CardTitle><CardDescription>El backend valida el monto positivo y mantiene el registro aislado por negocio.</CardDescription></CardHeader>
        <CardContent><form className="grid gap-3 md:grid-cols-2" onSubmit={submit}>
          <Input required maxLength={120} placeholder="Categoría (renta, herramienta, servicio…)" value={form.category} onChange={event => setForm({ ...form, category: event.target.value })}/>
          <Input required type="number" min="0.01" step="0.01" placeholder="Monto" value={form.amount} onChange={event => setForm({ ...form, amount: event.target.value })}/>
          <Input maxLength={255} placeholder="Descripción" value={form.description} onChange={event => setForm({ ...form, description: event.target.value })}/>
          <Input maxLength={100} placeholder="Referencia / folio" value={form.reference} onChange={event => setForm({ ...form, reference: event.target.value })}/>
          <Button disabled={saving}>{saving ? 'Guardando…' : 'Registrar gasto'}</Button>
        </form>{message && <p role="status" className="mt-3 text-sm text-emerald-700">{message}</p>}{error && <p role="alert" className="mt-3 text-sm text-destructive">{error}</p>}</CardContent>
      </Card>
      <Card><CardHeader><CardDescription>Total registrado</CardDescription><CardTitle className="text-3xl">{money.format(total)}</CardTitle></CardHeader><CardContent className="space-y-2">{Object.entries(grouped).sort((a,b)=>b[1]-a[1]).map(([category, amount])=><div className="flex justify-between text-sm" key={category}><span>{category}</span><span>{money.format(amount)}</span></div>)}</CardContent></Card>
      <Card className="lg:col-span-3"><CardHeader><CardTitle>Movimientos recientes</CardTitle></CardHeader><CardContent className="space-y-2">
        {loading && <p role="status" className="text-sm text-muted-foreground">Cargando gastos…</p>}
        {!loading && !error && expenses.map(expense => <div className="flex flex-wrap items-center justify-between gap-2 rounded border p-3" key={expense.id}><div><p className="font-medium">{expense.category}</p><p className="text-sm text-muted-foreground">{expense.description || 'Sin descripción'}{expense.reference ? ` · ${expense.reference}` : ''}</p></div><div className="text-right"><p className="font-semibold">{money.format(Number(expense.amount))}</p><p className="text-xs text-muted-foreground">{new Date(expense.createdAt).toLocaleString('es-MX')}</p></div></div>)}
        {!loading && !error && expenses.length === 0 && <p className="text-sm text-muted-foreground">Aún no hay gastos registrados.</p>}
        {error && <div className="flex items-center justify-between gap-3"><p role="alert" className="text-sm text-destructive">{error}</p><Button variant="outline" onClick={() => setRevision(value => value + 1)}>Reintentar</Button></div>}
      </CardContent></Card>
    </div>
  </BaseLayout>
}
