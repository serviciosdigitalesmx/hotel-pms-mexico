import { NavLink, useLocation } from 'react-router-dom'
import { useFixiNavigation } from './navigation-context'

const tabs = [
  { title: 'Todas', url: '/service-orders', capability: 'ORDERS_READ' },
  { title: 'Por asignar', url: '/orders/unassigned', capability: 'USERS_MANAGE' },
  { title: 'Diagnóstico', url: '/diagnostics', capability: 'ORDERS_READ' },
  { title: 'En reparación', url: '/repairs', capability: 'ORDERS_UPDATE' },
  { title: 'Pruebas', url: '/tests', capability: 'ORDERS_UPDATE' },
  { title: 'Por entregar', url: '/delivery', capability: 'PAYMENTS_RECEIVE' },
  { title: 'Pendientes de pago', url: '/orders/pending-payment', capability: 'PAYMENTS_RECEIVE' },
  { title: 'Agenda', url: '/calendar', capability: 'ORDERS_READ' },
] as const

export function visibleOrderTabs(capabilities?: ReadonlySet<string>) {
  return tabs.filter(tab => capabilities?.has(tab.capability))
}

export function OrderWorkspaceNavigation() {
  const { capabilities } = useFixiNavigation()
  const { pathname } = useLocation()
  const isOrderRoute = ['/service-orders', '/orders', '/reception', '/diagnostics', '/repairs', '/tests', '/delivery', '/calendar'].some(prefix => pathname === prefix || pathname.startsWith(prefix + '/'))
  if (!isOrderRoute) return null
  const items = visibleOrderTabs(capabilities)
  if (!items.length) return null
  return <nav aria-label="Vistas de órdenes" className="fixi-order-tabs mx-4 lg:mx-6">
    {items.map(tab => <NavLink key={tab.url} to={tab.url} end className={({ isActive }) => `fixi-order-tab ${isActive ? 'is-active' : ''}`}>{tab.title}</NavLink>)}
  </nav>
}
