import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import * as api from './api'

const field = 'w-full rounded-md border bg-background px-3 py-2 text-sm'
const customerRows = (result: { content: api.Customer[] } | api.Customer[]) => Array.isArray(result) ? result : result.content

export function ReceptionOrderForm() {
  const navigate = useNavigate()
  const [customers, setCustomers] = useState<api.Customer[]>([])
  const [customerSearch, setCustomerSearch] = useState('')
  const [customerLoading, setCustomerLoading] = useState(false)
  const [deviceSearch, setDeviceSearch] = useState('')
  const [deviceResults, setDeviceResults] = useState<api.Device[]>([])
  const [deviceSearchLoading, setDeviceSearchLoading] = useState(false)
  const [branches, setBranches] = useState<api.Branch[]>([])
  const [devices, setDevices] = useState<api.Device[]>([])
  const [services, setServices] = useState<api.ServiceCatalogEntry[]>([])
  const [users, setUsers] = useState<api.TeamUser[]>([])
  const [checklistTemplate, setChecklistTemplate] = useState<string[]>([])
  const [checkedChecklist, setCheckedChecklist] = useState<string[]>([])
  const [customerId, setCustomerId] = useState('')
  const [deviceId, setDeviceId] = useState('')
  const [branchId, setBranchId] = useState('')
  const [serviceName, setServiceName] = useState('')
  const [assignedUserId, setAssignedUserId] = useState('')
  const [priority, setPriority] = useState<'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'>('NORMAL')
  const [depositAmount, setDepositAmount] = useState('0')
  const [form, setForm] = useState({ summary: '', reportedFailure: '', intakeChecklist: '', promisedAt: '', intakeEvidence: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    let active = true
    Promise.all([api.listBranches(), api.listServices(), api.listUsers().catch(() => []), api.industryProfile().catch(() => null)])
      .then(([branchRows, serviceRows, teamRows, profile]) => {
        if (!active) return
        setBranches(branchRows)
        setServices(serviceRows)
        setUsers(teamRows.filter(user => user.active && user.role === 'TECHNICIAN'))
        if (profile) {
          try {
            const templates = JSON.parse(profile.catalogsTemplatesJson)
            const items = templates?.workshop?.intakeChecklist
            if (Array.isArray(items)) setChecklistTemplate(items.filter((item: unknown): item is string => typeof item === 'string'))
          } catch { /* Optional malformed templates do not prevent manual intake. */ }
        }
      })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los datos de recepción') })
    return () => { active = false }
  }, [])

  useEffect(() => {
    let active = true
    const query = customerSearch.trim()
    if (query.length < 2) {
      setCustomers([])
      setCustomerLoading(false)
      return () => { active = false }
    }
    setCustomers([])
    setCustomerLoading(true)
    const timer = window.setTimeout(() => {
      api.searchCustomers(query).then(result => {
        if (active) setCustomers(customerRows(result))
      }).catch(reason => {
        if (active) setError(reason instanceof Error ? reason.message : 'No se pudo buscar al cliente')
      }).finally(() => { if (active) setCustomerLoading(false) })
    }, 250)
    return () => { active = false; window.clearTimeout(timer) }
  }, [customerSearch])

  useEffect(() => {
    let active = true
    const query = deviceSearch.trim()
    if (query.length < 2) { setDeviceResults([]); setDeviceSearchLoading(false); return () => { active = false } }
    setDeviceResults([]); setDeviceSearchLoading(true)
    const timer = window.setTimeout(() => {
      api.searchDevices(query).then(result => {
        if (active) setDeviceResults(Array.isArray(result) ? result : result.content)
      }).catch(reason => {
        if (active) setError(reason instanceof Error ? reason.message : 'No se pudo buscar el equipo')
      }).finally(() => { if (active) setDeviceSearchLoading(false) })
    }, 250)
    return () => { active = false; window.clearTimeout(timer) }
  }, [deviceSearch])

  async function selectLocatedDevice(device: api.Device) {
    setCustomerSearch(device.customerName ?? device.customerId)
    setCustomerId(device.customerId); setDeviceId(device.id); setDeviceSearch(''); setDeviceResults([])
    try {
      const customer = await api.getCustomer(device.customerId)
      setCustomers([customer]); setCustomerSearch(`${customer.firstName} ${customer.lastName}`)
    } catch { setError('Equipo localizado, pero no se pudieron cargar los datos del cliente') }
  }

  useEffect(() => {
    let active = true
    if (!customerId) { setDevices([]); setDeviceId(''); return }
    api.listDevices(customerId).then(rows => { if (active) setDevices(rows) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los equipos') })
    return () => { active = false }
  }, [customerId])

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (!customerId) { setError('Busca y selecciona un cliente antes de crear la orden'); return }
    const intakeChecklist = [...checkedChecklist, form.intakeChecklist.trim()].filter(Boolean).join('\n')
    if (!intakeChecklist) { setError('Completa al menos un punto del checklist o agrega una nota de recepción'); return }
    setBusy(true); setError(''); setMessage('')
    try {
      const order = await api.createServiceOrder({
        ...form, intakeChecklist, customerId, deviceId: deviceId || null, branchId: branchId || null,
        serviceName: serviceName || null, assignedUserId: assignedUserId || null, priority,
        depositAmount: Number(depositAmount),
        promisedAt: form.promisedAt ? new Date(form.promisedAt).toISOString() : null,
      })
      setMessage('Orden creada')
      navigate(`/service-orders/${order.id}`)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo crear la orden')
    } finally { setBusy(false) }
  }

  return <BaseLayout title="Recepción" description="Captura comercial y técnica completa de la orden.">
    <div className="px-4 lg:px-6"><Card><CardContent className="pt-6">
      <form onSubmit={submit} className="space-y-4">
        <div className="space-y-2 rounded border p-3"><Label htmlFor="reception-device-search">Localizar por IMEI o número de serie</Label><Input id="reception-device-search" type="search" placeholder="IMEI, serie, marca o modelo" value={deviceSearch} onChange={event => setDeviceSearch(event.target.value)}/>{deviceSearchLoading && <p role="status" className="text-sm text-muted-foreground">Buscando equipo…</p>}{deviceResults.map(device => <button type="button" key={device.id} className="block w-full rounded border p-2 text-left text-sm hover:bg-muted" onClick={() => void selectLocatedDevice(device)}><span className="font-medium">{device.manufacturer} {device.model}</span><span className="block text-muted-foreground">{device.serialNumber || device.imei || device.category} · {device.customerName || device.customerId}</span></button>)}</div>
        <div className="grid gap-2 md:grid-cols-2">
          <Input type="search" aria-label="Buscar cliente" placeholder="Buscar cliente por nombre, teléfono o correo" value={customerSearch} onChange={event => { setCustomerSearch(event.target.value); setCustomerId(''); setDeviceId('') }}/>
          <select required aria-label="Cliente encontrado" className={field} value={customerId} onChange={event => setCustomerId(event.target.value)}>
            <option value="">{customerLoading ? 'Buscando clientes…' : customers.length ? 'Selecciona cliente' : 'Escribe al menos 2 caracteres para buscar'}</option>
            {customers.map(customer => <option key={customer.id} value={customer.id}>{customer.firstName} {customer.lastName} · {customer.phone || customer.email || ''}</option>)}
          </select>
        </div>
        <select aria-label="Equipo" className={field} value={deviceId} onChange={event => setDeviceId(event.target.value)}>
          <option value="">Equipo sin registrar</option>
          {devices.map(device => <option key={device.id} value={device.id}>{device.manufacturer} {device.model} · {device.serialNumber || device.imei || device.category}</option>)}
        </select>
        <div className="grid gap-3 md:grid-cols-2">
          <select aria-label="Sucursal" className={field} value={branchId} onChange={event => setBranchId(event.target.value)}><option value="">Sucursal</option>{branches.map(branch => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select>
          <select aria-label="Servicio" className={field} value={serviceName} onChange={event => setServiceName(event.target.value)}><option value="">Servicio a cotizar</option>{services.filter(service => service.active).map(service => <option key={service.id} value={service.name}>{service.name}</option>)}</select>
          <select aria-label="Responsable técnico" className={field} value={assignedUserId} onChange={event => setAssignedUserId(event.target.value)}><option value="">Sin asignar</option>{users.map(user => <option key={user.id} value={user.id}>{user.username}</option>)}</select>
          <select aria-label="Prioridad" className={field} value={priority} onChange={event => setPriority(event.target.value as typeof priority)}><option value="LOW">Baja</option><option value="NORMAL">Normal</option><option value="HIGH">Alta</option><option value="URGENT">Urgente</option></select>
          <div><Label>Anticipo recibido</Label><Input required type="number" min="0" step="0.01" value={depositAmount} onChange={event => setDepositAmount(event.target.value)}/></div>
        </div>
        <textarea required aria-label="Falla reportada" className={field} placeholder="Falla reportada por el cliente" value={form.reportedFailure} onChange={event => setForm({ ...form, reportedFailure: event.target.value })}/>
        {checklistTemplate.length > 0 && <fieldset className="space-y-2 rounded border p-3"><legend className="px-1 text-sm font-medium">Checklist configurable de recepción</legend>{checklistTemplate.map(item => <label className="flex items-start gap-2 text-sm" key={item}><input type="checkbox" checked={checkedChecklist.includes(item)} onChange={event => setCheckedChecklist(current => event.target.checked ? [...current, item] : current.filter(value => value !== item))}/>{item}</label>)}</fieldset>}
        <textarea aria-label="Notas del checklist de recepción" className={field} placeholder="Notas de recepción o checklist adicional" value={form.intakeChecklist} onChange={event => setForm({ ...form, intakeChecklist: event.target.value })}/>
        <textarea required aria-label="Resumen de orden" className={field} placeholder="Resumen de síntomas y condición" value={form.summary} onChange={event => setForm({ ...form, summary: event.target.value })}/>
        <div className="grid gap-3 md:grid-cols-2"><div><Label>Fecha compromiso</Label><Input required type="datetime-local" value={form.promisedAt} onChange={event => setForm({ ...form, promisedAt: event.target.value })}/></div><div><Label>Evidencia inicial / imágenes (URL o referencia)</Label><Input value={form.intakeEvidence} onChange={event => setForm({ ...form, intakeEvidence: event.target.value })}/></div></div>
        <Button disabled={busy || customerLoading || !customerId}>{busy ? 'Creando…' : 'Crear orden'}</Button>
        {message && <p role="status" className="text-sm text-emerald-700">{message}</p>}
        {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
      </form>
    </CardContent></Card></div>
  </BaseLayout>
}
