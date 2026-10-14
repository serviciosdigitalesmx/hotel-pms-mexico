import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import * as api from './api'

const field = 'w-full rounded-md border bg-background px-3 py-2 text-sm'
const blankProduct = { sku: '', name: '', description: '', category: 'General', cost: '0', price: '0', reorderLevel: '0', optional: false, supplierId: '', initialQuantity: '0' }

export function InventoryCatalogPage() {
  const products = useQuery(api.listProducts)
  const branches = useQuery(api.listBranches)
  const suppliers = useQuery(api.listSuppliers)
  const [branch, setBranch] = useState('')
  const stock = useQuery(() => branch ? api.stock(branch) : Promise.resolve([]), [branch])
  const [search, setSearch] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('ALL')
  const [stockFilter, setStockFilter] = useState('ALL')
  const [busy, setBusy] = useState(false)
  const [editing, setEditing] = useState('')
  const [selected, setSelected] = useState<string[]>([])
  const [percentage, setPercentage] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [movement, setMovement] = useState({ productId: '', delta: 1, reason: '' })
  const [form, setForm] = useState(blankProduct)
  const quantity = (id: string) => stock.data?.find(item => item.productId === id)?.quantity ?? 0
  const categories = [...new Set((products.data ?? []).map(product => product.category || 'General'))].sort((a,b)=>a.localeCompare(b,'es'))
  const rows = (products.data ?? []).filter(p => {
    const qty=quantity(p.id);const low=Boolean(branch)&&qty<=Number(p.reorderLevel??0)
    return `${p.sku} ${p.name} ${p.description??''} ${p.category ?? ''}`.toLowerCase().includes(search.trim().toLowerCase())&&(categoryFilter==='ALL'||(p.category||'General')===categoryFilter)&&(stockFilter==='ALL'||(stockFilter==='LOW'?low:stockFilter==='IN_STOCK'?qty>0:qty===0))
  })

  async function saveProduct(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setNotice('')
    try {
      const body = { ...form, cost: Number(form.cost), price: Number(form.price), reorderLevel: Number(form.reorderLevel), initialQuantity: editing ? 0 : Number(form.initialQuantity), branchId: !editing && Number(form.initialQuantity) > 0 ? branch : null, supplierId: form.supplierId || null }
      if (editing) await api.updateProduct(editing, body)
      else await api.createProduct(body)
      setForm(blankProduct); setNotice(editing ? 'Producto actualizado' : 'Producto creado y existencia inicial registrada'); setEditing('')
      await products.reload(); await stock.reload()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo guardar el producto') }
    finally { setBusy(false) }
  }

  function editProduct(product: api.Product) {
    setEditing(product.id)
    setForm({ sku: product.sku, name: product.name, description: product.description ?? '', category: product.category ?? 'General', cost: String(product.cost ?? 0), price: String(product.price ?? 0), reorderLevel: String(product.reorderLevel ?? 0), optional: product.optional, supplierId: product.supplierId ?? '', initialQuantity: '0' })
  }

  async function move(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setNotice('')
    try { await api.moveStock({ branchId: branch, ...movement }); setMovement(value => ({ ...value, reason: '' })); setNotice('Movimiento de inventario guardado'); await stock.reload() }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo registrar el movimiento') }
    finally { setBusy(false) }
  }

  function exportCsv() {
    const csv = [['sku', 'name', 'description', 'category', 'cost', 'price', 'reorderLevel', 'optional', 'supplierId', 'quantity', 'branchId'], ...rows.map(p => [p.sku, p.name, p.description ?? '', p.category ?? 'General', p.cost ?? 0, p.price ?? 0, p.reorderLevel ?? 0, p.optional, p.supplierId ?? '', branch ? quantity(p.id) : '', branch])].map(row => row.map(api.csvCell).join(',')).join('\r\n')
    const url = URL.createObjectURL(new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })); const link = document.createElement('a'); link.href = url; link.download = branch ? 'fixi-productos-inventario.csv' : 'fixi-productos.csv'; link.click(); URL.revokeObjectURL(url)
  }

  async function increaseSelectedPrices(event: FormEvent) {
    event.preventDefault()
    if (!selected.length) return
    setBusy(true); setError(''); setNotice('')
    try {
      await api.increaseProductPrices(selected, Number(percentage))
      setNotice(`Precio actualizado para ${selected.length} productos.`); setSelected([]); setPercentage(''); await products.reload()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron actualizar los precios') }
    finally { setBusy(false) }
  }

  return <BaseLayout title="Inventario y productos" description="Catálogo comercial, entradas iniciales, movimientos y alertas por sucursal.">
    <div className="space-y-5 px-4 lg:px-6">
      <Card><CardHeader><CardTitle>{editing ? 'Editar producto' : 'Alta de producto'}</CardTitle><CardDescription>{editing ? 'Actualiza datos comerciales sin modificar existencias.' : 'Asocia proveedor y registra existencias iniciales en una sucursal.'}</CardDescription></CardHeader>
        <CardContent><form onSubmit={saveProduct} className="grid gap-3 md:grid-cols-3">
          <Input required maxLength={80} placeholder="SKU" value={form.sku} onChange={e => setForm({ ...form, sku: e.target.value })}/><Input required maxLength={200} placeholder="Producto" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })}/><Input maxLength={2000} placeholder="Descripción" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })}/><Input required maxLength={120} placeholder="Categoría" value={form.category} onChange={e => setForm({ ...form, category: e.target.value })}/>
          <Input required type="number" min="0" step="0.01" placeholder="Costo" value={form.cost} onChange={e => setForm({ ...form, cost: e.target.value })}/><Input required type="number" min="0" step="0.01" placeholder="Precio de venta" value={form.price} onChange={e => setForm({ ...form, price: e.target.value })}/><Input required type="number" min="0" step="1" placeholder="Mínimo de stock" value={form.reorderLevel} onChange={e => setForm({ ...form, reorderLevel: e.target.value })}/>
          <select aria-label="Proveedor" className={field} value={form.supplierId} onChange={e => setForm({ ...form, supplierId: e.target.value })}><option value="">Sin proveedor</option>{(suppliers.data ?? []).map(s => <option key={s.id} value={s.id}>{s.name}</option>)}</select>
          {!editing && <><select aria-label="Sucursal para stock inicial" className={field} value={branch} onChange={e => setBranch(e.target.value)}><option value="">Sucursal (necesaria para stock inicial)</option>{(branches.data ?? []).map(b => <option key={b.id} value={b.id}>{b.name}</option>)}</select><Input required type="number" min="0" step="1" placeholder="Existencia inicial" value={form.initialQuantity} onChange={e => setForm({ ...form, initialQuantity: e.target.value })}/></>}
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.optional} onChange={e => setForm({ ...form, optional: e.target.checked })}/> Refacción opcional</label>
          <div className="flex gap-2"><Button disabled={busy || (!editing && Number(form.initialQuantity) > 0 && !branch)}>{busy ? 'Guardando…' : editing ? 'Guardar cambios' : 'Crear producto'}</Button>{editing && <Button type="button" variant="outline" disabled={busy} onClick={() => { setEditing(''); setForm(blankProduct) }}>Cancelar</Button>}</div>
        </form></CardContent>
      </Card>
      <CatalogCsvPanel onImported={() => void products.reload()}/>
      <Card><CardHeader><CardTitle>Aumento masivo de precio</CardTitle><CardDescription>Selecciona productos del catálogo. El porcentaje se aplica al precio de venta, conserva dos decimales y no cambia costo ni existencias.</CardDescription></CardHeader><CardContent><form className="flex flex-wrap items-center gap-3" onSubmit={increaseSelectedPrices}><Input aria-label="Porcentaje de aumento" type="number" min="0" max="1000" step="0.01" required placeholder="Porcentaje (p. ej. 10)" value={percentage} onChange={e => setPercentage(e.target.value)}/><span className="text-sm text-muted-foreground">{selected.length} seleccionados</span><Button disabled={busy || !selected.length}>{busy ? 'Actualizando…' : 'Aplicar aumento'}</Button><Button type="button" variant="outline" disabled={!selected.length} onClick={() => setSelected([])}>Limpiar selección</Button></form></CardContent></Card>
      <Card><CardHeader><CardTitle>Ajustar existencia</CardTitle><CardDescription>Movimiento positivo para entrada, negativo para salida; no puede dejar stock negativo. El motivo queda en auditoría.</CardDescription></CardHeader><CardContent><form onSubmit={move} className="grid gap-2 md:grid-cols-3"><select required aria-label="Sucursal del movimiento" className={field} value={branch} onChange={e => setBranch(e.target.value)}><option value="">Sucursal</option>{(branches.data ?? []).map(b => <option key={b.id} value={b.id}>{b.name}</option>)}</select><select required aria-label="Producto del movimiento" className={field} value={movement.productId} onChange={e => setMovement({ ...movement, productId: e.target.value })}><option value="">Producto</option>{(products.data ?? []).map(p => <option key={p.id} value={p.id}>{p.sku} · {p.name}</option>)}</select><Input type="number" required min="-1000000" value={movement.delta} onChange={e => setMovement({ ...movement, delta: Number(e.target.value) })}/><Input required maxLength={500} aria-label="Motivo del movimiento" placeholder="Motivo / referencia" value={movement.reason} onChange={e => setMovement({ ...movement, reason: e.target.value })}/><Button disabled={busy || !branch || !movement.reason.trim()}>{busy ? 'Guardando…' : 'Registrar movimiento'}</Button></form></CardContent></Card>
      <Card><CardHeader><div className="flex flex-wrap items-center justify-between gap-3"><CardTitle>Catálogo ({rows.length})</CardTitle><Button type="button" variant="outline" onClick={exportCsv}>Exportar CSV</Button></div><div className="grid gap-2 sm:grid-cols-3"><Input aria-label="Buscar productos" placeholder="Buscar SKU, nombre o categoría" value={search} onChange={e => setSearch(e.target.value)}/><select aria-label="Filtrar por categoría" className={field} value={categoryFilter} onChange={e=>setCategoryFilter(e.target.value)}><option value="ALL">Todas las categorías</option>{categories.map(category=><option key={category}>{category}</option>)}</select><select aria-label="Filtrar por existencias" className={field} value={stockFilter} onChange={e=>setStockFilter(e.target.value)}><option value="ALL">Todas las existencias</option><option value="IN_STOCK" disabled={!branch}>Con existencias</option><option value="OUT_OF_STOCK" disabled={!branch}>Agotados</option><option value="LOW" disabled={!branch}>En mínimo o debajo</option></select></div>{!branch&&stockFilter!=='ALL'&&<p className="text-xs text-muted-foreground">Selecciona una sucursal para filtrar por existencias.</p>}</CardHeader><CardContent className="space-y-3">{products.loading && <p role="status">Cargando productos…</p>}{rows.map(p => { const qty = quantity(p.id); const low = Boolean(branch) && qty <= Number(p.reorderLevel ?? 0); return <div key={p.id} className="flex flex-wrap items-center justify-between gap-3 rounded border p-3"><label className="flex items-center gap-3"><input aria-label={`Seleccionar ${p.name}`} type="checkbox" checked={selected.includes(p.id)} onChange={e => setSelected(current => e.target.checked ? [...new Set([...current, p.id])] : current.filter(id => id !== p.id))}/><span><span className="block font-medium">{p.name} · {p.sku}</span><span className="text-sm text-muted-foreground">{p.category ?? 'General'} · costo ${Number(p.cost ?? 0).toFixed(2)} · venta ${Number(p.price ?? 0).toFixed(2)}</span>{p.description&&<span className="block text-xs text-muted-foreground">{p.description}</span>}</span></label><div className="flex items-center gap-2"><span>{branch ? `${qty} unidades` : 'Elige sucursal'}</span>{low && <Badge variant="destructive">Reponer · mínimo {p.reorderLevel}</Badge>}<Button type="button" size="sm" variant="outline" onClick={() => editProduct(p)}>Editar</Button></div></div> })}{!products.loading && !products.error && !rows.length && <p className="text-sm text-muted-foreground">No hay productos que coincidan.</p>}{products.error && <Button type="button" variant="outline" onClick={() => void products.reload()}>Reintentar</Button>}</CardContent></Card>
      {notice && <p role="status" className="text-sm text-emerald-700">{notice}</p>}{error && <p role="alert" className="text-sm text-destructive">{error}</p>}{branches.error && <p role="alert" className="text-sm text-destructive">{branches.error}</p>}{stock.error && <p role="alert" className="text-sm text-destructive">{stock.error}</p>}
    </div>
  </BaseLayout>
}

function CatalogCsvPanel({ onImported }: { onImported: () => void }) {
  const [csv, setCsv] = useState('')
  const [fileName, setFileName] = useState('')
  const [branches, setBranches] = useState<api.Branch[]>([])
  const [branchId, setBranchId] = useState('')
  const [busy, setBusy] = useState(false)
  const [result, setResult] = useState('')
  const [error, setError] = useState('')
  useEffect(() => { let active = true; api.listBranches().then(rows => { if (active) setBranches(rows.filter(branch => branch.active)) }).catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar las sucursales') }); return () => { active = false } }, [])
  function downloadTemplate() {
    const content = 'sku,name,description,category,cost,price,reorderLevel,optional,supplierId,quantity,branchId\r\nREP-001,"Pantalla, modelo A",Pantalla compatible con modelo A,Pantallas,1200.00,1899.00,2,false,,,,\r\n'
    const url = URL.createObjectURL(new Blob([content], { type: 'text/csv;charset=utf-8' })); const link = document.createElement('a'); link.href = url; link.download = 'fixi-productos-plantilla.csv'; link.click(); URL.revokeObjectURL(url)
  }
  async function submit(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setResult('')
    try {
      const summary = await api.importProductsCsv(csv.replace(/^\uFEFF/, ''), branchId || undefined)
      setResult(`${summary.imported} importados, ${summary.rejected} rechazados${summary.errors.length ? `: ${summary.errors.slice(0, 5).join(' · ')}` : ''}`)
      if (summary.imported) onImported()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo importar el CSV') }
    finally { setBusy(false) }
  }
  async function readFile(file?: File) {
    setError(''); setResult('')
    if (!file) { setFileName(''); return }
    if (file.size > 5 * 1024 * 1024) { setError('El archivo supera el límite de 5 MB'); return }
    try { setCsv((await file.text()).replace(/^\uFEFF/, '')); setFileName(file.name) }
    catch { setError('No se pudo leer el archivo CSV') }
  }
  return <Card><CardHeader><CardTitle>Importar catálogo CSV</CardTitle><CardDescription>Admite plantillas Fixi y SAMII. SKU se genera si falta. Si Quantity es mayor que cero, elige la sucursal donde se registrará la existencia inicial.</CardDescription></CardHeader><CardContent><form className="space-y-3" onSubmit={submit}><label className="block text-sm">Seleccionar archivo CSV<Input type="file" accept=".csv,text/csv" disabled={busy} onChange={event=>void readFile(event.currentTarget.files?.[0])}/></label>{fileName&&<p className="text-xs text-muted-foreground">Archivo seleccionado: {fileName}</p>}<select aria-label="Sucursal para existencias iniciales" className={field} value={branchId} onChange={event=>setBranchId(event.target.value)}><option value="">Sucursal de importación (opcional si no hay cantidad inicial)</option>{branches.map(branch=><option key={branch.id} value={branch.id}>{branch.name}</option>)}</select><textarea required aria-label="Contenido CSV" className={field} rows={5} placeholder="category,name,description,cost,price,quantity" value={csv} onChange={event => {setCsv(event.target.value);setFileName('')}}/><div className="flex gap-2"><Button disabled={busy||!csv.trim()}>{busy ? 'Importando…' : 'Importar CSV'}</Button><Button type="button" variant="outline" onClick={downloadTemplate}>Descargar plantilla</Button></div></form>{result && <p role="status" className="mt-3 text-sm text-emerald-700">{result}</p>}{error && <p role="alert" className="mt-3 text-sm text-destructive">{error}</p>}</CardContent></Card>
}

function useQuery<T>(loader: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T>(); const [error, setError] = useState(''); const [loading, setLoading] = useState(true)
  const reload = async () => { setLoading(true); setError(''); try { setData(await loader()) } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los datos') } finally { setLoading(false) } }
  useEffect(() => { void reload() }, deps)
  return { data, error, loading, reload }
}
