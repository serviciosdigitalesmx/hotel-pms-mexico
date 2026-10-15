import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

export function ServiceCatalogManagementPage() {
  const [role, setRole] = useState('')
  const canManage = ['ADMIN', 'OWNER', 'MANAGER'].includes(role.replace(/^ROLE_/, ''))
  const [services, setServices] = useState<api.ServiceCatalogEntry[]>([])
  const [currency, setCurrency] = useState('MXN')
  const [name, setName] = useState('')
  const [basePrice, setBasePrice] = useState('0')
  const [estimatedMinutes, setEstimatedMinutes] = useState('60')
  const [search, setSearch] = useState('')
  const [editing, setEditing] = useState('')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [importing, setImporting] = useState(false)
  const [importErrors, setImportErrors] = useState<string[]>([])
  const [retryRows, setRetryRows] = useState<Array<{ sourceRow: number; values: string[] }>>([])
  const [retryHeaders, setRetryHeaders] = useState<string[]>([])

  useEffect(() => {
    let active = true
    void api.currentUser().then(user => { if (active) setRole(user?.role ?? '') })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudo validar el acceso al catálogo') })
    return () => { active = false }
  }, [])

  function parseCsv(text: string): string[][] {
    const rows: string[][] = []; let row: string[] = []; let cell = ''; let quoted = false
    for (let index = 0; index < text.length; index++) {
      const char = text[index]
      if (char === '"' && quoted && text[index + 1] === '"') { cell += '"'; index++ }
      else if (char === '"') quoted = !quoted
      else if (char === ',' && !quoted) { row.push(cell); cell = '' }
      else if ((char === '\n' || char === '\r') && !quoted) { if (char === '\r' && text[index + 1] === '\n') index++; row.push(cell); if (row.some(value => value.trim())) rows.push(row); row = []; cell = '' }
      else cell += char
    }
    if (quoted) throw new Error('El CSV tiene comillas sin cerrar')
    row.push(cell); if (row.some(value => value.trim())) rows.push(row)
    return rows
  }

  function downloadCsv(fileName: string, rows: unknown[][]) {
    const content = '\uFEFF' + rows.map(row => row.map(api.csvCell).join(',')).join('\r\n')
    const url = URL.createObjectURL(new Blob([content], { type: 'text/csv;charset=utf-8' })); const link = document.createElement('a'); link.href = url; link.download = fileName; link.click(); URL.revokeObjectURL(url)
  }

  function exportCsv() {
    downloadCsv('fixi-servicios.csv', [['nombre', 'precio_base', 'moneda', 'duracion_minutos', 'activo'], ...filtered.map(service => [service.name, service.basePrice, currency, service.estimatedMinutes, service.active])])
  }

  async function importCsv(file?: File) {
    if (!canManage) return
    setError(''); setMessage(''); setImportErrors([]); setRetryRows([]); setRetryHeaders([])
    if (!file) return
    if (file.size > 5 * 1024 * 1024) { setError('El archivo supera el límite de 5 MB'); return }
    setImporting(true)
    try {
      const rows = parseCsv((await file.text()).replace(/^\uFEFF/, ''))
      if (rows.length < 2) throw new Error('El archivo debe incluir encabezados y al menos un servicio')
      if (rows.length > 301) throw new Error('Importa un máximo de 300 servicios por archivo')
      setRetryHeaders(rows[0])
      const normalize = (value: string) => value.trim().toLocaleLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[ _-]/g, '')
      const aliases: Record<string, string> = { nombre: 'name', name: 'name', servicio: 'name', service: 'name', preciobase: 'price', baseprice: 'price', precio: 'price', price: 'price', moneda: 'currency', currency: 'currency', duracionminutos: 'minutes', estimatedminutes: 'minutes', duracion: 'minutes', duration: 'minutes', activo: 'active', active: 'active' }
      const headers = rows[0].map(value => aliases[normalize(value)] ?? '')
      if (!headers.includes('name')) throw new Error('Falta la columna obligatoria nombre/servicio')
      let created = 0; let partiallyApplied = 0; const rowErrors: string[] = []; const rejectedRows: Array<{ sourceRow: number; values: string[] }> = []
      for (const [index, values] of rows.slice(1).entries()) {
        const record: Record<string, string> = {}; headers.forEach((key, column) => { if (key) record[key] = values[column]?.trim() ?? '' })
        const serviceName = record.name?.trim(); const price = Number(record.price || 0); const minutes = Number(record.minutes || 60)
        if (!serviceName) { rowErrors.push(`Fila ${index + 2}: falta el nombre`); rejectedRows.push({ sourceRow: index + 2, values }); continue }
        if (record.currency && record.currency.toUpperCase() !== currency) { rowErrors.push(`Fila ${index + 2}: la moneda ${record.currency.toUpperCase()} no coincide con la moneda del negocio (${currency})`); rejectedRows.push({ sourceRow: index + 2, values }); continue }
        if (!Number.isFinite(price) || price < 0) { rowErrors.push(`Fila ${index + 2}: precio base inválido`); rejectedRows.push({ sourceRow: index + 2, values }); continue }
        if (!Number.isSafeInteger(minutes) || minutes < 1) { rowErrors.push(`Fila ${index + 2}: duración inválida (debe ser un entero positivo)`); rejectedRows.push({ sourceRow: index + 2, values }); continue }
        try {
          const service = await api.createService({ name: serviceName, basePrice: price, estimatedMinutes: minutes })
          created++
          if (record.active && /^(false|no|0|inactivo)$/i.test(record.active)) {
            try { await api.setServiceActive(service.id, false) }
            catch (reason) {
              partiallyApplied++
              rowErrors.push(`Fila ${index + 2}: servicio creado, pero no se pudo desactivar: ${reason instanceof Error ? reason.message : 'error al actualizar el estado'}`)
            }
          }
        } catch (reason) { rowErrors.push(`Fila ${index + 2}: ${reason instanceof Error ? reason.message : 'no se pudo guardar'}`) }
      }
      setImportErrors(rowErrors); setRetryRows(rejectedRows); setMessage(`${created} servicio(s) creados${partiallyApplied ? `; ${partiallyApplied} fila(s) quedaron con estado distinto al solicitado` : ''}${rejectedRows.length ? `; ${rejectedRows.length} fila(s) rechazadas por validación` : ''}${rowErrors.length > rejectedRows.length + partiallyApplied ? `; ${rowErrors.length - rejectedRows.length - partiallyApplied} fila(s) no pudieron guardarse` : ''}. La importación solo crea y no modifica servicios existentes.`)
      if (created) await reload()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo leer el CSV') }
    finally { setImporting(false) }
  }

  function downloadImportErrors() { downloadCsv('fixi-servicios-errores.csv', [['fila', 'error'], ...importErrors.map(value => { const [row, ...description] = value.split(': '); return [row.replace('Fila ', ''), description.join(': ')] })]) }
  function downloadRetryRows() { downloadCsv('fixi-servicios-filas-por-corregir.csv', [[...retryHeaders, 'fila_origen'], ...retryRows.map(row => [...row.values, row.sourceRow])]) }

  async function reload() {
    setLoading(true); setError('')
    try { setServices(await api.listServices()) }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo cargar el catálogo') }
    finally { setLoading(false) }
  }
  useEffect(() => { void reload() }, [])
  useEffect(() => { let active = true; api.getBusinessProfile().then(profile => { if (active && profile.currency) setCurrency(profile.currency.toUpperCase()) }).catch(() => {}); return () => { active = false } }, [])

  function formatPrice(value: number) {
    try { return new Intl.NumberFormat('es-MX', { style: 'currency', currency }).format(value) }
    catch { return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(value) }
  }

  async function save(event: FormEvent) {
    if (!canManage) return
    event.preventDefault(); setBusy(true); setError(''); setMessage('')
    try {
      const wasEditing = Boolean(editing)
      const payload = { name: name.trim(), basePrice: Number(basePrice), estimatedMinutes: Number(estimatedMinutes) }
      if (editing) await api.updateService(editing, payload)
      else await api.createService(payload)
      setName(''); setBasePrice('0'); setEstimatedMinutes('60'); setEditing(''); setMessage(wasEditing ? 'Servicio actualizado' : 'Servicio agregado'); await reload()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo guardar el servicio') }
    finally { setBusy(false) }
  }
  async function toggle(service: api.ServiceCatalogEntry) {
    if (!canManage) return
    setBusy(true); setError('')
    try { await api.setServiceActive(service.id, !service.active); await reload() }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo cambiar el estado') }
    finally { setBusy(false) }
  }

  const filtered = services.filter(service => service.name.toLocaleLowerCase().includes(search.trim().toLocaleLowerCase()))
  return <BaseLayout title="Servicios" description="Catálogo de trabajos cotizables, reutilizable en órdenes de reparación."><div className="space-y-5 px-4 lg:px-6">
    {canManage && <Card><CardHeader><CardTitle>{editing ? 'Editar servicio' : 'Agregar servicio'}</CardTitle></CardHeader><CardContent><form className="grid gap-2 sm:grid-cols-2 lg:grid-cols-4" onSubmit={save}><Input required maxLength={200} placeholder="Nombre del servicio" value={name} onChange={event => setName(event.target.value)}/><Input required aria-label={`Precio base en ${currency}`} type="number" min="0" step="0.01" placeholder={`Precio base ${currency}`} value={basePrice} onChange={event => setBasePrice(event.target.value)}/><Input required aria-label="Duración estimada en minutos" type="number" min="1" step="1" placeholder="Duración en minutos" value={estimatedMinutes} onChange={event => setEstimatedMinutes(event.target.value)}/><div className="flex gap-2"><Button disabled={busy || !name.trim() || !Number.isFinite(Number(basePrice)) || Number(basePrice) < 0 || !Number.isSafeInteger(Number(estimatedMinutes)) || Number(estimatedMinutes) < 1}>{busy ? 'Guardando…' : editing ? 'Guardar cambios' : 'Agregar servicio'}</Button>{editing && <Button type="button" variant="outline" disabled={busy} onClick={() => { setEditing(''); setName(''); setBasePrice('0'); setEstimatedMinutes('60') }}>Cancelar</Button>}</div></form></CardContent></Card>}
    <Card><CardHeader><div className="flex flex-wrap items-center justify-between gap-2"><CardTitle>Catálogo ({filtered.length})</CardTitle><div className="flex flex-wrap gap-2"><Button type="button" variant="outline" disabled={loading || !filtered.length} onClick={exportCsv}>Exportar filtrados</Button>{canManage && <><Button type="button" variant="outline" disabled={importing} onClick={() => downloadCsv('fixi-servicios-plantilla.csv', [['nombre', 'precio_base', 'moneda', 'duracion_minutos', 'activo']])}>Plantilla CSV</Button><label className="inline-flex cursor-pointer items-center rounded-md border px-3 py-2 text-sm">{importing ? 'Importando…' : 'Importar CSV'}<Input className="sr-only" type="file" accept=".csv,text/csv" disabled={importing} onChange={event => { const file = event.currentTarget.files?.[0]; event.currentTarget.value = ''; void importCsv(file) }}/></label>{retryRows.length > 0 && <Button type="button" variant="outline" onClick={downloadRetryRows}>Descargar filas por corregir ({retryRows.length})</Button>}{importErrors.length > 0 && <Button type="button" variant="outline" onClick={downloadImportErrors}>Descargar errores ({importErrors.length})</Button>}</>}</div></div><Input aria-label="Buscar servicios" placeholder="Buscar servicio" value={search} onChange={event => setSearch(event.target.value)}/></CardHeader><CardContent className="space-y-2">{loading && <p role="status">Cargando servicios…</p>}{filtered.map(service => <div key={service.id} className="flex flex-wrap items-center justify-between gap-2 rounded border p-3"><div><p>{service.name} · <span className="text-sm text-muted-foreground">{service.active ? 'Activo' : 'Inactivo'}</span></p><p className="text-sm text-muted-foreground">Base {formatPrice(service.basePrice)} · {service.estimatedMinutes} min</p></div>{canManage && <div className="flex gap-2"><Button type="button" size="sm" variant="outline" disabled={busy} onClick={() => { setEditing(service.id); setName(service.name); setBasePrice(String(service.basePrice)); setEstimatedMinutes(String(service.estimatedMinutes)) }}>Editar</Button><Button type="button" size="sm" variant="outline" disabled={busy} onClick={() => void toggle(service)}>{service.active ? 'Desactivar' : 'Activar'}</Button></div>}</div>)}{!loading && !error && !filtered.length && <p className="text-sm text-muted-foreground">No hay servicios que coincidan.</p>}</CardContent></Card>
    {message && <p role="status" className="text-sm text-emerald-700">{message}</p>}{error && <div className="flex items-center justify-between"><p role="alert" className="text-sm text-destructive">{error}</p><Button type="button" variant="outline" onClick={() => void reload()}>Reintentar</Button></div>}
  </div></BaseLayout>
}
