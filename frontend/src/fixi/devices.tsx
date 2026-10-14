import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const field = 'w-full rounded-md border bg-background px-3 py-2 text-sm'
const blank = { category: 'Teléfono', manufacturer: '', model: '', serialNumber: '', imei: '', customFields: '' }

export function DeviceManagementPage() {
  const [customers, setCustomers] = useState<api.Customer[]>([])
  const [customerId, setCustomerId] = useState('')
  const [devices, setDevices] = useState<api.Device[]>([])
  const [form, setForm] = useState(blank)
  const [editing, setEditing] = useState('')
  const [search, setSearch] = useState('')
  const [globalResults, setGlobalResults] = useState<api.Device[]>([])
  const [globalSearch, setGlobalSearch] = useState('')
  const [globalLoading, setGlobalLoading] = useState(false)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function loadDevices(id: string) { if (id) setDevices(await api.listDevices(id)); else setDevices([]) }
  useEffect(() => { let active = true; api.listCustomers().then(result => { if (active) setCustomers(Array.isArray(result) ? result : result.content) }).catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar clientes') }).finally(() => { if (active) setLoading(false) }); return () => { active = false } }, [])
  useEffect(() => { let active = true; if (customerId) api.listDevices(customerId).then(items => { if (active) setDevices(items) }).catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar equipos') }); else setDevices([]); return () => { active = false } }, [customerId])
  useEffect(() => {
    let active = true
    const query = globalSearch.trim()
    if (query.length < 2) { setGlobalResults([]); setGlobalLoading(false); return () => { active = false } }
    setGlobalResults([]); setGlobalLoading(true)
    const timer = window.setTimeout(() => {
      api.searchDevices(query).then(result => { if (active) setGlobalResults(Array.isArray(result) ? result : result.content) })
        .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudo buscar el equipo') })
        .finally(() => { if (active) setGlobalLoading(false) })
    }, 250)
    return () => { active = false; window.clearTimeout(timer) }
  }, [globalSearch])

  async function save(event: FormEvent) {
    event.preventDefault(); if (!customerId) return; setBusy(true); setError(''); setMessage('')
    try {
      if (editing) await api.updateDevice(customerId, editing, form)
      else await api.createDevice(customerId, form)
      await loadDevices(customerId); setForm(blank); setEditing(''); setMessage(editing ? 'Equipo actualizado' : 'Equipo registrado')
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo guardar el equipo') }
    finally { setBusy(false) }
  }
  function edit(device: api.Device) { setEditing(device.id); setForm({ category: device.category, manufacturer: device.manufacturer ?? '', model: device.model ?? '', serialNumber: device.serialNumber ?? '', imei: device.imei ?? '', customFields: device.customFields ?? '' }) }

  const visible = devices.filter(device => `${device.category} ${device.manufacturer ?? ''} ${device.model ?? ''} ${device.serialNumber ?? ''} ${device.imei ?? ''}`.toLocaleLowerCase().includes(search.trim().toLocaleLowerCase()))
  return <BaseLayout title="Dispositivos" description="Equipos asociados a clientes, con identificación e historial operativo."><div className="space-y-5 px-4 lg:px-6">
    <Card><CardHeader><CardTitle>Localizar por serie o IMEI</CardTitle><CardDescription>Busca equipos de cualquier cliente de esta cuenta.</CardDescription><Input aria-label="Búsqueda global de equipos" placeholder="IMEI, número de serie, marca o modelo" value={globalSearch} onChange={event => setGlobalSearch(event.target.value)}/></CardHeader><CardContent className="space-y-2">{globalLoading && <p role="status">Buscando equipos…</p>}{globalSearch.trim().length >= 2 && !globalLoading && globalResults.map(device => <div key={device.id} className="rounded border p-3"><p className="font-medium">{device.manufacturer || 'Marca no especificada'} {device.model || 'Modelo no especificado'}</p><p className="text-sm text-muted-foreground">{device.category} · Serie {device.serialNumber || '—'} · IMEI {device.imei || '—'}</p><p className="text-xs text-muted-foreground">Cliente {device.customerName || device.customerId}</p></div>)}{globalSearch.trim().length >= 2 && !globalLoading && !globalResults.length && <p className="text-sm text-muted-foreground">Sin equipos coincidentes.</p>}</CardContent></Card>
    <select aria-label="Cliente" className={field} value={customerId} onChange={event => { setCustomerId(event.target.value); setEditing(''); setForm(blank); setError('') }}><option value="">Selecciona cliente</option>{customers.map(customer => <option key={customer.id} value={customer.id}>{customer.firstName} {customer.lastName} · {customer.phone || customer.email || ''}</option>)}</select>
    {customerId && <Card><CardHeader><CardTitle>{editing ? 'Editar equipo' : 'Registrar equipo'}</CardTitle><CardDescription>El número de serie y el IMEI no pueden duplicarse para el mismo cliente.</CardDescription></CardHeader><CardContent><form onSubmit={save} className="grid gap-3 md:grid-cols-3"><Input required maxLength={80} placeholder="Categoría" value={form.category} onChange={e => setForm({ ...form, category: e.target.value })}/><Input maxLength={120} placeholder="Marca" value={form.manufacturer} onChange={e => setForm({ ...form, manufacturer: e.target.value })}/><Input maxLength={120} placeholder="Modelo" value={form.model} onChange={e => setForm({ ...form, model: e.target.value })}/><Input maxLength={120} placeholder="Número de serie" value={form.serialNumber} onChange={e => setForm({ ...form, serialNumber: e.target.value })}/><Input maxLength={120} placeholder="IMEI" value={form.imei} onChange={e => setForm({ ...form, imei: e.target.value })}/><Input maxLength={1000} placeholder="Detalles adicionales" value={form.customFields} onChange={e => setForm({ ...form, customFields: e.target.value })}/><div className="flex gap-2"><Button disabled={busy}>{busy ? 'Guardando…' : editing ? 'Guardar cambios' : 'Registrar equipo'}</Button>{editing && <Button type="button" variant="outline" disabled={busy} onClick={() => { setEditing(''); setForm(blank) }}>Cancelar</Button>}</div></form></CardContent></Card>}
    <Card><CardHeader><CardTitle>Equipos del cliente ({visible.length})</CardTitle><Input aria-label="Buscar equipos" placeholder="Buscar categoría, marca, modelo, serie o IMEI" value={search} onChange={e => setSearch(e.target.value)}/></CardHeader><CardContent className="space-y-2">{loading && <p role="status">Cargando clientes…</p>}{customerId && visible.map(device => <div className="flex flex-wrap items-center justify-between gap-3 rounded border p-3" key={device.id}><div><p className="font-medium">{device.manufacturer || 'Marca no especificada'} {device.model || 'Modelo no especificado'}</p><p className="text-sm text-muted-foreground">{device.category} · Serie {device.serialNumber || '—'} · IMEI {device.imei || '—'}</p>{device.customFields && <p className="text-xs text-muted-foreground">{device.customFields}</p>}</div><Button type="button" variant="outline" onClick={() => edit(device)}>Editar</Button></div>)}{customerId && !loading && !visible.length && <p className="text-sm text-muted-foreground">Este cliente aún no tiene equipos coincidentes.</p>}{!customerId && !loading && <p className="text-sm text-muted-foreground">Selecciona un cliente para consultar o registrar sus dispositivos.</p>}</CardContent></Card>
    {message && <p role="status" className="text-sm text-emerald-700">{message}</p>}{error && <p role="alert" className="text-sm text-destructive">{error}</p>}
  </div></BaseLayout>
}
