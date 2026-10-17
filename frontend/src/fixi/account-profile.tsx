import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import * as api from './api'

export function AccountProfilePage() {
  const [profile, setProfile] = useState<api.AccountProfile>()
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    let active = true
    api.getAccountProfile().then(value => {
      if (!active) return
      setProfile(value); setFullName(value.fullName ?? ''); setEmail(value.email)
    }).catch(reason => {
      if (active) setError(reason instanceof Error ? reason.message : 'No se pudo cargar el perfil')
    }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  async function save(event: FormEvent) {
    event.preventDefault(); setSaving(true); setError(''); setMessage('')
    try {
      const updated = await api.updateAccountProfile({ fullName: fullName.trim(), email: email.trim() })
      setProfile(updated); setFullName(updated.fullName); setEmail(updated.email)
      setMessage(updated.emailVerified ? 'Datos de la cuenta guardados.' : 'Nombre guardado. Revisa el nuevo correo para verificar la dirección; el correo anterior ya no recibirá notificaciones.')
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron guardar los datos') }
    finally { setSaving(false) }
  }

  return <BaseLayout title="Mis datos" description="Actualiza tu nombre y correo de contacto.">
    <div className="px-4 lg:px-6"><Card className="max-w-2xl"><CardHeader><CardTitle>Perfil de la cuenta</CardTitle><CardDescription>Los cambios aplican sólo a tu usuario autenticado. Al cambiar el correo se envía un enlace de verificación.</CardDescription></CardHeader><CardContent>
      {loading ? <p role="status" className="text-sm text-muted-foreground">Cargando perfil…</p> : <form className="space-y-4" onSubmit={save}>
        <div className="space-y-2"><Label htmlFor="profile-name">Nombre</Label><Input id="profile-name" required maxLength={160} autoComplete="name" value={fullName} onChange={event => setFullName(event.target.value)}/></div>
        <div className="space-y-2"><Label htmlFor="profile-email">Correo electrónico</Label><Input id="profile-email" required type="email" maxLength={100} autoComplete="email" value={email} onChange={event => setEmail(event.target.value)}/>{profile&&!profile.emailVerified&&<p className="text-xs text-amber-700">Este correo aún no está verificado.</p>}</div>
        {profile&&<p className="text-xs text-muted-foreground">Usuario para iniciar sesión: {profile.username}</p>}
        <Button disabled={saving||!fullName.trim()||!email.trim()}>{saving?'Guardando…':'Guardar'}</Button>
      </form>}
      {message&&<p role="status" className="mt-3 text-sm text-emerald-700">{message}</p>}{error&&<p role="alert" className="mt-3 text-sm text-destructive">{error}</p>}
    </CardContent></Card></div>
  </BaseLayout>
}
