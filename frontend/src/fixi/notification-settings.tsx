import { useEffect, useState } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import * as api from './api'

const labels: Record<string, string> = {
  ORDER_ASSIGNED: 'Orden nueva con técnico asignado',
  STOCK_LOW: 'Existencia baja',
  DELIVERY_DUE: 'Vencimiento de fecha compromiso',
  PAYMENT_DUE: 'Pago pendiente',
  ORDER_READY: 'Orden lista para control de calidad',
  DAY_SUMMARY: 'Resumen de cierre del día',
}
const channels: Array<'EMAIL' | 'WEB' | 'APP'> = ['EMAIL', 'WEB', 'APP']
const channelLabels = { EMAIL: 'Correo', WEB: 'Web', APP: 'App' }
const connectedEmailEvents = new Set(['ORDER_ASSIGNED', 'STOCK_LOW', 'DELIVERY_DUE', 'PAYMENT_DUE', 'ORDER_READY'])

export function NotificationSettingsPage() {
  const [preferences, setPreferences] = useState<api.NotificationPreferences>({})
  const [recipients, setRecipients] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  useEffect(() => {
    let active = true
    Promise.all([api.getNotificationPreferences(), api.getOperationalEmailRecipients()])
      .then(([value, emailRecipients]) => {
        if (!active) return
        setPreferences(value)
        setRecipients(Object.fromEntries(Object.entries(emailRecipients).map(([event, addresses]) => [event, addresses.join('\n')])))
      })
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
  async function saveRecipients() {
    setSaving(true); setError(''); setMessage('')
    try {
      const parsed = Object.fromEntries(Object.entries(recipients).map(([event, value]) => [event,
        [...new Set(value.split(/[;,\n]/).map(address => address.trim().toLowerCase()).filter(Boolean))],
      ]))
      const invalid = Object.entries(parsed).find(([, addresses]) =>
        addresses.length > 20 || addresses.some(address => !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(address)),
      )
      if (invalid) throw new Error(invalid[1].length > 20
        ? 'Máximo 20 destinatarios por evento'
        : `Revisa las direcciones de ${labels[invalid[0]] ?? invalid[0]}`)
      const updated = await api.updateOperationalEmailRecipients(parsed)
      setRecipients(Object.fromEntries(Object.entries(updated).map(([event, addresses]) => [event, addresses.join('\n')])))
      setMessage('Destinatarios de correo guardados')
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron guardar los destinatarios') }
    finally { setSaving(false) }
  }
  return <BaseLayout title="Notificaciones" description="Elige qué eventos operativos deben generar avisos por canal."><div className="space-y-5 px-4 lg:px-6">
    <Card><CardHeader><CardTitle>Canales por evento</CardTitle><CardDescription>Las preferencias se guardan por negocio. Web está conectado para todos los eventos; correo envía avisos de asignación, stock bajo, fecha compromiso vencida, saldo pendiente al entregar y órdenes listas para control de calidad. El cierre de día y las notificaciones móviles siguen pendientes.</CardDescription></CardHeader><CardContent><div className="overflow-x-auto"><table className="w-full min-w-[560px] text-sm"><thead><tr className="border-b text-left"><th className="p-3">Evento</th>{channels.map(channel => <th className="p-3" key={channel}>{channelLabels[channel]}{channel === 'APP' && <span className="ml-1 text-xs font-normal text-muted-foreground">(pendiente)</span>}</th>)}</tr></thead><tbody>{Object.entries(labels).map(([event, label]) => <tr className="border-b" key={event}><th className="p-3 text-left font-medium">{label}</th>{channels.map(channel => { const connected = channel === 'WEB' || (channel === 'EMAIL' && connectedEmailEvents.has(event)); return <td className="p-3" key={channel}><input aria-label={`${label} · ${channelLabels[channel]}`} type="checkbox" disabled={loading || saving || !connected} title={!connected ? 'Canal aún no conectado para este evento' : undefined} checked={preferences[event]?.[channel] ?? false} onChange={change => setPreferences(current => ({ ...current, [event]: { ...current[event], [channel]: change.target.checked } }))}/></td> })}</tr>)}</tbody></table></div><div className="mt-4 flex flex-wrap items-center gap-3"><Button disabled={loading || saving} onClick={() => void save()}>{saving ? 'Guardando…' : 'Guardar preferencias'}</Button>{loading && <span role="status" className="text-sm text-muted-foreground">Cargando…</span>}{message && <span role="status" className="text-sm text-emerald-700">{message}</span>}{error && <span role="alert" className="text-sm text-destructive">{error}</span>}</div></CardContent></Card>
    <Card><CardHeader><CardTitle>Destinatarios de correo por evento</CardTitle><CardDescription>Configura hasta 20 direcciones por evento, una por línea. El envío automático ya está conectado para asignación, stock bajo, vencimiento de fecha compromiso, saldo pendiente al entregar y control de calidad; las listas de cierre de día quedan preparadas para su productor.</CardDescription></CardHeader><CardContent className="space-y-3">{Object.entries(labels).map(([event, label]) => { const connected = connectedEmailEvents.has(event); return <label className="block space-y-1 text-sm font-medium" key={event}>{label}{!connected&&<span className="ml-2 text-xs font-normal text-muted-foreground">(guardado, envío pendiente)</span>}<textarea aria-label={`Destinatarios de correo · ${label}`} className="w-full rounded-md border bg-background px-3 py-2 font-normal" rows={2} maxLength={5200} placeholder="operaciones@negocio.com" disabled={loading || saving} value={recipients[event] ?? ''} onChange={change => setRecipients(current => ({ ...current, [event]: change.target.value }))}/></label>})}<Button disabled={loading || saving} onClick={() => void saveRecipients()}>{saving ? 'Guardando…' : 'Guardar destinatarios'}</Button></CardContent></Card>
  </div></BaseLayout>
}
