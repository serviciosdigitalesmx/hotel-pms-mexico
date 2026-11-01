"use client"
import * as React from 'react'
import { useEffect, useState } from 'react'
import {
  Calendar,
  CheckSquare,
  Clock,
  Laptop,
  Package,
  Settings,
  Users,
  Wrench,
  CircleDollarSign,
  BarChart3,
  Building2,
  Bell,
  Layers,
  FilePlus2,
  Stethoscope,
  Sparkles,
  MessageSquare,
  ShoppingCart,
  Receipt,
  Boxes,
  Truck,
  TrendingUp,
  UserCheck,
  FileSpreadsheet,
  Workflow
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { NavMain } from '@/components/nav-main'
import { NavUser } from '@/components/nav-user'
import { Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarMenu, SidebarMenuButton, SidebarMenuItem } from '@/components/ui/sidebar'
import * as api from '@/fixi/api'

type NavItem = { title:string; url:string; icon:typeof Wrench; capability?:string|string[] }
type NavGroup = { label:string; items:NavItem[] }
const groups:NavGroup[] = [
  { label: 'Prioridad', items: [{title:'Órdenes por asignar',url:'/orders/unassigned',icon:Clock,capability:'USERS_MANAGE'},{title:'Pendientes de pago',url:'/orders/pending-payment',icon:CircleDollarSign,capability:'PAYMENTS_RECEIVE'}] },
  { label: 'Fixi', items: [{title:'Dashboard',url:'/dashboard',icon:BarChart3,capability:'REPORTS_READ'},{title:'Alertas operativas',url:'/notifications',icon:Bell,capability:'REPORTS_READ'},{title:'Agenda',url:'/calendar',icon:Calendar,capability:'ORDERS_READ'}] },
  { label: 'Operación', items: [{title:'Recepción',url:'/reception/new',icon:FilePlus2,capability:'ORDERS_CREATE'},{title:'Órdenes de servicio',url:'/service-orders',icon:Wrench,capability:'ORDERS_READ'},{title:'Servicios',url:'/services',icon:Layers,capability:'ORDERS_READ'},{title:'Clientes',url:'/customers',icon:Users,capability:'CUSTOMERS_READ'},{title:'Importar clientes',url:'/customers/import',icon:Users,capability:'CUSTOMERS_WRITE'},{title:'Equipos',url:'/devices',icon:Laptop,capability:'DEVICES_READ'}] },
  { label: 'Taller', items: [{title:'Diagnósticos',url:'/diagnostics',icon:Stethoscope,capability:'ORDERS_READ'},{title:'Reparaciones',url:'/repairs',icon:Wrench,capability:'ORDERS_UPDATE'},{title:'Técnicos',url:'/technicians',icon:UserCheck,capability:'USERS_MANAGE'},{title:'Pruebas',url:'/tests',icon:CheckSquare,capability:'ORDERS_UPDATE'},{title:'Entrega',url:'/delivery',icon:Truck,capability:'PAYMENTS_RECEIVE'}] },
  { label: 'Inventario', items: [{title:'Refacciones',url:'/parts',icon:Package,capability:'INVENTORY_READ'},{title:'Inventario',url:'/inventory',icon:Boxes,capability:'INVENTORY_READ'},{title:'Proveedores',url:'/suppliers',icon:Truck,capability:'INVENTORY_READ'},{title:'Compras',url:'/purchases',icon:ShoppingCart,capability:'INVENTORY_PURCHASE'}] },
  { label: 'Administración', items: [{title:'Equipo / Usuarios',url:'/team',icon:Users,capability:'USERS_MANAGE'},{title:'Sucursales',url:'/branches',icon:Building2,capability:'BRANCHES_READ'},{title:'Gastos',url:'/expenses',icon:Receipt,capability:'FINANCE_READ'},{title:'Finanzas',url:'/reports/finance',icon:CircleDollarSign,capability:'FINANCE_READ'},{title:'Estados de órdenes',url:'/settings/order-workflows',icon:Workflow,capability:'WORKFLOWS_MANAGE'},{title:'Notificaciones',url:'/settings/notifications',icon:Bell,capability:'SETTINGS_MANAGE'},{title:'Dashboard sucursal',url:'/branch-dashboard',icon:BarChart3,capability:'BRANCHES_READ'},{title:'Reporte de órdenes',url:'/reports/orders',icon:FileSpreadsheet,capability:'REPORTS_READ'},{title:'Reporte de inventario',url:'/reports/inventory',icon:Boxes,capability:'REPORTS_READ'},{title:'Productividad',url:'/reports/productivity',icon:TrendingUp,capability:'REPORTS_READ'},{title:'Configuración',url:'/settings',icon:Settings,capability:'SETTINGS_READ'}] },
  { label: 'Integraciones', items: [{title:'WhatsApp',url:'/whatsapp',icon:MessageSquare,capability:'SETTINGS_MANAGE'},{title:'IA',url:'/ai',icon:Sparkles,capability:'SETTINGS_MANAGE'}] },
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

  return (
    <Sidebar {...props} className="border-r border-sidebar-border/70">
      <SidebarHeader className="border-b border-sidebar-border/50 pb-2.5">
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton size="lg" asChild className="rounded-xl hover:bg-sidebar-accent/80 transition-colors">
              <Link to={user?.platformRole ? "/platform" : "/dashboard"} className="flex items-center gap-3">
                <div className="flex size-9 items-center justify-center rounded-xl bg-gradient-to-tr from-primary to-primary/85 text-primary-foreground shadow-md shadow-primary/25 ring-1 ring-primary/20">
                  <Wrench className="size-4.5 stroke-[2.2]" />
                </div>
                <div className="grid flex-1 text-left leading-tight">
                  <div className="flex items-center gap-1.5">
                    <span className="truncate font-bold tracking-tight text-sidebar-foreground text-sm">
                      Fixi
                    </span>
                    <span className="rounded-full bg-primary/10 px-1.5 py-0.5 text-[9px] font-semibold text-primary">
                      TALLER
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
