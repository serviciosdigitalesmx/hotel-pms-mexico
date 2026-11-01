"use client"

import { useState } from 'react'
import { ChevronRight, type LucideIcon } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible'
import { SidebarGroup, SidebarGroupLabel, SidebarMenu, SidebarMenuButton, SidebarMenuItem, SidebarMenuSub, SidebarMenuSubButton, SidebarMenuSubItem, useSidebar } from '@/components/ui/sidebar'

type Item = { title: string; url: string; icon?: LucideIcon; isActive?: boolean; items?: { title: string; url: string; isActive?: boolean }[] }
const matchesRoute = (path: string, url: string) => {
  const aliases: Record<string, string> = { '/parts': '/inventory', '/technicians': '/team' }
  const current = aliases[path] ?? path
  return current === url || (url !== '/' && url !== '/settings' && current.startsWith(url + '/'))
}

export function NavMain({ label, items }: { label: string; items: Item[] }) {
  const { pathname } = useLocation()
  const { state, setOpen } = useSidebar()
  const activeParent = items.find(item => item.items?.some(child => matchesRoute(pathname, child.url)))?.title ?? null
  const [selection, setSelection] = useState<{ path: string; title: string | null }>({ path: pathname, title: activeParent })
  const expanded = selection.path === pathname ? selection.title : activeParent
  return <SidebarGroup className="fixi-nav-group">
    <SidebarGroupLabel className="fixi-nav-label">{label}</SidebarGroupLabel>
    <SidebarMenu className="gap-1.5">
      {items.map(item => {
        const activeChild = item.items?.filter(child => matchesRoute(pathname, child.url)).sort((a,b) => b.url.length-a.url.length)[0]?.url
        const active = item.items?.length ? Boolean(activeChild) : matchesRoute(pathname, item.url)
        if (!item.items?.length) return <SidebarMenuItem key={item.title}>
          <SidebarMenuButton asChild aria-label={item.title} tooltip={item.title} isActive={active} className="fixi-nav-item">
            <Link to={item.url}>{item.icon && <item.icon aria-hidden="true" />}<span>{item.title}</span></Link>
          </SidebarMenuButton>
        </SidebarMenuItem>
        return <Collapsible key={item.title} asChild open={expanded === item.title} onOpenChange={open => setSelection({ path: pathname, title: open ? item.title : null })}>
          <SidebarMenuItem>
            <CollapsibleTrigger asChild>
              <SidebarMenuButton aria-label={item.title} tooltip={item.title} isActive={active} className="fixi-nav-item" onClick={() => { if (state === 'collapsed') setOpen(true) }}>
                {item.icon && <item.icon aria-hidden="true" />}<span>{item.title}</span>
                <ChevronRight aria-hidden="true" className={`ml-auto size-4 transition-transform ${expanded === item.title ? 'rotate-90' : ''}`} />
              </SidebarMenuButton>
            </CollapsibleTrigger>
            <CollapsibleContent>
              <SidebarMenuSub className="fixi-nav-submenu">
                {item.items.map(child => <SidebarMenuSubItem key={child.url}>
                  <SidebarMenuSubButton asChild isActive={activeChild === child.url} className="fixi-nav-subitem">
                    <Link to={child.url}><span>{child.title}</span></Link>
                  </SidebarMenuSubButton>
                </SidebarMenuSubItem>)}
              </SidebarMenuSub>
            </CollapsibleContent>
          </SidebarMenuItem>
        </Collapsible>
      })}
    </SidebarMenu>
  </SidebarGroup>
}
