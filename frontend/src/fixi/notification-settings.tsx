import { useEffect, useState } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import * as api from './api'

const labels: Record<string, string> = {
  ORDER_ASSIGNED: 'Orden asignada',
  STOCK_LOW: 'Existencia baja',
  DELIVERY_DUE: 'Vencimiento de fecha compromiso',
  PAYMENT_DUE: 'Pago pendiente',
  ORDER_READY: 'Orden lista para entregar',
  DAY_SUMMARY: 'Resumen de cierre del día',
}
const channels: Array<'EMAIL' | 'WEB' | 'APP'> = ['EMAIL', 'WEB', 'APP']
const channelLabels = { EMAIL: 'Correo', WEB: 'Web', APP: 'App' }

export function NotificationSettingsPage() {
  const [preferences, setPreferences] = useState<api.NotificationPreferences>({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  useEffect(() => {
    let active = true
    api.getNotificationPreferences().then(value => { if (active) setPreferences(value) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar las preferencias') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])
  async function save() {
    setSaving(true); setError(''); setMessage('')
    try { setPreferences(await api.updateNotificationPreferences(preferences)); setMessage('Preferencias de notificación guardadas') }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron guardar las preferencias') }
    finally { setSaving(false) }
  }
  return <BaseLayout title="Notificaciones" description="Elige qué eventos operativos deben generar avisos por canal."><div className="space-y-5 px-4 lg:px-6">
    <Card><CardHeader><CardTitle>Canales por evento</CardTitle><CardDescription>Las preferencias se guardan por negocio. WEB controla la bandeja de alertas operativas; el despacho de correo y notificaciones móviles aún requiere conectar sus canales.</CardDescription></CardHeader><CardContent><div className="overflow-x-auto"><table className="w-full min-w-[560px] text-sm"><thead><tr className="border-b text-left"><th className="p-3">Evento</th>{channels.map(channel => <th className="p-3" key={channel}>{channelLabels[channel]}</th>)}</tr></thead><tbody>{Object.entries(labels).map(([event, label]) => <tr className="border-b" key={event}><th className="p-3 text-left font-medium">{label}</th>{channels.map(channel => <td className="p-3" key={channel}><input aria-label={`${label} · ${channelLabels[channel]}`} type="checkbox" disabled={loading || saving} checked={preferences[event]?.[channel] ?? false} onChange={change => setPreferences(current => ({ ...current, [event]: { ...current[event], [channel]: change.target.checked } }))}/></td>)}</tr>)}</tbody></table></div><div className="mt-4 flex flex-wrap items-center gap-3"><Button disabled={loading || saving} onClick={() => void save()}>{saving ? 'Guardando…' : 'Guardar preferencias'}</Button>{loading && <span role="status" className="text-sm text-muted-foreground">Cargando…</span>}{message && <span role="status" className="text-sm text-emerald-700">{message}</span>}{error && <span role="alert" className="text-sm text-destructive">{error}</span>}</div></CardContent></Card>
  </div></BaseLayout>
}
