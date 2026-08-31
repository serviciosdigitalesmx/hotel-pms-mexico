# ADD — mapa de consolidación: implementado, pendiente e integración

> Corte de evidencia: 2026-08-30. Fuente Git: `/Users/usuario/Desktop/HOTEL-PMS`.
> No se considera una función validada E2E solo porque exista en un commit.

## Estado Git que bloquea una versión única

| Referencia | Commit | Relación |
| --- | --- | --- |
| Base común | `c872cad3` | último ancestro común |
| `main` local / `recovery/20260817-073005` | `314a9f4` | 4 commits locales no presentes en `origin/main` |
| `origin/main` | `94b0c72` | 36 commits remotos no presentes en el local |
| Working tree | sin commit | 53 archivos modificados y 29 nuevos al corte |

Regla: no hacer merge masivo ni push directo. Crear rama de integración desde
`origin/main`, portar cambios por bloques verificables y probar cada bloque.

## Implementación local versionada (aún no integrada en `origin/main`)

| Orden | Commit | Implementación | Evidencia principal | Estado |
| --- | --- | --- | --- | --- |
| 1 | `e5fe679` | Localización y flujos de estancia/check-in, perfil operativo por hotel, ocupantes, cotizaciones y PDF | V15 tenant profile, V16 occupant count, `StayController`, `StayServiceImpl`, `CheckInContext`, `StayCheckInValidator`, `WalkInCheckInForm`, `CheckInForm`, `PaymentModal`, `HotelProfile` | Portar y validar |
| 2 | `42d4d33` | Asistente por tenant: configuración, UI, Redis y router local | V17, `AssistantService`, `AssistantController`, `ConversationSessionStore`, `LocalIntentRouter`, `pages/Assistant.tsx` | Portar separado de proveedor IA |
| 3 | `0de646f` | Configuración de modelo IA de bajo costo | V18, `HotelSettings`, Settings/Profile UI | Implementado; luego alterado localmente |
| 4 | `314a9f4` | Runtime, roles housekeeping, usuarios, gateway, asistente determinista y migraciones IA V19/V20 | `AuthenticationFilter`, `PublicBookingFilter`, `UserManagement*`, `Assistant*` | Comparar contra hardening remoto |

### Expediente

El expediente por estancia ya es parte del trabajo local histórico: concentra
estancia, huésped, factura, pagos, consumos F&B y checkout en
`/stays/:stayId/expediente`. No equivale a PMS terminado: no resuelve cambio de
habitación ni la adaptación mexicana del check-in/Alloggiati/FatturaPA.

## Trabajo remoto ya hecho (36 commits que deben preservarse)

| Grupo | Commits | Resultado a preservar |
| --- | --- | --- |
| Críticos | `efc0069`, `20a89d2` | sin auto-registro público y check-in forzado a CHECKED_IN |
| Auth/gateway/rate limit | `b5d11a8`, `673d5f1`, `191cf51`, `922dc4c`, `ff432e3` | IP confiable, lockout, no enumeración, documentos restringidos, frontera gateway |
| Concurrencia/multitenant | `5cda2ce`, `421d5bd`, `128d91c`, `509eb7a`, `5f5a698`, `83dfda4` | locking, doble reserva, guardas tenant y stale delete |
| Infra/dependencias | `0d63cb5`, `5fc4bb3`, `1006bac`, `a2c33eb`, `0c190b9`, `3469a19`, `a1d2c83` | CVEs, Redis auth, env, Docker y secretos |
| API/XML | `ac059d8`, `f1ab269`, `19a8258`, `d8cafcf` | límites, sin URLs Docker, XML seguro, headers nginx |
| CORS, identidad y perfil | `01f76a9`, `d4965a7`, `1ffcb4` | CORS configurable, limpieza de validación JWT y validación backend de logo |
| Auditoría | `5d35d5d`, `1f7d97e`, `508f6c8`, `e11e907`, `8a6c33f`, `3afa873`, `9978952`, `73b7468`, `94b0c72` | reportes y cierre de hallazgos |

## Trabajo hecho pero sin commit

### Candidato a conservar: México

| Área | Evidencia | Estado requerido |
| --- | --- | --- |
| Perfil fiscal de huésped | `Guest.java`, `GuestRequest.java`, `GuestResponse.java`, `GuestServiceImpl.java`, `frontend/src/types/guest.types.ts`, `GuestFormModal.tsx` | RFC, razón social, CP fiscal, régimen, uso CFDI y correo: revisar contrato y probar |
| Migración México | `guest-service/src/main/resources/db/migration/V10__add_mexico_cfdi_guest_profile.sql` | Sin commit ni evidencia Flyway: aplicar y probar antes de integrar |
| Perfil de hotel/facturación | `HotelProfile.tsx`, `InvoiceDetailModal.tsx` | Separar en commit y validar UI/API |
| Operación recepción | `Quotations*`, `RateCalendar*`, `RoomSelection*`, `RoomTypeList*` | Revisar por bloque funcional |

### Candidato a comparar con remoto: tenant/seguridad

| Área | Evidencia | Riesgo |
| --- | --- | --- |
| Gateway/booking público | `PublicBookingFilter.java`, `CsrfFilter.java`, config gateway | Alto: remoto endurece gateway/check-in; portar sobre remoto |
| Settings por hotel | `HotelSettings.java`, DTO, repositorio | Aún mezcla configuración tenant con campos italianos |
| Asistente por hotel | `Assistant*`, `ConversationStep`, parser/router y tests | Conservar aislamiento hotel-operador; separar proveedor IA |

### No integrar en el bloque PMS México

| Evidencia | Decisión |
| --- | --- |
| V21/V22/V23, excepciones AI, fallback AI | Rama propia de IA |
| `.ralph-add/`, `.add/`, `RALPH_TASK.md`, backups, `argos*` | No son producto |
| `node_modules/`, `.env.backup-*`, compose backups | No versionar |

### Legado italiano todavía activo

| Evidencia | Consecuencia |
| --- | --- |
| `frontend/src/pages/Stays.tsx` agrega `AlloggiatiReportSection` | No incluir: vuelve visible reporte italiano |
| `types/stay.types.ts`, `types/guest.types.ts` | Tipos italianos conviven con campos México |
| `HotelSettings`, `docker-compose.yml`, walk-in/check-in | Alloggiati sigue activo; requiere cambio funcional |

## Pendiente de implementar

| Prioridad | Entrega | Criterio visible |
| --- | --- | --- |
| P0 | Consolidación Git | Rama desde `origin/main`, hardening remoto preservado y bloques locales portados |
| P0 | Check-in México | Walk-in sin catálogos/tipos/documentos Alloggiati; crea estancia con contratos reales |
| P0 | Aislar Alloggiati | Sin reportes, credenciales, endpoints ni autoenvío activos/visibles en operación México |
| P0 | Aislar FatturaPA | Sin XML italiano visible; PDF, pago, factura y checkout siguen funcionando |
| P0 | Pruebas A→B | A no puede leer/modificar/cobrar/exportar recursos de B |
| P1 | Perfil fiscal México | V10 aplicada y Guest UI/API validada; no inventa reglas SAT/CFDI |
| P1 | Cambio de habitación | Requiere endpoint y reglas reales; no hay entrega confirmada |
| P1 | E2E expediente | Login → estancia → expediente → pago/consumo → checkout |

## Orden único de consolidación

1. Crear rama nueva desde `origin/main`; no modificar el checkout actual.
2. Portar `e5fe679` por bloques: estancia/expediente, perfil hotel y UI; resolver contra los fixes remotos.
3. Portar `42d4d33` y la parte multitenant de `314a9f4`; excluir migraciones/proveedores IA experimentales.
4. Portar Guest México + V10 como commit independiente y probar Flyway/UI/API.
5. Excluir `AlloggiatiReportSection` y todo reporte/UI italiano de la versión México.
6. Implementar check-in México y aislamiento Alloggiati/FatturaPA con pruebas.
7. Ejecutar seguridad/multitenant, migraciones, build y flujo autenticado antes de declarar lista la versión.

## Decisión rápida

| Elemento | ¿Incluir? | Motivo |
| --- | --- | --- |
| Fixes de `origin/main` | Sí, primero | Seguridad y concurrencia |
| Expediente/estancias local | Sí, por bloques | Operación central |
| Campos México + V10 | Sí, tras pruebas | Trabajo existente sin integrar |
| Alloggiati UI/reportes/config | No | Contradice PMS México |
| FatturaPA visible | No | Aislar; CFDI pendiente de requisitos reales |
| IA/Ollama/Ralph/backups | No en este bloque | Experimentos/artefactos mezclados |
