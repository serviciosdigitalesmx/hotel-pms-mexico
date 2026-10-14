import { useState, type ChangeEvent } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { BaseLayout } from '@/components/layouts/base-layout'
import * as api from './api'

function parseCsv(text: string): string[][] {
  const rows: string[][] = []
  let row: string[] = [], cell = '', quoted = false
  for (let i = 0; i < text.length; i++) {
    const char = text[i]
    if (char === '"' && quoted && text[i + 1] === '"') { cell += '"'; i++ }
    else if (char === '"') quoted = !quoted
    else if (char === ',' && !quoted) { row.push(cell); cell = '' }
    else if ((char === '\n' || char === '\r') && !quoted) {
      if (char === '\r' && text[i + 1] === '\n') i++
      row.push(cell); if (row.some(value => value.trim())) rows.push(row); row = []; cell = ''
    } else cell += char
  }
  if (quoted) throw new Error('El CSV tiene comillas sin cerrar')
  row.push(cell); if (row.some(value => value.trim())) rows.push(row)
  return rows
}

function csvCell(value: unknown) { return `"${String(value ?? '').replaceAll('"', '""')}"` }

export function CustomerCsvTools() {
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [rowErrors, setRowErrors] = useState<string[]>([])

  function downloadTemplate() {
    const csv = 'firstName,lastName,email,phone,address,city,country,rfc,fiscalName,fiscalPostalCode,fiscalRegime,cfdiUse\r\nAna,Pérez,ana@example.com,5551234567,,,,,,,,\r\n'
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
    const link = document.createElement('a'); link.href = url; link.download = 'fixi-clientes-plantilla.csv'; link.click(); URL.revokeObjectURL(url)
  }

  async function exportCustomers() {
    setBusy(true); setError(''); setMessage('')
    try {
      const customers: api.Customer[] = []
      for (let page = 0; page < 100; page++) {
        const result = await api.listCustomers(page)
        const content = Array.isArray(result) ? result : result.content
        customers.push(...content)
        if (content.length < 100 || Array.isArray(result)) break
      }
      const columns = ['firstName', 'lastName', 'email', 'phone', 'address', 'city', 'country', 'rfc', 'fiscalName', 'fiscalPostalCode', 'fiscalRegime', 'cfdiUse'] as const
      const csv = [columns, ...customers.map(customer => columns.map(column => customer[column] ?? ''))].map(row => row.map(csvCell).join(',')).join('\r\n')
      const url = URL.createObjectURL(new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })); const link = document.createElement('a'); link.href = url; link.download = 'fixi-clientes.csv'; link.click(); URL.revokeObjectURL(url)
      setMessage(`Exportación lista: ${customers.length} clientes.`)
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron exportar los clientes') }
    finally { setBusy(false) }
  }

  async function importFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    setBusy(true); setError(''); setMessage(''); setRowErrors([])
    try {
      const rows = parseCsv((await file.text()).replace(/^\uFEFF/, ''))
      if (rows.length < 2) throw new Error('El CSV no contiene filas de clientes')
      const headers = rows[0].map(header => header.trim().toLowerCase())
      const required = ['firstname', 'lastname']
      if (!required.every(header => headers.includes(header))) throw new Error('El CSV requiere firstName y lastName, y al menos email o phone por registro')
      const accepted = new Set(['firstname', 'lastname', 'email', 'phone', 'address', 'city', 'country', 'rfc', 'fiscalname', 'fiscalpostalcode', 'fiscalregime', 'cfdiuse'])
      let imported = 0; const errors: string[] = []
      for (const [index, values] of rows.slice(1).entries()) {
        const record = Object.fromEntries(headers.map((header, column) => [header, values[column]?.trim() ?? '']))
        const rowNumber = index + 2
        if (!record.firstname || !record.lastname || (!record.email && !record.phone)) { errors.push(`Fila ${rowNumber}: nombre, apellido y correo o teléfono son obligatorios`); continue }
        const names: Record<string, string> = { firstname: 'firstName', lastname: 'lastName', email: 'email', phone: 'phone', address: 'address', city: 'city', country: 'country', rfc: 'rfc', fiscalname: 'fiscalName', fiscalpostalcode: 'fiscalPostalCode', fiscalregime: 'fiscalRegime', cfdiuse: 'cfdiUse' }
        const payload: Record<string, string | null> = {}
        for (const [header, value] of Object.entries(record)) if (accepted.has(header) && names[header]) payload[names[header]] = value || null
        payload.email = record.email || null; payload.phone = record.phone || null
        payload.address ??= null; payload.city ??= null; payload.country ??= null; payload.dateOfBirth = null
        payload.billingEmail = record.email || null; payload.fiscalCode = null; payload.vatNumber = null; payload.companyName = null; payload.sdiCode = null; payload.pecEmail = null; payload.cap = null; payload.comune = null; payload.provincia = null
        try { await api.createCustomer(payload); imported++ } catch (reason) { errors.push(`Fila ${rowNumber}: ${reason instanceof Error ? reason.message : 'no se pudo guardar'}`) }
      }
      setRowErrors(errors)
      setMessage(`Importación terminada: ${imported} creados; ${errors.length} omitidos.${errors.length ? ` ${errors.slice(0, 5).join(' · ')}` : ''}`)
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo importar el CSV') }
    finally { setBusy(false) }
  }

  function downloadErrors() {
    const csv = [['fila', 'error'], ...rowErrors.map(entry => { const [row, ...message] = entry.split(': '); return [row.replace('Fila ', ''), message.join(': ')] })].map(row => row.map(csvCell).join(',')).join('\r\n')
    const url = URL.createObjectURL(new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })); const link = document.createElement('a'); link.href = url; link.download = 'fixi-clientes-errores-importacion.csv'; link.click(); URL.revokeObjectURL(url)
  }

  return <BaseLayout title="Importar / Exportar clientes" description="Carga y descarga segura del directorio en formato CSV."><div className="px-4 lg:px-6"><Card><CardHeader><CardTitle>Importar y exportar clientes</CardTitle><CardDescription>CSV UTF-8 con contacto y datos fiscales. Descarga la plantilla para usar las columnas admitidas.</CardDescription></CardHeader><CardContent className="flex flex-wrap items-center gap-3"><Button type="button" variant="outline" disabled={busy} onClick={downloadTemplate}>Descargar plantilla</Button><label className="inline-flex cursor-pointer items-center rounded-md border px-4 py-2 text-sm">{busy ? 'Procesando…' : 'Importar CSV'}<input className="sr-only" type="file" accept=".csv,text/csv" disabled={busy} onChange={event => void importFile(event)}/></label><Button type="button" variant="outline" disabled={busy} onClick={() => void exportCustomers()}>Exportar CSV</Button>{rowErrors.length>0&&<Button type="button" variant="outline" onClick={downloadErrors}>Descargar errores ({rowErrors.length})</Button>}{message && <p role="status" className="w-full text-sm text-emerald-700">{message}</p>}{error && <p role="alert" className="w-full text-sm text-destructive">{error}</p>}</CardContent></Card></div></BaseLayout>
}
