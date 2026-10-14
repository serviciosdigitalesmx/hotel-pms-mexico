import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

export function ServiceCatalogManagementPage() {
  const [services, setServices] = useState<api.ServiceCatalogEntry[]>([])
  const [name, setName] = useState('')
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
      if (editing) await api.updateService(editing, { name: name.trim() })
      else await api.createService({ name: name.trim() })
      setName(''); setEditing(''); setMessage(editing ? 'Servicio actualizado' : 'Servicio agregado'); await reload()
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
    <Card><CardHeader><CardTitle>{editing ? 'Editar servicio' : 'Agregar servicio'}</CardTitle></CardHeader><CardContent><form className="flex flex-col gap-2 sm:flex-row" onSubmit={save}><Input required maxLength={200} placeholder="Nombre del servicio" value={name} onChange={event => setName(event.target.value)}/><Button disabled={busy}>{busy ? 'Guardando…' : editing ? 'Guardar cambios' : 'Agregar servicio'}</Button>{editing && <Button type="button" variant="outline" disabled={busy} onClick={() => { setEditing(''); setName('') }}>Cancelar</Button>}</form></CardContent></Card>
    <Card><CardHeader><CardTitle>Catálogo ({filtered.length})</CardTitle><Input aria-label="Buscar servicios" placeholder="Buscar servicio" value={search} onChange={event => setSearch(event.target.value)}/></CardHeader><CardContent className="space-y-2">{loading && <p role="status">Cargando servicios…</p>}{filtered.map(service => <div key={service.id} className="flex flex-wrap items-center justify-between gap-2 rounded border p-3"><span>{service.name} · <span className="text-sm text-muted-foreground">{service.active ? 'Activo' : 'Inactivo'}</span></span><div className="flex gap-2"><Button type="button" size="sm" variant="outline" disabled={busy} onClick={() => { setEditing(service.id); setName(service.name) }}>Editar</Button><Button type="button" size="sm" variant="outline" disabled={busy} onClick={() => void toggle(service)}>{service.active ? 'Desactivar' : 'Activar'}</Button></div></div>)}{!loading && !error && !filtered.length && <p className="text-sm text-muted-foreground">No hay servicios que coincidan.</p>}</CardContent></Card>
    {message && <p role="status" className="text-sm text-emerald-700">{message}</p>}{error && <div className="flex items-center justify-between"><p role="alert" className="text-sm text-destructive">{error}</p><Button type="button" variant="outline" onClick={() => void reload()}>Reintentar</Button></div>}
  </div></BaseLayout>
}
