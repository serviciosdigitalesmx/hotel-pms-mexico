"use client"

import {
  EllipsisVertical,
  LogOut,
  CircleUser,
  Settings,
} from "lucide-react"
import { Link, useNavigate } from "react-router-dom"
import { useEffect, useState } from "react"
import { currentUser, logout } from "@/fixi/api"

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar"

export function NavUser({
  user,
}: {
  user: {
    name: string
    email: string
    avatar: string
  }
}) {
  const { isMobile } = useSidebar()
  const navigate = useNavigate()
  const [session, setSession] = useState(user)
  useEffect(() => {
    currentUser().then(value => value && setSession({
      name: value.username,
      email: `${value.role} · ${value.businessName ?? 'Fixi'}`,
      avatar: ''
    }))
  }, [])
  async function signOut() {
    await logout()
    navigate('/login')
  }

  const initials = (session.name || 'U').slice(0, 2).toUpperCase()

  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <SidebarMenuButton
              size="lg"
              className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground cursor-pointer rounded-lg hover:bg-sidebar-accent/80 transition-colors"
            >
              <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary font-bold text-xs ring-1 ring-primary/20">
                {initials}
              </div>
              <div className="grid flex-1 text-left text-sm leading-tight">
                <span className="truncate font-semibold text-foreground text-xs">{session.name}</span>
                <span className="text-muted-foreground truncate text-[11px]">
                  {session.email}
                </span>
              </div>
              <EllipsisVertical className="ml-auto size-4 text-muted-foreground" />
            </SidebarMenuButton>
          </DropdownMenuTrigger>
          <DropdownMenuContent
            className="w-(--radix-dropdown-menu-trigger-width) min-w-56 rounded-xl border-border/60 shadow-lg"
            side={isMobile ? "bottom" : "right"}
            align="end"
            sideOffset={4}
          >
            <DropdownMenuLabel className="p-0 font-normal">
              <div className="flex items-center gap-2.5 px-3 py-2 text-left text-sm">
                <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary font-bold text-xs ring-1 ring-primary/20">
                  {initials}
                </div>
                <div className="grid flex-1 text-left text-sm leading-tight">
                  <span className="truncate font-semibold text-foreground text-xs">{session.name}</span>
                  <span className="text-muted-foreground truncate text-[11px]">
                    {session.email}
                  </span>
                </div>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem asChild className="cursor-pointer gap-2 text-xs">
                <Link to="/settings/account">
                  <CircleUser className="size-3.5" />
                  Mi Cuenta
                </Link>
              </DropdownMenuItem>
              <DropdownMenuItem asChild className="cursor-pointer gap-2 text-xs">
                <Link to="/settings">
                  <Settings className="size-3.5" />
                  Configuración
                </Link>
              </DropdownMenuItem>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              className="cursor-pointer gap-2 text-xs text-destructive focus:bg-destructive/10 focus:text-destructive"
              onClick={signOut}
            >
              <LogOut className="size-3.5" />
              Cerrar sesión
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}
