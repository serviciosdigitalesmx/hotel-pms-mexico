import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import * as api from './api'

type NavigationSession = { user: api.SessionUser | null; capabilities?: ReadonlySet<string> }
const NavigationContext = createContext<NavigationSession>({ user: null })

export function FixiNavigationProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<NavigationSession>({ user: null })
  useEffect(() => {
    let active = true
    void api.currentUser().then(async user => {
      if (!active) return
      if (!user || user.platformRole) { setSession({ user, capabilities: new Set() }); return }
      try {
        const result = await api.effectiveCapabilities(user.branchId)
        if (active) setSession({ user, capabilities: new Set(result.capabilities) })
      } catch { if (active) setSession({ user, capabilities: new Set() }) }
    }).catch(() => { if (active) setSession({ user: null, capabilities: new Set() }) })
    return () => { active = false }
  }, [])
  return <NavigationContext.Provider value={session}>{children}</NavigationContext.Provider>
}

export const useFixiNavigation = () => useContext(NavigationContext)
