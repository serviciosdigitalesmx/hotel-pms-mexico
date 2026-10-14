import { useCallback, useEffect, useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import * as api from './api'

export function OrderStockReservations({ orderId, orderStatus, revision }: { orderId: string; orderStatus: string; revision: number }) {
  const [reservations, setReservations] = useState<api.StockReservation[]>([])
  const [products, setProducts] = useState<api.Product[]>([])
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState('')
  const load = useCallback(async () => {
    setError('')
    try {
      const [rows, productRows] = await Promise.all([api.listOrderStockReservations(orderId), api.listProducts()])
      setReservations(rows); setProducts(productRows)
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron cargar las refacciones reservadas') }
  }, [orderId])
  useEffect(() => { void load() }, [load, revision])

  async function release(row: api.StockReservation) {
    setBusyId(row.id); setError('')
    try { await api.releaseStock(orderId, row.productId); await load() }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo liberar la refacción') }
    finally { setBusyId('') }
  }

  const statusLabel: Record<api.StockReservation['status'], string> = { RESERVED: 'Reservada', RELEASED: 'Liberada', CONSUMED: 'Consumida' }
  return <div className="space-y-2"><h3 className="text-sm font-medium">Refacciones de esta orden</h3>
    {reservations.map(row => <div className="flex flex-wrap items-center justify-between gap-2 rounded border p-2 text-sm" key={row.id}>
      <span>{products.find(product => product.id === row.productId)?.name ?? row.productId} · {row.quantity} pzas.</span>
      <div className="flex items-center gap-2"><Badge variant={row.status === 'CONSUMED' ? 'default' : 'outline'}>{statusLabel[row.status]}</Badge>{row.status === 'RESERVED' && orderStatus !== 'COMPLETED' && <Button type="button" size="sm" variant="outline" disabled={!!busyId} onClick={() => void release(row)}>{busyId === row.id ? 'Liberando…' : 'Liberar'}</Button>}</div>
    </div>)}
    {!reservations.length && !error && <p className="text-sm text-muted-foreground">Todavía no hay refacciones reservadas.</p>}
    {error && <div className="flex items-center justify-between gap-2"><p role="alert" className="text-sm text-destructive">{error}</p><Button size="sm" type="button" variant="outline" onClick={() => void load()}>Reintentar</Button></div>}
  </div>
}
