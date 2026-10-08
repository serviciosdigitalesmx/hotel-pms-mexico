"use client"
import * as React from 'react'
import { Calendar, CheckSquare, ClipboardList, Laptop, Package, Settings, Users, Wrench } from 'lucide-react'
import { Link } from 'react-router-dom'
import { NavMain } from '@/components/nav-main'
import { NavUser } from '@/components/nav-user'
import { Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarMenu, SidebarMenuButton, SidebarMenuItem } from '@/components/ui/sidebar'
const groups = [
  { label: 'Fixi', items: [{title:'Dashboard',url:'/dashboard',icon:ClipboardList},{title:'Agenda',url:'/calendar',icon:Calendar}] },
  { label: 'Operación', items: [{title:'Recepción',url:'/reception',icon:ClipboardList},{title:'Órdenes de servicio',url:'/service-orders',icon:Wrench},{title:'Clientes',url:'/customers',icon:Users},{title:'Equipos',url:'/equipment',icon:Laptop},{title:'Agenda',url:'/calendar',icon:Calendar}] },
  { label: 'Taller', items: [{title:'Diagnósticos',url:'/diagnostics',icon:ClipboardList},{title:'Reparaciones',url:'/repairs',icon:Wrench},{title:'Técnicos',url:'/technicians',icon:Users},{title:'Pruebas',url:'/tests',icon:CheckSquare},{title:'Entrega',url:'/delivery',icon:CheckSquare}] },
  { label: 'Inventario', items: [{title:'Refacciones',url:'/parts',icon:Package},{title:'Inventario',url:'/inventory',icon:Package},{title:'Proveedores',url:'/suppliers',icon:Users},{title:'Compras',url:'/purchases',icon:ClipboardList}] },
  { label: 'Administración', items: [{title:'Equipo / Usuarios',url:'/team',icon:Users},{title:'Sucursales',url:'/branches',icon:ClipboardList},{title:'Reportes',url:'/reports',icon:ClipboardList},{title:'Configuración',url:'/settings',icon:Settings}] },
  { label: 'Integraciones', items: [{title:'WhatsApp',url:'/whatsapp',icon:ClipboardList},{title:'IA',url:'/ai',icon:Wrench}] },
]
export function AppSidebar(props: React.ComponentProps<typeof Sidebar>) { return <Sidebar {...props}><SidebarHeader><SidebarMenu><SidebarMenuItem><SidebarMenuButton size="lg" asChild><Link to="/dashboard"><div className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground"><Wrench className="size-5"/></div><div className="grid flex-1 text-left text-sm leading-tight"><span className="truncate font-semibold">Fixi</span><span className="truncate text-xs">Taller en control</span></div></Link></SidebarMenuButton></SidebarMenuItem></SidebarMenu></SidebarHeader><SidebarContent>{groups.map(g=><NavMain key={g.label} label={g.label} items={g.items}/>)}</SidebarContent><SidebarFooter><NavUser user={{name:'Usuario Fixi',email:'Sesión activa',avatar:''}}/></SidebarFooter></Sidebar> }
