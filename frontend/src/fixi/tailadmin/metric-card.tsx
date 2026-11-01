// Adapted from TailAdmin's MetricCard, MIT (c) 2023 TailAdmin.
// Source: df1e9f37e56e0bf83030e8063f5353da2e0ecf18. See public/licenses/TailAdmin-MIT.txt.
import type { ReactNode } from 'react'

export function MetricCard({ title, value, icon, priority = false }: { title: ReactNode; value: ReactNode; icon: ReactNode; priority?: boolean }) {
  return <article className={`fixi-metric-card ${priority ? 'fixi-metric-priority' : ''}`}>
    <div className="fixi-metric-top"><span className="fixi-metric-icon" aria-hidden="true">{icon}</span><span className="fixi-metric-caption">{title}</span></div>
    <div className="fixi-metric-value">{value}</div>
  </article>
}
