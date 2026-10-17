"use client"

import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import * as api from '@/fixi/api'

export function SessionIdleLock() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [locked, setLocked] = useState(false)
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [activityRevision, setActivityRevision] = useState(0)

  useEffect(() => {
    let active = true
    let cleanup: () => void = () => {}
    const configure = async () => {
      cleanup()
      try {
        const [user, preference] = await Promise.all([api.currentUser(), api.getSessionSecurityPreference().catch(() => null)])
        if (!active || !user || !preference?.idleTimeoutMinutes) {
          if (!preference?.idleTimeoutMinutes) setLocked(false)
          return
        }
        setUsername(user.username)
        let timer: number | undefined
        const timeout = preference.idleTimeoutMinutes * 60_000
        const reset = () => {
          if (timer !== undefined) window.clearTimeout(timer)
          timer = window.setTimeout(() => { setLocked(true); setError('') }, timeout)
        }
        const events: Array<keyof WindowEventMap> = ['pointerdown', 'keydown', 'touchstart', 'scroll']
        events.forEach(event => window.addEventListener(event, reset, { passive: true }))
        reset()
        cleanup = () => {
          if (timer !== undefined) window.clearTimeout(timer)
          events.forEach(event => window.removeEventListener(event, reset))
        }
      } catch { /* Missing authentication or preferences must not block public pages. */ }
    }
    window.addEventListener('fixi:session-security-changed', configure)
    void configure()
    return () => { active = false; window.removeEventListener('fixi:session-security-changed', configure); cleanup() }
  }, [locked, activityRevision])

  async function unlock(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError('')
    try {
      await api.login(username, password)
      setPassword(''); setLocked(false); setActivityRevision(revision => revision + 1)
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo verificar la contraseña') }
    finally { setBusy(false) }
  }

  async function signOut() {
    setBusy(true)
    try { await api.logout() } finally { navigate('/login'); setBusy(false) }
  }

  if (!locked) return null
  return <div role="dialog" aria-modal="true" aria-labelledby="idle-lock-title" className="fixed inset-0 z-[100] flex items-center justify-center bg-background/95 p-4 backdrop-blur-sm">
    <form onSubmit={unlock} className="w-full max-w-sm space-y-4 rounded-xl border bg-card p-6 shadow-xl">
      <div><h2 id="idle-lock-title" className="text-xl font-semibold">Sesión bloqueada</h2><p className="mt-1 text-sm text-muted-foreground">Se bloqueó por inactividad. Vuelve a autenticarte para continuar como {username}.</p></div>
      <Input type="password" autoComplete="current-password" autoFocus required aria-label="Contraseña para desbloquear" placeholder="Contraseña" value={password} onChange={event => setPassword(event.target.value)}/>
      {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
      <div className="flex gap-2"><Button className="flex-1" disabled={busy}>{busy ? 'Verificando…' : 'Desbloquear'}</Button><Button type="button" variant="outline" disabled={busy} onClick={() => void signOut()}>Cerrar sesión</Button></div>
    </form>
  </div>
}
