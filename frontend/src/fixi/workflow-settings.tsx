import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { BaseLayout } from '@/components/layouts/base-layout'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import * as api from './api'

const field = 'w-full rounded-md border bg-background px-3 py-2 text-sm'
const phases = ['INTAKE','DIAGNOSIS','QUOTE','AUTHORIZATION','WORK','READY','DELIVERED','WARRANTY','CLOSED','CANCELLED']
const transitionPermissions = ['BRANCHES_READ','BRANCHES_MANAGE','CAPABILITIES_READ','CAPABILITIES_MANAGE','CUSTOMERS_READ','CUSTOMERS_WRITE','DEVICES_READ','DEVICES_WRITE','DEVICE_TRANSFER','ORDERS_READ','ORDERS_CREATE','ORDERS_UPDATE','ORDERS_CANCEL','ORDERS_STATUS_CHANGE','QUOTES_CREATE','QUOTES_MODIFY','QUOTES_AUTHORIZE_RECORD','INVENTORY_READ','INVENTORY_ADJUST','INVENTORY_PURCHASE','CASH_OPEN','CASH_CLOSE','CASH_MOVE','PAYMENTS_RECEIVE','PAYMENTS_REFUND','FINANCE_READ','PERMISSIONS_MANAGE','WORKFLOWS_MANAGE','SETTINGS_MANAGE','SETTINGS_READ','USERS_MANAGE','REPORTS_READ']

export function WorkflowSettingsPage() {
  const [workflows, setWorkflows] = useState<api.Workflow[]>([])
  const [selected, setSelected] = useState('')
  const [name, setName] = useState('')
  const [verticalKey, setVerticalKey] = useState('REPAIR')
  const [state, setState] = useState({ key: '', label: '', semanticPhase: 'WORK', initialState: false, terminalState: false })
  const [editingStateKey, setEditingStateKey] = useState('')
  const [editingStateLabel, setEditingStateLabel] = useState('')
  const [transition, setTransition] = useState({ fromStateKey: '', toStateKey: '', transitionKey: '', requiredPermission: 'ORDERS_STATUS_CHANGE' })
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const active = workflows.find(workflow => workflow.id === selected)

  const reload = useCallback(async () => {
    setLoading(true)
    try { const items = await api.listWorkflows(); setWorkflows(items); setSelected(current => current || items[0]?.id || '') }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudieron cargar los flujos') }
    finally { setLoading(false) }
  }, [])
  useEffect(() => { void reload() }, [reload])

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

  async function renameState(event: FormEvent, item: api.WorkflowState) {
    event.preventDefault()
    if (!active) return
    const label = editingStateLabel.trim()
    if (!label || label === item.label) { setEditingStateKey(''); return }
    setBusy(true); setError(''); setMessage('')
    try { await api.updateWorkflowStateLabel(active.id, item.key, label); setEditingStateKey(''); await reload(); setMessage('Nombre del estado actualizado') }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo editar el estado') }
    finally { setBusy(false) }
  }

  async function toggleState(item: api.WorkflowState) {
    if (!active) return
    if (item.initialState && item.active) { setError('El estado inicial no puede desactivarse'); return }
    const next = !item.active
    const action = next ? 'activar' : 'desactivar'
    if (!window.confirm(`¿${action[0].toUpperCase()}${action.slice(1)} “${item.label}”?${next ? '' : ' No se permite si existen órdenes actualmente en ese estado.'}`)) return
    setBusy(true); setError(''); setMessage('')
    try { await api.setWorkflowStateActive(active.id, item.key, next); await reload(); setMessage(next ? 'Estado activado' : 'Estado desactivado') }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo cambiar el estado') }
    finally { setBusy(false) }
  }

  return <BaseLayout title="Estados y flujos de órdenes" description="Configura estados de reparación y transiciones explícitas para tu negocio."><div className="space-y-5 px-4 lg:px-6">
    <Card><CardHeader><CardTitle>Nuevo flujo</CardTitle><CardDescription>Separa los estados por vertical o proceso de trabajo.</CardDescription></CardHeader><CardContent><form onSubmit={create} className="grid gap-3 md:grid-cols-3"><Input required maxLength={100} placeholder="Nombre del flujo" value={name} onChange={e => setName(e.target.value)}/><Input required maxLength={80} placeholder="Clave vertical" value={verticalKey} onChange={e => setVerticalKey(e.target.value.toUpperCase().replace(/[^A-Z0-9_-]/g, '_'))}/><Button disabled={busy}>{busy ? 'Guardando…' : 'Crear flujo'}</Button></form></CardContent></Card>
    <Card><CardHeader><CardTitle>Configurar estados</CardTitle></CardHeader><CardContent><select aria-label="Flujo de órdenes" className={field} value={selected} onChange={e => { setSelected(e.target.value); setEditingStateKey('') }}><option value="">Seleccionar flujo</option>{workflows.map(workflow => <option key={workflow.id} value={workflow.id}>{workflow.name} · {workflow.verticalKey}</option>)}</select>{!loading && !error && workflows.length === 0 && <p className="mt-3 text-sm text-muted-foreground">Aún no hay flujos configurados. Crea uno para definir estados y transiciones de órdenes.</p>}{active && <div className="mt-4 grid gap-5 lg:grid-cols-2"><form className="space-y-3" onSubmit={addState}><h3 className="font-medium">Agregar estado</h3><Input required maxLength={60} placeholder="Clave (p. ej. WAITING_PART)" value={state.key} onChange={e => setState({ ...state, key: e.target.value.toUpperCase().replace(/[^A-Z0-9_-]/g, '_') })}/><Input required maxLength={120} placeholder="Nombre visible" value={state.label} onChange={e => setState({ ...state, label: e.target.value })}/><select aria-label="Fase semántica" className={field} value={state.semanticPhase} onChange={e => setState({ ...state, semanticPhase: e.target.value })}>{phases.map(phase => <option key={phase}>{phase}</option>)}</select><label className="flex gap-2 text-sm"><input type="checkbox" checked={state.initialState} onChange={e => setState({ ...state, initialState: e.target.checked })}/> Estado inicial</label><label className="flex gap-2 text-sm"><input type="checkbox" checked={state.terminalState} onChange={e => setState({ ...state, terminalState: e.target.checked })}/> Estado final</label><Button disabled={busy}>{busy ? 'Guardando…' : 'Agregar estado'}</Button></form><form className="space-y-3" onSubmit={addTransition}><h3 className="font-medium">Agregar transición</h3><select required aria-label="Estado de origen" className={field} value={transition.fromStateKey} onChange={e => setTransition({ ...transition, fromStateKey: e.target.value })}><option value="">Desde estado</option>{active.states.filter(item=>item.active).map(item => <option key={item.key} value={item.key}>{item.label}</option>)}</select><select required aria-label="Estado de destino" className={field} value={transition.toStateKey} onChange={e => setTransition({ ...transition, toStateKey: e.target.value })}><option value="">Hacia estado</option>{active.states.filter(item=>item.active).map(item => <option key={item.key} value={item.key}>{item.label}</option>)}</select><Input required maxLength={60} placeholder="Clave de acción" value={transition.transitionKey} onChange={e => setTransition({ ...transition, transitionKey: e.target.value.toUpperCase().replace(/[^A-Z0-9_-]/g, '_') })}/><select required aria-label="Permiso requerido" className={field} value={transition.requiredPermission} onChange={e => setTransition({ ...transition, requiredPermission: e.target.value })}>{transitionPermissions.map(permission => <option key={permission}>{permission}</option>)}</select><p className="text-xs text-muted-foreground">El permiso se valida en auth-service contra los permisos efectivos del usuario y el tenant.</p><Button disabled={busy || active.states.filter(item=>item.active).length < 2}>{busy ? 'Guardando…' : 'Agregar transición'}</Button></form></div>}</CardContent></Card>
    {active && <div className="grid gap-4 lg:grid-cols-2"><Card><CardHeader><CardTitle>Estados ({active.states.length})</CardTitle></CardHeader><CardContent className="space-y-2">{active.states.length === 0 && <p className="text-sm text-muted-foreground">Este flujo todavía no tiene estados.</p>}{active.states.map(item => <div className="rounded border p-3" key={item.key}><div className="flex flex-wrap items-center justify-between gap-3"><span>{item.label} · {item.key} · {item.semanticPhase}{item.initialState ? ' · inicial' : ''}{item.terminalState ? ' · final' : ''} · {item.active?'Activo':'Inactivo'}</span><div className="flex gap-2"><Button type="button" size="sm" variant="outline" disabled={busy} onClick={()=>{setEditingStateKey(item.key);setEditingStateLabel(item.label);setError('')}}>Editar nombre</Button><Button type="button" size="sm" variant="outline" disabled={busy||item.initialState&&item.active} onClick={()=>void toggleState(item)}>{item.active?'Desactivar':'Activar'}</Button></div></div>{editingStateKey===item.key&&<form className="mt-3 flex flex-wrap gap-2" onSubmit={event=>void renameState(event,item)}><Input required maxLength={120} aria-label={`Nuevo nombre para ${item.label}`} value={editingStateLabel} onChange={event=>setEditingStateLabel(event.target.value)}/><Button type="submit" size="sm" disabled={busy||!editingStateLabel.trim()}>{busy?'Guardando…':'Guardar nombre'}</Button><Button type="button" size="sm" variant="outline" disabled={busy} onClick={()=>setEditingStateKey('')}>Cancelar</Button></form>}</div>)}</CardContent></Card><Card><CardHeader><CardTitle>Transiciones ({active.transitions.length})</CardTitle></CardHeader><CardContent className="space-y-2">{active.transitions.length === 0 && <p className="text-sm text-muted-foreground">Este flujo todavía no tiene transiciones.</p>}{active.transitions.map(item => <div className="rounded border p-3" key={item.key}><p>{active.states.find(state=>state.key===item.fromStateKey)?.label??item.fromStateKey} → {active.states.find(state=>state.key===item.toStateKey)?.label??item.toStateKey} · {item.key}</p><p className="text-sm text-muted-foreground">Permiso: {item.requiredPermission}</p></div>)}</CardContent></Card></div>}
    {loading && <p role="status">Cargando flujos…</p>}{error && <div className="flex flex-wrap items-center gap-3"><p role="alert" className="text-sm text-destructive">{error}</p><Button type="button" size="sm" variant="outline" disabled={loading} onClick={()=>void reload()}>Reintentar</Button></div>}{message && <p role="status" className="text-sm text-emerald-700">{message}</p>}
  </div></BaseLayout>
}
