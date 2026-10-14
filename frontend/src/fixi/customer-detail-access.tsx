import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const money = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })
type Draft = { firstName: string; lastName: string; email: string; phone: string; identityNumber: string; address: string; rfc: string; fiscalName: string; fiscalPostalCode: string; fiscalRegime: string; cfdiUse: string }
const emptyDraft: Draft = { firstName: '', lastName: '', email: '', phone: '', identityNumber: '', address: '', rfc: '', fiscalName: '', fiscalPostalCode: '', fiscalRegime: '', cfdiUse: '' }

export function CustomerDetailAccessPage() {
  const { id = '' } = useParams()
  const [customer, setCustomer] = useState<api.Customer>()
  const [devices, setDevices] = useState<api.Device[]>([])
  const [orders, setOrders] = useState<api.ServiceOrder[]>([])
  const [warranties, setWarranties] = useState<api.WarrantyClaim[]>([])
  const [draft, setDraft] = useState<Draft>(emptyDraft)
  const [canEdit, setCanEdit] = useState(false)
  const [permissionLoading, setPermissionLoading] = useState(true)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [editing, setEditing] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [message, setMessage] = useState('')
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    let active = true
    setLoading(true)
    setCustomer(undefined); setDevices([]); setOrders([]); setWarranties([]); setEditing(false)
    Promise.allSettled([
      api.getCustomer(id),
      api.listDevices(id),
      api.customerServiceOrders(id),
      api.listWarrantyClaims(id),
    ]).then(([customerResult, devicesResult, ordersResult, warrantiesResult]) => {
      if (!active) return
      const nextErrors: Record<string, string> = {}
      if (customerResult.status === 'fulfilled') {
        setCustomer(customerResult.value)
        setDraft({
          firstName: customerResult.value.firstName,
          lastName: customerResult.value.lastName,
          email: customerResult.value.email ?? '',
          phone: customerResult.value.phone ?? '',
          identityNumber: customerResult.value.identityNumber ?? '',
          address: customerResult.value.address ?? '',
          rfc: customerResult.value.rfc ?? '',
          fiscalName: customerResult.value.fiscalName ?? '',
          fiscalPostalCode: customerResult.value.fiscalPostalCode ?? '',
          fiscalRegime: customerResult.value.fiscalRegime ?? '',
          cfdiUse: customerResult.value.cfdiUse ?? '',
        })
      } else nextErrors.customer = customerResult.reason instanceof Error ? customerResult.reason.message : 'No se pudo cargar el cliente'
      if (devicesResult.status === 'fulfilled') setDevices(devicesResult.value)
      else nextErrors.devices = devicesResult.reason instanceof Error ? devicesResult.reason.message : 'No se pudieron cargar los equipos'
      if (ordersResult.status === 'fulfilled') setOrders(ordersResult.value)
      else nextErrors.orders = ordersResult.reason instanceof Error ? ordersResult.reason.message : 'No se pudo cargar el historial de servicio'
      if (warrantiesResult.status === 'fulfilled') setWarranties(warrantiesResult.value)
      else nextErrors.warranties = warrantiesResult.reason instanceof Error ? warrantiesResult.reason.message : 'No se pudieron cargar las garantías'
      setErrors(nextErrors)
    }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id, revision])

  useEffect(() => {
    let active = true
    api.effectiveCapabilities()
      .then(result => { if (active) setCanEdit(result.capabilities.includes('CUSTOMERS_WRITE')) })
      .catch(reason => { if (active) setErrors(current => ({ ...current, permissions: reason instanceof Error ? reason.message : 'No se pudieron consultar los permisos' })) })
      .finally(() => { if (active) setPermissionLoading(false) })
    return () => { active = false }
  }, [])

  async function save(event: FormEvent) {
    event.preventDefault()
    if (!canEdit || !customer) return
    setSaving(true); setErrors(current => ({ ...current, save: '' })); setMessage('')
    try {
      const updated = await api.updateCustomer(id, {
        ...customer,
        ...draft,
        billingEmail: draft.email.trim() || null,
        identityNumber: draft.identityNumber.trim() || null,
        address: draft.address.trim() || null,
      })
      setCustomer(updated)
      setEditing(false)
      setMessage('Datos del cliente actualizados')
    } catch (reason) {
      setErrors(current => ({ ...current, save: reason instanceof Error ? reason.message : 'No se pudieron guardar los cambios' }))
    } finally { setSaving(false) }
  }

  const title = customer ? `${customer.firstName} ${customer.lastName}` : 'Cliente'
  return <BaseLayout title={title} description="Contacto, equipos, reparaciones y garantías en un solo historial.">
    <div className="space-y-5 px-4 lg:px-6">
      {loading && <p role="status" className="text-sm text-muted-foreground">Cargando ficha de cliente…</p>}
      {errors.customer && <div className="flex items-center justify-between gap-3"><p role="alert" className="text-sm text-destructive">No se pudo cargar el cliente: {errors.customer}</p><Button type="button" variant="outline" onClick={() => setRevision(value => value + 1)}>Reintentar</Button></div>}
      {customer && <Card><CardHeader><div className="flex flex-wrap items-center justify-between gap-3"><CardTitle>Contacto y facturación</CardTitle>{permissionLoading ? <span role="status" className="text-xs text-muted-foreground">Consultando permisos…</span> : canEdit && !editing && <Button type="button" variant="outline" onClick={() => { setErrors(current => ({ ...current, save: '' })); setEditing(true) }}>Editar datos</Button>}</div></CardHeader><CardContent>
        {editing && canEdit ? <form onSubmit={save} className="grid gap-3 md:grid-cols-3">
          <Input required maxLength={100} aria-label="Nombre" value={draft.firstName} onChange={event => setDraft({ ...draft, firstName: event.target.value })}/>
          <Input required maxLength={100} aria-label="Apellido" value={draft.lastName} onChange={event => setDraft({ ...draft, lastName: event.target.value })}/>
          <Input type="email" maxLength={254} aria-label="Correo" value={draft.email} onChange={event => setDraft({ ...draft, email: event.target.value })}/>
          <Input type="tel" maxLength={20} aria-label="Teléfono" value={draft.phone} onChange={event => setDraft({ ...draft, phone: event.target.value })}/>
          <Input aria-label="Identificación" maxLength={100} pattern="[A-Za-z0-9-]*" value={draft.identityNumber} onChange={event => setDraft({ ...draft, identityNumber: event.target.value })}/>
          <Input aria-label="Dirección" maxLength={255} value={draft.address} onChange={event => setDraft({ ...draft, address: event.target.value })}/>
          <Input aria-label="RFC" maxLength={13} value={draft.rfc} onChange={event => setDraft({ ...draft, rfc: event.target.value.toUpperCase() })}/>
          <Input aria-label="Razón social" maxLength={200} value={draft.fiscalName} onChange={event => setDraft({ ...draft, fiscalName: event.target.value })}/>
          <Input aria-label="CP fiscal" inputMode="numeric" maxLength={5} value={draft.fiscalPostalCode} onChange={event => setDraft({ ...draft, fiscalPostalCode: event.target.value })}/>
          <Input aria-label="Régimen fiscal" maxLength={3} value={draft.fiscalRegime} onChange={event => setDraft({ ...draft, fiscalRegime: event.target.value })}/>
          <Input aria-label="Uso CFDI" maxLength={4} value={draft.cfdiUse} onChange={event => setDraft({ ...draft, cfdiUse: event.target.value.toUpperCase() })}/>
          <div className="flex gap-2"><Button disabled={saving}>{saving ? 'Guardando…' : 'Guardar cambios'}</Button><Button type="button" variant="outline" disabled={saving} onClick={() => setEditing(false)}>Cancelar</Button></div>
        </form> : <div className="space-y-1 text-sm"><p>{customer.phone || 'Sin teléfono'} · {customer.email || 'Sin correo'}</p><p>{customer.identityNumber ? `Identificación ${customer.identityNumber}` : 'Sin identificación'} · {customer.address || 'Sin dirección'}</p><p>{customer.fiscalName || 'Sin razón social'}{customer.rfc ? ` · RFC ${customer.rfc}` : ''}</p>{customer.fiscalPostalCode && <p>CP fiscal {customer.fiscalPostalCode} · Régimen {customer.fiscalRegime || '—'} · CFDI {customer.cfdiUse || '—'}</p>}</div>}
        {message && <p role="status" className="mt-3 text-sm text-emerald-700">{message}</p>}{errors.save && <p role="alert" className="mt-3 text-sm text-destructive">{errors.save}</p>}{errors.permissions && <p role="alert" className="mt-3 text-xs text-muted-foreground">No se habilitó la edición porque no se pudieron consultar permisos.</p>}
      </CardContent></Card>}

      <div className="grid gap-4 lg:grid-cols-2">
        <Card><CardHeader><CardTitle>Equipos ({devices.length})</CardTitle></CardHeader><CardContent className="space-y-2">
          {errors.devices && <p role="alert" className="text-sm text-destructive">No se pudieron cargar los equipos: {errors.devices}</p>}
          {!errors.devices && devices.map(device => <div key={device.id} className="rounded border p-3"><p className="font-medium">{device.manufacturer || 'Equipo'} {device.model || device.category}</p><p className="text-xs text-muted-foreground">Serie {device.serialNumber || '—'} · IMEI {device.imei || '—'}</p>{device.customFields && <p className="whitespace-pre-wrap text-sm">{device.customFields}</p>}</div>)}
          {!loading && !errors.devices && !devices.length && <p className="text-sm text-muted-foreground">No hay equipos registrados.</p>}
        </CardContent></Card>
        <Card><CardHeader><CardTitle>Garantías ({warranties.length})</CardTitle></CardHeader><CardContent className="space-y-2">
          {errors.warranties && <p role="alert" className="text-sm text-destructive">No se pudieron cargar las garantías: {errors.warranties}</p>}
          {!errors.warranties && warranties.map(warranty => <div key={warranty.id} className="flex flex-wrap items-center justify-between gap-2 rounded border p-3"><span>{warranty.description} · vence {warranty.expiresAt}</span><Badge>{warranty.status}</Badge></div>)}
          {!loading && !errors.warranties && !warranties.length && <p className="text-sm text-muted-foreground">No hay garantías registradas.</p>}
        </CardContent></Card>
      </div>

      <Card><CardHeader><div className="flex flex-wrap items-center justify-between gap-3"><CardTitle>Historial de servicio ({orders.length})</CardTitle>{errors.orders && <Button type="button" size="sm" variant="outline" onClick={() => setRevision(value => value + 1)}>Reintentar</Button>}</div></CardHeader><CardContent className="space-y-2">
        {errors.orders && <p role="alert" className="text-sm text-destructive">No se pudo cargar el historial: {errors.orders}</p>}
        {!errors.orders && orders.map(order => <Link to={`/service-orders/${order.id}`} className="flex flex-wrap items-center justify-between gap-3 rounded border p-3 hover:bg-muted/50" key={order.id}>
          <span><span className="block font-medium">{order.summary}</span><span className="block text-xs text-muted-foreground">{order.id} · {order.createdAt ? new Date(order.createdAt).toLocaleDateString('es-MX') : 'Fecha sin registrar'} · Prioridad {order.priority ?? 'normal'}{order.promisedAt ? ` · Compromiso ${new Date(order.promisedAt).toLocaleDateString('es-MX')}` : ''}</span>{order.quoteTotal != null && <span className="block text-xs text-muted-foreground">Total {money.format(Number(order.quoteTotal))} · recibido {money.format(api.serviceOrderAmountReceived(order))} · saldo {money.format(api.serviceOrderBalance(order))}</span>}</span><Badge>{order.status}</Badge>
        </Link>)}
        {!loading && !errors.orders && !orders.length && <p className="text-sm text-muted-foreground">Este cliente aún no tiene órdenes de servicio.</p>}
      </CardContent></Card>
    </div>
  </BaseLayout>
}
