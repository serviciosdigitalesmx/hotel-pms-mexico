"use client"
import * as React from 'react'
import { BarChart3, Wrench, Users, Boxes, Truck, Layers, Receipt, CircleDollarSign, Settings, type LucideIcon } from 'lucide-react'
import { Link } from 'react-router-dom'
import { NavMain } from '@/components/nav-main'
import { NavUser } from '@/components/nav-user'
import { Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarMenu, SidebarMenuButton, SidebarMenuItem } from '@/components/ui/sidebar'
import { useFixiNavigation } from '@/fixi/navigation-context'

export type FixiNavItem = { title: string; url: string; icon?: LucideIcon; capability?: string | string[]; roles?: readonly string[]; items?: FixiNavItem[] }
type NavGroup = { label: string; items: FixiNavItem[] }
const administrators = ['ADMIN', 'OWNER', 'MANAGER'] as const
const groups: NavGroup[] = [{ label: 'Tu taller', items: [
  { title: 'Dashboard', url: '/dashboard', icon: BarChart3, capability: 'REPORTS_READ' },
  { title: 'Órdenes de servicio', url: '/service-orders', icon: Wrench, items: [
    { title: 'Todas las órdenes', url: '/service-orders', capability: 'ORDERS_READ' },
    { title: 'Nueva recepción', url: '/reception/new', capability: 'ORDERS_CREATE' },
    { title: 'Agenda', url: '/calendar', capability: 'ORDERS_READ' },
  ] },
  { title: 'Clientes', url: '/customers', icon: Users, items: [
    { title: 'Directorio de clientes', url: '/customers', capability: 'CUSTOMERS_READ' },
    { title: 'Equipos', url: '/devices', capability: 'DEVICES_READ' },
    { title: 'Importar / exportar', url: '/customers/import', capability: 'CUSTOMERS_WRITE' },
  ] },
  { title: 'Inventario', url: '/inventory', icon: Boxes, items: [
    { title: 'Productos y existencias', url: '/inventory', capability: 'INVENTORY_READ' },
    { title: 'Compras y recepción', url: '/purchases', capability: 'INVENTORY_PURCHASE' },
  ] },
  { title: 'Proveedores', url: '/suppliers', icon: Truck, capability: 'INVENTORY_READ' },
  { title: 'Servicios', url: '/services', icon: Layers, capability: 'ORDERS_READ' },
  { title: 'Gastos', url: '/expenses', icon: Receipt, capability: 'FINANCE_READ' },
  { title: 'Reportes', url: '/reports', icon: CircleDollarSign, items: [
    { title: 'Finanzas', url: '/reports/finance', capability: 'FINANCE_READ', roles: ['ADMIN', 'OWNER'] },
    { title: 'Reporte de órdenes', url: '/reports/orders', capability: 'REPORTS_READ' },
    { title: 'Reporte de inventario', url: '/reports/inventory', capability: 'REPORTS_READ' },
    { title: 'Productividad', url: '/reports/productivity', capability: 'REPORTS_READ' },
    { title: 'Resumen por sucursal', url: '/branch-dashboard', capability: 'BRANCHES_READ', roles: administrators },
  ] },
  { title: 'Configuración', url: '/settings', icon: Settings, items: [
    { title: 'Mi negocio', url: '/settings', capability: 'SETTINGS_READ', roles: administrators },
    { title: 'Usuarios y permisos', url: '/team', capability: 'USERS_MANAGE', roles: administrators },
    { title: 'Sucursales', url: '/branches', capability: 'BRANCHES_READ', roles: administrators },
    { title: 'Políticas y plantillas', url: '/settings/order-preferences', capability: 'SETTINGS_MANAGE', roles: administrators },
    { title: 'Estados de órdenes', url: '/settings/order-workflows', capability: 'WORKFLOWS_MANAGE', roles: ['ADMIN', 'OWNER'] },
    { title: 'Notificaciones', url: '/settings/notifications', capability: 'SETTINGS_MANAGE', roles: administrators },
    { title: 'WhatsApp', url: '/whatsapp', capability: 'SETTINGS_MANAGE', roles: administrators },
    { title: 'Asistente IA', url: '/ai', capability: 'SETTINGS_MANAGE', roles: administrators },
  ] },
] }]

/** Presentation of capabilities already resolved by the backend, never a grant. */
export function visibleFixiNavGroups(capabilities: ReadonlySet<string> | undefined, role?: string) {
  const permitted = (item: FixiNavItem) => {
    if (!item.capability || !capabilities) return false
    if (item.roles && (!role || !item.roles.includes(role))) return false
    const required = Array.isArray(item.capability) ? item.capability : [item.capability]
    return required.some(capability => capabilities.has(capability))
  }
  return groups.map(group => ({ ...group, items: group.items.flatMap(item => {
    if (!item.items) return permitted(item) ? [item] : []
    const children = item.items.filter(permitted)
    return children.length ? [{ ...item, url: children[0].url, items: children }] : []
  }) })).filter(group => group.items.length > 0)
}

export function AppSidebar(props: React.ComponentProps<typeof Sidebar>) {
  const { user, capabilities } = useFixiNavigation()

  const visibleGroups = visibleFixiNavGroups(capabilities, user?.role)

  return (
    <Sidebar {...props} className="fixi-tailadmin-sidebar border-r border-sidebar-border">
      <SidebarHeader className="border-b border-sidebar-border/50 pb-2.5">
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton size="lg" asChild className="rounded-xl hover:bg-sidebar-accent/80 transition-colors">
              <Link to={user?.platformRole ? "/platform" : "/dashboard"} className="flex items-center gap-3">
                <div className="flex size-9 items-center justify-center rounded-xl bg-gradient-to-tr from-primary to-primary/85 text-primary-foreground shadow-md shadow-primary/25 ring-1 ring-primary/20">
                  <Wrench className="size-5 stroke-[2]" />
                </div>
                <div className="grid flex-1 text-left leading-tight">
                  <div className="flex items-center gap-1.5">
                    <span className="truncate font-semibold tracking-tight text-sidebar-foreground text-2xl">
                      Fixi
                    </span>
                  </div>
                  <span className="truncate text-[11px] text-muted-foreground font-normal">
                    {user?.businessName || 'Taller en control'}
                  </span>
                </div>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent className="gap-2 px-1.5 py-2">
        {visibleGroups.map(group => (
          <NavMain key={group.label} label={group.label} items={group.items} />
        ))}
      </SidebarContent>
      <SidebarFooter className="border-t border-sidebar-border/50 pt-2">
        <NavUser user={{ name: user?.username ?? 'Usuario Fixi', email: 'Sesión activa', avatar: '' }} />
      </SidebarFooter>
    </Sidebar>
  )
}
