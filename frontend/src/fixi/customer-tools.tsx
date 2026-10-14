import { useState, type ChangeEvent } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { BaseLayout } from '@/components/layouts/base-layout'
import * as api from './api'
import { InventoryCatalogPage } from './inventory'

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

function csvCell(value: unknown) { return api.csvCell(value) }

export function CustomerCsvTools() {
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [rowErrors, setRowErrors] = useState<string[]>([])

  function downloadTemplate() {
    const csv = 'FullName,Email,Phone,Dni,ContactDescription\r\nAna Pérez,ana@example.com,5551234567,,\r\n'
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
    const link = document.createElement('a'); link.href = url; link.download = 'fixi-clientes-plantilla.csv'; link.click(); URL.revokeObjectURL(url)
  }

  async function importFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    setBusy(true); setError(''); setMessage(''); setRowErrors([])
    try {
      const rows = parseCsv((await file.text()).replace(/^\uFEFF/, ''))
      if (rows.length < 2) throw new Error('El CSV no contiene filas de clientes')
      const headers = rows[0].map(header => header.trim().toLowerCase().replace(/[\s-]+/g, '_'))
      const hasFullName = headers.some(header => ['full_name', 'fullname', 'name'].includes(header))
      const hasSplitName = headers.some(header => ['first_name', 'firstname'].includes(header)) && headers.some(header => ['last_name', 'lastname'].includes(header))
      if (!hasSplitName && !hasFullName) throw new Error('El CSV requiere FullName o las columnas firstName y lastName')
      const columns = ['first_name', 'last_name', 'email', 'phone', 'rfc', 'dni', 'fiscal_name', 'fiscal_postal_code', 'fiscal_regime', 'cfdi_use', 'billing_email', 'address', 'city', 'country']
      const aliases: Record<string, string[]> = { first_name: ['first_name', 'firstname'], last_name: ['last_name', 'lastname'], full_name: ['full_name', 'fullname', 'name'], email: ['email'], phone: ['phone'], rfc: ['rfc'], dni: ['dni', 'identity_number', 'identitynumber'], fiscal_name: ['fiscal_name', 'fiscalname'], fiscal_postal_code: ['fiscal_postal_code', 'fiscalpostalcode'], fiscal_regime: ['fiscal_regime', 'fiscalregime'], cfdi_use: ['cfdi_use', 'cfdiuse'], billing_email: ['billing_email', 'billingemail'], address: ['address', 'contactdescription'], city: ['city'], country: ['country'] }
      const indexByHeader = new Map(headers.map((header, index) => [header, index]))
      const normalizedRows: Array<{ values: string[]; sourceRow: number }> = []
      const errors: string[] = []
      for (const [index, values] of rows.slice(1).entries()) {
        const value = (key: string) => {
          const column = aliases[key].map(alias => indexByHeader.get(alias)).find(column => column !== undefined)
          return column === undefined ? '' : values[column]?.trim() ?? ''
        }
        const record = Object.fromEntries(columns.map(column => [column, value(column)]))
        if (!record.first_name && !record.last_name && value('full_name')) {
          const parts = value('full_name').trim().split(/\s+/)
          record.first_name = parts.shift() ?? ''
          record.last_name = parts.join(' ')
        }
        if (Object.values(record).every(item => !item)) continue
        if (!record.first_name || !record.last_name || (!record.email && !record.phone)) { errors.push(`Fila ${index + 2}: nombre, apellido y correo o teléfono son obligatorios`); continue }
        normalizedRows.push({ values: columns.map(column => record[column]), sourceRow: index + 2 })
      }
      let imported = 0
      for (let offset = 0; offset < normalizedRows.length; offset += 500) {
        const batch = normalizedRows.slice(offset, offset + 500)
        const csv = [columns, ...batch.map(row => row.values)].map(row => row.map(csvCell).join(',')).join('\r\n')
        try {
          const result = await api.importCustomersCsv(new File([`\uFEFF${csv}`], `clientes-${offset + 1}.csv`, { type: 'text/csv' }))
          imported += result.imported
          errors.push(...result.errors.map(message => {
            const match = message.match(/^Fila (\d+):\s*(.*)$/)
            const sourceRow = match ? batch[Number(match[1]) - 2]?.sourceRow : undefined
            return sourceRow ? `Fila ${sourceRow}: ${match?.[2]}` : message
          }))
        } catch (reason) {
          errors.push(`Lote desde fila ${batch[0]?.sourceRow ?? offset + 2}: ${reason instanceof Error ? reason.message : 'no se pudo importar'}`)
        }
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

  return <BaseLayout title="Importar / Exportar" description="Carga y descarga CSV de clientes y productos, preservando sus datos comerciales y operativos."><div className="space-y-5 px-4 lg:px-6"><Card><CardHeader><CardTitle>Importar y exportar clientes</CardTitle><CardDescription>CSV UTF-8 con contacto y datos fiscales. Descarga la plantilla para usar las columnas admitidas.</CardDescription></CardHeader><CardContent className="flex flex-wrap items-center gap-3"><Button type="button" variant="outline" disabled={busy} onClick={downloadTemplate}>Descargar plantilla de clientes</Button><label className="inline-flex cursor-pointer items-center rounded-md border px-4 py-2 text-sm">{busy ? 'Procesando…' : 'Importar clientes CSV'}<input className="sr-only" type="file" accept=".csv,text/csv" disabled={busy} onChange={event => void importFile(event)}/></label><Button type="button" variant="outline" asChild><a href="/api/v1/customers/export">Exportar clientes CSV</a></Button>{rowErrors.length>0&&<Button type="button" variant="outline" onClick={downloadErrors}>Descargar errores ({rowErrors.length})</Button>}{message && <p role="status" className="w-full text-sm text-emerald-700">{message}</p>}{error && <p role="alert" className="w-full text-sm text-destructive">{error}</p>}</CardContent></Card><InventoryCatalogPage/></div></BaseLayout>
}
