import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import * as api from './api'

const money = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })
const methods = ['CASH', 'CHECK', 'BANK_DEPOSIT', 'PAYPAL', 'CARD'] as const
const methodName: Record<typeof methods[number], string> = { CASH: 'Efectivo', CHECK: 'Cheque', BANK_DEPOSIT: 'Depósito bancario', PAYPAL: 'PayPal', CARD: 'Tarjeta' }

export function ServiceOrderPaymentSettlementPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState<api.ServiceOrder>()
  const [method, setMethod] = useState<typeof methods[number]>('CASH')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let active = true
    api.getServiceOrder(id).then(value => { if (active) setOrder(value) }).catch(reason => {
      if (active) setError(reason instanceof Error ? reason.message : 'No se pudo cargar la orden')
    })
    return () => { active = false }
  }, [id])

  async function settle() {
    setBusy(true); setError('')
    try {
      await api.recordPendingServiceOrderPayment(id, method)
      navigate('/orders/pending-payment')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo registrar el pago')
    } finally { setBusy(false) }
  }

  const balance = Math.max(0, Number(order?.quoteTotal ?? 0) - Number(order?.depositAmount ?? 0))
  return <BaseLayout title="Liquidar orden" description={order?.summary ?? id}><div className="px-4 lg:px-6">
    <Card><CardHeader><CardTitle>Registrar pago pendiente</CardTitle><CardDescription>El pago quedará asentado en la orden y en su historial operativo.</CardDescription></CardHeader><CardContent className="space-y-4">
      {!order && !error && <p role="status">Cargando orden…</p>}
      {order && <><div className="space-y-1 rounded border p-3 text-sm"><p>Folio: {order.id}</p><p>Estado: {order.status} · Pago: {order.paymentStatus ?? 'sin registrar'}</p><p>Total cotizado: {money.format(Number(order.quoteTotal ?? 0))}</p><p>Anticipo: {money.format(Number(order.depositAmount ?? 0))}</p><p className="font-semibold">Saldo a liquidar: {money.format(balance)}</p></div>
        <label className="block space-y-1 text-sm">Método recibido<select aria-label="Método recibido" className="h-10 w-full rounded-md border bg-background px-3" value={method} onChange={event => setMethod(event.target.value as typeof method)}>{methods.map(value => <option key={value} value={value}>{methodName[value]}</option>)}</select></label>
        <div className="flex gap-2"><Button disabled={busy || order.status !== 'COMPLETED' || order.paymentStatus !== 'PENDING' || balance <= 0} onClick={() => void settle()}>{busy ? 'Registrando…' : 'Confirmar pago recibido'}</Button><Button type="button" variant="outline" disabled={busy} onClick={() => navigate('/orders/pending-payment')}>Volver a pendientes</Button></div>
        {(order.status !== 'COMPLETED' || order.paymentStatus !== 'PENDING') && <p className="text-sm text-muted-foreground">Esta orden ya no tiene un pago pendiente liquidable.</p>}
      </>}
      {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
    </CardContent></Card>
  </div></BaseLayout>
}
