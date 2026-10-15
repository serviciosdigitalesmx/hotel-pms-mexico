import { useEffect, useState } from 'react'

/** A stable clock value refreshed once per minute for due-date indicators. */
export function useCurrentTime() {
  const [now, setNow] = useState(0)
  useEffect(() => {
    const refresh = () => setNow(Date.now())
    refresh()
    const timer = window.setInterval(refresh, 60_000)
    return () => window.clearInterval(timer)
  }, [])
  return now
}
