"use client"
import * as React from 'react'
import { useEffect, useState } from 'react'
import { Calendar, CheckSquare, ClipboardList, Laptop, Package, Settings, Users, Wrench, CircleDollarSign, BarChart3, Building2, Bell } from 'lucide-react'
import { Link } from 'react-router-dom'
import { NavMain } from '@/components/nav-main'
import { NavUser } from '@/components/nav-user'
import { Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarMenu, SidebarMenuButton, SidebarMenuItem } from '@/components/ui/sidebar'
import * as api from '@/fixi/api'

type NavItem = { title:string; url:string; icon:typeof Wrench; capability?:string|string[] }
type NavGroup = { label:string; items:NavItem[] }
const groups:NavGroup[] = [
  { label: 'Prioridad', items: [{title:'Órdenes por asignar',url:'/orders/unassigned',icon:ClipboardList,capability:'USERS_MANAGE'},{title:'Pendientes de pago',url:'/orders/pending-payment',icon:CircleDollarSign,capability:'PAYMENTS_RECEIVE'}] },
  { label: 'Fixi', items: [{title:'Dashboard',url:'/dashboard',icon:ClipboardList,capability:'REPORTS_READ'},{title:'Alertas operativas',url:'/notifications',icon:Bell,capability:'REPORTS_READ'},{title:'Agenda',url:'/calendar',icon:Calendar,capability:'ORDERS_READ'}] },
  { label: 'Operación', items: [{title:'Recepción',url:'/reception/new',icon:ClipboardList,capability:'ORDERS_CREATE'},{title:'Órdenes de servicio',url:'/service-orders',icon:Wrench,capability:'ORDERS_READ'},{title:'Servicios',url:'/services',icon:Wrench,capability:'ORDERS_READ'},{title:'Clientes',url:'/customers',icon:Users,capability:'CUSTOMERS_READ'},{title:'Importar clientes',url:'/customers/import',icon:Users,capability:'CUSTOMERS_WRITE'},{title:'Equipos',url:'/devices',icon:Laptop,capability:'DEVICES_READ'}] },
  { label: 'Taller', items: [{title:'Diagnósticos',url:'/diagnostics',icon:ClipboardList,capability:'ORDERS_READ'},{title:'Reparaciones',url:'/repairs',icon:Wrench,capability:'ORDERS_UPDATE'},{title:'Técnicos',url:'/technicians',icon:Users,capability:'USERS_MANAGE'},{title:'Pruebas',url:'/tests',icon:CheckSquare,capability:'ORDERS_UPDATE'},{title:'Entrega',url:'/delivery',icon:CheckSquare,capability:'PAYMENTS_RECEIVE'}] },
  { label: 'Inventario', items: [{title:'Refacciones',url:'/parts',icon:Package,capability:'INVENTORY_READ'},{title:'Inventario',url:'/inventory',icon:Package,capability:'INVENTORY_READ'},{title:'Proveedores',url:'/suppliers',icon:Users,capability:'INVENTORY_READ'},{title:'Compras',url:'/purchases',icon:ClipboardList,capability:'INVENTORY_PURCHASE'}] },
  { label: 'Administración', items: [{title:'Equipo / Usuarios',url:'/team',icon:Users,capability:'USERS_MANAGE'},{title:'Sucursales',url:'/branches',icon:Building2,capability:'BRANCHES_READ'},{title:'Gastos',url:'/expenses',icon:CircleDollarSign,capability:'FINANCE_READ'},{title:'Finanzas',url:'/reports/finance',icon:CircleDollarSign,capability:'FINANCE_READ'},{title:'Estados de órdenes',url:'/settings/order-workflows',icon:Settings,capability:'WORKFLOWS_MANAGE'},{title:'Notificaciones',url:'/settings/notifications',icon:Settings,capability:'SETTINGS_MANAGE'},{title:'Dashboard sucursal',url:'/branch-dashboard',icon:BarChart3,capability:'BRANCHES_READ'},{title:'Reporte de órdenes',url:'/reports/orders',icon:BarChart3,capability:'REPORTS_READ'},{title:'Reporte de inventario',url:'/reports/inventory',icon:BarChart3,capability:'REPORTS_READ'},{title:'Productividad',url:'/reports/productivity',icon:BarChart3,capability:'REPORTS_READ'},{title:'Configuración',url:'/settings',icon:Settings,capability:'SETTINGS_READ'}] },
  { label: 'Integraciones', items: [{title:'WhatsApp',url:'/whatsapp',icon:ClipboardList,capability:'SETTINGS_MANAGE'},{title:'IA',url:'/ai',icon:Wrench,capability:'SETTINGS_MANAGE'}] },
]

export function visibleFixiNavGroups(capabilities: ReadonlySet<string> | undefined) {
  return groups.map(group => ({
    ...group,
    items: group.items.filter(item => {
      if (!item.capability) return false
      const needed = Array.isArray(item.capability) ? item.capability : [item.capability]
      return needed.some(capability => capabilities?.has(capability))
    }),
  })).filter(group => group.items.length > 0)
}

export function AppSidebar(props: React.ComponentProps<typeof Sidebar>) {
  const [capabilities, setCapabilities] = useState<Set<string>>()
  const [user, setUser] = useState<api.SessionUser | null>(null)
  useEffect(() => {
    let active = true
    void api.currentUser().then(async current => {
      if (!active) return
      setUser(current)
      if (!current || current.platformRole) { setCapabilities(new Set()); return }
      try {
        const result = await api.effectiveCapabilities(current.branchId)
        if (active) setCapabilities(new Set(result.capabilities))
      } catch {
        if (active) setCapabilities(new Set())
      }
    })
    return () => { active = false }
  }, [])

  const visibleGroups = visibleFixiNavGroups(capabilities)

  return <Sidebar {...props}><SidebarHeader><SidebarMenu><SidebarMenuItem><SidebarMenuButton size="lg" asChild><Link to={user?.platformRole ? "/platform" : "/dashboard"}><div className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground"><Wrench className="size-5"/></div><div className="grid flex-1 text-left text-sm leading-tight"><span className="truncate font-semibold">Fixi</span><span className="truncate text-xs">Taller en control</span></div></Link></SidebarMenuButton></SidebarMenuItem></SidebarMenu></SidebarHeader><SidebarContent>{visibleGroups.map(group=><NavMain key={group.label} label={group.label} items={group.items}/>)}</SidebarContent><SidebarFooter><NavUser user={{name:user?.username??'Usuario Fixi',email:'Sesión activa',avatar:''}}/></SidebarFooter></Sidebar>
}
