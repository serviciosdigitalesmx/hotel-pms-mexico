"use client"

import { ChevronRight, type LucideIcon } from "lucide-react"
import { Link, useLocation } from "react-router-dom"
import { cn } from "@/lib/utils"

import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible"
import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
} from "@/components/ui/sidebar"

export function NavMain({
  label,
  items,
}: {
  label: string
  items: {
    title: string
    url: string
    icon?: LucideIcon
    isActive?: boolean
    items?: {
      title: string
      url: string
      isActive?: boolean
    }[]
  }[]
}) {
  const location = useLocation()

  // Check if any subitem is active to determine if parent should be open
  const shouldBeOpen = (item: typeof items[0]) => {
    if (item.isActive) return true
    return item.items?.some(subItem => location.pathname === subItem.url) || false
  }

  return (
    <SidebarGroup className="py-1">
      <SidebarGroupLabel className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground/60 px-2.5 py-1 select-none">
        {label}
      </SidebarGroupLabel>
      <SidebarMenu className="gap-0.5">
        {items.map((item) => {
          const isItemActive = location.pathname === item.url || (item.url !== '/' && item.url !== '/dashboard' && location.pathname.startsWith(item.url + '/'))
          return (
            <Collapsible
              key={item.title}
              asChild
              defaultOpen={shouldBeOpen(item)}
              className="group/collapsible"
            >
              <SidebarMenuItem>
                {item.items?.length ? (
                  <>
                    <CollapsibleTrigger asChild>
                      <SidebarMenuButton 
                        tooltip={item.title} 
                        className="cursor-pointer text-sm rounded-lg transition-colors hover:bg-sidebar-accent/80"
                      >
                        {item.icon && <item.icon className="size-4 shrink-0 text-muted-foreground group-hover:text-foreground" />}
                        <span className="truncate font-medium">{item.title}</span>
                        <ChevronRight className="ml-auto size-3.5 text-muted-foreground/70 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90" />
                      </SidebarMenuButton>
                    </CollapsibleTrigger>
                    <CollapsibleContent>
                      <SidebarMenuSub className="my-0.5 ml-3 border-l border-sidebar-border/70 pl-2">
                        {item.items?.map((subItem) => (
                          <SidebarMenuSubItem key={subItem.title}>
                            <SidebarMenuSubButton 
                              asChild 
                              className="cursor-pointer text-sm rounded-md transition-colors data-[active=true]:bg-primary/10 data-[active=true]:text-primary data-[active=true]:font-medium" 
                              isActive={location.pathname === subItem.url}
                            >
                              <Link 
                                to={subItem.url}
                                target={(item.title === "Auth Pages" || item.title === "Errors") ? "_blank" : undefined}
                                rel={(item.title === "Auth Pages" || item.title === "Errors") ? "noopener noreferrer" : undefined}
                              >
                                <span>{subItem.title}</span>
                              </Link>
                            </SidebarMenuSubButton>
                          </SidebarMenuSubItem>
                        ))}
                      </SidebarMenuSub>
                    </CollapsibleContent>
                  </>
                ) : (
                  <SidebarMenuButton 
                    asChild 
                    tooltip={item.title} 
                    className="cursor-pointer text-sm rounded-lg transition-colors data-[active=true]:bg-primary/10 data-[active=true]:text-primary data-[active=true]:font-semibold hover:bg-sidebar-accent/80" 
                    isActive={isItemActive}
                  >
                    <Link to={item.url} className="flex items-center gap-2.5">
                      {item.icon && <item.icon className={cn("size-4 shrink-0 transition-colors", isItemActive ? "text-primary" : "text-muted-foreground")} />}
                      <span className="truncate">{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                )}
              </SidebarMenuItem>
            </Collapsible>
          )
        })}
      </SidebarMenu>
    </SidebarGroup>
  )
}
