# Fase 0: decisiones y evidencia Fixi

## Alcance y método

Se inspeccionó directamente `D:\Projects\Sdmx-pagina-principal1` y el repo Camra actual. En Fixi se leyeron `docs/FIXI_PRODUCT_SPEC.md`, `docs/SOURCE_OF_TRUTH.md`, `docs/ARCHITECTURE_CURRENT.md`, todos los documentos de `docs/canonical/` y las especificaciones/decisiones T00-T20 relevantes. Se contrastaron controladores, rutas montadas, servicios, clientes frontend, migraciones y esquema descrito por `supabase/migrations/`.

**Límite de evidencia:** no se consultó la instancia productiva de Supabase. “Esquema actual” significa el esquema reconstruible desde las migraciones del repo y los nombres/columnas que usa el código, no un `pg_catalog` de producción. Por eso los valores tenant-specific (por ejemplo, estados realmente guardados por cada taller) se marcan `INDETERMINADO` cuando requieren leer datos live.

Estados usados en este documento:

- **IMPLEMENTADO ACTUALMENTE:** hay una ruta/flujo alcanzable y datos/migraciones que lo soportan.
- **PARCIAL:** existe la base, pero el recorrido está incompleto, desconectado o tiene límites demostrables.
- **SOLO DOCUMENTADO/PLANEADO:** aparece en el objetivo/canónico, pero no hay implementación física/ruta comprobada.
- **LEGADO:** hay código/ruta anterior que sigue presente, pero no es el contrato usado por el flujo frontend actual o usa otro esquema.
- **CONTRADICTORIO:** fuentes activas describen o implementan comportamientos incompatibles.
- **INDETERMINADO:** hace falta inspeccionar estado live o tomar una decisión de producto para afirmarlo.

Las especificaciones `docs/specs/decisions_t07_t08.md`, `decisions_t09_t10.md`, `decisions_t11_t12.md` y `decisions_t13_t14_t15.md` están fechadas el 2026-06-20. Migraciones posteriores crean reservas, historial/garantías, autorizaciones, cola de borradores, work logs, comisiones y endurecimiento de POS/caja. Esas decisiones documentan la evidencia disponible en su fecha, no prevalecen sobre la implementación posterior.

## Hallazgos por punto

### 1. Estados actualmente utilizados por las órdenes

**Estado:** CONTRADICTORIO; el conjunto live por tenant es **INDETERMINADO**.

- **Documentación:** `FIXI_PRODUCT_SPEC.md` resume `recibido`, revisión, presupuestado, reparación, listo y entregado. `docs/specs/spec_01_fundaciones.md` exige nueve estados canónicos; `docs/canonical/spec_00_modelo_datos_maestro.md` enumera un flujo más extenso, con borrador, diagnosis, autorización, trabajo, QA, entrega, garantía, cancelación y cierre.
- **Código:** `apps/api/src/services/tenant-config.ts` tiene defaults de reparación: `recibido`, `en_espera_de_refaccion`, `diagnostico`, `cotizado`, `reparacion`, `listo_para_entrega`, `listo`, `entregado`. También define un workflow HVAC diferente (`solicitud_recibida`, `visita_programada`, `en_revision`, `cotizacion_enviada`, `autorizado`, `servicio_programado`, `servicio_realizado`, `garantia_activa`, `cerrado`, `cancelado`). La configuración puede leer filas `tenant_workflow_statuses` y `metadata` del tenant.
- **Migraciones:** `20260527030000_tenant_industry_config_phase1.sql` crea `tenant_workflow_statuses`; `20260525021500_relax_service_order_status_constraint.sql` reemplaza el CHECK enumerado por “status no vacío”. Las RPCs de creación inicializan en `recibido`.
- **Comportamiento actualmente ejecutable:** el valor se guarda como texto; no hay enum global que determine los datos live. `updateOrderStatus` valida contra los statuses configurados más una fase canónica. No se consultó la base live, por lo que no se afirma qué filas/tenant usan hoy cada clave. Además `normalizeOrderStatus` en `controllers/orders.ts` reconoce el vocabulario de reparación y no todas las claves HVAC; workflows configurables pueden fallar al normalizar el estado previo.
- **DECISIÓN REQUERIDA:** mantener estados globales y transiciones fijas, o mantener estados por tenant/industria con fases canónicas comunes. La primera alternativa simplifica API/reportes; la segunda conserva verticalización, pero exige normalizadores y transiciones compatibles con claves personalizadas.

### 2. Transiciones actualmente permitidas

**Estado:** PARCIAL.

- **Documentación:** Spec 00 define una tabla de transiciones estricta; la especificación exhaustiva solo resume cambios atómicos; `decisions_t07_t08.md` contiene otra lista de estados terminales para liberar reservas.
- **Código:** `apps/api/src/services/order-workflow.ts` define fases y transiciones permitidas: intake -> diagnosis/quote/work/cancelled; diagnosis -> quote/authorization/work/cancelled; quote -> authorization/work/cancelled; authorization -> quote/work/cancelled; work -> ready/cancelled; ready -> work/delivered/warranty/closed/cancelled; delivered -> warranty/closed; warranty -> diagnosis/work/ready/closed; cancelled -> closed; closed no tiene salidas. `metadata.next_status_keys` puede restringir candidatos pero también se valida contra esa matriz. `controllers/orders.ts:updateOrderStatus` aplica la validación antes de llamar SQL.
- **Migraciones:** `update_order_status_atomic` bloquea la fila, escribe status/evento y actualiza timestamps, pero no verifica dentro de SQL que el estado vigente siga siendo `p_previous_status` ni valida la transición; el comentario dice que esa validación queda delegada al controller.
- **Comportamiento actualmente ejecutable:** la protección de transiciones está en el endpoint autenticado, no en la RPC como invariante de base de datos. Una llamada simultánea puede validar contra un estado leído antes y luego la RPC actualizar aunque el estado ya haya cambiado. Los custom status HVAC también chocan con `normalizeOrderStatus`.
- **DECISIÓN REQUERIDA:** decidir si la máquina canónica se fija a todos los tenants o si cada workflow de industria tiene transitions configurables validadas contra fases compartidas. Antes de transformación, elegir una fuente transaccional de validación; no considerar que el lock SQL actual protege la transición completa.

### 3. ¿Dispositivo/equipo está en `service_orders` o existe una entidad `devices`?

**Estado:** CONTRADICTORIO entre modelo canónico y esquema ejecutado.

- **Documentación:** Spec 01 dice que los datos del dispositivo viven dentro de la orden; Spec 03 dice que el historial se agrupa por identificadores. El modelo maestro propone tablas `devices` y `device_categories` como modelo futuro.
- **Código:** creación de orden recibe `deviceType`, `deviceModel`, `serialNumber` y construye `service_orders.device_info` (JSONB) y `serial_number`. `getDeviceHistoryBySerial` usa una RPC que busca órdenes por serial; no carga una entidad `Device`. Hay catálogos separados de familias/marcas/modelos/fallas.
- **Migraciones:** la búsqueda sobre la cadena SQL no encontró `CREATE TABLE devices` ni `CREATE TABLE quotes/quote_items`. `20260624155000_t09_device_history.sql` implementa historial por serie; `20260724000000_phase1_catalogs.sql` crea catálogos, no una entidad de equipo propiedad del cliente.
- **Comportamiento actualmente ejecutable:** el activo reparado es una captura inline por orden y el historial se reconstruye por `serial_number`. Los catálogos no equivalen a un activo persistente: no hay ID estable del equipo ni transferencia de propiedad.
- **DECISIÓN REQUERIDA:** conservar el modelo inline + historial por serie, o normalizar `Device` para propiedad, varios órdenes por activo, custodia y cambios de cliente. La primera evita migrar y deduplicar historia; la segunda habilita identidad persistente, pero necesita política de serie duplicada/propiedad y migración de órdenes antiguas.

### 4. Relación cliente -> dispositivo -> orden

**Estado:** IMPLEMENTADO ACTUALMENTE para cliente y snapshot de equipo; entidad persistente de dispositivo no existe.

- **Documentación:** `FIXI_PRODUCT_SPEC.md` y Spec 00 describen cliente con múltiples dispositivos y órdenes; Spec 01 precisa que el equipo físico actual está inline en la orden.
- **Código:** `create_service_order_transaction` busca cliente del tenant por teléfono **o** email y crea uno si no lo encuentra; crea `service_orders` con `customer_id`, JSON `device_info`, `serial_number`, problema, checklist, sucursal, folio e idempotency key. `createCustomer` también hace matching por teléfono/nombre, con fallback de nombre parcial. `getDeviceHistoryBySerial` agrupa órdenes por tenant + serie.
- **Migraciones:** `service_orders.customer_id`, `customers`, `service_order_checklists`, `service_order_status_history` y el RPC transaccional existen en la cadena.
- **Comportamiento actualmente ejecutable:** cliente pertenece a tenant y se enlaza a orden; el equipo se duplica como snapshot por orden y se relaciona históricamente por identificador, no por FK a device. Solicitud pública guarda datos denormalizados y no necesariamente cliente hasta convertirla.
- **Riesgo/decisión:** la búsqueda SQL por `phone OR email` puede elegir un cliente cuando teléfono y correo apuntan a personas distintas; el matching por nombre parcial también es ambiguo. **DECISIÓN REQUERIDA:** escoger una política de identidad/deduplicación (phone first, email first, confirmación manual, o combinación estricta) antes de mapear clientes Camra.

### 5. ¿Hay presupuestos versionados físicos?

**Estado:** PARCIAL; el presupuesto versionado como agregado es **SOLO DOCUMENTADO/PLANEADO**.

- **Documentación:** Spec 00 define `quotes`/`quote_items` versionados; T11 requiere autorizar snapshot; `FIXI_PRODUCT_SPEC.md` describe `estimated_cost`, `final_cost` y autorización.
- **Código:** `service_orders` lleva costos escalares mutables; `/orders/:id/financials` actualiza `estimated_cost/final_cost` directamente. La autorización nueva congela snapshots numéricos de esos campos, pero no una lista de conceptos, refacciones, descuentos, impuestos/version completa.
- **Migraciones:** no se encontró definición física de `quotes` o `quote_items`; sí existen `service_order_authorizations` y la autorización vieja `customer_authorizations`.
- **Comportamiento ejecutable:** el taller puede modificar el monto y guardar una autorización del monto actual; no se reconstruye una cotización histórica completa ni hay tabla/version que reconcilie mano de obra + piezas + impuestos con lo cobrado.
- **DECISIÓN REQUERIDA:** si Camra debe crear un agregado quote/version/items y autorizar una versión inmutable, o conservar el costo scalar con snapshots de autorización. El primer camino soporta costo desglosado y cambios defendibles; el segundo replica el nivel funcional actual, no el modelo maestro.

### 6. Autorización del cliente

**Estado:** CONTRADICTORIO: hay dos implementaciones físicas y endpoints activos.

- **Documentación:** `FIXI_PRODUCT_SPEC.md` y T11/T12 dicen que aceptación se persiste ligada a orden; decisiones T11/T12 de 2026-06-20 dicen que aún no había tabla y proponen token/snapshot.
- **Código actual preferido por frontend:** `apps/web-clientes/src/lib/api/orders.ts` consume `/api/public/tenant/:tenantSlug/orders/:publicToken/authorization`; `controllers/public.ts` valida decisión, nombre, tipo, monto, términos, token e idempotencia y llama `submit_service_order_authorization`. El RPC inserta audit/event y verifica que el monto aceptado coincide con `estimated_cost` actual.
- **Código legacy también montado:** `routes/public-portal.ts` expone `/api/public-portal/order/:publicToken/authorize`, que escribe `customer_authorizations` y actualiza status directamente desde `presupuesto/diagnostico` a `reparacion/presupuesto_rechazado`.
- **Migraciones:** `service_order_authorizations` y su RPC se crean en T11 (junio/julio); `customer_authorizations` se crea después en `20260724000003_phase4_automation_customer_portal.sql`. Son tablas diferentes y ambos routers se montan desde `apps/api/src/index.ts`.
- **Comportamiento actualmente ejecutable:** el portal web observado usa la ruta nueva. Esa RPC registra aceptación/rechazo con snapshot, términos, actor y audit request id, **pero no transiciona `service_orders.status`**. La ruta vieja sí intenta transición, pero usa otro almacenamiento/contrato.
- **DECISIÓN REQUERIDA:** definir una sola fuente de verdad, efecto de aceptar/rechazar sobre workflow, y tratar `customer_authorizations` como legado o contrato activo. No migrar ambas tablas como si fueran equivalentes.

### 7. Estado real de work logs

**Estado:** PARCIAL.

- **Documentación:** `FIXI_PRODUCT_SPEC.md` declara API `start/pause/stop`, `in_progress/paused/completed` y bloqueo de múltiples logs activos; Spec T14 del 2026-06-20 dice que todavía no existían tablas.
- **Código:** `services/work-logs.ts` implementa start/pause/resume/stop/list; inserta eventos; técnico solo opera sus propios logs; registro de orden se filtra por tenant/sucursal/asignado. `routes/orders.ts` monta `/work-logs/...`.
- **Migraciones:** `20260625075448_t14_work_logs_commissions.sql` crea `work_logs` y `work_log_events`, estados `active/paused/completed/cancelled` y unique index por tenant+técnico para `active/paused`.
- **Comportamiento actualmente ejecutable:** backend y tablas existen. La exclusión es por técnico en todo el tenant, no por técnico/orden. El estado se actualiza y luego el evento se inserta en una operación separada, por lo que evento y estado pueden divergir ante fallo.
- **Frontend:** `ApiGateway` usa `/worklogs/...` sin guion; backend monta `/work-logs/...`. El frontend también llama `/commissions/rules` mientras el backend monta `/commission-rules`. La integración mediante esos métodos no coincide con rutas actuales.
- **DECISIÓN REQUERIDA:** definir si se permiten varios logs activos en órdenes distintas, y si el UI actual debe conservar las rutas del frontend o usar las rutas del backend. No presentar la función como E2E hasta resolver el drift.

### 8. Estado real de comisiones

**Estado:** PARCIAL.

- **Documentación:** Fixi Product Spec declara reglas de owner y comisión acumulada en work logs; T14 de junio decía que solo había estimaciones de estados y proponía `timesheets`/reglas.
- **Código:** `technician_commission_rules` admite `none`, `fixed_per_work_log`, `per_hour`, `% estimated_cost` y `% final_cost`; al detener work log se calcula monto y guarda snapshot/status `calculated` o `pending_cost`. Rutas permiten CRUD de reglas a owner/manager.
- **Migraciones:** la migración T14 crea reglas y columnas de comisión en work logs; no hay migración de pago/nómina/settlement.
- **Comportamiento actualmente ejecutable:** cálculo técnico por log al stop; selecciona la regla activa de prioridad más alta. No hay flujo de aprobación/pago/conciliación de comisiones; `approved_by/approved_at` de work_logs no aparecen en el servicio de cálculo. Si un costo es cero, queda `pending_cost`.
- **Frontend:** rutas de reglas en `ApiGateway` no coinciden con las rutas reales.
- **DECISIÓN REQUERIDA:** determinar si “comisión” en Fixi significa cálculo informativo por work log o pasivo financiero aprobable/pagable; define ledger, reversa y garantía/retrabajo.

### 9. Estado real de reservas de inventario

**Estado:** PARCIAL; reserva por orden está implementada, automatización del ciclo de orden no.

- **Documentación:** Fixi Product Spec declara activa/consumida/liberada; decisión T07 de 2026-06-20 dice que no existía tabla ni endpoint y propone reserva automática.
- **Código:** `routes/inventory.ts` monta GET/POST reservations y release/consume; controllers llaman RPCs. Reserva usa sucursal de la orden, calcula stock menos reservas activas y exige idempotency key.
- **Migraciones:** `20260624120000_t07_inventory_reservations.sql` crea tabla/RPCs con estados `active`, `partial`, `consumed`, `released`, `cancelled`, `expired`, cantidades separadas, expiración y auditoría.
- **Comportamiento actualmente ejecutable:** una persona autorizada crea reserva explícita; no se encontró disparador al aceptar presupuesto, ni liberación automática al cancelar/rechazar/abandonar orden. Las decisiones de junio que decían “no existe” están supersedidas por la migración posterior.
- **DECISIÓN REQUERIDA:** reservar al aceptar presupuesto automáticamente o mantener acción manual; determinar qué estados liberan reserva, si `expired` se procesa automáticamente y el scope de sucursal/técnico.

### 10. Estado real de movimientos y consumo de inventario

**Estado:** PARCIAL con RPCs transaccionales para consumo y transferencia.

- **Documentación:** Product Spec afirma transacciones RPC y transferencias atómicas; T07/T08 explica stock físico menos reserva activa.
- **Código:** catálogo/stock usa `products`, `sucursal_inventory`, `inventory_movements`; transfer llama `transfer_inventory_transaction`; reserva consume mediante `consume_inventory_reservation`.
- **Migraciones:** T08 RPC bloquea reserva y fila de inventario, descuenta `stock_current`, actualiza reserva, crea movimiento `service_order_consumed`, registra auditoría y evita repetición por reference/idempotency. Transfer RPC bloquea stock origen/destino, actualiza ambas sucursales y escribe `transfer_out/in` atómicamente.
- **Comportamiento actualmente ejecutable:** consumo reservado y transferencias usan transacción SQL. Reserva en sí no genera movimiento físico. Ajustes manuales de `catalogs.ts` actualizan `sucursal_inventory` y luego insertan `inventory_movements` por separado; creación/ajuste no tiene la misma garantía atómica que el consumo/transfer.
- **DECISIÓN REQUERIDA:** establecer ledger como fuente de verdad y decidir si ajuste manual también debe ser RPC atómica antes de migrar movimientos a Camra. Mantener separado “reservado” de “stock descontado”.

### 11. Estado real de cola de mensajes/automatizaciones

**Estado:** CONTRADICTORIO con el resumen de “message queue con retry/workers”.

- **Documentación:** FIXI_PRODUCT_SPEC.md describe `pending/processing/failed/completed`, retry_count, workers, retry/purge y automatización de WhatsApp/correo.
- **Código:** `automation.ts` permite CRUD de `automation_rules`, templates y logs. `updateOrderStatus` evalúa reglas inline: crea borrador manual `wa.me` o push, registra `automation_logs`; no se encontró worker/dispatcher que consuma trabajos. `whatsapp-messages.ts` persiste mensaje generado y lista historial.
- **Migraciones:** `20260625070326_t13_message_queue.sql` crea `message_queue` con `generated/opened_manual/cancelled/failed`, proveedor `manual_wa_me`, texto y `wa_me_url`. No tiene `pending/processing/completed`, `retry_count`, claim/lease ni esquema de dispatcher.
- **Comportamiento actualmente ejecutable:** hay bitácora/idempotencia para drafts y reglas inline; no envío automático de WhatsApp, reintentos de worker ni cola genérica. Push PWA es otro canal. Las decisiones T13 del 2026-06-20 preceden a esta tabla, pero aún describen el dispatcher como pendiente.
- **DECISIÓN REQUERIDA:** escoger si Camra debe preservar el alcance actual de drafts/manual send o si Fixi objetivo requiere cola durable + proveedor WhatsApp + worker/dispatcher y estados de entrega. No llamar al sistema actual envío automático.

### 12. Modelo real de usuarios: un tenant o memberships múltiples

**Estado:** CONTRADICTORIO entre modelo canónico y flujo ejecutable; producción multi-tenant live **INDETERMINADA**.

- **Documentación:** `especificacion_aprobada.md` permite que un usuario pertenezca a varios tenants; Spec 01 y `FIXI_PRODUCT_SPEC.md` describen `users.tenant_id` tenant-scoped.
- **Código:** `auth` JWT requiere `tenant_id`; `requireAuth` busca `users` por `auth_user_id + tenant_id`. `exchangeSupabaseSession` busca `users` con solo `auth_user_id` usando `maybeSingle`, luego firma un token para una sola fila/tenant. No hay selector de memberships en el flujo de login.
- **Migraciones:** `users.tenant_id NOT NULL`, índice único `(tenant_id, auth_user_id)`; no se encontró tabla `tenant_memberships`. Eso permite varias filas para el mismo auth user en SQL, pero no hace que el login actual pueda resolverlas sin ambigüedad.
- **Comportamiento actualmente ejecutable:** una sesión/token opera un tenant; crear múltiples perfiles `users` para un mismo auth user puede hacer ambiguo el `.maybeSingle()` de exchange. Onboarding `register` llama `createUser` y además la migración instala trigger `auth.users -> create_tenant_transaction`; la función que instala el trigger y la ruta hacen provisionamiento por vías separadas y la función no es idempotente por auth user. Si el trigger está activo en producción, existe riesgo de crear dos tenants/filas para el mismo usuario. El despliegue real del trigger no fue consultado. El usuario indicó que probablemente quitará ese trigger post-insert; es una intención comunicada, no un cambio ejecutado ni evidencia de que esté activo en producción.
- **DECISIÓN REQUERIDA:** usuario global con memberships explícitas y cambio de tenant, o usuario ligado a un solo tenant; además confirmar si el trigger está desplegado y si se retirará, dejando una sola autoridad idempotente de provisioning. Consecuencias: schema, JWT, login/selector, revocación, unicidad y signup existente.

### 13. Modelo real de sucursales y scopes

**Estado:** PARCIAL.

- **Documentación:** Spec 00 espera branch scope por usuario/rol; Fixi Product Spec habla de `sucursales`, inventario/caja por ubicación y usuario ligado a sucursal.
- **Código:** tabla/rutas vivas son `sucursales`; usuario tiene un solo `sucursal_id`. `resolveScope` permite owner vista consolidada y resuelve otros roles a modo branch usando header/query `x-fixi-sucursal-id` o la sucursal del token. El header solicitado tiene prioridad sobre la sucursal guardada; no hay tabla de acceso a varias sucursales.
- **Migraciones:** `20260527091000` y `20260527093000` hacen cutover de `branches` a `sucursales`; migraciones posteriores retiran compatibilidad. FKs de operación usan `sucursal_id`.
- **Comportamiento actualmente ejecutable:** varias sucursales por tenant existen y se usan en órdenes, stock, caja y usuarios. No existe membership de usuario a una lista de sucursales. Controllers que validan ownership limitan ID al tenant; no todos verifican que el manager solo seleccione su sucursal asignada. `DELETE /sucursales/:id` realiza delete físico, aunque documentación habla de inactivar/conservar histórico.
- **DECISIÓN REQUERIDA:** sucursal única por usuario vs acceso múltiple explícito; decidir si manager puede cambiar a cualquier sucursal tenant o solo a asignadas. Resolver delete histórico vs `is_active=false`. Además el router de caja no monta `attachScope`, pero sus handlers llaman `requireScopedBranch`, por lo que apertura/listado normal de caja puede fallar con `BRANCH_REQUIRED`.

### 14. Roles y permisos reales

**Estado:** PARCIAL/CONTRADICTORIO.

- **Documentación:** Fixi Product Spec declara owner > manager > technician > receptionist; modelo canónico agrega admin, cashier, supervisor, cliente y permisos por recurso/acción/estado/sucursal.
- **Código:** `users.role` es scalar; roles efectivos son `owner`, `manager`, `technician`, `client`, con aliases `admin->owner`, `operador/compras->manager`, `tecnico->technician`, `cliente->client`. `requireRole` aplica jerarquía rank-based; varias rutas tienen roles estáticos. No hay rol efectivo `receptionist`, `cashier` o `supervisor` separado.
- **Migraciones:** existen tabla global `permissions` y tabla tenant-scoped `tenant_role_permissions`. La ruta `/tenant-roles/permissions` permite a owner guardar permisos para manager/technician; el helper usado por `orders.override_pending_balance` consulta `permissions` global, no `tenant_role_permissions`. No hay aplicación de esos overrides en `requireRole` mostrado.
- **Comportamiento actualmente ejecutable:** autorización principal es el rol escalar + guards estáticos; configuración tenant-role puede guardarse/consultarse pero no gobierna los guards examinados. `compras` tiene rol efectivo manager. La diferencia receptionist/cashier/supervisor del target no está implementada como separación de mínimos privilegios.
- **DECISIÓN REQUERIDA:** RBAC fijo por rol vs permisos tenant-configurables/ABAC por estado/sucursal. Acordar roles Fixi reales y qué puede hacer cada uno; no copiar aliases que elevan `compras` a manager sin autorización de producto.

### 15. Planes y capabilities realmente implementados

**Estado:** CONTRADICTORIO; enforcement de billing está desactivado actualmente en el código revisado.

- **Documentación:** decisiones T19 de 2026-06-20 dicen Basic 3 usuarios/1 sucursal/50 órdenes/500 MB, Pro 10/5/500/5000 MB y que enforcement real falta parcialmente. `FIXI_PRODUCT_SPEC.md` afirma plan/capability activo.
- **Código:** `tenant-capabilities.ts` tiene `MODULE_REGISTRY` y `PLAN_REGISTRY`: Basic 2 usuarios/1 sucursal/50 órdenes/2048 MB; Pro 5/2/500/10240 MB; Scale ilimitado en usuarios/sucursales/órdenes y 102400 MB. Checkout usa Basic/Pro/Enterprise y precios en `services/billing.ts`; adapter lee `organizations` o `tenants` según modo. Algunos módulos Basic aparecen a la vez `required_plan: pro` y en su allowlist Basic. `requireTenantModule` omite check para `portal` y `whatsapp`.
- **Migraciones/código:** `20260809071909_enable_global_free_tenant_access.sql` cambia todos los tenants a `billing_exempt=true`, plan enterprise, y la función de onboarding crea tenants exempt. Más directamente, `middleware/tenantBilling.ts` tiene enforcement comentado y siempre llama `next()`. `tenant-plan-limits.ts` usa el registro TS, no tabla física `plans`; el middleware de cuota de usuarios consulta `user_profiles`, ausente en migraciones revisadas (se define `profiles` legacy y `users`, no `user_profiles`). `assertTenantPlanLimit` alternativo cuenta `users`. Monthly quota solo se aplica a create order, no a conversión de request.
- **Comportamiento actualmente ejecutable:** capability/module check puede limitar rutas según módulos; billing check no bloquea; onboarding global exempt/Scale neutraliza límites nuevos. El checkout/webhook existen, pero el modo billing default es legacy y no limpian `billing_exempt`. El límite de usuarios tiene dos implementaciones y posible consulta a tabla no creada por la cadena.
- **DECISIÓN REQUERIDA:** decidir si el producto sigue en acceso global gratuito o restaura billing enforcement; confirmar plan keys, precios/límites y módulo Basic vs Pro. Definir fuente única de plan/capability y cerrar rutas de cuota para users, requests->orders, storage y módulos públicos.

### 16. Configuración/verticalización por tenant

**Estado:** PARCIAL.

- **Documentación:** `SOURCE_OF_TRUTH.md` declara SaaS horizontal y verticalización configurable; canonical requiere `modules_config`, `reception_config`, fields y reglas por tenant.
- **Código:** `tenant-config.ts` lee `tenant_industry_profiles`, `tenant_enabled_modules`, `tenant_label_overrides`, `tenant_workflow_statuses`, `tenant_field_definitions`, `tenant_semaphore_rules`; campos requeridos se aplican en intake público/interno. Tiene plantillas hardcoded solo `electronics_repair`/`cellphone_repair` y `hvac_service`, y fallback a `electronics_repair`. `getIndustryTemplate` resuelve cualquier industria desconocida a esa default. La configuración `operationalSettings` aparece en helpers pero la carga runtime llama merge con `null`.
- **Migraciones:** `20260527030000`, `20260527050000`, `20260527070000` crean perfil, módulos/labels/workflows, fields y semaphore rules con tenant_id/RLS.
- **Comportamiento actualmente ejecutable:** tenant puede tener labels, modules, fields, statuses y semaphore rules en tablas; plantillas profundas y workflow defaults provienen de dos verticales en código. No es un motor universal de verticales arbitrarias.
- **DECISIÓN REQUERIDA:** Fixi destino debe soportar solo esos perfiles iniciales o permitir verticales/arbitrariedad administrada por tenant. Definir fallback de perfil desconocido y si configuración legacy en JSON debe seguir siendo fuente.

### 17. Flujo real solicitud -> cliente -> orden

**Estado:** PARCIAL.

- **Documentación:** Fixi Product Spec dice anónimo crea, manager aprueba/rechaza/convierte y conversión transaccional genera cliente+orden.
- **Código:** `POST /api/public/quotes` resuelve tenant por slug e inserta `service_requests` con customer fields y datos de dispositivo/metadata. `GET /requests` y `POST /requests/:id/convert` son autenticados; conversión llama RPC que bloquea la request, busca/crea customer y crea `service_orders`, actualiza request a `convertida` y guarda `converted_order_id`.
- **Migraciones:** `service_requests`, RLS, `converted_order_id` y RPC `convert_service_request_transaction` existen. Conversión usa `FOR UPDATE` y evita doble conversión.
- **Comportamiento actualmente ejecutable:** la solicitud pública no crea `customers` al ingresar; se crea/reutiliza al convertir. No se encontró endpoint backend para transición `en_revision` o `rechazada`; la RPC solo rechaza ya convertidas y no comprueba que la request esté en estado pendiente, por lo que una rechazada puede ser convertida. La API de conversión usa customer match por teléfono/email.
- **DECISIÓN REQUERIDA:** agregar ciclo revisar/rechazar o aceptar la simplicidad actual pendiente->convertida; decidir si una solicitud rechazada/duplicada puede volver a convertirse, y si cliente se crea al intake o solo al convertir.

### 18. Flujo real orden -> diagnóstico -> presupuesto/autorización -> reparación -> pago -> entrega

**Estado:** PARCIAL.

- **Documentación:** Fixi Product Spec afirma ciclo completo y atómico; canonical detalla diagnóstico, presupuesto versionado, autorización exacta, QA, entrega y garantía.
- **Código:** alta transaccional crea orden recibida, cliente, snapshot de equipo y checklist. La orden tiene `problem_description`, `estimated_cost/final_cost`, estado y campos/metadata; no hay ruta dedicada de diagnosis/quote con line items. Status API permite fases con el workflow. Autorización nueva guarda scope/terms y snapshot; work logs y reserva de stock son endpoints distintos. Pago manual calcula saldo, exige turno abierto y escribe `customer_payments`. Transición a delivered/closed exige saldo cero o permiso de override + motivo/audit.
- **Migraciones:** hay checklist, eventos, history, autorizaciones, payments/refunds, worklogs, reservas, warranty claims y RPC status/order.
- **Comportamiento actualmente ejecutable:** fragmentos críticos funcionan, pero no existe presupuesto físico versionado; `/financials` puede mutar costos tras autorización; la autorización nueva no mueve status; el consumo de pieza/reserva no se dispara automáticamente por aprobar presupuesto o cancelar orden; payment insert no es atómico con la suma de saldo; QA no es invariante estructural en la transición. Endpoint viejo de autorización sí intenta cambiar status con contrato distinto.
- **DECISIÓN REQUERIDA:** escoger lifecycle y fuente quote/version; definir qué transiciones exige autorización, cómo se reautoriza un cambio de costo y si QA/entrega son gates obligatorios. Alinear pagos, caja y consumo de stock con esas transiciones en una operación auditable.

### 19. POS/caja/pagos/finanzas

**Estado:** PARCIAL/CONTRADICTORIO.

- **Documentación:** Fixi Product Spec declara POS transaccional, caja por sucursal, pagos/refunds, finanzas e ingresos/gastos automáticos. T17-T20 de junio dice que billing/POS/cash cubre tickets posteriores.
- **Código POS:** no hay `routes/pos.ts`; `apps/api/src/index.ts` monta `/api/cash`, y `POST /cash/sales` crea venta POS mediante `execute_pos_sale_transaction`. La RPC bloquea shift/inventario, valida stock y precio server-side, persiste `sales/sale_items`, descuenta `sucursal_inventory`, registra movimiento y soporta idempotency key.
- **Código caja:** `cash_registers`, `cash_shifts`, close-shift RPC y gastos de turno existen. Pero `routes/cash.ts` no monta `validateTenant`, `attachScope`, `attachTenantCapabilities` ni `requireRole`; sus controllers `createRegister/getRegisters/openShift` llaman `requireScopedBranch(req)` y `req.scope` no se adjunta en esa ruta. El flujo normal de abrir caja queda sin el contexto requerido; `createSale` además exige shift abierto. Debe confirmarse con request real, pero la cadena de middleware muestra el bloqueo.
- **Pagos:** `POST /orders/:id/payments` inserta pagos parciales en `customer_payments` y requiere cash shift; refund inserta importe negativo con `parent_payment_id` y valida suma previa, sin lock transaccional para concurrencia. Pagos de orden y POS son modelos separados.
- **Pago online:** el endpoint legacy `/api/public-portal/order/:token/payment` crea una preference de Mercado Pago; no inserta `online_payments/customer_payments`. El webhook montado trata metadata de billing SaaS y no concilia el `order_id` de esa preference con `customer_payments`.
- **Finanzas:** `/finance/balance` calcula ingreso sumando `final_cost` de órdenes, no pagos confirmados; resta `finances.expense`. Gastos manuales escriben campos resumen de `finances` e ignoran description/category/date en el insert. Existe también tabla `expenses` y las compras pueden alimentarla. `deleteExpense` borra físicamente fila `finances`, contradiciendo la regla documental de no destruir historial. Reportes usan límites `.limit(500)` para varias fuentes y `.limit(1000)` para movimientos antes de agregar.
- **Migraciones:** Phase3 POS/cash, `execute_pos_sale_transaction`, customer payment cash shift, refund parent, sales idempotency y constraints existen.
- **DECISIÓN REQUERIDA:** fijar libro de verdad de caja vs pagos vs ventas vs `finances`; separar SaaS billing de cobros del taller; decidir si checkout online del cliente es parte del contrato y cómo liquida al order ledger. Corregir primero scope/middleware de caja y asegurar refund/pay concurrency antes de portar como comportamiento válido.

### 20. Portal cliente y tokens públicos

**Estado:** CONTRADICTORIO; portal por token está implementado, también hay rutas legacy por folio.

- **Documentación:** canonical T11/T12 exige token público, rechazo genérico de folios y payload sanitizado; `FIXI_PRODUCT_SPEC.md` habla de portal por tenant/folio, `public_token`, aprobar y descargar documentos.
- **Código frontend actual:** `apps/web-clientes` usa `getPortalOrderByToken` y `submitOrderAuthorization`; obtiene estado, autorización, documentos visibles/garantías y PDF usando el token. No se observó POST público de chat/mensaje del cliente; `/orders/:id/messages` requiere auth.
- **Código backend actual:** `/api/public/tenant/:tenantSlug/orders/:publicToken/portal`, `/authorization`, `/pdf` resuelve tenant+token; el portal filtra documentos por `is_customer_visible`/retención y excluye notas internas. El enlace de PDF usa token. `service_orders.public_token` se genera en alta y es único por tenant.
- **Rutas legacy adicionales:** `/api/public/tracking` busca por `tenantSlug + folio`; `email` es opcional, y la respuesta devuelve el `data` consultado, incluidos campos de orden/metadata. `/api/public/:tenantSlug/orders/:folio` se llama folio pero su handler consulta `public_token`; `/api/public-portal/order/:publicToken` sigue montado y usa tablas/columnas antiguas (`customer_authorizations`, `order_id`, `file_url`, `visible_to_customer`) frente a la ruta nueva (`service_order_authorizations`, `service_order_id`, `public_url`, `is_customer_visible`).
- **Migraciones:** hay public_token, visibilidad/retención de documentos, `service_order_authorizations` y también `customer_authorizations` legacy.
- **Comportamiento actualmente ejecutable:** la SPA de clientes observada usa token nuevo; los endpoints folio/legacy siguen alcanzables en la API. No se debe afirmar portal seguro token-only hasta retirar/cerrar el tracking por folio sin email y verificar/retirar `/api/public-portal` legacy. Las URLs de documento de Storage pueden ser públicas, aunque el API filtre cuáles entrega.
- **DECISIÓN REQUERIDA:** token-only para datos sensibles (recomendación de seguridad/canónico) vs conservar búsqueda folio+email como UX; si se conserva, exigir una verificación no enumerativa. Escoger una sola API de autorización y definir si cliente puede pagar/chatear desde portal.

## DECISIONES BLOQUEANTES

| Decisión | Estado actual comprobado | Alternativas | Impacto en Camra -> Fixi | Recomendación técnica | Requiere decisión humana |
|---|---|---|---|---|---|
| Máquina de estados y catálogo | Status es text no vacío; defaults por industria, runtime tenant configurable; el mapa de docs ofrece 6/9/18 claves y HVAC falla con normalización actual | Estados fijos globales / estados por industria bajo fases compartidas | Workflow, APIs, UI, events, reportes, cuotas y migración de histórico | Adoptar catálogo por industria con conjunto semántico de fases compartido solo si producto aprueba; no congelar 9 estados sin resolver fuentes | Sí |
| Device inline vs entidad | Orden almacena `device_info`/serial; no hay tabla `devices`; catálogos e historial por serial sí existen | Seguir inline / introducir `devices` con customer ownership | FKs, historial, deduplicación y tratamiento de órdenes existentes | Mantener inline en la primera transformación Camra salvo requisito formal de propiedad/activos persistentes | Sí |
| Presupuestos/quotes | No hay `quotes` ni `quote_items`; solo costos escalares y snapshots en autorizaciones | Costo scalar + authorization snapshot / quote versionada con items e impuestos | Control financiero, autorización, status, UI, reportes y migración | No simular versionado; aprobar modelo objetivo antes de transportar flujo | Sí |
| Autorización de cliente | Dos tablas, dos endpoints; web actual usa `service_order_authorizations`; nueva RPC no transiciona orden; legacy sí intenta | Una autorización table + transición explícita / conservar customer_authorizations legacy | Evitar evidencia divergente y reparación sin estado coherente | Elegir una fuente y hacer que registro de aceptación + decisión de workflow tengan semántica transaccional | Sí |
| Alcance del workflow por industria | `electronics_repair` y HVAC en código; tenant statuses configurable, pero normalizador no cubre HVAC | Solo taller repair / workflows por vertical | Define si Camra elimina por completo hotel domain o conserva motor configurable transversal | Mantener configuración genérica; agregar vertical solo con contrato y transición testeada | Sí |
| Revisión/rechazo de solicitudes | API pública crea request; GET/convert existen; no hay endpoint revisar/rechazar; convert RPC no exige estado pendiente | Pipeline mínimo pendiente->convertida / revisión y rechazo explícitos | Bandeja solicitudes, permission matrix, reportes de conversión | Fijar lifecycle antes de reusar `Quotation` o Stay; no mapear nombres hoteleros | Sí |
| Identidad multi-tenant | JWT y `users` operan con un tenant; exchange usa `maybeSingle` por auth_user_id; tabla canónica memberships no existe | Un usuario por tenant / auth user global con tenant memberships | Auth-service Camra, JWT, login/context switching, scopes | Preservar una identidad de tenant inicialmente para compatibilidad, pero bloquear migración multi-tenant hasta decisión | Sí |
| Scope de sucursal | `users.sucursal_id` singular; scope header/query puede prevalecer; enforcement desigual | Una sucursal por usuario / lista de sucursales por rol | Autorización por branch, inventario, cash, reportes y gateway | Definir autorizaciones de branch del lado servidor; header solo selecciona contexto permitido | Sí |
| Roles/permisos | Guards estáticos owner/manager/technician/client; `tenant_role_permissions` editable no se usa por `requireRole`; `permissions` global usado por override | RBAC global fijo / matriz tenant configurable por operación | Seguridad de todos los servicios Camra y tools IA | Llevar primero matriz actual a contrato explícito; decidir si tenants pueden ajustar permisos | Sí |
| Planes y billing enforcement | Plan registry y MercadoPago existen; billing middleware es no-op; signup/migración marca todo exempt Enterprise; values docs vs code difieren; quota user busca `user_profiles` | Seguir free / activar Basic-Pro-Scale enforceado | Tenant lifecycle, módulos, pricing y bloqueos de API | No portar bypass como política permanente ni asumir precios; aprobar límites/plan y desactivar bypass mediante cambio separado | Sí |
| Verticalización y default | Datos tenant config reales, pero solo hay dos templates; fallback desconocido = electronics repair | Reparación electrónica + HVAC / plataforma abierta multi-industria | Settings, catálogos, campos, estados y UI Camra | Preservar tablas de configuración, pero requerir industria explícita y resolver fallback antes de nuevas altas | Sí |
| Automatización/WhatsApp | Queue guarda drafts manuales `wa.me`; no worker/proveedor/send/attempt retries; reglas se ejecutan inline | Draft manual / dispatcher duradero con proveedor WhatsApp elegido | Notification-service Camra, Evolution/API, retries, opt-in y SLO | Tratar auto-send como no implementado; elegir proveedor y semantics de entrega antes de migrar | Sí |
| Reserva y lifecycle de stock | RPC reserva/consumo/libera existe; acciones manuales; no release automático por cancelación/approval hook | Manual por técnico / automación ligada a estado/quote | Inventario/cancelación/availability Camra, integridad concurrente | Portar RPCs probadas; decidir estados que reservan/liberan y enlazar con workflow después | Sí |
| Ajustes y ledger inventario | Consumo y transferencia atómicos; ajuste CRUD no combina stock+movement en una RPC | Conservar ajuste actual / hacer todos los movimientos atómicos | Evitar stock sin kardex al portar a Postgres/Flyway | La fuente de verdad debe ser saldo+ledger transaccional; no importar ajustes no reconciliados | Sí |
| Pagos/finanzas/caja | Pagos/reembolsos/cash/POS existen, pero web cash sin scope attach; finanzas suma final_cost, y gateways online no concilian pagos de orden | Un solo ledger cash/payments / conservar agregados separados con conciliación | `billing-service` Camra y saldo/entrega/POS | Definir fuente única y reconciliación; no considerar `final_cost` como ingreso cobrado | Sí |
| Portal/token público | Frontend usa token; `/api/public/tracking` acepta folio con email opcional y old portal route sigue montado | Token-only / mantener lookup alternativo con verificación robusta | Seguridad pública, gateway rate limits, customer portal | Token-only para datos sensibles; retirar o endurecer lookup por folio antes de migrar contratos | Sí |
| Onboarding tenant | Public auth y provisioning existen; Auth trigger y controller llaman a provisioning; función no idempotente por auth user | Un solo provisioning owner: trigger o controller/RPC | `auth-service` Camra, tenant/owner/branch y seed config | Una sola entrada idempotente con compensación verificable; revisar si trigger está desplegado | No para principio técnico; sí para decidir compatibilidad del signup actual |
| Portabilidad T20 | Export JSON capado por entidad y preview import existen; no hay confirm/apply, jobs, worker ni archivo privado | Mantener export/preview / completar async import/export | API y DB Camra, datos y Storage; relación con plan suspendido | Considerar solo export y preview implementados, el resto fuera del contrato confirmado | Sí |
| Estado live del schema | Migraciones/versionado del repo inspeccionables; no se consultó Supabase productiva | Fijar migraciones repo como baseline / auditar DB real y drift antes de portar | Backfill, compatibilidad y datos existentes | Ejecutar snapshot/migration audit contra entorno autorizado antes de DDL Camra | Sí |

## CONTRATO FIXI CONFIRMADO

Solo se consideran hechos suficientemente respaldados por código, migraciones y rutas montadas; no representan decisiones futuras:

- Fixi en este repo usa Express/Node como API central, Supabase Auth/PostgreSQL/Storage, tres apps frontend (web-public, web-admin, web-clientes) y rutas HTTP montadas principalmente bajo `/api/...`; no existe `/api/v1` en el router inspeccionado.
- Toda operación backend autenticada toma `tenant_id` del JWT y filtra por tenant en los handlers observados. La sesión JWT activa fija un tenant; las tablas `users` físicas tienen `tenant_id` y `sucursal_id` escalares.
- Tablas físicas comprobadas en el código/migraciones: `tenants`, `users`, `sucursales`, `customers`, `service_requests`, `service_orders`, checklists/events/status history/documents, `customer_payments`, `finances`, `products`, `sucursal_inventory`, `inventory_movements`, `inventory_reservations`, `purchase_orders/items`, `suppliers`, `cash_registers`, `cash_shifts`, `sales/sale_items`, `work_logs/events`, `technician_commission_rules`, `service_order_authorizations`, `customer_authorizations`, `service_order_warranties`, `message_queue`, `automation_rules/logs`, tenant config tables y audit logs.
- `service_orders` es el root físico de órdenes; equipo/datos de cliente se guardan inline en `device_info` más columnas serial/device/problem. El historial de dispositivo comprobado se reconstruye por serial dentro de tenant; no existe tabla física `devices` en las migraciones revisadas.
- `service_requests` se crea públicamente por tenant slug. Su conversión a cliente/orden se ejecuta en RPC con row lock, matching/creación de customer, creación de service order y prevención de doble conversión.
- `estimated_cost`/`final_cost` y `service_order_authorizations` son físicos. No se comprobó almacenamiento de presupuesto versionado con quote items.
- Los estados son texto y pueden depender de configuración tenant/industria. Hay defaults electronics y HVAC en runtime code; la configuración de DB tiene `tenant_workflow_statuses`. Los valores live por taller no se conocen sin leer la base desplegada.
- Reserva, consumo de reserva, transferencia de stock y venta POS tienen RPCs SQL; el consumo reserved y las transferencias escriben movimientos y audit logs transaccionalmente. Los ajustes manuales de stock no comparten esa garantía.
- Work logs y reglas de comisiones están en tablas y endpoints backend. Las rutas frontend observadas para work logs/commission rules no coinciden exactamente con las rutas backend.
- El `message_queue` físico registra borradores de WhatsApp manual `wa.me`; no se comprobó worker de envío, proveedor WhatsApp automático ni retry queue con estados `pending/processing/completed`.
- Existe una nueva ruta de portal/token y autorización que usa `service_order_authorizations`, más una ruta antigua que usa `customer_authorizations`; también permanece lookup público por folio en `/api/public/tracking`.
- Hay POS/caja y payments/refunds implementados en parte; `cash` route no adjunta `req.scope` aunque algunos handlers lo exigen. Finanzas/reportes derivan ingresos de `service_orders.final_cost` y tienen límites de filas, por lo que no equivalen a ledger de cobros reconciliado.
- Hay `PLAN_REGISTRY`, `MODULE_REGISTRY`, billing checkout/webhook, admin y portability endpoints. El middleware de billing está desactivado en código; global-free migration hace tenants billing-exempt; export/preview import no equivale a import/export asincrónico completo.
- Verticalización de datos/configuración existe por tenant; plantillas codificadas inspeccionadas cubren `electronics_repair` y `hvac_service`, con fallback a electronics para industria desconocida.
