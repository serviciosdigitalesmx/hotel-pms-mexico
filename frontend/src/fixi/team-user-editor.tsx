import { useEffect, useState, type FormEvent } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const field = 'w-full rounded-md border bg-background px-3 py-2 text-sm'

export function TeamUserEditor() {
  const [users, setUsers] = useState<api.TeamUser[]>([])
  const [userId, setUserId] = useState('')
  const [draft, setDraft] = useState({ email: '', role: 'TECHNICIAN' })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const selectableRoles = ['MANAGER', 'RECEPTIONIST', 'TECHNICIAN']
  const currentRoleIsLegacy = !!userId && !selectableRoles.includes(draft.role)
  useEffect(() => {
    let active = true
    api.listUsers().then(rows => { if (active) setUsers(rows) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar usuarios') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])
  function select(id: string) {
    setUserId(id)
    const user = users.find(row => row.id === id)
    if (user) setDraft({ email: user.email, role: user.role })
    setMessage(''); setError('')
  }
  async function save(event: FormEvent) {
    event.preventDefault(); if (!userId) return
    setSaving(true); setError(''); setMessage('')
    try {
      const updated = await api.updateUser(userId, draft)
      setUsers(rows => rows.map(row => row.id === updated.id ? updated : row))
      setMessage('Perfil actualizado')
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo actualizar el usuario') }
    finally { setSaving(false) }
  }
  return <Card><CardHeader><CardTitle>Editar perfil y rol</CardTitle></CardHeader><CardContent>
    <form className="grid gap-3 md:grid-cols-3" onSubmit={save}>
      <select aria-label="Usuario a editar" className={field} value={userId} disabled={loading} onChange={event => select(event.target.value)}>
        <option value="">Selecciona usuario</option>{users.map(user => <option key={user.id} value={user.id}>{user.username} · {user.role}{user.active ? '' : ' · inactivo'}</option>)}
      </select>
      <Input required type="email" maxLength={254} aria-label="Correo del usuario" placeholder="Correo" disabled={!userId} value={draft.email} onChange={event => setDraft({ ...draft, email: event.target.value })}/>
      <select aria-label="Rol del usuario" className={field} disabled={!userId} value={draft.role} onChange={event => setDraft({ ...draft, role: event.target.value })}>
        {currentRoleIsLegacy && <option value={draft.role} disabled>{draft.role} · rol histórico (no asignable)</option>}
        {selectableRoles.map(role => <option key={role} value={role}>{role}</option>)}
      </select>
      <Button disabled={saving || !userId}>{saving ? 'Guardando…' : 'Guardar perfil'}</Button>
    </form>
    {loading && <p role="status" className="mt-3 text-sm text-muted-foreground">Cargando usuarios…</p>}
    {message && <p role="status" className="mt-3 text-sm text-emerald-700">{message}</p>}
    {error && <p role="alert" className="mt-3 text-sm text-destructive">{error}</p>}
  </CardContent></Card>
}
