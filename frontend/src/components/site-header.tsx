"use client"

import { useLocation, Link } from "react-router-dom"
import { useFixiNavigation } from "@/fixi/navigation-context"
import { Plus, Bell } from "lucide-react"
import { Separator } from "@/components/ui/separator"
import { SidebarTrigger } from "@/components/ui/sidebar"
import { ModeToggle } from "@/components/mode-toggle"
import { Button } from "@/components/ui/button"
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb"

const routeTitles: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/branch-dashboard": "Dashboard Sucursal",
  "/service-orders": "Órdenes de servicio",
  "/reception/new": "Nueva Recepción",
  "/customers": "Clientes",
  "/customers/import": "Importar Clientes",
  "/devices": "Equipos y Dispositivos",
  "/diagnostics": "Diagnósticos",
  "/repairs": "Reparaciones",
  "/tests": "Pruebas QA",
  "/delivery": "Entregas",
  "/parts": "Refacciones",
  "/inventory": "Inventario",
  "/suppliers": "Proveedores",
  "/purchases": "Compras",
  "/expenses": "Control de Gastos",
  "/reports/finance": "Finanzas",
  "/team": "Equipo & Usuarios",
  "/technicians": "Técnicos",
  "/branches": "Sucursales",
  "/calendar": "Agenda Operativa",
  "/settings": "Configuración del Taller",
  "/settings/overview": "Resumen de Ajustes",
  "/settings/account": "Mi Cuenta",
  "/settings/order-workflows": "Flujos de Órdenes",
  "/settings/order-preferences": "Preferencias de Órdenes",
  "/settings/notifications": "Notificaciones",
  "/notifications": "Alertas Operativas",
  "/whatsapp": "Integración WhatsApp",
  "/ai": "Asistente IA",
}

export function SiteHeader() {
  const { user, capabilities } = useFixiNavigation()
  const canCreate = capabilities?.has('ORDERS_CREATE') ?? false
  const canSeeAlerts = ['ORDERS_READ', 'INVENTORY_READ', 'FINANCE_READ'].some(key => capabilities?.has(key))
  const home = user?.platformRole ? '/platform' : capabilities?.has('REPORTS_READ') ? '/dashboard' : capabilities?.has('ORDERS_READ') ? '/service-orders' : '/settings/account'
  const location = useLocation()
  const pathname = location.pathname
  const currentTitle = routeTitles[pathname] ||
    (pathname.startsWith("/service-orders/") ? "Detalle de Orden" :
     pathname.startsWith("/customers/") ? "Detalle de Cliente" : "Fixi")

  return (
    <header className="sticky top-0 z-20 flex h-(--header-height) shrink-0 items-center gap-2 border-b border-border/60 bg-background/80 backdrop-blur-md transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-(--header-height)">
      <div className="flex w-full items-center gap-2 px-4 py-2.5 lg:px-6">
        <SidebarTrigger className="-ml-1 text-muted-foreground hover:text-foreground transition-colors" />
        <Separator
          orientation="vertical"
          className="mx-1 h-4 bg-border/60"
        />
        <Breadcrumb className="hidden sm:block">
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link to={home} className="text-xs font-medium text-muted-foreground hover:text-primary transition-colors">
                  Fixi
                </Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            {currentTitle !== "Fixi" && (
              <>
                <BreadcrumbSeparator className="text-muted-foreground/40" />
                <BreadcrumbItem>
                  <BreadcrumbPage className="text-xs font-semibold text-foreground">
                    {currentTitle}
                  </BreadcrumbPage>
                </BreadcrumbItem>
              </>
            )}
          </BreadcrumbList>
        </Breadcrumb>
        <div className="sm:hidden text-xs font-semibold text-foreground truncate max-w-44">
          {currentTitle}
        </div>

        <div className="ml-auto flex items-center gap-2">
          {canCreate && <Button
            size="sm"
            asChild
            className="hidden md:inline-flex h-8 gap-1.5 rounded-lg bg-primary text-primary-foreground font-medium text-xs shadow-xs hover:bg-primary/90 transition-all active:scale-[0.98]"
          >
            <Link to="/reception/new">
              <Plus className="size-3.5" />
              <span>Nueva Orden</span>
            </Link>
          </Button>}

          {canSeeAlerts && <Button
            variant="ghost"
            size="icon"
            asChild
            className="h-8 w-8 text-muted-foreground hover:text-foreground rounded-lg"
          >
            <Link to="/notifications" title="Alertas operativas">
              <Bell className="size-4" />
              <span className="sr-only">Alertas operativas</span>
            </Link>
          </Button>}

          {/* Theme switcher */}
          <ModeToggle variant="ghost" />
        </div>
      </div>
    </header>
  )
}
