import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from 'react';
import { Link, NavLink, Outlet, useNavigate, useParams } from 'react-router-dom';
import api from '../services/api';

type Page<T> = { content: T[]; page: number; size: number; totalElements: number; totalPages: number };
type Metrics = {
  tenantsTotal: number; tenantsActive: number; tenantsSuspended: number;
  usersTotal: number; ownersTotal: number; serviceOrdersTotal: number;
};
type Tenant = {
  id: string; name: string; slug: string; status: string; createdAt: string;
  ownerUsername?: string | null; ownerName?: string | null; ownerEmail?: string | null;
  planKey?: string | null; planActive: boolean; onboardingCompleted: boolean;
};
type PlatformUser = {
  id: string; username: string; fullName?: string | null; email: string; role: string;
  active: boolean; tenantId: string; tenantName?: string | null; createdAt: string;
};
type Plan = { planKey: string; tenantsAssigned: number };
type Subscription = {
  id: string; tenantId: string; tenantName: string; planKey: string; status: string;
  startsAt: string; endsAt?: string | null; createdBy: string;
};
type Audit = {
  id: string; actorUsername: string; action: string; targetTenantId?: string | null;
  occurredAt: string; metadataJson: string;
};

const platformRoot = '/api/v1/auth/platform';
const navItems = [
  ['/', 'Resumen'], ['/tenants', 'Talleres'], ['/users', 'Usuarios'],
  ['/plans', 'Planes'], ['/subscriptions', 'Suscripciones'], ['/audit', 'Auditoría'],
] as const;

function formatDate(value?: string | null) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : new Intl.DateTimeFormat('es-MX', { dateStyle: 'medium' }).format(date);
}

function StatusBadge({ status }: { status: string }) {
  const active = status.toUpperCase() === 'ACTIVE';
  return <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${active ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-900'}`}>{status}</span>;
}

function Panel({ title, subtitle, children, action }: { title: string; subtitle?: string; children: ReactNode; action?: ReactNode }) {
  return <section className="space-y-5">
    <header className="flex flex-wrap items-end justify-between gap-3">
      <div><h1 className="text-2xl font-semibold text-on-surface">{title}</h1>{subtitle && <p className="mt-1 text-sm text-on-surface-variant">{subtitle}</p>}</div>
      {action}
    </header>
    {children}
  </section>;
}

function Notice({ error }: { error: string }) {
  return error ? <p role="alert" className="rounded-xl border border-error/30 bg-error-container px-4 py-3 text-sm text-on-error-container">{error}</p> : null;
}

function PageControls<T>({ page, onChange }: { page: Page<T> | null; onChange: (next: number) => void }) {
  if (!page || page.totalPages <= 1) return null;
  return <div className="flex flex-wrap items-center justify-between gap-3 text-sm">
    <span className="text-on-surface-variant">{page.totalElements} resultados · página {page.page + 1} de {page.totalPages}</span>
    <div className="flex gap-2"><button type="button" disabled={page.page <= 0} onClick={() => onChange(page.page - 1)} className="rounded-lg border border-outline px-3 py-2 disabled:opacity-50">Anterior</button><button type="button" disabled={page.page + 1 >= page.totalPages} onClick={() => onChange(page.page + 1)} className="rounded-lg border border-outline px-3 py-2 disabled:opacity-50">Siguiente</button></div>
  </div>;
}

export function PlatformShell() {
  const [access, setAccess] = useState<'checking' | 'allowed' | 'denied'>('checking');
  const [error, setError] = useState('');
  useEffect(() => {
    let mounted = true;
    api.get<Metrics>(`${platformRoot}/dashboard`)
      .then(() => { if (mounted) setAccess('allowed'); })
      .catch((reason: unknown) => {
        if (!mounted) return;
        setAccess('denied');
        setError(axiosMessage(reason));
      });
    return () => { mounted = false; };
  }, []);

  if (access === 'checking') return <div className="p-8 text-sm text-on-surface-variant">Validando autoridad de plataforma…</div>;
  if (access === 'denied') return <main className="mx-auto max-w-xl p-8"><Notice error={error || 'Esta cuenta no tiene autoridad SUPER_ADMIN.'} /><Link className="mt-4 inline-block underline" to="/dashboard">Volver a Fixi</Link></main>;
  return <div className="min-h-screen bg-surface-container-low md:flex">
    <aside className="border-b border-outline-variant bg-surface md:min-h-screen md:w-64 md:border-b-0 md:border-r">
      <div className="px-5 py-6"><p className="text-xs font-bold uppercase tracking-[0.18em] text-primary">Fixi</p><h2 className="mt-1 text-xl font-semibold">Plataforma</h2><p className="mt-1 text-xs text-on-surface-variant">Consola Super Admin</p></div>
      <nav aria-label="Navegación de plataforma" className="flex gap-1 overflow-x-auto px-3 pb-3 md:flex-col">
        {navItems.map(([path, label]) => <NavLink key={path} end={path === '/'} to={`/platform${path}`} className={({ isActive }) => `rounded-lg px-3 py-2.5 text-sm font-medium ${isActive ? 'bg-primary-container text-on-primary-container' : 'text-on-surface-variant hover:bg-surface-container-high'}`}>{label}</NavLink>)}
      </nav>
      <div className="hidden px-5 pb-6 pt-8 text-xs text-on-surface-variant md:block">Acceso global sujeto a autorización del backend.</div>
    </aside>
    <main className="min-w-0 flex-1 p-4 md:p-8"><div className="mx-auto max-w-7xl"><Outlet /></div></main>
  </div>;
}

export function PlatformDashboardPage() {
  const [data, setData] = useState<Metrics | null>(null);
  const [error, setError] = useState('');
  useEffect(() => { api.get<Metrics>(`${platformRoot}/dashboard`).then(({ data: result }) => setData(result)).catch((reason: unknown) => setError(axiosMessage(reason))); }, []);
  const cards: Array<[string, number | undefined]> = [
    ['Talleres registrados', data?.tenantsTotal], ['Activos', data?.tenantsActive],
    ['Suspendidos', data?.tenantsSuspended], ['Usuarios', data?.usersTotal],
    ['Owners', data?.ownersTotal], ['Órdenes registradas', data?.serviceOrdersTotal],
  ];
  return <Panel title="Resumen de plataforma" subtitle="Indicadores agregados desde registros reales de Fixi.">
    <Notice error={error} />
    {!data && !error ? <p role="status">Cargando métricas…</p> : <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">{cards.map(([label, value]) => <article key={label} className="rounded-2xl border border-outline-variant bg-surface p-5 shadow-sm"><p className="text-sm text-on-surface-variant">{label}</p><p className="mt-3 text-3xl font-semibold">{value?.toLocaleString('es-MX') ?? '—'}</p></article>)}</div>}
    <p className="text-xs text-on-surface-variant">Las órdenes muestran el total histórico de service_orders. No se presentan ingresos, uso o actividad sin una fuente de datos definida.</p>
  </Panel>;
}

export function PlatformTenantsPage() {
  const [page, setPage] = useState<Page<Tenant> | null>(null);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [pageNumber, setPageNumber] = useState(0);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [formError, setFormError] = useState('');
  const [form, setForm] = useState({ name: '', slug: '', ownerUsername: '', ownerEmail: '', initialPassword: '' });
  const load = useCallback(() => {
    setLoading(true); setError('');
    const params = new URLSearchParams({ page: String(pageNumber), size: '50' });
    if (search.trim()) params.set('search', search.trim());
    if (status) params.set('status', status);
    api.get<Page<Tenant>>(`${platformRoot}/tenants?${params}`).then(({ data }) => setPage(data)).catch((reason: unknown) => setError(axiosMessage(reason))).finally(() => setLoading(false));
  }, [search, status, pageNumber]);
  useEffect(() => { load(); }, [load]);
  async function submit(event: FormEvent) {
    event.preventDefault(); setFormError('');
    try {
      await api.post(`${platformRoot}/hotels`, form);
      setForm({ name: '', slug: '', ownerUsername: '', ownerEmail: '', initialPassword: '' });
      setCreating(false); load();
    } catch (reason) { setFormError(axiosMessage(reason)); }
  }
  return <Panel title="Talleres" subtitle="Directorio y estado operativo de tenants."
    action={<button type="button" onClick={() => setCreating(!creating)} className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-on-primary">{creating ? 'Cerrar formulario' : 'Crear taller'}</button>}>
    <Notice error={error} />
    {creating && <form onSubmit={submit} className="grid gap-3 rounded-2xl border border-outline-variant bg-surface p-5 sm:grid-cols-2">
      <h2 className="sm:col-span-2 text-lg font-semibold">Alta de tenant y owner inicial</h2>
      {(['name', 'slug', 'ownerUsername', 'ownerEmail', 'initialPassword'] as const).map((key) => <label key={key} className="grid gap-1 text-sm font-medium">{({ name: 'Nombre del taller', slug: 'Slug', ownerUsername: 'Usuario owner', ownerEmail: 'Correo owner', initialPassword: 'Contraseña temporal' })[key]}<input required type={key === 'ownerEmail' ? 'email' : key === 'initialPassword' ? 'password' : 'text'} autoComplete={key === 'initialPassword' ? 'new-password' : 'off'} maxLength={key === 'name' ? 160 : key === 'slug' ? 80 : undefined} value={form[key]} onChange={(event) => setForm((current) => ({ ...current, [key]: event.target.value }))} className="rounded-lg border border-outline-variant bg-surface px-3 py-2" /></label>)}
      <div className="sm:col-span-2"><Notice error={formError} /><button className="mt-3 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-on-primary">Crear tenant</button></div>
    </form>}
    <div className="flex flex-wrap gap-3"><input aria-label="Buscar talleres" placeholder="Buscar nombre o slug" value={search} onChange={(event) => { setSearch(event.target.value); setPageNumber(0); }} className="min-w-56 flex-1 rounded-lg border border-outline-variant bg-surface px-3 py-2" /><select aria-label="Filtrar estado" value={status} onChange={(event) => { setStatus(event.target.value); setPageNumber(0); }} className="rounded-lg border border-outline-variant bg-surface px-3 py-2"><option value="">Todos los estados</option><option value="ACTIVE">Activo</option><option value="SUSPENDED">Suspendido</option><option value="ONBOARDING">Onboarding</option></select></div>
    <div className="overflow-x-auto rounded-2xl border border-outline-variant bg-surface"><table className="w-full min-w-[850px] text-left text-sm"><thead className="bg-surface-container-low text-xs uppercase text-on-surface-variant"><tr>{['Taller', 'Estado', 'Owner principal', 'Plan', 'Creado', ''].map((label) => <th key={label} className="px-4 py-3">{label}</th>)}</tr></thead><tbody className="divide-y divide-outline-variant">{page?.content.map((tenant) => <tr key={tenant.id} className="hover:bg-surface-container-low"><td className="px-4 py-3"><Link to={`/platform/tenants/${tenant.id}`} className="font-semibold text-primary hover:underline">{tenant.name}</Link><div className="text-xs text-on-surface-variant">{tenant.slug}</div></td><td className="px-4 py-3"><StatusBadge status={tenant.status} /></td><td className="px-4 py-3">{tenant.ownerName || tenant.ownerUsername || 'Sin owner'}<div className="text-xs text-on-surface-variant">{tenant.ownerEmail || '—'}</div></td><td className="px-4 py-3">{tenant.planKey || 'Sin asignar'}{tenant.planKey && !tenant.planActive && <div className="text-xs text-amber-700">inactivo</div>}</td><td className="px-4 py-3">{formatDate(tenant.createdAt)}</td><td className="px-4 py-3"><Link to={`/platform/tenants/${tenant.id}`} className="rounded-lg border border-outline px-3 py-1.5">Ver detalle</Link></td></tr>)}{!loading && page?.content.length === 0 && <tr><td colSpan={6} className="px-4 py-10 text-center text-on-surface-variant">No hay talleres que coincidan.</td></tr>}</tbody></table></div>
    {loading && <p role="status" className="text-sm text-on-surface-variant">Cargando talleres…</p>}
    <PageControls page={page} onChange={setPageNumber} />
  </Panel>;
}

export function PlatformTenantDetailPage() {
  const { tenantId = '' } = useParams();
  const navigate = useNavigate();
  const [tenant, setTenant] = useState<Tenant | null>(null);
  const [subscriptions, setSubscriptions] = useState<Page<Subscription> | null>(null);
  const [planKey, setPlanKey] = useState('');
  const [historyPage, setHistoryPage] = useState(0);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const load = useCallback(() => Promise.all([
    api.get<Tenant>(`${platformRoot}/tenants/${tenantId}`),
    api.get<Page<Subscription>>(`${platformRoot}/tenants/${tenantId}/subscriptions?page=${historyPage}&size=20`),
  ]).then(([tenantResult, historyResult]) => {
    setTenant(tenantResult.data); setPlanKey(tenantResult.data.planKey || ''); setSubscriptions(historyResult.data);
  }).catch((reason: unknown) => setError(axiosMessage(reason))), [tenantId, historyPage]);
  useEffect(() => { void load(); }, [load]);
  async function setStatus(next: 'ACTIVE' | 'SUSPENDED') {
    if (!window.confirm(next === 'SUSPENDED' ? 'Suspender este taller? Sus usuarios dejarán de operar, sin borrar datos.' : 'Reactivar este taller?')) return;
    setSaving(true); setError('');
    try { const { data } = await api.patch<Tenant>(`${platformRoot}/tenants/${tenantId}/status`, { status: next }); setTenant(data); }
    catch (reason) { setError(axiosMessage(reason)); }
    finally { setSaving(false); }
  }
  async function savePlan(event: FormEvent) {
    event.preventDefault(); setSaving(true); setError('');
    try { const { data } = await api.put<Tenant>(`${platformRoot}/tenants/${tenantId}/plan`, { planKey }); setTenant(data); void load(); }
    catch (reason) { setError(axiosMessage(reason)); }
    finally { setSaving(false); }
  }
  if (!tenant && !error) return <p role="status">Cargando taller…</p>;
  return <Panel title={tenant?.name || 'Detalle del taller'} subtitle={tenant?.slug} action={<button onClick={() => navigate('/platform/tenants')} className="rounded-lg border border-outline px-3 py-2 text-sm">Volver</button>}>
    <Notice error={error} />
    {tenant && <>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{[
        ['Estado', <StatusBadge status={tenant.status} />], ['Owner principal', tenant.ownerName || tenant.ownerUsername || 'Sin owner'],
        ['Correo', tenant.ownerEmail || '—'], ['Creado', formatDate(tenant.createdAt)],
      ].map(([label, value]) => <article key={String(label)} className="rounded-2xl border border-outline-variant bg-surface p-4"><p className="text-xs text-on-surface-variant">{label}</p><div className="mt-2 font-medium">{value}</div></article>)}</div>
      <article className="rounded-2xl border border-outline-variant bg-surface p-5"><div className="flex flex-wrap items-center justify-between gap-3"><div><h2 className="font-semibold">Estado operativo</h2><p className="mt-1 text-sm text-on-surface-variant">Suspender bloquea login, refresh y API tenant; no elimina registros.</p></div><button disabled={saving} onClick={() => void setStatus(tenant.status === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED')} className="rounded-lg border border-outline px-4 py-2 text-sm font-semibold disabled:opacity-50">{tenant.status === 'SUSPENDED' ? 'Reactivar taller' : 'Suspender taller'}</button></div></article>
      <article className="rounded-2xl border border-outline-variant bg-surface p-5"><h2 className="font-semibold">Asignación administrativa de plan</h2><p className="mt-1 text-sm text-on-surface-variant">No cobra ni crea una suscripción con proveedor externo. Registra el historial administrativo.</p><form onSubmit={savePlan} className="mt-4 flex flex-wrap gap-2"><input required maxLength={80} pattern="[A-Za-z0-9._-]+" aria-label="Plan key" value={planKey} onChange={(event) => setPlanKey(event.target.value)} placeholder="planKey" className="min-w-56 rounded-lg border border-outline-variant bg-surface px-3 py-2" /><button disabled={saving} className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-on-primary disabled:opacity-50">Guardar plan</button></form><p className="mt-2 text-sm">Actual: {tenant.planKey || 'Sin asignar'} · {tenant.planActive ? 'activo' : 'inactivo'}</p></article>
      <article className="overflow-x-auto rounded-2xl border border-outline-variant bg-surface p-5"><h2 className="mb-3 font-semibold">Historial de asignaciones</h2><table className="w-full min-w-[600px] text-left text-sm"><thead><tr className="text-xs uppercase text-on-surface-variant"><th className="py-2">Plan</th><th>Estado</th><th>Inicio</th><th>Fin</th><th>Actor</th></tr></thead><tbody className="divide-y divide-outline-variant">{subscriptions?.content.map((item) => <tr key={item.id}><td className="py-3">{item.planKey}</td><td>{item.status}</td><td>{formatDate(item.startsAt)}</td><td>{formatDate(item.endsAt)}</td><td>{item.createdBy}</td></tr>)}{subscriptions?.content.length === 0 && <tr><td colSpan={5} className="py-6 text-on-surface-variant">Sin historial de asignación.</td></tr>}</tbody></table></article>
      <PageControls page={subscriptions} onChange={setHistoryPage} />
    </>}
  </Panel>;
}

export function PlatformUsersPage() {
  const [result, setResult] = useState<Page<PlatformUser> | null>(null);
  const [search, setSearch] = useState('');
  const [role, setRole] = useState('');
  const [active, setActive] = useState('');
  const [pageNumber, setPageNumber] = useState(0);
  const [error, setError] = useState('');
  useEffect(() => { const params = new URLSearchParams({ page: String(pageNumber), size: '100' }); if (search.trim()) params.set('search', search.trim()); if (role) params.set('role', role); if (active) params.set('active', active); api.get<Page<PlatformUser>>(`${platformRoot}/users?${params}`).then(({ data }) => setResult(data)).catch((reason: unknown) => setError(axiosMessage(reason))); }, [search, role, active, pageNumber]);
  return <Panel title="Usuarios globales" subtitle="Directorio administrativo; no habilita edición de cuentas tenant.">
    <Notice error={error} /><div className="flex flex-wrap gap-3"><input aria-label="Buscar usuarios" placeholder="Usuario, nombre o correo" value={search} onChange={(event) => { setSearch(event.target.value); setPageNumber(0); }} className="min-w-56 flex-1 rounded-lg border border-outline-variant bg-surface px-3 py-2" /><select aria-label="Filtrar rol" value={role} onChange={(event) => { setRole(event.target.value); setPageNumber(0); }} className="rounded-lg border border-outline-variant bg-surface px-3 py-2"><option value="">Todos los roles</option>{['OWNER','ADMIN','MANAGER','RECEPTIONIST','TECHNICIAN','KITCHEN','HOUSEKEEPER','GUEST'].map((item) => <option key={item} value={item}>{item}</option>)}</select><select aria-label="Filtrar estado" value={active} onChange={(event) => { setActive(event.target.value); setPageNumber(0); }} className="rounded-lg border border-outline-variant bg-surface px-3 py-2"><option value="">Todos los estados</option><option value="true">Activo</option><option value="false">Inactivo</option></select></div>
    <div className="overflow-x-auto rounded-2xl border border-outline-variant bg-surface"><table className="w-full min-w-[800px] text-left text-sm"><thead className="bg-surface-container-low text-xs uppercase text-on-surface-variant"><tr>{['Usuario', 'Rol', 'Estado', 'Tenant', 'Creado'].map((item) => <th key={item} className="px-4 py-3">{item}</th>)}</tr></thead><tbody className="divide-y divide-outline-variant">{result?.content.map((user) => <tr key={user.id}><td className="px-4 py-3">{user.fullName || user.username}<div className="text-xs text-on-surface-variant">{user.username} · {user.email}</div></td><td className="px-4 py-3">{user.role}</td><td className="px-4 py-3"><StatusBadge status={user.active ? 'ACTIVE' : 'INACTIVE'} /></td><td className="px-4 py-3"><Link className="text-primary hover:underline" to={`/platform/tenants/${user.tenantId}`}>{user.tenantName || user.tenantId}</Link></td><td className="px-4 py-3">{formatDate(user.createdAt)}</td></tr>)}{result?.content.length === 0 && <tr><td colSpan={5} className="px-4 py-8 text-center text-on-surface-variant">No se encontraron usuarios.</td></tr>}</tbody></table></div>
    <PageControls page={result} onChange={setPageNumber} />
  </Panel>;
}

export function PlatformPlansPage() {
  const [plans, setPlans] = useState<Plan[]>([]);
  const [error, setError] = useState('');
  useEffect(() => { api.get<Plan[]>(`${platformRoot}/plans`).then(({ data }) => setPlans(data)).catch((reason: unknown) => setError(axiosMessage(reason))); }, []);
  return <Panel title="Planes" subtitle="Claves existentes de TenantPlan, sin catálogo ni límites inventados."><Notice error={error} /><div className="overflow-x-auto rounded-2xl border border-outline-variant bg-surface"><table className="w-full text-left text-sm"><thead className="bg-surface-container-low text-xs uppercase text-on-surface-variant"><tr><th className="px-4 py-3">planKey</th><th className="px-4 py-3">Talleres asignados</th></tr></thead><tbody className="divide-y divide-outline-variant">{plans.map((plan) => <tr key={plan.planKey}><td className="px-4 py-3 font-medium">{plan.planKey}</td><td className="px-4 py-3">{plan.tenantsAssigned}</td></tr>)}{plans.length === 0 && <tr><td colSpan={2} className="px-4 py-8 text-center text-on-surface-variant">No hay planes asignados actualmente.</td></tr>}</tbody></table></div><Link className="text-sm text-primary underline" to="/platform/tenants">Administrar asignaciones por taller</Link></Panel>;
}

export function PlatformSubscriptionsPage() {
  const [result, setResult] = useState<Page<Subscription> | null>(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [error, setError] = useState('');
  useEffect(() => { api.get<Page<Subscription>>(`${platformRoot}/subscriptions?page=${pageNumber}&size=100`).then(({ data }) => setResult(data)).catch((reason: unknown) => setError(axiosMessage(reason))); }, [pageNumber]);
  return <Panel title="Suscripciones" subtitle="Historial de asignaciones de plan en Fixi; no representa facturación externa."><Notice error={error} /><div className="overflow-x-auto rounded-2xl border border-outline-variant bg-surface"><table className="w-full min-w-[700px] text-left text-sm"><thead className="bg-surface-container-low text-xs uppercase text-on-surface-variant"><tr>{['Taller', 'Plan', 'Estado', 'Inicio', 'Fin', 'Actor'].map((item) => <th key={item} className="px-4 py-3">{item}</th>)}</tr></thead><tbody className="divide-y divide-outline-variant">{result?.content.map((item) => <tr key={item.id}><td className="px-4 py-3"><Link className="text-primary hover:underline" to={`/platform/tenants/${item.tenantId}`}>{item.tenantName}</Link></td><td className="px-4 py-3">{item.planKey}</td><td className="px-4 py-3">{item.status}</td><td className="px-4 py-3">{formatDate(item.startsAt)}</td><td className="px-4 py-3">{formatDate(item.endsAt)}</td><td className="px-4 py-3">{item.createdBy}</td></tr>)}{result?.content.length === 0 && <tr><td colSpan={6} className="px-4 py-8 text-center text-on-surface-variant">Sin historial.</td></tr>}</tbody></table></div><PageControls page={result} onChange={setPageNumber} /></Panel>;
}

export function PlatformAuditPage() {
  const [result, setResult] = useState<Page<Audit> | null>(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [error, setError] = useState('');
  useEffect(() => { api.get<Page<Audit>>(`${platformRoot}/audit?page=${pageNumber}&size=100`).then(({ data }) => setResult(data)).catch((reason: unknown) => setError(axiosMessage(reason))); }, [pageNumber]);
  return <Panel title="Auditoría de plataforma" subtitle="Eventos administrativos persistidos; no incluye credenciales ni tokens."><Notice error={error} /><div className="overflow-x-auto rounded-2xl border border-outline-variant bg-surface"><table className="w-full min-w-[800px] text-left text-sm"><thead className="bg-surface-container-low text-xs uppercase text-on-surface-variant"><tr>{['Fecha', 'Actor', 'Acción', 'Tenant', 'Metadatos'].map((item) => <th key={item} className="px-4 py-3">{item}</th>)}</tr></thead><tbody className="divide-y divide-outline-variant">{result?.content.map((event) => <tr key={event.id}><td className="px-4 py-3">{formatDate(event.occurredAt)}</td><td className="px-4 py-3">{event.actorUsername}</td><td className="px-4 py-3 font-medium">{event.action}</td><td className="px-4 py-3">{event.targetTenantId || '—'}</td><td className="max-w-sm truncate px-4 py-3 font-mono text-xs">{event.metadataJson}</td></tr>)}{result?.content.length === 0 && <tr><td colSpan={5} className="px-4 py-8 text-center text-on-surface-variant">Aún no hay eventos de plataforma.</td></tr>}</tbody></table></div><PageControls page={result} onChange={setPageNumber} /></Panel>;
}

function axiosMessage(reason: unknown) {
  if (typeof reason === 'object' && reason !== null && 'response' in reason) {
    const response = (reason as { response?: { data?: { detail?: string; message?: string }; status?: number } }).response;
    return response?.data?.detail || response?.data?.message || `Solicitud rechazada (HTTP ${response?.status ?? 'desconocido'}).`;
  }
  return reason instanceof Error ? reason.message : 'No fue posible completar la solicitud.';
}
