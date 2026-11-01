"use client"

import * as React from "react"
import { OrderWorkspaceNavigation } from "@/fixi/order-navigation"
import { FixiNavigationProvider } from "@/fixi/navigation-context"
import { AppSidebar } from "@/components/app-sidebar"
import { SiteHeader } from "@/components/site-header"
import { SessionIdleLock } from "@/components/session-idle-lock"
import {
  SidebarInset,
  SidebarProvider,
} from "@/components/ui/sidebar"

interface BaseLayoutProps {
  children: React.ReactNode
  title?: string
  description?: string
}

export function BaseLayout({ children, title, description }: BaseLayoutProps) {
  return (
    <FixiNavigationProvider>
    <SidebarProvider
      className="fixi-workspace"
      style={
        {
          "--sidebar-width": "18rem",
          "--sidebar-width-icon": "3.25rem",
          "--header-height": "4.5rem",
        } as React.CSSProperties
      }
    >
      <AppSidebar variant="inset" collapsible="icon" side="left" />
      <SessionIdleLock />
      <SidebarInset className="border-border/60 bg-background/50">
        <SiteHeader />
        <div className="fixi-main flex flex-1 flex-col">
          <div className="@container/main flex flex-1 flex-col gap-2">
            <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
              {title && (
                <div className="px-4 lg:px-6">
                  <div className="fixi-page-heading flex flex-col gap-2 pb-2">
                    <h1 className="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
                      {title}
                    </h1>
                    {description && (
                      <p className="text-sm text-muted-foreground font-normal">
                        {description}
                      </p>
                    )}
                  </div>
                </div>
              )}
              <OrderWorkspaceNavigation />
              {children}
            </div>
          </div>
        </div>
      </SidebarInset>
    </SidebarProvider>
    </FixiNavigationProvider>
  )
}
