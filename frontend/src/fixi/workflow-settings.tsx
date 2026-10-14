import { useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const field = 'w-full rounded-md border bg-background px-3 py-2 text-sm'
const phases = ['INTAKE','DIAGNOSIS','QUOTE','AUTHORIZATION','WORK','READY','DELIVERED','WARRANTY','CLOSED','CANCELLED']
const transitionPermissions = ['ORDERS_STATUS_CHANGE','ORDERS_UPDATE','ORDERS_READ','ORDERS_CREATE','QUOTES_CREATE','QUOTES_MODIFY','QUOTES_AUTHORIZE_RECORD','INVENTORY_READ','INVENTORY_ADJUST','FINANCE_READ','REPORTS_READ']

export function WorkflowSettingsPage() {
  const [workflows, setWorkflows] = useState<api.Workflow[]>([])
  const [selected, setSelected] = useState('')
  const [name, setName] = useState('')
  const [verticalKey, setVerticalKey] = useState('REPAIR')
  const [state, setState] = useState({ key: '', label: '', semanticPhase: 'WORK', initialState: false, terminalState: false })
  const [transition, setTransition] = useState({ fromStateKey: '', toStateKey: '', transitionKey: '', requiredPermission: 'ORDERS_STATUS_CHANGE' })
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const active = workflows.find(workflow => workflow.id === selected)

  async function reload() {
    setLoading(true)
    try { const items = await api.listWorkflows(); setWorkflows(items); if (!selected && items[0]) setSelected(items[0].id) }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los flujos') }
    finally { setLoading(false) }
  }
  useEffect(() => { void reload() }, [])

  async function create(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setMessage('')
    try { const item = await api.createWorkflow({ name, verticalKey }); setName(''); await reload(); setSelected(item.id); setMessage('Flujo creado') }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo crear el flujo') }
    finally { setBusy(false) }
  }
  async function addState(event: FormEvent) {
    event.preventDefault(); if (!active) return; setBusy(true); setError(''); setMessage('')
    try { await api.addWorkflowState(active.id, state); setState({ key: '', label: '', semanticPhase: 'WORK', initialState: false, terminalState: false }); await reload(); setMessage('Estado agregado') }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo agregar el estado') }
    finally { setBusy(false) }
  }
  async function addTransition(event: FormEvent) {
    event.preventDefault(); if (!active) return; setBusy(true); setError(''); setMessage('')
    try { await api.addWorkflowTransition(active.id, transition); setTransition({ ...transition, transitionKey: '' }); await reload(); setMessage('Transición agregada') }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo agregar la transición') }
    finally { setBusy(false) }
  }

  return <BaseLayout title="Estados y flujos de órdenes" description="Configura estados de reparación y transiciones explícitas para tu negocio."><div className="space-y-5 px-4 lg:px-6">
    <Card><CardHeader><CardTitle>Nuevo flujo</CardTitle><CardDescription>Separa los estados por vertical o proceso de trabajo.</CardDescription></CardHeader><CardContent><form onSubmit={create} className="grid gap-3 md:grid-cols-3"><Input required maxLength={100} placeholder="Nombre del flujo" value={name} onChange={e => setName(e.target.value)}/><Input required maxLength={80} placeholder="Clave vertical" value={verticalKey} onChange={e => setVerticalKey(e.target.value.toUpperCase().replace(/[^A-Z0-9_-]/g, '_'))}/><Button disabled={busy}>{busy ? 'Guardando…' : 'Crear flujo'}</Button></form></CardContent></Card>
    <Card><CardHeader><CardTitle>Configurar estados</CardTitle></CardHeader><CardContent><select aria-label="Flujo de órdenes" className={field} value={selected} onChange={e => setSelected(e.target.value)}><option value="">Seleccionar flujo</option>{workflows.map(workflow => <option key={workflow.id} value={workflow.id}>{workflow.name} · {workflow.verticalKey}</option>)}</select>{active && <div className="mt-4 grid gap-5 lg:grid-cols-2"><form className="space-y-3" onSubmit={addState}><h3 className="font-medium">Agregar estado</h3><Input required maxLength={60} placeholder="Clave (p. ej. WAITING_PART)" value={state.key} onChange={e => setState({ ...state, key: e.target.value.toUpperCase().replace(/[^A-Z0-9_-]/g, '_') })}/><Input required maxLength={120} placeholder="Nombre visible" value={state.label} onChange={e => setState({ ...state, label: e.target.value })}/><select aria-label="Fase semántica" className={field} value={state.semanticPhase} onChange={e => setState({ ...state, semanticPhase: e.target.value })}>{phases.map(phase => <option key={phase}>{phase}</option>)}</select><label className="flex gap-2 text-sm"><input type="checkbox" checked={state.initialState} onChange={e => setState({ ...state, initialState: e.target.checked })}/> Estado inicial</label><label className="flex gap-2 text-sm"><input type="checkbox" checked={state.terminalState} onChange={e => setState({ ...state, terminalState: e.target.checked })}/> Estado final</label><Button disabled={busy}>{busy ? 'Guardando…' : 'Agregar estado'}</Button></form><form className="space-y-3" onSubmit={addTransition}><h3 className="font-medium">Agregar transición</h3><select required aria-label="Estado de origen" className={field} value={transition.fromStateKey} onChange={e => setTransition({ ...transition, fromStateKey: e.target.value })}><option value="">Desde estado</option>{active.states.map(item => <option key={item.key} value={item.key}>{item.label}</option>)}</select><select required aria-label="Estado de destino" className={field} value={transition.toStateKey} onChange={e => setTransition({ ...transition, toStateKey: e.target.value })}><option value="">Hacia estado</option>{active.states.map(item => <option key={item.key} value={item.key}>{item.label}</option>)}</select><Input required maxLength={60} placeholder="Clave de acción" value={transition.transitionKey} onChange={e => setTransition({ ...transition, transitionKey: e.target.value.toUpperCase().replace(/[^A-Z0-9_-]/g, '_') })}/><select required aria-label="Permiso requerido" className={field} value={transition.requiredPermission} onChange={e => setTransition({ ...transition, requiredPermission: e.target.value })}>{transitionPermissions.map(permission => <option key={permission}>{permission}</option>)}</select><p className="text-xs text-muted-foreground">El permiso se valida en auth-service contra los permisos efectivos del usuario y el tenant.</p><Button disabled={busy || active.states.length < 2}>{busy ? 'Guardando…' : 'Agregar transición'}</Button></form></div>}</CardContent></Card>
    {active && <div className="grid gap-4 lg:grid-cols-2"><Card><CardHeader><CardTitle>Estados ({active.states.length})</CardTitle></CardHeader><CardContent className="space-y-2">{active.states.map(item => <div className="flex justify-between rounded border p-3" key={item.key}><span>{item.label} · {item.key}</span><span className="text-sm text-muted-foreground">{item.semanticPhase}{item.initialState ? ' · inicial' : ''}{item.terminalState ? ' · final' : ''}</span></div>)}</CardContent></Card><Card><CardHeader><CardTitle>Transiciones ({active.transitions.length})</CardTitle></CardHeader><CardContent className="space-y-2">{active.transitions.map(item => <div className="rounded border p-3" key={item.key}><p>{item.fromStateKey} → {item.toStateKey} · {item.key}</p><p className="text-sm text-muted-foreground">Permiso: {item.requiredPermission}</p></div>)}</CardContent></Card></div>}
    {loading && <p role="status">Cargando flujos…</p>}{message && <p role="status" className="text-sm text-emerald-700">{message}</p>}{error && <p role="alert" className="text-sm text-destructive">{error}</p>}
  </div></BaseLayout>
}
