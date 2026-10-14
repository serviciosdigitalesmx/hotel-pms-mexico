import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import * as api from './api'

type IndustryProfile = Awaited<ReturnType<typeof api.industryProfile>>

function parseTemplates(json: string): Record<string, unknown> {
  const value: unknown = JSON.parse(json || '{}')
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    throw new Error('Las plantillas deben ser un objeto JSON')
  }
  return value as Record<string, unknown>
}

function workshopTemplates(templates: Record<string, unknown>) {
  const workshop = templates.workshop
  return workshop && typeof workshop === 'object' && !Array.isArray(workshop)
    ? workshop as Record<string, unknown>
    : {}
}

function checklistFrom(json: string) {
  try {
    const items = workshopTemplates(parseTemplates(json)).intakeChecklist
    return Array.isArray(items) ? items.filter((item): item is string => typeof item === 'string') : []
  } catch { return [] }
}

function warrantyPolicyFrom(json: string) {
  try {
    const value = workshopTemplates(parseTemplates(json)).warrantyPolicy
    return typeof value === 'string' ? value : ''
  } catch { return '' }
}

function updateWorkshopTemplates(json: string, checklist: string[], warrantyPolicy: string) {
  const templates = parseTemplates(json)
  return JSON.stringify({
    ...templates,
    workshop: {
      ...workshopTemplates(templates),
      intakeChecklist: checklist,
      warrantyPolicy: warrantyPolicy.trim(),
    },
  }, null, 2)
}

const emptyBusiness: api.BusinessProfile = {
  hotelName: '', address: '', city: '', state: '', country: 'México', postalCode: '',
  currency: 'MXN', locale: 'es-MX', timezone: 'America/Mexico_City', publicSlug: '', vatNumber: '', defaultTaxRate: null,
}

export function BusinessSettings() {
  const [profile, setProfile] = useState<IndustryProfile>({
    industryKey: 'ELECTRONICS_REPAIR', enabledModulesJson: '[]', dynamicFieldsJson: '{}',
    formsLabelsJson: '{}', catalogsTemplatesJson: '{}',
  })
  const [checklistText, setChecklistText] = useState('')
  const [warrantyPolicy, setWarrantyPolicy] = useState('')
  const [business, setBusiness] = useState<api.BusinessProfile>(emptyBusiness)
  const [password, setPassword] = useState({ currentPassword: '', newPassword: '' })
  const [idleTimeoutMinutes, setIdleTimeoutMinutes] = useState<0 | 15 | 60>(0)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let active = true
    Promise.all([api.industryProfile(), api.getBusinessProfile(), api.getSessionSecurityPreference().catch(() => null)])
      .then(([industry, settings, sessionPreference]) => {
        if (!active) return
        setProfile(industry)
        setChecklistText(checklistFrom(industry.catalogsTemplatesJson).join('\n'))
        setWarrantyPolicy(warrantyPolicyFrom(industry.catalogsTemplatesJson))
        setBusiness(current => ({ ...current, ...settings }))
        if (sessionPreference) setIdleTimeoutMinutes(sessionPreference.idleTimeoutMinutes)
      })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudo cargar la configuración') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  async function saveBusiness(event: FormEvent) {
    event.preventDefault(); setSaving(true); setError(''); setMessage('')
    try {
      await api.updateBusinessProfile(business)
      setMessage('Datos del negocio guardados')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo guardar el negocio')
    } finally { setSaving(false) }
  }

  async function saveIndustry(event: FormEvent) {
    event.preventDefault(); setSaving(true); setError(''); setMessage('')
    try {
      const catalogsTemplatesJson = updateWorkshopTemplates(
        profile.catalogsTemplatesJson,
        checklistText.split('\n').map(item => item.trim()).filter(Boolean),
        warrantyPolicy,
      )
      const updated = { ...profile, catalogsTemplatesJson }
      await api.updateIndustryProfile(updated)
      setProfile(updated)
      setMessage('Preferencias, checklist y política de garantía guardados')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudieron guardar las preferencias')
    } finally { setSaving(false) }
  }

  async function changePassword(event: FormEvent) {
    event.preventDefault(); setSaving(true); setError(''); setMessage('')
    try {
      await api.changePassword(password.currentPassword, password.newPassword)
      setPassword({ currentPassword: '', newPassword: '' })
      setMessage('Contraseña actualizada; las sesiones anteriores fueron invalidadas')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo actualizar la contraseña')
    } finally { setSaving(false) }
  }

  async function saveSessionSecurity() {
    setSaving(true); setError(''); setMessage('')
    try {
      const saved = await api.updateSessionSecurityPreference(idleTimeoutMinutes)
      setIdleTimeoutMinutes(saved.idleTimeoutMinutes)
      window.dispatchEvent(new Event('fixi:session-security-changed'))
      setMessage(saved.idleTimeoutMinutes === 0 ? 'Bloqueo automático desactivado' : `Bloqueo automático después de ${saved.idleTimeoutMinutes} minutos sin actividad`)
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo guardar la duración de sesión') }
    finally { setSaving(false) }
  }

  return <BaseLayout title="Configuración" description="Perfil del negocio, políticas operativas y seguridad.">
    <div className="grid gap-5 px-4 lg:grid-cols-2 lg:px-6">
      {loading && <p role="status" className="text-sm text-muted-foreground">Cargando configuración…</p>}
      <Card className="lg:col-span-2"><CardHeader><CardTitle>Datos generales del negocio</CardTitle></CardHeader>
        <CardContent><form className="grid gap-3 md:grid-cols-3" onSubmit={saveBusiness}>
          <Input required aria-label="Nombre del negocio" placeholder="Nombre del negocio" value={business.hotelName} onChange={e => setBusiness({ ...business, hotelName: e.target.value })}/>
          <Input aria-label="Dirección" placeholder="Dirección" value={business.address} onChange={e => setBusiness({ ...business, address: e.target.value })}/>
          <Input aria-label="Ciudad" placeholder="Ciudad" value={business.city} onChange={e => setBusiness({ ...business, city: e.target.value })}/>
          <Input aria-label="Estado" placeholder="Estado" value={business.state} onChange={e => setBusiness({ ...business, state: e.target.value })}/>
          <Input aria-label="País" placeholder="País" value={business.country} onChange={e => setBusiness({ ...business, country: e.target.value })}/>
          <Input aria-label="Código postal" inputMode="numeric" placeholder="Código postal" value={business.postalCode} onChange={e => setBusiness({ ...business, postalCode: e.target.value })}/>
          <Input aria-label="RFC" placeholder="RFC" value={business.vatNumber} onChange={e => setBusiness({ ...business, vatNumber: e.target.value.toUpperCase() })}/>
          <div><Label htmlFor="default-tax-rate">Impuesto predeterminado (%)</Label><Input id="default-tax-rate" type="number" min="0" max="100" step="0.01" placeholder="Sin tasa predeterminada" value={business.defaultTaxRate ?? ''} onChange={e => setBusiness({ ...business, defaultTaxRate: e.target.value === '' ? null : Number(e.target.value) })}/><p className="text-xs text-muted-foreground">Se aplica a mano de obra y refacciones al generar cotizaciones.</p></div>
          <Input aria-label="Moneda" maxLength={3} placeholder="Moneda (MXN)" value={business.currency} onChange={e => setBusiness({ ...business, currency: e.target.value.toUpperCase() })}/>
          <Input aria-label="Zona horaria" placeholder="America/Mexico_City" value={business.timezone} onChange={e => setBusiness({ ...business, timezone: e.target.value })}/>
          <Input aria-label="Idioma y región" placeholder="es-MX" value={business.locale} onChange={e => setBusiness({ ...business, locale: e.target.value })}/>
          <Input aria-label="Identificador público" placeholder="Slug público" value={business.publicSlug} onChange={e => setBusiness({ ...business, publicSlug: e.target.value })}/>
          <Button disabled={saving || loading}>{saving ? 'Guardando…' : 'Guardar datos del negocio'}</Button>
        </form></CardContent>
      </Card>

      <Card><CardHeader><CardTitle>Preferencias y políticas del taller</CardTitle></CardHeader>
        <CardContent><form className="space-y-3" onSubmit={saveIndustry}>
          <Label>Industria</Label><Input value={profile.industryKey} onChange={e => setProfile({ ...profile, industryKey: e.target.value })}/>
          <Label>Módulos habilitados (JSON)</Label><Input value={profile.enabledModulesJson} onChange={e => setProfile({ ...profile, enabledModulesJson: e.target.value })}/>
          <Label>Campos dinámicos (JSON)</Label><textarea className="w-full rounded border p-2" value={profile.dynamicFieldsJson} onChange={e => setProfile({ ...profile, dynamicFieldsJson: e.target.value })}/>
          <Label>Etiquetas de formularios (JSON)</Label><textarea className="w-full rounded border p-2" value={profile.formsLabelsJson} onChange={e => setProfile({ ...profile, formsLabelsJson: e.target.value })}/>
          <Label>Plantillas de catálogos (JSON)</Label><textarea className="w-full rounded border p-2" value={profile.catalogsTemplatesJson} onChange={e => setProfile({ ...profile, catalogsTemplatesJson: e.target.value })}/>
          <Label>Checklist configurable de recepción (un elemento por línea)</Label>
          <textarea aria-label="Checklist configurable de recepción" className="w-full rounded border p-2" rows={5} placeholder={'Condición física\nAccesorios recibidos\nPrueba de encendido'} value={checklistText} onChange={e => setChecklistText(e.target.value)}/>
          <Label>Política de garantía para mostrar e imprimir en la orden</Label>
          <textarea aria-label="Política de garantía" className="w-full rounded border p-2" rows={5} maxLength={5000} placeholder="Describe cobertura, vigencia, exclusiones y condiciones de garantía." value={warrantyPolicy} onChange={e => setWarrantyPolicy(e.target.value)}/>
          <Button disabled={saving || loading}>{saving ? 'Guardando…' : 'Guardar preferencias y políticas'}</Button>
        </form></CardContent>
      </Card>

      <Card><CardHeader><CardTitle>Seguridad de la cuenta</CardTitle></CardHeader>
        <CardContent><form className="space-y-3" onSubmit={changePassword}>
          <Input type="password" autoComplete="current-password" required minLength={6} placeholder="Contraseña actual" value={password.currentPassword} onChange={e => setPassword({ ...password, currentPassword: e.target.value })}/>
          <Input type="password" autoComplete="new-password" required minLength={6} placeholder="Nueva contraseña" value={password.newPassword} onChange={e => setPassword({ ...password, newPassword: e.target.value })}/>
          <Button disabled={saving}>{saving ? 'Guardando…' : 'Actualizar contraseña'}</Button>
        </form><div className="mt-5 space-y-3 border-t pt-4"><Label htmlFor="idle-timeout">Bloqueo por inactividad</Label><select id="idle-timeout" aria-label="Duración de la sesión" className="h-10 w-full rounded-md border bg-background px-3 text-sm" disabled={loading || saving} value={idleTimeoutMinutes} onChange={event => setIdleTimeoutMinutes(Number(event.target.value) as 0 | 15 | 60)}><option value={0}>Nunca bloquear (recomendado)</option><option value={15}>Después de 15 minutos</option><option value={60}>Después de 1 hora</option></select><Button type="button" variant="outline" disabled={loading || saving} onClick={() => void saveSessionSecurity()}>{saving ? 'Guardando…' : 'Guardar seguridad de sesión'}</Button><p className="text-xs text-muted-foreground">Al bloquear, la pantalla requerirá volver a autenticar tu cuenta para continuar.</p></div></CardContent>
      </Card>
      {message && <p role="status" className="text-emerald-600">{message}</p>}
      {error && <p role="alert" className="text-destructive">{error}</p>}
    </div>
  </BaseLayout>
}
