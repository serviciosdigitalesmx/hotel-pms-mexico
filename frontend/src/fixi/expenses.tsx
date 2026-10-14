import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const money = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })
const localToday = () => { const now = new Date(); return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}` }

export function ExpensesPage() {
  const [expenses, setExpenses] = useState<api.Expense[]>([])
  const [openCashSession, setOpenCashSession] = useState<api.CashSessionSummary | null>(null)
  const [form, setForm] = useState({ expenseType: 'VARIABLE' as 'FIXED' | 'VARIABLE' | 'INVENTORY', category: '', subcategory: '', amount: '', description: '', reference: '', paid: true, spentAt: localToday(), dueAt: '' })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [editingId, setEditingId] = useState('')
  const [revision, setRevision] = useState(0)
  const [view, setView] = useState<'ALL' | 'PAID' | 'PENDING'>('ALL')
  const [updatingId, setUpdatingId] = useState('')
  const [paymentMethod, setPaymentMethod] = useState<'CASH'|'BANK_TRANSFER'|'CARD'|'OTHER'>('OTHER')
  const [pendingPaymentMethods, setPendingPaymentMethods] = useState<Record<string, 'CASH'|'BANK_TRANSFER'|'CARD'|'OTHER'>>({})
  const [typeFilter, setTypeFilter] = useState('ALL')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [observation, setObservation] = useState('')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    void api.listExpenses().then(rows => { if (active) setExpenses(rows) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los gastos') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [revision])

  useEffect(() => {
    let active = true
    void api.getOpenCashSession().then(session => { if (active) setOpenCashSession(session) })
      .catch(() => { if (active) setOpenCashSession(null) })
    return () => { active = false }
  }, [revision])

  const paidTotal = expenses.filter(expense => expense.paid !== false).reduce((sum, expense) => sum + Number(expense.amount), 0)
  const pendingTotal = expenses.filter(expense => expense.paid === false).reduce((sum, expense) => sum + Number(expense.amount), 0)
  const visibleExpenses = expenses.filter(expense => (view === 'ALL' || (view === 'PAID' ? expense.paid !== false : expense.paid === false)) && (typeFilter === 'ALL' || (expense.expenseType ?? 'VARIABLE') === typeFilter) && (!categoryFilter || expense.category === categoryFilter) && (!observation || `${expense.description ?? ''} ${expense.subcategory ?? ''} ${expense.reference ?? ''}`.toLocaleLowerCase().includes(observation.toLocaleLowerCase())) && (!dateFrom || (expense.spentAt ?? expense.createdAt.slice(0, 10)) >= dateFrom) && (!dateTo || (expense.spentAt ?? expense.createdAt.slice(0, 10)) <= dateTo))
  const grouped = expenses.reduce<Record<string, number>>((result, expense) => {
    result[expense.category] = (result[expense.category] ?? 0) + Number(expense.amount)
    return result
  }, {})

  function exportCsv() {
    const rows = [['id', 'fecha_gasto', 'tipo', 'categoria', 'subcategoria', 'descripcion', 'referencia', 'monto', 'estado', 'fecha_vencimiento', 'forma_pago', 'fecha_pago'],
      ...visibleExpenses.map(expense => [expense.id, expense.spentAt ?? expense.createdAt.slice(0, 10), expense.expenseType ?? 'VARIABLE', expense.category, expense.subcategory ?? '', expense.description ?? '', expense.reference ?? '', expense.amount, expense.paid === false ? 'PENDIENTE' : 'PAGADO', expense.dueAt ?? '', expense.paymentMethod ?? '', expense.paidAt ?? ''])]
    const csv = rows.map(row => row.map(api.csvCell).join(',')).join('\r\n')
    const url = URL.createObjectURL(new Blob(['\uFEFF', csv], { type: 'text/csv;charset=utf-8' }))
    const link = document.createElement('a')
    link.href = url
    link.download = 'fixi-gastos.csv'
    link.click()
    URL.revokeObjectURL(url)
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError('')
    setMessage('')
    try {
      const expense = { expenseType: form.expenseType, category: form.category.trim(), subcategory: form.subcategory.trim() || undefined, amount: Number(form.amount), description: form.description.trim() || undefined, reference: form.reference.trim() || undefined, spentAt: form.spentAt || undefined, dueAt: form.dueAt || undefined }
      if (editingId) await api.updateExpense(editingId, { ...expense, paid: false })
      else {
        if (form.paid && paymentMethod === 'CASH' && !openCashSession) throw new Error('Abre una caja antes de registrar un gasto pagado en efectivo')
        await api.createExpense({ ...expense, cashSessionId: form.paid && paymentMethod === 'CASH' ? openCashSession?.id : undefined, paid: form.paid, paymentMethod: form.paid ? paymentMethod === 'CASH' ? 'OTHER' : paymentMethod : undefined })
      }
      setEditingId('')
      setForm({ expenseType: 'VARIABLE', category: '', subcategory: '', amount: '', description: '', reference: '', paid: true, spentAt: localToday(), dueAt: '' })
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
      const method = pendingPaymentMethods[id] ?? 'OTHER'
      if (method === 'CASH' && !openCashSession) throw new Error('Abre una caja antes de pagar este gasto en efectivo')
      await api.markExpensePaid(id, method === 'CASH' ? 'OTHER' : method, method === 'CASH' ? openCashSession?.id : undefined)
      setRevision(value => value + 1)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo actualizar el gasto')
    } finally {
      setUpdatingId('')
    }
  }

  return <BaseLayout title="Gastos" description="Captura y consulta los egresos operativos del negocio.">
    <div className="grid gap-5 px-4 lg:grid-cols-3 lg:px-6">
      <Card className="lg:col-span-2"><CardHeader><CardTitle>{editingId ? 'Editar gasto pendiente' : 'Registrar gasto'}</CardTitle><CardDescription>Clasifica el egreso y distingue pagos realizados de cuentas pendientes. Los gastos pagados no se editan para proteger los movimientos de caja.</CardDescription></CardHeader>
        <CardContent><form className="grid gap-3 md:grid-cols-2" onSubmit={submit}>
          <select aria-label="Tipo de gasto" className="h-10 rounded-md border bg-background px-3 text-sm" value={form.expenseType} onChange={event => setForm({ ...form, expenseType: event.target.value as typeof form.expenseType })}><option value="FIXED">Gasto fijo</option><option value="VARIABLE">Gasto variable</option><option value="INVENTORY">Compra de inventario</option></select>
          <Input required maxLength={120} placeholder="Categoría (renta, herramienta, servicio…)" value={form.category} onChange={event => setForm({ ...form, category: event.target.value })}/>
          <Input maxLength={120} placeholder="Subcategoría" value={form.subcategory} onChange={event => setForm({ ...form, subcategory: event.target.value })}/>
          <Input required type="number" min="0.01" step="0.01" placeholder="Monto" value={form.amount} onChange={event => setForm({ ...form, amount: event.target.value })}/>
          <Input maxLength={255} placeholder="Descripción" value={form.description} onChange={event => setForm({ ...form, description: event.target.value })}/>
          <Input maxLength={100} placeholder="Referencia / folio" value={form.reference} onChange={event => setForm({ ...form, reference: event.target.value })}/>
          {!editingId && <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.paid} onChange={event => setForm({ ...form, paid: event.target.checked })}/> Ya fue pagado</label>}
          {!editingId && form.paid && <label className="grid gap-1 text-sm text-muted-foreground">Forma de pago<select aria-label="Forma de pago inicial" className="h-10 rounded-md border bg-background px-3 text-sm text-foreground" value={paymentMethod} onChange={event => setPaymentMethod(event.target.value as typeof paymentMethod)}><option value="CASH" disabled={!openCashSession}>Efectivo{openCashSession ? ' · caja abierta' : ' · requiere caja abierta'}</option><option value="BANK_TRANSFER">Transferencia</option><option value="CARD">Tarjeta</option><option value="OTHER">Otro / externo</option></select></label>}
          <label className="grid gap-1 text-sm text-muted-foreground">Fecha del gasto<Input type="date" value={form.spentAt} onChange={event => setForm({ ...form, spentAt: event.target.value })}/></label>
          {!form.paid && <label className="grid gap-1 text-sm text-muted-foreground">Vence el<Input type="date" value={form.dueAt} onChange={event => setForm({ ...form, dueAt: event.target.value })}/></label>}
          <div className="flex gap-2"><Button disabled={saving}>{saving ? 'Guardando…' : editingId ? 'Guardar cambios' : 'Registrar gasto'}</Button>{editingId&&<Button type="button" variant="outline" disabled={saving} onClick={()=>{setEditingId('');setForm({ expenseType:'VARIABLE',category:'',subcategory:'',amount:'',description:'',reference:'',paid:true,spentAt:localToday(),dueAt:'' })}}>Cancelar</Button>}</div>
        </form>{message && <p role="status" className="mt-3 text-sm text-emerald-700">{message}</p>}{error && <p role="alert" className="mt-3 text-sm text-destructive">{error}</p>}</CardContent>
      </Card>
      <Card><CardHeader><CardDescription>Pagado · Pendiente</CardDescription><CardTitle className="text-2xl">{money.format(paidTotal)}</CardTitle><CardDescription>{money.format(pendingTotal)} por pagar</CardDescription></CardHeader><CardContent className="space-y-2">{Object.entries(grouped).sort((a,b)=>b[1]-a[1]).map(([category, amount])=><div className="flex justify-between text-sm" key={category}><span>{category}</span><span>{money.format(amount)}</span></div>)}</CardContent></Card>
      <Card className="lg:col-span-3"><CardHeader><div className="flex flex-wrap items-center justify-between gap-2"><CardTitle>Movimientos recientes</CardTitle><Button type="button" size="sm" variant="outline" disabled={loading || !!error || !visibleExpenses.length} onClick={exportCsv}>Exportar filtrados CSV ({visibleExpenses.length})</Button></div><div className="flex flex-wrap gap-2 pt-2">{([['ALL','Todos'],['PAID','Realizados'],['PENDING','Pendientes']] as const).map(([key,label])=><Button key={key} type="button" size="sm" variant={view===key?'default':'outline'} onClick={()=>setView(key)}>{label} ({key==='ALL'?expenses.length:key==='PAID'?expenses.filter(e=>e.paid!==false).length:expenses.filter(e=>e.paid===false).length})</Button>)}</div><div className="grid gap-2 pt-3 sm:grid-cols-2 lg:grid-cols-5"><select aria-label="Filtrar tipo de gasto" className="h-10 rounded-md border bg-background px-3 text-sm" value={typeFilter} onChange={e=>setTypeFilter(e.target.value)}><option value="ALL">Todos los tipos</option><option value="FIXED">Fijo</option><option value="VARIABLE">Variable</option><option value="INVENTORY">Inventario</option></select><select aria-label="Filtrar categoría" className="h-10 rounded-md border bg-background px-3 text-sm" value={categoryFilter} onChange={e=>setCategoryFilter(e.target.value)}><option value="">Todas las categorías</option>{[...new Set(expenses.map(expense=>expense.category))].sort().map(category=><option key={category}>{category}</option>)}</select><Input aria-label="Buscar observación" placeholder="Observación / referencia" value={observation} onChange={e=>setObservation(e.target.value)}/><Input aria-label="Fecha desde" type="date" value={dateFrom} onChange={e=>setDateFrom(e.target.value)}/><Input aria-label="Fecha hasta" type="date" value={dateTo} onChange={e=>setDateTo(e.target.value)}/></div></CardHeader><CardContent className="space-y-2">
        {loading && <p role="status" className="text-sm text-muted-foreground">Cargando gastos…</p>}
        {!loading && !error && visibleExpenses.map(expense => <div className="flex flex-wrap items-center justify-between gap-2 rounded border p-3" key={expense.id}><div><p className="font-medium">{expense.category}{expense.subcategory ? ` · ${expense.subcategory}` : ''}</p><p className="text-sm text-muted-foreground">{{FIXED:'Fijo',VARIABLE:'Variable',INVENTORY:'Inventario'}[expense.expenseType ?? 'VARIABLE']} · {expense.description || 'Sin descripción'}{expense.reference ? ` · ${expense.reference}` : ''}</p><p className="text-xs text-muted-foreground">{expense.paid === false ? `Pendiente${expense.dueAt ? ` · vence ${expense.dueAt}` : ''}` : `Pagado · ${{CASH:'Efectivo',BANK_TRANSFER:'Transferencia',CARD:'Tarjeta',OTHER:'Otro'}[expense.paymentMethod ?? 'OTHER']}${expense.paidAt ? ` · ${new Date(expense.paidAt).toLocaleString('es-MX')}` : ''}`}{expense.spentAt ? ` · gasto ${expense.spentAt}` : ''}</p></div><div className="flex items-center gap-3">{expense.paid === false&&<><select aria-label={`Forma de pago para ${expense.category}`} className="h-9 rounded-md border bg-background px-2 text-xs" value={pendingPaymentMethods[expense.id] ?? 'OTHER'} onChange={event=>setPendingPaymentMethods(current=>({...current,[expense.id]:event.target.value as 'CASH'|'BANK_TRANSFER'|'CARD'|'OTHER'}))}><option value="CASH" disabled={!openCashSession}>Efectivo{openCashSession ? '' : ' · requiere caja abierta'}</option><option value="BANK_TRANSFER">Transferencia</option><option value="CARD">Tarjeta</option><option value="OTHER">Otro / externo</option></select><Button size="sm" variant="outline" disabled={updatingId===expense.id||saving} onClick={()=>void markPaid(expense.id)}>{updatingId===expense.id?'Actualizando…':'Marcar pagado'}</Button><Button size="sm" variant="outline" onClick={()=>{setEditingId(expense.id);setForm({expenseType:expense.expenseType??'VARIABLE',category:expense.category,subcategory:expense.subcategory??'',amount:String(expense.amount),description:expense.description??'',reference:expense.reference??'',paid:false,spentAt:expense.spentAt??expense.createdAt.slice(0,10),dueAt:expense.dueAt??''})}}>Editar</Button></>}<div className="text-right"><p className="font-semibold">{money.format(Number(expense.amount))}</p><p className="text-xs text-muted-foreground">{new Date(expense.createdAt).toLocaleString('es-MX')}</p></div></div></div>)}
        {!loading && !error && visibleExpenses.length === 0 && <p className="text-sm text-muted-foreground">No hay gastos que coincidan con estos filtros.</p>}
        {error && <div className="flex items-center justify-between gap-3"><p role="alert" className="text-sm text-destructive">{error}</p><Button variant="outline" onClick={() => setRevision(value => value + 1)}>Reintentar</Button></div>}
      </CardContent></Card>
    </div>
  </BaseLayout>
}
