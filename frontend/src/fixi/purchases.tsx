import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import * as api from './api'

const field = 'w-full rounded-md border bg-background px-3 py-2 text-sm'
type PurchaseLine = { productId: string; quantityOrdered: number; quantityReceived: number; unitCost: number }
type Purchase = { id: string; branchId: string; supplierId: string; serviceOrderId?: string; status: string; createdAt: string; lines: PurchaseLine[] }
type DraftLine = { productId: string; quantity: number; unitCost: number }

export function Purchases() {
  const [purchases, setPurchases] = useState<Purchase[]>([])
  const [branches, setBranches] = useState<api.Branch[]>([])
  const [suppliers, setSuppliers] = useState<api.Supplier[]>([])
  const [products, setProducts] = useState<api.Product[]>([])
  const [serviceOrders, setServiceOrders] = useState<api.ServiceOrder[]>([])
  const [form, setForm] = useState({ branchId: '', supplierId: '', serviceOrderId: '' })
  const [draftLines, setDraftLines] = useState<DraftLine[]>([{ productId: '', quantity: 1, unitCost: 0 }])
  const [purchaseId, setPurchaseId] = useState('')
  const [receiptLines, setReceiptLines] = useState<Array<{ productId: string; quantity: number }>>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function load() {
    setLoading(true); setError('')
    try {
      const [orders, branchRows, supplierRows, productRows, serviceOrderRows] = await Promise.all([
        api.listPurchases(), api.listBranches(), api.listSuppliers(), api.listProducts(), api.listServiceOrders().catch(() => []),
      ])
      setPurchases(orders as Purchase[]); setBranches(branchRows); setSuppliers(supplierRows); setProducts(productRows); setServiceOrders(serviceOrderRows)
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron cargar las compras') }
    finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [])

  async function create(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setMessage('')
    try {
      await api.createPurchase({ ...form, serviceOrderId: form.serviceOrderId || null, lines: draftLines })
      setForm(current => ({ ...current, serviceOrderId: '' })); setDraftLines([{ productId: '', quantity: 1, unitCost: 0 }])
      setMessage('Orden de compra creada con partidas'); await load()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo crear la compra') }
    finally { setBusy(false) }
  }

  function selectPurchase(id: string) {
    setPurchaseId(id)
    const order = purchases.find(item => item.id === id)
    setReceiptLines(order?.lines.filter(line => line.quantityReceived < line.quantityOrdered)
      .map(line => ({ productId: line.productId, quantity: Math.max(1, line.quantityOrdered - line.quantityReceived) })) ?? [])
  }

  async function receive(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setMessage('')
    try {
      await api.receivePurchase(purchaseId, { lines: receiptLines })
      setMessage('Recepción registrada y existencias actualizadas'); setPurchaseId(''); setReceiptLines([]); await load()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo registrar la recepción') }
    finally { setBusy(false) }
  }

  async function cancel(order: Purchase) {
    if (!window.confirm('¿Cancelar esta orden de compra? La acción quedará en el historial y no modificará existencias.')) return
    setBusy(true); setError(''); setMessage('')
    try {
      await api.cancelPurchase(order.id)
      setMessage('Orden cancelada; el historial y las existencias recibidas no se modificaron')
      if (purchaseId === order.id) { setPurchaseId(''); setReceiptLines([]) }
      await load()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo cancelar la compra') }
    finally { setBusy(false) }
  }

  const branchName = (id: string) => branches.find(branch => branch.id === id)?.name ?? 'Sucursal'
  const supplierName = (id: string) => suppliers.find(supplier => supplier.id === id)?.name ?? 'Proveedor'
  const selectedPurchase = purchases.find(order => order.id === purchaseId)
  const remaining = (productId: string) => {
    const line = selectedPurchase?.lines.find(item => item.productId === productId)
    return Math.max(0, (line?.quantityOrdered ?? 0) - (line?.quantityReceived ?? 0))
  }
  const purchaseAmounts = (order: Purchase) => (order.lines ?? []).reduce((totals, line) => ({
    ordered: totals.ordered + line.quantityOrdered * line.unitCost,
    received: totals.received + line.quantityReceived * line.unitCost,
  }), { ordered: 0, received: 0 })
  const draftTotal = draftLines.reduce((total, line) => total + line.quantity * line.unitCost, 0)
  const formatMoney = (amount: number) => new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(amount)

  return <BaseLayout title="Compras" description="Órdenes con partidas previstas y recepción parcial o completa.">
    <div className="grid gap-5 px-4 lg:grid-cols-2 lg:px-6">
      <Card><CardHeader><CardTitle>Nueva compra</CardTitle></CardHeader><CardContent>
        <form className="space-y-3" onSubmit={create}>
          <select required aria-label="Sucursal de compra" className={field} value={form.branchId} onChange={event => setForm(current => ({ ...current, branchId: event.target.value, serviceOrderId: '' }))}>
            <option value="">Sucursal</option>{branches.map(branch => <option key={branch.id} value={branch.id}>{branch.name}</option>)}
          </select>
          <select required aria-label="Proveedor de compra" className={field} value={form.supplierId} onChange={event => {
            const supplierId = event.target.value
            setForm(current => ({ ...current, supplierId }))
            const compatibleIds = new Set(products.filter(product => !product.supplierId || product.supplierId === supplierId).map(product => product.id))
            setDraftLines(current => current.map(line => line.productId && !compatibleIds.has(line.productId)
              ? { ...line, productId: '', unitCost: 0 } : line))
          }}>
            <option value="">Proveedor</option>{suppliers.map(supplier => <option key={supplier.id} value={supplier.id}>{supplier.name}</option>)}
          </select>
          <select aria-label="Orden de servicio asociada" className={field} disabled={!form.branchId} value={form.serviceOrderId} onChange={event => setForm({ ...form, serviceOrderId: event.target.value })}>
            <option value="">Sin orden de servicio asociada</option>
            {serviceOrders.filter(order => order.branchId === form.branchId && !['COMPLETED', 'CANCELLED'].includes(order.status)).map(order => <option key={order.id} value={order.id}>{order.summary} · {order.status} · {order.id.slice(0, 8)}</option>)}
          </select>
          {draftLines.map((line, index) => <div key={index} className="grid gap-2 sm:grid-cols-[1fr_6rem_8rem_auto]">
            <select required aria-label={`Producto ordenado ${index + 1}`} className={field} value={line.productId} onChange={event => setDraftLines(current => current.map((item, i) => i === index ? { ...item, productId: event.target.value, unitCost: Number(products.find(product => product.id === event.target.value)?.cost ?? 0) } : item))}>
              <option value="">Producto</option>{products.filter(product => (!product.supplierId || product.supplierId === form.supplierId)
                && (product.id === line.productId || !draftLines.some((selected, selectedIndex) => selectedIndex !== index && selected.productId === product.id)))
                .map(product => <option key={product.id} value={product.id}>{product.sku} · {product.name}</option>)}
            </select>
            <Input required aria-label={`Cantidad ordenada ${index + 1}`} type="number" min="1" step="1" value={line.quantity} onChange={event => setDraftLines(current => current.map((item, i) => i === index ? { ...item, quantity: Number(event.target.value) } : item))}/>
            <Input required aria-label={`Costo unitario ${index + 1}`} type="number" min="0" step="0.01" value={line.unitCost} onChange={event => setDraftLines(current => current.map((item, i) => i === index ? { ...item, unitCost: Number(event.target.value) } : item))}/>
            <Button type="button" variant="outline" disabled={draftLines.length === 1} onClick={() => setDraftLines(current => current.filter((_, i) => i !== index))}>Quitar</Button>
          </div>)}
          <div className="flex flex-wrap gap-2">
            <Button type="button" variant="outline" onClick={() => setDraftLines(current => [...current, { productId: '', quantity: 1, unitCost: 0 }])}>Agregar producto</Button>
            <Button disabled={busy || loading || !form.supplierId || draftLines.some(line => !line.productId || !Number.isSafeInteger(line.quantity) || line.quantity < 1 || !Number.isFinite(line.unitCost) || line.unitCost < 0)}>{busy ? 'Guardando…' : 'Crear orden de compra'}</Button>
          </div>
          <p className="text-right text-sm font-medium">Total estimado: {formatMoney(draftTotal)}</p>
        </form>
      </CardContent></Card>

      <Card><CardHeader><CardTitle>Recibir refacciones</CardTitle></CardHeader><CardContent>
        <form className="space-y-3" onSubmit={receive}>
          <select required aria-label="Orden de compra pendiente" className={field} value={purchaseId} onChange={event => selectPurchase(event.target.value)}>
            <option value="">Compra pendiente</option>{purchases.filter(order => order.status !== 'CANCELLED' && order.lines?.some(line => line.quantityReceived < line.quantityOrdered)).map(order => <option key={order.id} value={order.id}>{supplierName(order.supplierId)} · {branchName(order.branchId)} · {order.id.slice(0, 8)}</option>)}
          </select>
          {receiptLines.map((line, index) => <div className="grid gap-2 sm:grid-cols-[1fr_8rem]" key={line.productId}>
            <div className="flex items-center rounded border px-3 text-sm">{products.find(product => product.id === line.productId)?.name ?? line.productId} · pendientes {remaining(line.productId)}</div>
            <Input required aria-label={`Cantidad recibida ${index + 1}`} type="number" min="1" max={remaining(line.productId)} step="1" value={line.quantity} onChange={event => setReceiptLines(current => current.map((item, i) => i === index ? { ...item, quantity: Number(event.target.value) } : item))}/>
          </div>)}
          {purchaseId && !receiptLines.length && <p className="text-sm text-muted-foreground">La orden no tiene cantidades pendientes.</p>}
          <Button disabled={busy || loading || !purchaseId || !receiptLines.length || receiptLines.some(line => line.quantity < 1 || line.quantity > remaining(line.productId))}>{busy ? 'Procesando…' : 'Registrar recepción'}</Button>
        </form>
      </CardContent></Card>

      <Card className="lg:col-span-2"><CardHeader><CardTitle>Historial de órdenes de compra ({purchases.length})</CardTitle></CardHeader><CardContent className="space-y-2">
        {loading && <p role="status" className="text-sm text-muted-foreground">Cargando compras…</p>}
        {purchases.map(order => <div className="flex flex-wrap items-center justify-between gap-2 rounded border p-3" key={order.id}>
          <div><p className="font-medium">{supplierName(order.supplierId)} · {branchName(order.branchId)}</p>
            <p className="text-xs text-muted-foreground">{order.id}{order.serviceOrderId ? ` · Servicio ${order.serviceOrderId}` : ''}{order.createdAt ? ` · ${new Date(order.createdAt).toLocaleDateString('es-MX')}` : ''}</p>
            <div className="mt-2 space-y-1 text-sm">{order.lines?.map(line => <p key={line.productId}>{products.find(product => product.id === line.productId)?.name ?? line.productId}: {line.quantityReceived}/{line.quantityOrdered} recibidas · {new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(line.unitCost)} c/u</p>)}{order.lines?.length ? <p className="border-t pt-1 font-medium">Recibido {new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(purchaseAmounts(order).received)} · Pendiente {new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(purchaseAmounts(order).ordered - purchaseAmounts(order).received)} · Total {new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(purchaseAmounts(order).ordered)}</p> : <p className="text-amber-700">Orden heredada sin partidas detalladas.</p>}</div>
          </div><div className="flex items-center gap-2"><Badge variant={order.status === 'RECEIVED' ? 'default' : 'outline'}>{order.status}</Badge>
            {!['RECEIVED', 'CANCELLED'].includes(order.status) && <Button type="button" variant="outline" disabled={busy || loading || order.lines?.some(line => line.quantityReceived > 0)} title={order.lines?.some(line => line.quantityReceived > 0) ? 'No se puede cancelar una orden con recepción parcial' : 'Cancelar orden'} onClick={() => void cancel(order)}>Cancelar</Button>}
          </div>
        </div>)}
        {!loading && !error && !purchases.length && <p className="text-sm text-muted-foreground">No hay órdenes de compra registradas.</p>}
        {error && <div className="flex flex-wrap items-center justify-between gap-2"><p role="alert" className="text-sm text-destructive">{error}</p><Button type="button" variant="outline" onClick={() => void load()}>Reintentar</Button></div>}
        {message && <p role="status" className="text-sm text-emerald-600">{message}</p>}
      </CardContent></Card>
    </div>
  </BaseLayout>
}
