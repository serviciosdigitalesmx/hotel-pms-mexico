import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const field = 'w-full rounded-md border bg-background px-3 py-2 text-sm'
type Profile = Awaited<ReturnType<typeof api.industryProfile>>
type Templates = { workshop?: { warrantyPolicy?: string; intakeChecklist?: string[] }; [key: string]: unknown }

export function OrderPreferencesPage() {
  const [profile, setProfile] = useState<Profile>()
  const [warrantyPolicy, setWarrantyPolicy] = useState('')
  const [checklist, setChecklist] = useState<string[]>([])
  const [newItem, setNewItem] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function load() {
    setLoading(true); setError('')
    try {
      const current = await api.industryProfile()
      const templates = JSON.parse(current.catalogsTemplatesJson || '{}') as Templates
      setProfile(current)
      setWarrantyPolicy(typeof templates.workshop?.warrantyPolicy === 'string' ? templates.workshop.warrantyPolicy : '')
      setChecklist(Array.isArray(templates.workshop?.intakeChecklist) ? templates.workshop.intakeChecklist.filter((item): item is string => typeof item === 'string') : [])
    } catch (reason) {
      setError(reason instanceof Error ? `No se pudieron cargar las preferencias: ${reason.message}` : 'No se pudieron cargar las preferencias')
    } finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [])

  async function save(event: FormEvent) {
    event.preventDefault()
    if (!profile) return
    setSaving(true); setError(''); setMessage('')
    try {
      const current = JSON.parse(profile.catalogsTemplatesJson || '{}') as Templates
      const templates: Templates = { ...current, workshop: { ...current.workshop, warrantyPolicy: warrantyPolicy.trim(), intakeChecklist: checklist.map(item => item.trim()).filter(Boolean) } }
      const updated = await api.updateIndustryProfile({ ...profile, catalogsTemplatesJson: JSON.stringify(templates) }) as Profile
      setProfile(updated); setMessage('Preferencias de órdenes guardadas')
    } catch (reason) {
      setError(reason instanceof Error ? `No se pudieron guardar las preferencias: ${reason.message}` : 'No se pudieron guardar las preferencias')
    } finally { setSaving(false) }
  }

  function addItem() {
    const value = newItem.trim()
    if (!value || checklist.some(item => item.toLocaleLowerCase() === value.toLocaleLowerCase())) return
    setChecklist(items => [...items, value]); setNewItem('')
  }

  return <BaseLayout title="Políticas y plantillas de órdenes" description="Configura la política que aparece en los tickets y el checklist reutilizable en recepción.">
    <div className="space-y-5 px-4 lg:px-6">
      {loading && <p role="status" className="text-sm text-muted-foreground">Cargando preferencias…</p>}
      {!loading && !profile && <div className="flex items-center gap-3"><p role="alert" className="text-sm text-destructive">{error}</p><Button type="button" variant="outline" onClick={() => void load()}>Reintentar</Button></div>}
      {profile && <form className="space-y-5" onSubmit={save}>
        <Card><CardHeader><CardTitle>Política de garantía</CardTitle><CardDescription>Este texto se muestra en la impresión de la orden para lectura y firma del cliente.</CardDescription></CardHeader><CardContent><textarea aria-label="Política de garantía" className={field} rows={8} maxLength={10000} placeholder="Describe cobertura, exclusiones, vigencia y condiciones…" value={warrantyPolicy} onChange={event => setWarrantyPolicy(event.target.value)}/><p className="mt-1 text-right text-xs text-muted-foreground">{warrantyPolicy.length}/10,000</p></CardContent></Card>
        <Card><CardHeader><CardTitle>Checklist de recepción ({checklist.length})</CardTitle><CardDescription>Los elementos se ofrecen como casillas en la captura de una nueva orden. Las notas específicas se guardan junto al checklist.</CardDescription></CardHeader><CardContent className="space-y-3">
          <div className="flex gap-2"><Input aria-label="Nuevo elemento de checklist" maxLength={160} placeholder="Estado físico, accesorios, encendido…" value={newItem} onChange={event => setNewItem(event.target.value)}/><Button type="button" variant="outline" disabled={!newItem.trim() || checklist.length >= 50} onClick={addItem}>Agregar</Button></div>
          <div className="space-y-2">{checklist.map((item, index) => <div className="flex items-center gap-2" key={`${index}-${item}`}><span className="flex-1 rounded border p-2 text-sm">{item}</span><Button type="button" size="sm" variant="outline" aria-label={`Subir ${item}`} disabled={index === 0} onClick={() => setChecklist(items => items.map((value, position) => position === index - 1 ? items[index] : position === index ? items[index - 1] : value))}>Subir</Button><Button type="button" size="sm" variant="outline" aria-label={`Bajar ${item}`} disabled={index === checklist.length - 1} onClick={() => setChecklist(items => items.map((value, position) => position === index + 1 ? items[index] : position === index ? items[index + 1] : value))}>Bajar</Button><Button type="button" size="sm" variant="outline" aria-label={`Quitar ${item}`} onClick={() => setChecklist(items => items.filter((_, position) => position !== index))}>Quitar</Button></div>)}{!checklist.length && <p className="text-sm text-muted-foreground">Sin plantilla: recepción seguirá aceptando notas manuales.</p>}</div>
        </CardContent></Card>
        <div className="flex flex-wrap items-center gap-3"><Button disabled={saving || loading}>{saving ? 'Guardando…' : 'Guardar preferencias'}</Button>{message && <p role="status" className="text-sm text-emerald-700">{message}</p>}{error && <p role="alert" className="text-sm text-destructive">{error}</p>}</div>
      </form>}
    </div>
  </BaseLayout>
}
