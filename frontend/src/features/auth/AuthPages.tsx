import { useState, type FormEvent, type ReactNode } from 'react'
import { Link, Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import { authApi } from '../../api/endpoints'
import { errorMessage } from '../../api/http'
import { useSession } from '../../auth/session'
import { Alert, Button, Field, Input, Loading } from '../../components/ui'

export function AuthCard({ title, subtitle, children }: { title: string; subtitle?: string; children: ReactNode }) {
  const [theme, setTheme] = useState<'light' | 'dark'>(() => (document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light'))

  function toggleTheme() {
    const next = theme === 'dark' ? 'light' : 'dark'
    setTheme(next)
    document.documentElement.dataset.theme = next
    try { localStorage.setItem('fixi-theme', next) } catch { /* theme still applies for this session */ }
  }

  return (
    <div className="fx-auth-shell">
      <button type="button" className="fx-auth-theme" onClick={toggleTheme} aria-label={theme === 'dark' ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'}>
        <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round">
          {theme === 'dark' ? <><circle cx="12" cy="12" r="4" /><path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" /></> : <path d="M20 15.5A8.5 8.5 0 0 1 8.5 4 8.5 8.5 0 1 0 20 15.5z" />}
        </svg>
      </button>
      <section className="fx-auth-story" aria-label="Fixi para talleres">
        <Link to="/login" className="fx-auth-brand">
          <span className="fixi-brand-mark">F</span>
          <span>Fixi</span>
        </Link>
        <div className="fx-auth-message">
          <span>Operación de taller, sin fricción</span>
          <h2>Todo el taller, en un solo pulso.</h2>
          <p>Recepción, reparación, refacciones y cobros conectados en una experiencia clara para tomar decisiones y avanzar más rápido.</p>
        </div>
        <div className="fx-auth-proof">
          <span>Órdenes y expedientes</span>
          <span>Inventario y mostrador</span>
          <span>Caja y seguimiento</span>
        </div>
      </section>
      <main className="fx-auth-side">
        <div className="fx-auth-card">
          <div className="fx-auth-card-head">
            <span className="fixi-brand-mark">F</span>
            <div>
              <h1>{title}</h1>
              {subtitle ? <p>{subtitle}</p> : null}
            </div>
          </div>
          {children}
        </div>
      </main>
    </div>
  )
}

const DEMO_USERS = [
  { email: 'admin@local', password: 'Admin123456', role: 'Administrador' },
  { email: 'tech@local', password: 'Tech123456', role: 'Técnico' },
  { email: 'recepcion@local', password: 'Recepcion123', role: 'Recepción' },
  { email: 'caja@local', password: 'Caja123456', role: 'Caja' },
]

export function LoginPage() {
  const { state, login } = useSession()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  if (state.status === 'authenticated') return <Navigate to={from} replace />

  async function submit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login(email.trim(), password)
      navigate(from, { replace: true })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthCard title="Ingresar" subtitle="Gestión de servicio técnico">
      <form onSubmit={submit} className="space-y-4" noValidate>
        <Field label="Correo electrónico">
          <Input type="email" autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
        </Field>
        <Field label="Contraseña">
          <Input type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </Field>
        {error ? <Alert tone="rose">{error}</Alert> : null}
        <Button type="submit" variant="primary" className="w-full" loading={loading} disabled={!email || !password}>
          Ingresar
        </Button>
        <p className="text-center text-sm">
          <Link to="/olvide" className="text-brand-700 hover:underline">
            Olvidé mi contraseña
          </Link>
        </p>
        <p className="border-t border-slate-100 pt-4 text-center text-sm text-slate-600">
          ¿Aún no tienes cuenta en Fixi?{' '}
          <Link to="/registro" className="font-medium text-brand-700 hover:underline">
            Pruébalo gratis
          </Link>
        </p>
      </form>

      <aside className="fx-auth-android" aria-label="Aplicación Android de Fixi">
        <div>
          <strong>Fixi en Android</strong>
          <span>Administra tu taller desde el celular.</span>
        </div>
        <a href="/downloads/fixi-android.apk" download>
          Descargar APK 4.0.1
        </a>
      </aside>

      {import.meta.env.DEV || import.meta.env.VITE_SHOW_DEMO_USERS === 'true' ? (
        <div className="fx-auth-demo">
          <p>Acceso rápido de demostración</p>
          <ul>
            {DEMO_USERS.map((u) => (
              <li key={u.email}>
                <button
                  type="button"
                  onClick={() => {
                    setEmail(u.email)
                    setPassword(u.password)
                  }}
                >
                  <strong className="block text-slate-700">{u.role}</strong>
                  {u.email} / {u.password}
                </button>
              </li>
            ))}
          </ul>
        </div>
      ) : null}
    </AuthCard>
  )
}

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [loading, setLoading] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    try {
      await authApi.forgotPassword(email.trim())
      setSent(true)
    } catch (err) {
      toast.error(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthCard title="Recuperar acceso">
      {sent ? (
        <div className="space-y-4 text-sm text-slate-700">
          <Alert tone="green">Si el correo electrónico está registrado, te enviamos un enlace para restablecer la contraseña (vence en 2 horas).</Alert>
          <p>Si no te llega, pídele a un administrador que genere un enlace desde Configuración → Usuarios.</p>
          <Link to="/login" className="text-brand-700 hover:underline">
            Volver al inicio de sesión
          </Link>
        </div>
      ) : (
        <form onSubmit={submit} className="space-y-4">
          <Field label="Correo electrónico">
            <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
          </Field>
          <Button type="submit" variant="primary" className="w-full" loading={loading} disabled={!email}>
            Enviar enlace
          </Button>
          <p className="text-center text-sm">
            <Link to="/login" className="text-brand-700 hover:underline">
              Volver
            </Link>
          </p>
        </form>
      )}
    </AuthCard>
  )
}

function PasswordForm({ submitLabel, onSubmit, withName }: { submitLabel: string; onSubmit: (password: string, displayName?: string) => Promise<void>; withName?: boolean }) {
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [name, setName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (password !== confirm) {
      setError('Las contraseñas no coinciden.')
      return
    }
    setError(null)
    setLoading(true)
    try {
      await onSubmit(password, name.trim() || undefined)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={submit} className="space-y-4">
      {withName ? (
        <Field label="Tu nombre (opcional)">
          <Input value={name} onChange={(e) => setName(e.target.value)} />
        </Field>
      ) : null}
      <Field label="Nueva contraseña" hint="Mínimo 8 caracteres, con letras y números.">
        <Input type="password" autoComplete="new-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
      </Field>
      <Field label="Repetir contraseña">
        <Input type="password" autoComplete="new-password" value={confirm} onChange={(e) => setConfirm(e.target.value)} required />
      </Field>
      {error ? <Alert tone="rose">{error}</Alert> : null}
      <Button type="submit" variant="primary" className="w-full" loading={loading} disabled={!password || !confirm}>
        {submitLabel}
      </Button>
    </form>
  )
}

function useTokenInfo() {
  const [params] = useSearchParams()
  const token = params.get('token') ?? ''
  const info = useQuery({ queryKey: ['token-info', token], queryFn: () => authApi.tokenInfo(token), enabled: !!token, retry: false })
  return { token, info }
}

export function ResetPasswordPage() {
  const { token, info } = useTokenInfo()
  const navigate = useNavigate()
  return (
    <AuthCard title="Nueva contraseña" subtitle={info.data ? info.data.email : undefined}>
      {!token || info.isError ? (
        <Alert tone="rose">El enlace es inválido o venció. Pide uno nuevo desde “Olvidé mi contraseña”.</Alert>
      ) : info.isLoading ? (
        <Loading />
      ) : (
        <PasswordForm
          submitLabel="Guardar contraseña"
          onSubmit={async (password) => {
            await authApi.resetPassword(token, password)
            toast.success('Contraseña actualizada. Ya puedes ingresar.')
            navigate('/login')
          }}
        />
      )}
    </AuthCard>
  )
}

export function AcceptInvitationPage() {
  const { token, info } = useTokenInfo()
  const { acceptSession } = useSession()
  const navigate = useNavigate()
  return (
    <AuthCard title="Bienvenido/a" subtitle={info.data ? `${info.data.shopName} · ${info.data.email}` : undefined}>
      {!token || info.isError ? (
        <Alert tone="rose">La invitación es inválida o venció. Pídele al administrador que te envíe una nueva.</Alert>
      ) : info.isLoading ? (
        <Loading />
      ) : (
        <PasswordForm
          withName
          submitLabel="Activar mi cuenta"
          onSubmit={async (password, displayName) => {
            const session = await authApi.acceptInvitation(token, password, displayName)
            acceptSession(session)
            navigate('/')
          }}
        />
      )}
    </AuthCard>
  )
}
