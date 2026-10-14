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
const localDateTime = (value: string) => value ? `${value}:00` : null

export function ReceptionOrderForm() {
  const navigate = useNavigate()
  const [customers, setCustomers] = useState<api.Customer[]>([])
  const [customerSearch, setCustomerSearch] = useState('')
  const [customerLoading, setCustomerLoading] = useState(false)
  const [quickCreateCustomer, setQuickCreateCustomer] = useState(false)
  const [quickCustomer, setQuickCustomer] = useState({ firstName: '', lastName: '', email: '', phone: '' })
  const [customerSaving, setCustomerSaving] = useState(false)
  const [customerNotice, setCustomerNotice] = useState('')
  const [deviceSearch, setDeviceSearch] = useState('')
  const [deviceResults, setDeviceResults] = useState<api.Device[]>([])
  const [deviceSearchLoading, setDeviceSearchLoading] = useState(false)
  const [branches, setBranches] = useState<api.Branch[]>([])
  const [devices, setDevices] = useState<api.Device[]>([])
  const [quickCreateDevice, setQuickCreateDevice] = useState(false)
  const [quickDevice, setQuickDevice] = useState({ category: 'Teléfono', manufacturer: '', model: '', serialNumber: '', imei: '', customFields: '' })
  const [deviceSaving, setDeviceSaving] = useState(false)
  const [services, setServices] = useState<api.ServiceCatalogEntry[]>([])
  const [currency, setCurrency] = useState('MXN')
  const [users, setUsers] = useState<api.TeamUser[]>([])
  const [branchTechnicianIds, setBranchTechnicianIds] = useState<string[]>()
  const [branchTechniciansLoading, setBranchTechniciansLoading] = useState(false)
  const [capabilities, setCapabilities] = useState<Set<string>>(new Set())
  const [capabilitiesLoading, setCapabilitiesLoading] = useState(true)
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
  const selectedService = services.find(service => service.name === serviceName)
  const assignableTechnicians = branchId && branchTechnicianIds ? users.filter(user => branchTechnicianIds.includes(user.id)) : branchId ? [] : users

  useEffect(() => {
    let active = true
    api.effectiveCapabilities().then(result => { if (active) setCapabilities(new Set(result.capabilities)) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los permisos') })
      .finally(() => { if (active) setCapabilitiesLoading(false) })
    return () => { active = false }
  }, [])

  useEffect(() => {
    let active = true
    Promise.all([api.listAllCustomers(), api.listBranches(), api.listServices(), api.listUsers().catch(() => []), api.industryProfile().catch(() => null), api.getBusinessProfile().catch(() => null)])
      .then(([customerRows, branchRows, serviceRows, teamRows, profile, business]) => {
        if (!active) return
        setCustomers(customerRows)
        setBranches(branchRows)
        setServices(serviceRows)
        if (business?.currency) setCurrency(business.currency.toUpperCase())
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

  async function createCustomerQuickly() {
    const firstName = quickCustomer.firstName.trim()
    const lastName = quickCustomer.lastName.trim()
    const email = quickCustomer.email.trim()
    const phone = quickCustomer.phone.trim()
    if (!firstName || !lastName || (!email && !phone)) {
      setError('Captura nombre, apellido y al menos un correo o teléfono')
      return
    }
    setCustomerSaving(true); setError(''); setCustomerNotice('')
    try {
      const duplicate = await api.potentialCustomerDuplicate({ email, phone })
      if (duplicate) {
        setCustomers([duplicate]); setCustomerId(duplicate.id); setDeviceId('')
        setCustomerSearch(`${duplicate.firstName} ${duplicate.lastName}`)
        setQuickCreateCustomer(false)
        setCustomerNotice('Ya existía un cliente con esos datos; se seleccionó el registro existente.')
        return
      }
      const customer = await api.createCustomer({
        firstName, lastName, email: email || null, phone: phone || null,
        address: null, city: null, country: null, dateOfBirth: null,
        billingEmail: email || null, fiscalCode: null, vatNumber: null,
        companyName: null, sdiCode: null, pecEmail: null, cap: null, comune: null, provincia: null,
      })
      setCustomers([customer]); setCustomerId(customer.id); setCustomerSearch(`${customer.firstName} ${customer.lastName}`)
      setDeviceId(''); setQuickCustomer({ firstName: '', lastName: '', email: '', phone: '' })
      setQuickCreateCustomer(false); setCustomerNotice('Cliente creado y seleccionado para esta orden.')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo crear el cliente')
    } finally { setCustomerSaving(false) }
  }

  async function createDeviceQuickly() {
    if (!customerId) { setError('Selecciona un cliente antes de registrar su equipo'); return }
    setDeviceSaving(true); setError(''); setCustomerNotice('')
    try {
      const device = await api.createDevice(customerId, {
        ...quickDevice,
        category: quickDevice.category.trim(),
        manufacturer: quickDevice.manufacturer.trim() || null,
        model: quickDevice.model.trim() || null,
        serialNumber: quickDevice.serialNumber.trim() || null,
        imei: quickDevice.imei.trim() || null,
        customFields: quickDevice.customFields.trim() || null,
      })
      setDevices(current => [...current, device])
      setDeviceId(device.id)
      setQuickDevice({ category: 'Teléfono', manufacturer: '', model: '', serialNumber: '', imei: '', customFields: '' })
      setQuickCreateDevice(false)
      setCustomerNotice('Equipo registrado y asociado a esta orden.')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'No se pudo registrar el equipo')
    } finally { setDeviceSaving(false) }
  }

  useEffect(() => {
    let active = true
    if (!customerId) { setDevices([]); setDeviceId(''); return }
    api.listDevices(customerId).then(rows => { if (active) setDevices(rows) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los equipos') })
    return () => { active = false }
  }, [customerId])

  useEffect(() => {
    let active = true
    if (!branchId) { setBranchTechnicianIds(undefined); setBranchTechniciansLoading(false); return () => { active = false } }
    setBranchTechnicianIds(undefined); setBranchTechniciansLoading(true)
    api.listBranchMembers(branchId).then(ids => { if (active) setBranchTechnicianIds(ids) })
      .catch(reason => { if (active) { setBranchTechnicianIds([]); setError(reason instanceof Error ? reason.message : 'No se pudieron consultar los técnicos de la sucursal') } })
      .finally(() => { if (active) setBranchTechniciansLoading(false) })
    return () => { active = false }
  }, [branchId])

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (!customerId) { setError('Busca y selecciona un cliente antes de crear la orden'); return }
    const intakeChecklist = [...checkedChecklist, form.intakeChecklist.trim()].filter(Boolean).join('\n')
    if (!intakeChecklist) { setError('Completa al menos un punto del checklist o agrega una nota de recepción'); return }
    if (!/^\d{1,10}(?:\.\d{1,2})?$/.test(depositAmount.trim())) { setError('El anticipo debe ser un monto positivo o cero, con máximo dos decimales'); return }
    setBusy(true); setError(''); setMessage('')
    try {
      const order = await api.createServiceOrder({
        ...form, intakeChecklist, customerId, deviceId: deviceId || null, branchId: branchId || null,
        serviceName: serviceName || null, assignedUserId: assignedUserId || null, priority,
        depositAmount: Number(depositAmount),
        promisedAt: localDateTime(form.promisedAt),
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
          <Input type="search" aria-label="Buscar cliente" placeholder="Buscar cliente por nombre, teléfono o correo" value={customerSearch} onChange={event => { setCustomerSearch(event.target.value); setCustomerId(''); setDeviceId(''); setError(''); setCustomerNotice('') }}/>
          <select required aria-label="Cliente encontrado" className={field} value={customerId} onChange={event => {setCustomerId(event.target.value);setDeviceId('');setError('')}}>
            <option value="">{customerLoading ? 'Buscando clientes…' : customers.length ? 'Selecciona cliente' : 'Escribe al menos 2 caracteres para buscar'}</option>
            {customers.map(customer => <option key={customer.id} value={customer.id}>{customer.firstName} {customer.lastName} · {customer.phone || customer.email || ''}</option>)}
          </select>
        </div>
        <div className="space-y-2">
          {capabilitiesLoading&&<p role="status" className="text-xs text-muted-foreground">Consultando permisos de alta…</p>}{capabilities.has('CUSTOMERS_WRITE')&&<Button type="button" variant="outline" size="sm" disabled={customerSaving} onClick={() => { setQuickCreateCustomer(value => !value); setError('') }}>{quickCreateCustomer ? 'Cancelar alta rápida' : 'Crear cliente rápido'}</Button>}{!capabilitiesLoading&&!capabilities.has('CUSTOMERS_WRITE')&&<p className="text-xs text-muted-foreground">Para alta rápida de clientes se requiere CUSTOMERS_WRITE.</p>}
          {quickCreateCustomer && <div className="grid gap-2 rounded border p-3 sm:grid-cols-2">
            <Input required maxLength={100} aria-label="Nombre del nuevo cliente" placeholder="Nombre" value={quickCustomer.firstName} onChange={event => setQuickCustomer({ ...quickCustomer, firstName: event.target.value })} onKeyDown={event => { if (event.key === 'Enter') event.preventDefault() }}/>
            <Input required maxLength={100} aria-label="Apellido del nuevo cliente" placeholder="Apellido" value={quickCustomer.lastName} onChange={event => setQuickCustomer({ ...quickCustomer, lastName: event.target.value })} onKeyDown={event => { if (event.key === 'Enter') event.preventDefault() }}/>
            <Input type="email" maxLength={150} aria-label="Correo del nuevo cliente" placeholder="Correo (opcional si hay teléfono)" value={quickCustomer.email} onChange={event => setQuickCustomer({ ...quickCustomer, email: event.target.value })} onKeyDown={event => { if (event.key === 'Enter') event.preventDefault() }}/>
            <Input type="tel" maxLength={20} aria-label="Teléfono del nuevo cliente" placeholder="Teléfono (opcional si hay correo)" value={quickCustomer.phone} onChange={event => setQuickCustomer({ ...quickCustomer, phone: event.target.value })} onKeyDown={event => { if (event.key === 'Enter') event.preventDefault() }}/>
            <Button type="button" className="sm:col-span-2" disabled={customerSaving || !quickCustomer.firstName.trim() || !quickCustomer.lastName.trim() || (!quickCustomer.email.trim() && !quickCustomer.phone.trim())} onClick={() => void createCustomerQuickly()}>{customerSaving ? 'Guardando cliente…' : 'Guardar y seleccionar cliente'}</Button>
          </div>}
          {customerNotice && <p role="status" className="text-sm text-emerald-700">{customerNotice}</p>}
        </div>
        <select aria-label="Equipo" className={field} value={deviceId} onChange={event => setDeviceId(event.target.value)}>
          <option value="">Equipo sin registrar</option>
          {devices.map(device => <option key={device.id} value={device.id}>{device.manufacturer} {device.model} · {device.serialNumber || device.imei || device.category}</option>)}
        </select>
        <div className="space-y-2">
          {capabilities.has('DEVICES_WRITE')&&<Button type="button" size="sm" variant="outline" disabled={!customerId || deviceSaving} onClick={() => { setQuickCreateDevice(value => !value); setError('') }}>{quickCreateDevice ? 'Cancelar alta de equipo' : 'Registrar equipo para este cliente'}</Button>}{!capabilitiesLoading&&!capabilities.has('DEVICES_WRITE')&&<p className="text-xs text-muted-foreground">Para alta rápida de equipos se requiere DEVICES_WRITE.</p>}
          {quickCreateDevice && <div className="grid gap-2 rounded border p-3 sm:grid-cols-2 lg:grid-cols-3">
            <Input required maxLength={80} aria-label="Categoría del equipo" placeholder="Categoría" value={quickDevice.category} onChange={event => setQuickDevice({ ...quickDevice, category: event.target.value })}/>
            <Input maxLength={120} aria-label="Marca del equipo" placeholder="Marca" value={quickDevice.manufacturer} onChange={event => setQuickDevice({ ...quickDevice, manufacturer: event.target.value })}/>
            <Input maxLength={120} aria-label="Modelo del equipo" placeholder="Modelo" value={quickDevice.model} onChange={event => setQuickDevice({ ...quickDevice, model: event.target.value })}/>
            <Input maxLength={120} aria-label="Serie del equipo" placeholder="Número de serie" value={quickDevice.serialNumber} onChange={event => setQuickDevice({ ...quickDevice, serialNumber: event.target.value })}/>
            <Input maxLength={120} aria-label="IMEI del equipo" placeholder="IMEI" value={quickDevice.imei} onChange={event => setQuickDevice({ ...quickDevice, imei: event.target.value })}/>
            <textarea maxLength={1000} aria-label="Detalles adicionales del equipo" className={field} rows={3} placeholder="Detalles y condición del equipo (color, accesorios, daños visibles…)" value={quickDevice.customFields} onChange={event => setQuickDevice({ ...quickDevice, customFields: event.target.value })}/>
            <Button type="button" className="sm:col-span-2 lg:col-span-3" disabled={deviceSaving || !quickDevice.category.trim()} onClick={() => void createDeviceQuickly()}>{deviceSaving ? 'Registrando equipo…' : 'Registrar y seleccionar equipo'}</Button>
          </div>}
        </div>
        <div className="grid gap-3 md:grid-cols-2">
          <select aria-label="Sucursal" className={field} value={branchId} onChange={event => { setBranchId(event.target.value); setAssignedUserId('') }}><option value="">Sucursal</option>{branches.map(branch => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select>
          <select aria-label="Servicio" className={field} value={serviceName} onChange={event => setServiceName(event.target.value)}><option value="">Servicio a cotizar</option>{services.filter(service => service.active).map(service => { let price: string; try { price = new Intl.NumberFormat('es-MX', { style: 'currency', currency }).format(service.basePrice) } catch { price = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(service.basePrice) } return <option key={service.id} value={service.name}>{service.name} · {price} · {service.estimatedMinutes} min</option> })}</select>
          <label className="grid gap-1 text-sm text-muted-foreground">Responsable técnico<select aria-label="Responsable técnico" className={field} value={assignedUserId} onChange={event => setAssignedUserId(event.target.value)}><option value="">Sin asignar</option>{assignableTechnicians.map(user => <option key={user.id} value={user.id}>{user.username}</option>)}</select>{branchTechniciansLoading&&<span role="status" className="text-xs">Consultando técnicos asignados…</span>}{branchId&&!branchTechniciansLoading&&!assignableTechnicians.length&&<span className="text-xs">No hay técnicos asignados a esta sucursal; la orden puede quedar sin asignar.</span>}</label>
          <select aria-label="Prioridad" className={field} value={priority} onChange={event => setPriority(event.target.value as typeof priority)}><option value="LOW">Baja</option><option value="NORMAL">Normal</option><option value="HIGH">Alta</option><option value="URGENT">Urgente</option></select>
          <div><Label>Anticipo recibido</Label><Input required type="number" min="0" max="9999999999.99" step="0.01" inputMode="decimal" value={depositAmount} onChange={event => setDepositAmount(event.target.value)}/>{selectedService&&Number(depositAmount)>selectedService.basePrice&&<p role="note" className="mt-1 text-xs text-amber-700">El anticipo excede el precio base de {selectedService.name}; verifica que cubra cargos o piezas adicionales.</p>}</div>
        </div>
        <textarea required aria-label="Falla reportada" className={field} placeholder="Falla reportada por el cliente" value={form.reportedFailure} onChange={event => setForm({ ...form, reportedFailure: event.target.value })}/>
        {checklistTemplate.length > 0 && <fieldset className="space-y-2 rounded border p-3"><legend className="px-1 text-sm font-medium">Checklist configurable de recepción</legend>{checklistTemplate.map(item => <label className="flex items-start gap-2 text-sm" key={item}><input type="checkbox" checked={checkedChecklist.includes(item)} onChange={event => setCheckedChecklist(current => event.target.checked ? [...current, item] : current.filter(value => value !== item))}/>{item}</label>)}</fieldset>}
        <textarea aria-label="Notas del checklist de recepción" className={field} placeholder="Notas de recepción o checklist adicional" value={form.intakeChecklist} onChange={event => setForm({ ...form, intakeChecklist: event.target.value })}/>
        <textarea required aria-label="Resumen de orden" className={field} placeholder="Resumen de síntomas y condición" value={form.summary} onChange={event => setForm({ ...form, summary: event.target.value })}/>
        <div className="grid gap-3 md:grid-cols-2"><div><Label>Fecha compromiso (opcional)</Label><Input type="datetime-local" value={form.promisedAt} onChange={event => setForm({ ...form, promisedAt: event.target.value })}/></div><div><Label>Evidencia inicial / imágenes (URL o referencia)</Label><Input value={form.intakeEvidence} onChange={event => setForm({ ...form, intakeEvidence: event.target.value })}/></div></div>
        <Button disabled={busy || customerLoading || !customerId}>{busy ? 'Creando…' : 'Crear orden'}</Button>
        {message && <p role="status" className="text-sm text-emerald-700">{message}</p>}
        {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
      </form>
    </CardContent></Card></div>
  </BaseLayout>
}
