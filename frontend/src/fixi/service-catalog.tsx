import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

export function ServiceCatalogManagementPage() {
  const [services, setServices] = useState<api.ServiceCatalogEntry[]>([])
  const [name, setName] = useState('')
  const [basePrice, setBasePrice] = useState('0')
  const [estimatedMinutes, setEstimatedMinutes] = useState('60')
  const [search, setSearch] = useState('')
  const [editing, setEditing] = useState('')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function reload() {
    setLoading(true); setError('')
    try { setServices(await api.listServices()) }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo cargar el catálogo') }
    finally { setLoading(false) }
  }
  useEffect(() => { void reload() }, [])

  async function save(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setMessage('')
    try {
      const wasEditing = Boolean(editing)
      const payload = { name: name.trim(), basePrice: Number(basePrice), estimatedMinutes: Number(estimatedMinutes) }
      if (editing) await api.updateService(editing, payload)
      else await api.createService(payload)
      setName(''); setBasePrice('0'); setEstimatedMinutes('60'); setEditing(''); setMessage(wasEditing ? 'Servicio actualizado' : 'Servicio agregado'); await reload()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo guardar el servicio') }
    finally { setBusy(false) }
  }
  async function toggle(service: api.ServiceCatalogEntry) {
    setBusy(true); setError('')
    try { await api.setServiceActive(service.id, !service.active); await reload() }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo cambiar el estado') }
    finally { setBusy(false) }
  }

  const filtered = services.filter(service => service.name.toLocaleLowerCase().includes(search.trim().toLocaleLowerCase()))
  return <BaseLayout title="Servicios" description="Catálogo de trabajos cotizables, reutilizable en órdenes de reparación."><div className="space-y-5 px-4 lg:px-6">
    <Card><CardHeader><CardTitle>{editing ? 'Editar servicio' : 'Agregar servicio'}</CardTitle></CardHeader><CardContent><form className="grid gap-2 sm:grid-cols-2 lg:grid-cols-4" onSubmit={save}><Input required maxLength={200} placeholder="Nombre del servicio" value={name} onChange={event => setName(event.target.value)}/><Input required aria-label="Precio base" type="number" min="0" step="0.01" placeholder="Precio base MXN" value={basePrice} onChange={event => setBasePrice(event.target.value)}/><Input required aria-label="Duración estimada en minutos" type="number" min="1" step="1" placeholder="Duración en minutos" value={estimatedMinutes} onChange={event => setEstimatedMinutes(event.target.value)}/><div className="flex gap-2"><Button disabled={busy || !name.trim() || !Number.isFinite(Number(basePrice)) || Number(basePrice) < 0 || !Number.isSafeInteger(Number(estimatedMinutes)) || Number(estimatedMinutes) < 1}>{busy ? 'Guardando…' : editing ? 'Guardar cambios' : 'Agregar servicio'}</Button>{editing && <Button type="button" variant="outline" disabled={busy} onClick={() => { setEditing(''); setName(''); setBasePrice('0'); setEstimatedMinutes('60') }}>Cancelar</Button>}</div></form></CardContent></Card>
    <Card><CardHeader><CardTitle>Catálogo ({filtered.length})</CardTitle><Input aria-label="Buscar servicios" placeholder="Buscar servicio" value={search} onChange={event => setSearch(event.target.value)}/></CardHeader><CardContent className="space-y-2">{loading && <p role="status">Cargando servicios…</p>}{filtered.map(service => <div key={service.id} className="flex flex-wrap items-center justify-between gap-2 rounded border p-3"><div><p>{service.name} · <span className="text-sm text-muted-foreground">{service.active ? 'Activo' : 'Inactivo'}</span></p><p className="text-sm text-muted-foreground">Base {new Intl.NumberFormat('es-MX',{style:'currency',currency:'MXN'}).format(service.basePrice)} · {service.estimatedMinutes} min</p></div><div className="flex gap-2"><Button type="button" size="sm" variant="outline" disabled={busy} onClick={() => { setEditing(service.id); setName(service.name); setBasePrice(String(service.basePrice)); setEstimatedMinutes(String(service.estimatedMinutes)) }}>Editar</Button><Button type="button" size="sm" variant="outline" disabled={busy} onClick={() => void toggle(service)}>{service.active ? 'Desactivar' : 'Activar'}</Button></div></div>)}{!loading && !error && !filtered.length && <p className="text-sm text-muted-foreground">No hay servicios que coincidan.</p>}</CardContent></Card>
    {message && <p role="status" className="text-sm text-emerald-700">{message}</p>}{error && <div className="flex items-center justify-between"><p role="alert" className="text-sm text-destructive">{error}</p><Button type="button" variant="outline" onClick={() => void reload()}>Reintentar</Button></div>}
  </div></BaseLayout>
}
