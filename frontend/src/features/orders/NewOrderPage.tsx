import { useEffect, useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'
import { customersApi, devicesApi, ordersApi, usersApi } from '../../api/endpoints'
import { errorMessage, newIdempotencyKey } from '../../api/http'
import type { CloudLockStatus, Customer, Device, Priority, ReceptionChecklist, RepairOrder, UnlockMethod } from '../../api/types'
import { CustomerSearch, SignaturePad } from '../../components/domain'
import { Alert, Button, Card, Checkbox, Field, Input, PageHeader, Select, Textarea } from '../../components/ui'
import { cn } from '../../lib/cn'
import { fromLocalInput } from '../../lib/format'
import { openPdf, printPdf } from '../../lib/files'
import { CLOUD_LOCK, ISSUE_CATEGORIES, PRIORITY, UNLOCK_METHOD } from '../../lib/labels'
import { trackingWhatsAppUrl } from '../../lib/whatsapp'
import { CustomerForm, DeviceForm } from '../customers/forms'
import { emptyCustomer, emptyDevice } from '../customers/model'

type Step = 'customer' | 'device' | 'issue' | 'done'

const CHECKS: { key: keyof ReceptionChecklist; label: string }[] = [
  { key: 'chargingOk', label: 'Carga' },
  { key: 'screenOk', label: 'Pantalla / táctil' },
  { key: 'camerasOk', label: 'Cámaras' },
  { key: 'speakersOk', label: 'Parlantes' },
  { key: 'microphoneOk', label: 'Micrófono' },
  { key: 'buttonsOk', label: 'Botones' },
  { key: 'faceIdOk', label: 'Face ID' },
  { key: 'fingerprintOk', label: 'Huella' },
]

function defaultPromise() {
  const d = new Date()
  d.setDate(d.getDate() + 3)
  d.setHours(18, 0, 0, 0)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T18:00`
}

export function NewOrderPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [params] = useSearchParams()
  const [step, setStep] = useState<Step>('customer')
  const [customer, setCustomer] = useState<Customer | null>(null)
  const [device, setDevice] = useState<Device | null>(null)
  const [creatingCustomer, setCreatingCustomer] = useState(false)
  const [creatingDevice, setCreatingDevice] = useState(false)
  const [order, setOrder] = useState<RepairOrder | null>(null)

  // Issue + reception
  const [issue, setIssue] = useState('')
  const [category, setCategory] = useState('')
  const [notes, setNotes] = useState('')
  const [priority, setPriority] = useState<Priority>('Normal')
  const [promised, setPromised] = useState(defaultPromise())
  const [technicianId, setTechnicianId] = useState('')
  const [checks, setChecks] = useState<Record<string, boolean | null>>({})
  const [cloudLock, setCloudLock] = useState<CloudLockStatus>('Unknown')
  const [battery, setBattery] = useState('')
  const [cosmetic, setCosmetic] = useState('')
  const [powersOn, setPowersOn] = useState<boolean | null>(null)
  const [unlockMethod, setUnlockMethod] = useState<UnlockMethod>('None')
  const [unlockValue, setUnlockValue] = useState('')
  const [notify, setNotify] = useState(false)
  const [signature, setSignature] = useState<string | null>(null)
  const [signer, setSigner] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [issueError, setIssueError] = useState<string | undefined>()
  const idempotencyKey = useMemo(() => newIdempotencyKey(), [])
  const trackingMessageUrl = order?.customerNotificationsOptIn
    ? trackingWhatsAppUrl(customer?.whatsAppNumber, order.customerName, order.code, order.trackingUrl)
    : null

  const presetCustomerId = params.get('customerId')
  useEffect(() => {
    if (!presetCustomerId) return
    let cancelled = false
    customersApi
      .get(presetCustomerId)
      .then((c) => {
        if (cancelled) return
        setCustomer(c)
        setStep('device')
      })
      .catch(() => undefined)
    return () => {
      cancelled = true
    }
  }, [presetCustomerId])

  const devices = useQuery({ queryKey: ['devices', 'customer', customer?.id], queryFn: () => devicesApi.search({ customerId: customer!.id, take: 50 }), enabled: !!customer })
  const techs = useQuery({ queryKey: ['users', 'assignable'], queryFn: usersApi.assignable, staleTime: 5 * 60_000 })

  async function createOrder() {
    if (!customer || !device) return
    if (issue.trim().length < 5) {
      setIssueError('Describe la falla con al menos 5 caracteres.')
      document.querySelector<HTMLInputElement | HTMLTextAreaElement>('[name="issueDescription"]')?.focus()
      return
    }
    setSaving(true)
    setError(null)
    try {
      const created = await ordersApi.create(
        {
          customerId: customer.id,
          deviceId: device.id,
          issueDescription: issue.trim(),
          notes: notes.trim() || null,
          issueCategory: category || null,
          priority,
          assignedTechnicianId: technicianId || null,
          promisedAtUtc: fromLocalInput(promised),
          sendReceivedMessage: notify && customer.notificationsOptIn,
        },
        idempotencyKey
      )

      // Follow-up records: each one is independent; a failure doesn't lose the order.
      const problems: string[] = []
      try {
        await ordersApi.saveReception(created.id, {
          ...(checks as unknown as ReceptionChecklist),
          powersOn,
          chargingOk: checks.chargingOk ?? null,
          cloudLock,
          batteryPercent: battery ? Number(battery) : null,
          cosmeticNotes: cosmetic.trim() || null,
        })
      } catch (err) {
        problems.push(`checklist: ${errorMessage(err)}`)
      }
      if (unlockMethod !== 'None' && unlockValue.trim()) {
        try {
          await ordersApi.setUnlock(created.id, unlockMethod, unlockValue.trim())
        } catch (err) {
          problems.push(`código de desbloqueo: ${errorMessage(err)}`)
        }
      }
      if (signature) {
        try {
          await ordersApi.saveSignature(created.id, 'Reception', signer.trim() || customer.fullName, signature)
        } catch (err) {
          problems.push(`firma: ${errorMessage(err)}`)
        }
      }

      setOrder(created)
      setStep('done')
      void queryClient.invalidateQueries({ queryKey: ['orders'] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      if (problems.length) toast.warning('La orden se creó, pero hubo detalles sin guardar', { description: problems.join(' · ') })
      else toast.success(`Orden ${created.code} creada`)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const steps: { id: Step; label: string }[] = [
    { id: 'customer', label: '1. Cliente' },
    { id: 'device', label: '2. Equipo' },
    { id: 'issue', label: '3. Recepción' },
    { id: 'done', label: '4. Listo' },
  ]

  return (
    <div className="mx-auto max-w-3xl">
      <PageHeader title="Nueva orden de reparación" subtitle="Registra el estado del equipo al recibirlo. La firma de recepción es opcional." />
      <ol className="fx-stepper mb-5 flex gap-2 text-sm" aria-label="Pasos">
        {steps.map((s) => (
          <li key={s.id} className={cn('flex-1 rounded-lg border px-3 py-2 text-center', s.id === step ? 'border-brand-500 bg-brand-50 font-medium text-brand-800' : 'border-slate-200 bg-white text-slate-500')} aria-current={s.id === step ? 'step' : undefined}>
            {s.label}
          </li>
        ))}
      </ol>

      {step === 'customer' ? (
        <Card title="¿Quién trae el equipo?">
          {creatingCustomer ? (
            <CustomerForm
              compact
              initial={emptyCustomer()}
              submitLabel="Crear cliente y seguir"
              onCancel={() => setCreatingCustomer(false)}
              onUseExisting={async (id) => {
                const selected = await customersApi.get(id)
                setCustomer(selected)
                setNotify(false)
                setCreatingCustomer(false)
                setStep('device')
              }}
              onSubmit={async (input) => {
                const c = await customersApi.create(input)
                setCustomer(c)
                setNotify(false)
                setCreatingCustomer(false)
                setStep('device')
              }}
            />
          ) : (
            <div className="space-y-4">
              <CustomerSearch
                autoFocus
                onSelect={(c) => {
                  setCustomer(c)
                  setNotify(false)
                  setStep('device')
                }}
              />
              <div className="flex items-center gap-3 text-sm text-slate-500">
                <span className="h-px flex-1 bg-slate-200" /> o <span className="h-px flex-1 bg-slate-200" />
              </div>
              <Button variant="outline" className="w-full" onClick={() => setCreatingCustomer(true)}>
                + Cliente nuevo
              </Button>
            </div>
          )}
        </Card>
      ) : null}

      {step === 'device' && customer ? (
        <Card
          title={`Equipo de ${customer.fullName}`}
          actions={
            <Button size="sm" variant="ghost" onClick={() => { setCustomer(null); setDevice(null); setStep('customer') }}>
              Cambiar cliente
            </Button>
          }
        >
          {creatingDevice || (devices.data && devices.data.items.length === 0) ? (
            <DeviceForm
              initial={emptyDevice()}
              submitLabel="Agregar equipo y seguir"
              onCancel={devices.data && devices.data.items.length > 0 ? () => setCreatingDevice(false) : undefined}
              onSubmit={async (input) => {
                const d = await devicesApi.create({ ...input, customerId: customer.id })
                setDevice(d)
                setCreatingDevice(false)
                void queryClient.invalidateQueries({ queryKey: ['devices'] })
                setStep('issue')
              }}
            />
          ) : (
            <div className="space-y-3">
              <ul className="divide-y divide-slate-100 rounded-lg border border-slate-200">
                {(devices.data?.items ?? []).map((d) => (
                  <li key={d.id}>
                    <button type="button" className="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-slate-50" onClick={() => { setDevice(d); setStep('issue') }}>
                      <span>
                        <span className="font-medium text-slate-800">
                          {d.brand} {d.model}
                        </span>
                        {d.label ? <span className="ml-2 text-slate-500">{d.label}</span> : null}
                        {d.imei ? <span className="ml-2 text-xs text-slate-400">IMEI {d.imei}</span> : null}
                      </span>
                      <span className="text-brand-700">Elegir →</span>
                    </button>
                  </li>
                ))}
              </ul>
              <Button variant="outline" className="w-full" onClick={() => setCreatingDevice(true)}>
                + Otro equipo
              </Button>
            </div>
          )}
        </Card>
      ) : null}

      {step === 'issue' && customer && device ? (
        <div className="space-y-4">
          <Card
            title={`${device.brand} ${device.model} · ${customer.fullName}`}
            actions={
              <Button size="sm" variant="ghost" onClick={() => setStep('device')}>
                Cambiar equipo
              </Button>
            }
          >
            <div className="space-y-4">
              <Field label="Falla que reporta el cliente" required error={issueError} hint="Descríbela con sus palabras: qué pasa, desde cuándo y si tuvo golpes o humedad.">
                <Textarea name="issueDescription" rows={3} value={issue} onChange={(e) => { setIssue(e.target.value); setIssueError(undefined) }} autoFocus maxLength={500} />
              </Field>
              <div className="grid gap-3 sm:grid-cols-3">
                <Field label="Categoría">
                  <Select value={category} onChange={(e) => setCategory(e.target.value)}>
                    <option value="">Sin categoría</option>
                    {ISSUE_CATEGORIES.map((c) => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </Select>
                </Field>
                <Field label="Prioridad">
                  <Select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
                    {(Object.keys(PRIORITY) as Priority[]).map((p) => (
                      <option key={p} value={p}>
                        {PRIORITY[p].label}
                      </option>
                    ))}
                  </Select>
                </Field>
                <Field label="Fecha prometida">
                  <Input type="datetime-local" value={promised} onChange={(e) => setPromised(e.target.value)} />
                </Field>
              </div>
              <div className="grid gap-3 sm:grid-cols-2">
                <Field label="Técnico">
                  <Select value={technicianId} onChange={(e) => setTechnicianId(e.target.value)}>
                    <option value="">Asignar después</option>
                    {(techs.data ?? []).map((t) => (
                      <option key={t.id} value={t.id}>
                        {t.displayName}
                      </option>
                    ))}
                  </Select>
                </Field>
                <Field label="Notas internas">
                  <Input value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Accesorios que deja, observaciones…" />
                </Field>
              </div>
            </div>
          </Card>

          <Card title="Estado en que ingresa (checklist de recepción)">
            <div className="space-y-4">
              <TriStateCheck label="Enciende al ingresar" value={powersOn} onChange={setPowersOn} />
              <fieldset>
                <legend className="mb-2 text-sm font-medium text-slate-700">Revisión de funciones</legend>
                <p className="mb-2 text-xs text-slate-500">Marca “Funciona” o “Falla” después de revisar. Lo que dejes sin marcar queda como no revisado.</p>
                <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                  {CHECKS.map((c) => (
                    <TriStateCheck key={c.key} label={c.label} value={checks[c.key] ?? null} onChange={(value) => setChecks((prev) => ({ ...prev, [c.key]: value }))} />
                  ))}
                </div>
              </fieldset>
              <div className="grid gap-3 sm:grid-cols-3">
                <Field label="Cuenta (iCloud / Google)">
                  <Select value={cloudLock} onChange={(e) => setCloudLock(e.target.value as CloudLockStatus)}>
                    {(Object.keys(CLOUD_LOCK) as CloudLockStatus[]).map((k) => (
                      <option key={k} value={k}>
                        {CLOUD_LOCK[k]}
                      </option>
                    ))}
                  </Select>
                </Field>
                <Field label="Batería (%)">
                  <Input type="number" min={0} max={100} value={battery} onChange={(e) => setBattery(e.target.value)} />
                </Field>
                <Field label="Estado estético">
                  <Input value={cosmetic} onChange={(e) => setCosmetic(e.target.value)} placeholder="Rayones, golpes, vidrio trizado…" />
                </Field>
              </div>
            </div>
          </Card>

          <Card title="Código de desbloqueo">
            <div className="grid gap-3 sm:grid-cols-2">
              <Field label="Tipo">
                <Select value={unlockMethod} onChange={(e) => setUnlockMethod(e.target.value as UnlockMethod)}>
                  {(Object.keys(UNLOCK_METHOD) as UnlockMethod[]).map((k) => (
                    <option key={k} value={k}>
                      {UNLOCK_METHOD[k]}
                    </option>
                  ))}
                </Select>
              </Field>
              <Field label="Código" hint={unlockMethod === 'Pattern' ? 'Ej: 1-2-3-6-9 (puntos del 1 al 9)' : 'Se guarda cifrado y se borra al entregar el equipo.'}>
                <Input value={unlockValue} disabled={unlockMethod === 'None'} onChange={(e) => setUnlockValue(e.target.value)} autoComplete="off" />
              </Field>
            </div>
          </Card>

          <Card title="Conformidad del cliente">
            <div className="space-y-3">
              <Field label="Nombre de quien firma">
                <Input value={signer} onChange={(e) => setSigner(e.target.value)} placeholder={customer.fullName} />
              </Field>
              <SignaturePad onChange={setSignature} />
              <p className="text-xs text-slate-500">Opcional. Si el cliente firma, se imprime en el comprobante junto con los términos de recepción configurados.</p>
            </div>
          </Card>

          <Checkbox label="Avisarle al cliente que recibimos el equipo (con el enlace de seguimiento)" description={customer.notificationsOptIn ? 'Solo se enviará si el cliente aceptó recibir avisos.' : 'El cliente no aceptó recibir avisos; no se enviará ningún mensaje.'} checked={notify && customer.notificationsOptIn} disabled={!customer.notificationsOptIn} onChange={(e) => setNotify(e.target.checked)} />
          {error ? <Alert tone="rose">{error}</Alert> : null}
          <div className="fx-reception-actions flex justify-end gap-2">
            <Button onClick={() => setStep('device')}>Anterior</Button>
            <Button onClick={() => navigate(-1)}>Cancelar</Button>
            <Button
              variant="primary"
              size="lg"
              loading={saving}
              disabled={saving}
              onClick={() => void createOrder()}
            >
              Crear orden
            </Button>
          </div>
        </div>
      ) : null}

      {step === 'done' && order ? (
        <Card>
          <div className="space-y-4 text-center">
            <p className="text-sm text-slate-500">Orden creada</p>
            <p className="text-4xl font-bold tracking-tight text-slate-900">{order.code}</p>
            <p className="text-sm text-slate-600">
              {order.deviceLabel} · {order.customerName}
            </p>
            <div className="flex flex-wrap justify-center gap-2">
              {trackingMessageUrl ? (
                <a
                  href={trackingMessageUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="fixi-button inline-flex h-10 items-center justify-center gap-2 rounded-md border border-emerald-600 bg-emerald-600 px-3.5 text-sm font-medium text-white transition hover:bg-emerald-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-1 focus-visible:ring-emerald-500"
                >
                  Enviar seguimiento por WhatsApp
                </a>
              ) : null}
              <Button variant="primary" onClick={() => void openPdf(`/orders/${order.id}/documents/intake`)}>
                Imprimir comprobante de ingreso
              </Button>
              <Button onClick={() => void printPdf(`/orders/${order.id}/documents/label`)}>Imprimir etiqueta QR</Button>
              <Button onClick={() => navigate(`/orders/${order.id}`)}>Ver orden</Button>
              <Button variant="ghost" onClick={() => window.location.reload()}>
                Cargar otra
              </Button>
            </div>
            <p className="text-xs text-slate-500">
              Enlace de seguimiento para el cliente:{' '}
              <a href={order.trackingUrl} target="_blank" rel="noreferrer" className="break-all text-brand-700 underline">
                {order.trackingUrl}
              </a>
            </p>
            {trackingMessageUrl ? (
              <p className="text-xs text-slate-500">WhatsApp abrirá el chat con el mensaje listo; confirma el envío en la aplicación.</p>
            ) : order.customerNotificationsOptIn ? (
              <p className="text-xs text-amber-700">No se pudo preparar WhatsApp porque el teléfono del cliente no tiene un formato válido.</p>
            ) : (
              <p className="text-xs text-slate-500">El cliente no autorizó avisos por WhatsApp. El comprobante incluye el QR para consultar el estatus.</p>
            )}
            <p className="text-xs text-slate-500">El comprobante incluye los datos de ingreso; la etiqueta QR es la versión compacta para colocar en el equipo.</p>
          </div>
        </Card>
      ) : null}
    </div>
  )
}

function TriStateCheck({ label, value, onChange }: { label: string; value: boolean | null; onChange: (value: boolean | null) => void }) {
  return (
    <div className="rounded-lg border border-slate-200 p-2">
      <p className="mb-2 text-sm font-medium text-slate-700">{label}</p>
      <div className="flex gap-1">
        {([[true, 'Funciona'], [false, 'Falla'], [null, 'No revisado']] as const).map(([next, text]) => (
          <button key={text} type="button" aria-pressed={value === next} onClick={() => onChange(next)} className={cn('rounded-md border px-2 py-1 text-xs', value === next ? next === true ? 'border-emerald-500 bg-emerald-50 text-emerald-700' : next === false ? 'border-rose-500 bg-rose-50 text-rose-700' : 'border-slate-400 bg-slate-100 text-slate-700' : 'border-slate-200 text-slate-500 hover:bg-slate-50')}>
            {text}
          </button>
        ))}
      </div>
    </div>
  )
}
