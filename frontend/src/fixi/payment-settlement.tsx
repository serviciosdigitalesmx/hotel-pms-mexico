import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const money = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })
const methods = ['CASH', 'CHECK', 'BANK_DEPOSIT', 'PAYPAL', 'CARD'] as const
const methodName: Record<typeof methods[number], string> = { CASH: 'Efectivo', CHECK: 'Cheque', BANK_DEPOSIT: 'Depósito bancario', PAYPAL: 'PayPal', CARD: 'Tarjeta' }

export function ServiceOrderPaymentSettlementPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState<api.ServiceOrder>()
  const [method, setMethod] = useState<typeof methods[number]>('CASH')
  const [amount, setAmount] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let active = true
    api.getServiceOrder(id).then(value => { if (active) { setOrder(value); setAmount(api.serviceOrderBalance(value).toFixed(2)) } }).catch(reason => {
      if (active) setError(reason instanceof Error ? reason.message : 'No se pudo cargar la orden')
    })
    return () => { active = false }
  }, [id])

  async function settle() {
    setBusy(true); setError('')
    try {
      const amountReceived = Number(amount)
      if (!Number.isFinite(amountReceived) || amountReceived <= 0 || !/^\d{1,10}(?:\.\d{1,2})?$/.test(amount.trim())) throw new Error('Captura un monto mayor a cero con máximo dos decimales')
      if (amountReceived > balance) throw new Error('El monto recibido no puede superar el saldo pendiente')
      const updated = await api.recordPendingServiceOrderPayment(id, method, amountReceived)
      if (updated.paymentStatus === 'PAID') navigate('/orders/pending-payment')
      else {
        const remaining = api.serviceOrderBalance(updated)
        setOrder(updated); setAmount(remaining.toFixed(2)); setMessage(`Abono de ${money.format(amountReceived)} registrado. Saldo restante: ${money.format(remaining)}.`)
      }
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo registrar el pago')
    } finally { setBusy(false) }
  }

  const balance = order ? api.serviceOrderBalance(order) : 0
  return <BaseLayout title="Liquidar orden" description={order?.summary ?? id}><div className="px-4 lg:px-6">
    <Card><CardHeader><CardTitle>Registrar pago pendiente</CardTitle><CardDescription>El pago quedará asentado en la orden y en su historial operativo.</CardDescription></CardHeader><CardContent className="space-y-4">
      {!order && !error && <p role="status">Cargando orden…</p>}
      {order && <><div className="space-y-1 rounded border p-3 text-sm"><p>Folio: {order.id}</p><p>Estado: {order.status} · Pago: {order.paymentStatus ?? 'sin registrar'}</p><p>Total cotizado: {money.format(Number(order.quoteTotal ?? 0))}</p><p>Anticipo recibido: {money.format(Number(order.depositAmount ?? 0))}</p><p>Total recibido: {money.format(api.serviceOrderAmountReceived(order))}</p><p className="font-semibold">Saldo a liquidar: {money.format(balance)}</p></div>
        <label className="block space-y-1 text-sm">Monto recibido<Input aria-label="Monto recibido" type="number" min="0.01" max={balance} step="0.01" inputMode="decimal" value={amount} onChange={event=>setAmount(event.target.value)}/></label>
        <label className="block space-y-1 text-sm">Método recibido<select aria-label="Método recibido" className="h-10 w-full rounded-md border bg-background px-3" value={method} onChange={event => setMethod(event.target.value as typeof method)}>{methods.map(value => <option key={value} value={value}>{methodName[value]}</option>)}</select></label>
        <div className="flex gap-2"><Button disabled={busy || order.status !== 'COMPLETED' || order.paymentStatus !== 'PENDING' || balance <= 0 || Number(amount) <= 0 || Number(amount) > balance} onClick={() => void settle()}>{busy ? 'Registrando…' : Number(amount) < balance ? 'Registrar abono' : 'Confirmar pago recibido'}</Button><Button type="button" variant="outline" disabled={busy} onClick={() => navigate('/orders/pending-payment')}>Volver a pendientes</Button></div>
        {(order.status !== 'COMPLETED' || order.paymentStatus !== 'PENDING') && <p className="text-sm text-muted-foreground">Esta orden ya no tiene un pago pendiente liquidable.</p>}
      </>}
      {message && <p role="status" className="text-sm text-emerald-700">{message}</p>}
      {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
    </CardContent></Card>
  </div></BaseLayout>
}
