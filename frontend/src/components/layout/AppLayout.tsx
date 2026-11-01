import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import type { Permission } from '../../api/types'
import { errorMessage } from '../../api/http'
import { useSession } from '../../auth/session'
import { cn } from '../../lib/cn'
import { initials } from '../../lib/format'
import { ROLE } from '../../lib/labels'
import { SubscriptionBanner } from './SubscriptionBanner'

interface NavItem { to: string; label: string; icon: string; permission?: Permission; end?: boolean }
const NAV: { section: string; items: NavItem[] }[] = [
  { section: 'Trabajo', items: [
    { to: '/', label: 'Inicio', icon: 'home', end: true },
    { to: '/orders', label: 'Órdenes', icon: 'orders', end: true },
    { to: '/orders/board', label: 'Tablero', icon: 'board' },
    { to: '/orders/new', label: 'Recibir equipo', icon: 'plus', permission: 'orders.manage' },
    { to: '/customers', label: 'Clientes', icon: 'customers' },
  ] },
  { section: 'Mostrador', items: [
    { to: '/pos', label: 'Punto de venta', icon: 'pos', permission: 'sales' },
    { to: '/sales', label: 'Ventas', icon: 'sales', permission: 'sales' },
    { to: '/cash', label: 'Caja', icon: 'cash', permission: 'sales' },
    { to: '/invoices', label: 'Facturas', icon: 'invoice', permission: 'sales' },
  ] },
  { section: 'Inventario', items: [
    { to: '/inventory', label: 'Inventario', icon: 'inventory' },
    { to: '/purchases', label: 'Compras', icon: 'purchase', permission: 'inventory.manage' },
    { to: '/transfers', label: 'Transferencias', icon: 'transfer', permission: 'inventory.manage' },
  ] },
  { section: 'Administración', items: [
    { to: '/reports', label: 'Reportes', icon: 'reports', permission: 'reports' },
    { to: '/messages', label: 'Mensajes', icon: 'messages', permission: 'orders.work' },
    { to: '/settings', label: 'Configuración', icon: 'settings', permission: 'admin' },
    { to: '/billing', label: 'Suscripción', icon: 'billing', permission: 'admin' },
  ] },
]
export function AppIcon({ name, className }: { name: string; className?: string }) {
  const paths: Record<string, string> = {
    home: 'M3 10.5 12 3l9 7.5M5 9v11h14V9M9 20v-7h6v7',
    orders: 'M6 4h12v16H6zM9 8h6M9 12h6M9 16h4',
    board: 'M4 4h16v16H4zM10 4v16m5-16v16',
    plus: 'M12 5v14M5 12h14',
    customers: 'M16 20c0-3-2-5-5-5s-5 2-5 5m5-8a4 4 0 1 0 0-8 4 4 0 0 0 0 8m7-5a3 3 0 0 1 0 6m1 2c2 .7 3 2.5 3 5',
    pos: 'M4 5h16v12H4zM2 20h20M8 17l-1 3m10-3 1 3',
    sales: 'M5 4h14v16H5zM8 8h8M8 12h8M8 16h4',
    cash: 'M3 6h18v13H3zM3 10h18m-9 2v4',
    invoice: 'M6 3h12v18l-3-2-3 2-3-2-3 2zM9 8h6m-6 4h6',
    inventory: 'm12 3 9 5-9 5-9-5zm-9 5v9l9 5m9-14v9l-9 5m0-9v9',
    purchase: 'M5 5h14l-1 15H6zM9 9V6a3 3 0 0 1 6 0v3',
    transfer: 'M7 7h13l-3-3m3 3-3 3M17 17H4l3-3m-3 3 3 3',
    reports: 'M4 19V5m0 14h17M7 15l4-4 3 2 5-7',
    messages: 'M4 5h16v12H9l-5 4zM8 9h8m-8 4h5',
    settings: 'M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8m0-5v2m0 14v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M3 12h2m14 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4',
    billing: 'M4 5h16v14H4zM4 9h16m-12 5h4',
    menu: 'M4 6h16M4 12h16M4 18h16',
    sun: 'M12 3v2m0 14v2M3 12h2m14 0h2M5.6 5.6 7 7m10 10 1.4 1.4m0-12.8L17 7M7 17l-1.4 1.4M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0',
    moon: 'M20 15.5A8.5 8.5 0 0 1 8.5 4 8.5 8.5 0 1 0 20 15.5z',
    pin: 'M12 21s7-7 7-12a7 7 0 0 0-14 0c0 5 7 12 7 12zm0-9a3 3 0 1 0 0-6 3 3 0 0 0 0 6',
    chevron: 'm9 18 6-6-6-6',
    search: 'm20 20-4-4m2-6a8 8 0 1 1-16 0 8 8 0 0 1 16 0',
  }
  return <svg aria-hidden="true" className={cn('h-[18px] w-[18px] shrink-0', className)} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.65" strokeLinecap="round" strokeLinejoin="round"><path d={paths[name] ?? paths.orders}/></svg>
}

function getInitialTheme() {
  try { const saved = localStorage.getItem('fixi-theme'); if (saved === 'dark' || saved === 'light') return saved } catch { /* storage may be unavailable */ }
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function AppLayout() {
  const { user, shops, can, logout, switchShop } = useSession()
  const [mobileOpen, setMobileOpen] = useState(false)
  const [switching, setSwitching] = useState(false)
  const [theme, setTheme] = useState<'light' | 'dark'>(getInitialTheme)
  const [menuOpen, setMenuOpen] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const nav = NAV.map(s => ({ ...s, items: s.items.filter(i => !i.permission || can(i.permission)) })).filter(s => s.items.length > 0)
  const primary = nav[0]
  const secondary = nav.slice(1)
  const allItems = NAV.flatMap(s => s.items)
  const activeTitle = allItems.find(i => location.pathname === i.to)?.label ?? allItems.find(i => i.to !== '/' && location.pathname.startsWith(i.to + '/'))?.label ?? 'Fixi'

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    try { localStorage.setItem('fixi-theme', theme) } catch { /* theme remains available for this session */ }
  }, [theme])

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setMobileOpen(false)
        setMenuOpen(false)
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [])

  async function onSwitch(shopId: string) {
    if (shopId === user?.shopId) return
    setSwitching(true)
    try { await switchShop(shopId); navigate('/'); toast.success('Cambiaste de sucursal') }
    catch (err) { toast.error('No se pudo cambiar de sucursal', { description: errorMessage(err) }) }
    finally { setSwitching(false) }
  }

  const navLink = (item: NavItem) => (
    <NavLink key={item.to} to={item.to} end={item.end} onClick={() => setMobileOpen(false)}
      className={({ isActive }) => cn('fixi-navlink', isActive && 'is-active')}>
      <AppIcon name={item.icon}/><span>{item.label}</span>
    </NavLink>
  )

  const sidebar = (
    <nav aria-label="Principal" className="fixi-sidebar-inner">
      <NavLink to="/" className="fixi-brand" onClick={() => setMobileOpen(false)}>
        <span className="fixi-brand-mark">F</span><span className="fixi-brand-copy"><b>Fixi</b><small>{user?.shopName}</small></span>
      </NavLink>
      <div className="fixi-nav-scroll">
        <p className="fixi-nav-caption">Taller</p>
        {primary?.items.map(navLink)}
        {secondary.map(section => <details className="fixi-nav-group" key={section.section} open={section.section === 'Administración' || section.items.some(i => location.pathname === i.to || location.pathname.startsWith(i.to + '/'))}>
          <summary>{section.section}<span className="fixi-nav-count">{section.items.length}</span></summary>
          <div>{section.items.map(navLink)}</div>
        </details>)}
      </div>
      <div className="fixi-sidebar-bottom">
        <button type="button" className="fixi-quiet-link" onClick={() => setTheme(t => t === 'dark' ? 'light' : 'dark')} aria-label={theme === 'dark' ? 'Usar modo claro' : 'Usar modo oscuro'}>
          <AppIcon name={theme === 'dark' ? 'sun' : 'moon'}/><span>Modo {theme === 'dark' ? 'oscuro' : 'claro'}</span>
        </button>
        <button type="button" className="fixi-user-button" onClick={() => setMenuOpen(v => !v)} aria-expanded={menuOpen} aria-haspopup="menu">
          <span className="fixi-avatar">{initials(user?.displayName ?? '')}</span><span className="fixi-brand-copy"><b>{user?.displayName}</b><small>{user ? ROLE[user.role] : ''}</small></span><span className="fixi-user-more">···</span>
        </button>
        {menuOpen && <button type="button" className="fixi-menu-dismiss" aria-label="Cerrar opciones de cuenta" onClick={() => setMenuOpen(false)} />}
        {menuOpen && <div role="menu" className="fixi-user-menu">
          <button role="menuitem" onClick={() => { setMenuOpen(false); navigate('/profile') }}>Mi cuenta</button>
          <button role="menuitem" onClick={() => { setMenuOpen(false); void logout() }}>Cerrar sesión</button>
        </div>}
      </div>
    </nav>
  )

  return (
    <div className="fixi-app-shell">
      <a className="fixi-skip-link" href="#contenido">Saltar al contenido</a>
      <aside className="fixi-sidebar no-print">{sidebar}</aside>
      {mobileOpen && <div className="fixi-mobile-backdrop no-print" onClick={() => setMobileOpen(false)}>
        <aside id="fixi-mobile-navigation" className="fixi-mobile-panel" onClick={e => e.stopPropagation()}>{sidebar}</aside>
      </div>}
      <div className="fixi-main-column">
        <header className="fixi-topbar no-print">
          <button type="button" className="fixi-icon-button fixi-mobile-menu" onClick={() => setMobileOpen(true)} aria-label="Abrir menú" aria-expanded={mobileOpen} aria-controls="fixi-mobile-navigation"><AppIcon name="menu"/></button>
          <div className="fixi-breadcrumb"><AppIcon name="pin"/><span className="truncate">{user?.shopName}</span><span className="fixi-crumb-separator">/</span><b>{activeTitle}</b></div>
          <div className="fixi-top-actions">
            {shops.length > 1 ? <label className="fixi-branch-control"><span>Sucursal</span><select value={user?.shopId} disabled={switching} onChange={e => void onSwitch(e.target.value)} aria-label="Cambiar de sucursal">
              {shops.map(s => <option key={s.shopId} value={s.shopId}>{s.shopName}</option>)}
            </select></label> : null}
            <button type="button" className="fixi-icon-button fixi-theme-top" onClick={() => setTheme(t => t === 'dark' ? 'light' : 'dark')} aria-label={theme === 'dark' ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'}><AppIcon name={theme === 'dark' ? 'sun' : 'moon'}/></button>
          </div>
        </header>
        <main id="contenido" className="fixi-main-content">
          <SubscriptionBanner/>
          <Outlet/>
        </main>
      </div>
    </div>
  )
}
