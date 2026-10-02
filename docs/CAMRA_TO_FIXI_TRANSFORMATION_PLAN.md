# Plan de transformación de Camra a Fixi

## Alcance y premisa

**Camra es el repositorio y la base de ejecución del producto Fixi resultante.** No se plantea un segundo proyecto, una reimplementación paralela ni seleccionar utilidades para ensamblar otro sistema. Se conservan los módulos Gradle, los despliegues, la biblioteca compartida, las prácticas operativas y los límites de servicio siempre que sigan teniendo sentido. Los modelos, contratos y reglas hoteleras se sustituyen cuando no expresan el producto SaaS y sus flujos operativos Fixi.

Fuentes utilizadas:

- `docs/CAMRA_SYSTEM_SPEC.md` y el código Camra revisado: controladores, servicios, entidades, clientes Feign, rutas del gateway, migraciones Flyway, tests, frontend, Compose y CI.
- `D:\Projects\Sdmx-pagina-principal1\docs\FIXI_PRODUCT_SPEC.md`, el repo Fixi localizado en `D:\Projects\Sdmx-pagina-principal1`, `docs/SOURCE_OF_TRUTH.md`, `docs/ARCHITECTURE_CURRENT.md` y los documentos de `docs/canonical/` y `docs/specs/`.
- `docs/FIXI_CAMRA_CAPABILITY_MAP.md` y `docs/CAMRA_SYSTEM_SPEC.md` del repo Camra.

Fixi es un **SaaS horizontal multi-tenant con verticalización por tenant/configuración**. Reparación es el flujo operativo implementado que se analiza aquí, no una restricción del producto a talleres ni una razón para hardcodear reglas de taller en toda la plataforma. El repo Fixi actual usa tres apps (web-public, web-admin, web-clientes), una API central y Supabase; esa implementación aporta evidencia funcional. El objetivo de esta transformación conserva en cambio los límites y runtime de Camra, por la premisa explícita de que Camra es la base. No se propone sustituir Camra por la topología actual de Fixi.

Hay diferencias entre documentos de diseño canónico y esquema/código actual Fixi. En particular, la pertenencia de usuario a uno o varios tenants, los estados canónicos de orden, la normalización de dispositivos, y el estado implementado de reservas de inventario/colas no son consistentes entre todas las fuentes. Este plan no presenta como existentes tablas que solo estén en el modelo maestro. La fase 0 deberá cerrar una tabla de decisión por capacidad: (a) comportamiento del `FIXI_PRODUCT_SPEC.md` como snapshot de implementación, (b) código/migraciones actuales Fixi, (c) contrato canónico aprobado y (d) decisión destino. Hasta entonces, no se ejecuta backfill de esas entidades ni se define compatibilidad por intuición.

La separación entre `Evolucionar` y `Sustituir` es deliberada: `Guest` puede evolucionar a cliente porque conserva la responsabilidad de mantener una ficha de persona; `Stay`, en cambio, no debe convertirse en orden de reparación, porque sus invariantes son habitación ocupada, check-in/out y liquidación de estancia. Se reemplaza su modelo conservando el servicio, transacciones, protección tenant, manejo de fallos y pruebas aplicables.

## Arquitectura que permanece

Se conserva el sistema como una evolución de los mismos servicios y despliegues Camra:

- Spring Boot, Gradle multi-project, PostgreSQL, JPA, Flyway, Feign y los contratos HTTP versionados.
- Los límites `api-gateway`, `auth-service`, `guest-service`, `frontdesk-service`, `billing-service`, `fb-service`, `notification-service` y `config-service`. Los nombres de artefacto pueden mantenerse durante la transición y renombrarse en sitio al quedar libres de referencias hoteleras. No se crea un servicio Fixi paralelo.
- `internal-auth-lib`, contexto de tenant, HMAC service-to-service, nonce anti-replay en Redis, RBAC/Spring Security, rate limiting, errores API normalizados y pruebas de aislamiento.
- Configuración central, health checks, Micrometer/Actuator, Prometheus, Grafana, Loki, Zipkin, Alertmanager, circuit breakers, retries, Docker Compose, redes segmentadas, pgBackRest y workflows CI.
- La SPA React existente, sus clientes HTTP, estado de autenticación, localización, componentes, pruebas Vitest y E2E Playwright. Se reemplaza la experiencia hotelera por flujos Fixi dentro de la SPA.
- El asistente IA, su integración de proveedores, su contexto tenant, el límite de herramientas, el patrón lectura/propuesta y confirmación humana. Se cambia el dominio que entiende/ejecuta, no se descarta el asistente.
- Las capacidades de plataforma Fixi que no son del taller: registro/onboarding público de tenants, planes y capabilities, configuración/verticalización por tenant, backoffice de soporte, portabilidad de datos y límites de plan. Se implementan dentro de servicios/rutas Camra, no se eliminan por no ser flujo de reparación.

La transformación mantiene **una sola fuente de verdad por agregado** y las llamadas entre servicios pasan por contratos internos existentes con autenticación HMAC. No se debe crear una segunda pila, una segunda aplicación web ni una copia del mismo agregado mientras se migra. Se conserva la separación de experiencias pública, operativa y cliente que Fixi ya ofrece funcionalmente, pero se aloja en la SPA Camra existente mediante rutas/layouts y los servicios de dominio del frontend; no se requiere reproducir tres repositorios frontend.

## Dominio hotelero que desaparece

Retirar gradualmente, después de sustituir su uso y migrar/preservar los datos requeridos:

- Huésped como viajero, datos de ocupantes y estancia, documento de identidad para check-in, GDPR/retención hotelera y campos exclusivamente necesarios para Alloggiati.
- Reservación de alojamiento por fechas/noches, asignación y cambio de habitación, disponibilidad hotelera, check-in/out, ocupación, housekeeping y estados de cuarto limpio/sucio.
- Tarifas nocturnas, temporadas, calendarios, tipo de habitación y cotización de alojamiento que se convierte en reservación.
- Consumo de restaurante ligado a estancia, menú/cocina y cargos F&B a invoice de habitación.
- Factura cuyo origen/invariante sea estancia, reserva, huésped o cargo nocturno; exportación FatturaPA/SDI y datos fiscales italianos no requeridos por Fixi.
- Configuración hotelera, nombre/slug de hotel cuando sea solo identidad de propiedad, Alloggiati y texto/políticas de estancia.
- Roles `RECEPTIONIST`, `KITCHEN`, `HOUSEKEEPER` y `GUEST` como políticas de hotel, rutas `/rooms`, `/reservations`, `/stays`, `/rate-calendar` y `/fb` cuando dejen de ser contratos activos.

Antes de retirar cada bloque, determinar qué configuración y datos deben conservarse legalmente, exportarse o anonimizarse. No borrar ni modificar migraciones Flyway históricas para hacerlas parecer Fixi: el dominio hotelero se elimina por migraciones posteriores, después de backfill/validación y de expirar la ventana de compatibilidad.

## Dominio Fixi que lo sustituye

El producto resultante conserva dos capas explícitas: **plataforma SaaS** (tenant, planes/capabilities, onboarding, configuración por industria, usuarios/soporte/portabilidad) y **vertical operativa configurable**. El flujo de reparación documentado en Fixi es la primera vertical que Camra debe soportar por tenant y sucursal:

1. Crear/mantener clientes y sus datos de contacto.
2. Identificar los dispositivos que pertenecen al cliente, con marca/modelo/serie y atributos configurables por industria.
3. Recibir solicitudes/leads y convertirlas en órdenes; la recepción de un equipo crea evidencia, folio y estado de trabajo.
4. Administrar orden con diagnóstico, presupuesto versionado, autorización del cliente, asignación de técnico y transiciones de estado.
5. Registrar trabajo/tiempo, piezas consumidas/reservadas, movimientos de stock y tareas relacionadas.
6. Cobrar, registrar pagos/reembolsos y reflejar ingresos/gastos y caja sin confundir venta, factura y efectivo.
7. Entregar equipo, conservar documentos/evidencias y gestionar la garantía posterior.
8. Permitir al cliente consultar el estado mediante portal tokenizado y recibir notificaciones.

La verticalización de Fixi (industria, campos, módulos y límites por plan) debe mantenerse como capacidad de plataforma. Los catálogos configurables de dispositivo/campos son su primer uso; no se deben convertir en constantes globales de Camra ni retirar al eliminar la hotelería.

Fixi requiere tenant -> sucursales para las operaciones documentadas. `hotelId` no puede limitarse a un renombre textual: cambia el significado y el alcance. Toda orden, inventario, caja, usuario, reporte y movimiento debe declarar explícitamente si pertenece a tenant, sucursal o ambos. El modelo canónico Fixi permite membresías de una persona en varios tenants, mientras que el snapshot de implementación describe usuarios tenant-scoped; conservar el comportamiento actual inicialmente y habilitar membresías multi-tenant solo tras decisión explícita, diseño de sesiones/JWT y pruebas de cambio de tenant.

Estados, reglas de transición completas, campos fiscales mexicanos, política de sucursal para usuarios y medios de pago deberán fijarse en contratos de dominio antes de activar sus escrituras. Las fuentes disponibles dan estados ejemplo de orden, pero no un catálogo exhaustivo aplicable a todos los tenants.

## Capacidades Fixi que deben agregarse

| Capacidad | Ubicación primaria dentro de Camra | Relación y resultado esperado |
|---|---|---|
| Clientes | `guest-service`, evolucionado en sitio a contexto de clientes | Ficha tenant-scoped, sucursal/tag/contacto/consentimiento e historial; retirar semántica de huésped/ocupante. |
| Dispositivos y catálogos | `frontdesk-service`, como captura de equipo y catálogo configurable | Mantener primero los campos de equipo inline en la orden como en Fixi actual; tenant/industria define categorías y campos. Normalizar a entidad `Device` solo si la decisión de historial/propiedad lo requiere; no reutilizar `Room`. |
| Solicitudes | `frontdesk-service` | Bandeja pública/operativa, revisión, rechazo o conversión transaccional a orden y cliente. No reutilizar la transición quotation->reservation. |
| Orden, diagnóstico, presupuesto y autorización | `frontdesk-service` | Agregado que concentra intake, ciclo de trabajo, diagnóstico, versiones de presupuesto y estados; autorización del cliente separada de confirmación IA de un empleado. |
| Técnicos, work logs y tareas | `frontdesk-service` | Empleados/asignación, tiempo con pausas, tareas por orden y trazabilidad de quién trabajó. El usuario/rol procede de `auth-service`. |
| Garantías y documentos/evidencias | `frontdesk-service` | Ciclo post-entrega y metadatos/acceso seguro a evidencia; almacenamiento de archivos debe definirse sin reutilizar documentos de identidad. |
| Productos, proveedores, compras e inventario | `fb-service`, transformado en contexto de inventario/comercio | Catálogo SKU, proveedor, PO/recepción parcial, existencias por sucursal, ledger de movimientos y reservas consumibles/liberables. |
| POS | `fb-service` transformado | Venta inmediata de productos sin orden, ligada a sucursal, pago/caja e inventario; nunca “cargo a habitación”. |
| Pagos y finanzas operativas | `billing-service`, transformado en contexto de cobros/finanzas | Pagos, reembolsos, ingresos/gastos y reportes financieros con referencias a orden/POS/compra/caja. Conservar PDF/servicios donde sirvan. Aclarar por separado si Fixi necesita emitir CFDI y qué proveedor/reglas aplica; no conservar facturación de hotel por defecto. |
| Caja | `billing-service` o contexto de cobros, dentro del límite actual de billing mientras se valida el volumen | Turnos por sucursal, apertura/cierre, arqueo, discrepancia y regla de ventas en efectivo con caja abierta. Su dueño de agregado debe permanecer único. |
| Sucursales/tenant | `auth-service` durante la transición de `HotelRegistry` a organización/tenant y nuevas sucursales | Tenant como raíz; sucursales como dimensión operativa; usuario/rol y scopes coherentes. La fuente autoritativa de sucursales se fija antes de propagar FK. |
| Portal cliente | SPA React existente y endpoints públicos del servicio propietario de órdenes | Consulta por tenant/folio/token, estados, evidencia y aprobación autenticada o tokenizada con protección anti-abuso. |
| Reportes Fixi | reportes del servicio propietario de cada dato; agregación en endpoint de reporting existente (`billing-service`) solo con contratos | Operación/productividad/stock en datos del servicio dueño; ingresos/gastos/caja desde billing. Evitar joins cross-schema como API implícita. |
| SaaS público y onboarding | rutas públicas existentes de `auth-service`/gateway y experiencia pública de la SPA | Landing/signup, creación de tenant, owner y primera sucursal; preservar slugs y validación. No confundir alta de tenant con solicitud de reparación. |
| Planes y capabilities | paquete de plataforma de `billing-service` para monetización y policy checks coordinados con `auth-service` | Plan, estado/trial/suspensión, límites de uso y módulos habilitados por tenant. Separar suscripción SaaS de ingresos/gastos del taller. |
| Verticalización configurable | configuración tenant del servicio dueño y catálogos en `frontdesk-service` | Industria, campos requeridos, reglas de captura y módulos opt-in sin hardcodear “taller” en autenticación/gateway. |
| Backoffice/portabilidad | endpoints platform/admin de `auth-service` y jobs dentro de los servicios dueños | Soporte excepcional auditado, export/import aislado por tenant y operación de planes; no dar acceso silencioso ni omitir RLS-equivalent. |

La ubicación es una asignación inicial dentro de los servicios existentes, no autorización para crear otro sistema. Si a futuro se renombra un deployable, se hace en sitio conservando el historial de CI/deploy, no con coexistencia de dos backends de dominio.

## Transformación de cada microservicio

### guest-service -> servicio de clientes

- **Responsabilidad técnica que conserva:** API de personas, búsqueda/paginación, validación, tenant scope, persistencia, protección de PII, soft delete, integración Feign y control de errores.
- **Responsabilidad hotelera que desaparece:** viajero/ocupante de estancia, datos de check-in/identificación policial, Alloggiati y retención ligada a estancia/factura hotelera. Mantener solo obligaciones de privacidad que apliquen a los clientes Fixi.
- **Responsabilidad Fixi:** ficha de cliente de negocio; contactos, etiquetas, estado activo, vínculo opcional a sucursal y datos usados por solicitud/orden/pago.
- **Entidades:** evolucionar `Guest` a `Customer` en el mismo servicio si hay registros útiles. Sustituir `GuestPrivacySettings` por preferencias/consentimientos aplicables. Sustituir `IdentityDocument` por modelo de adjuntos solo si Fixi requiere identificaciones, nunca reutilizarlo como foto/evidencia del dispositivo.
- **Campos:** retirar `dateOfBirth`, datos de ocupantes, ciudadanía/género/documentos de viaje, campos `comune/provincia/CAP` y FatturaPA cuando no sean requeridos. Conservar nombre/contacto/dirección únicamente según producto. Añadir `tenantId`, `branchId` nullable si aplica, tipo persona/empresa, parent/contact, tags, estado bloqueado/activo, preferencia y consentimiento de contacto/datos según el modelo implementado Fixi. No asumir que cliente necesita login: portal customer puede identificarse por token/folio. Definir con Fixi los campos fiscales CFDI, no copiar ni descartar sin validación fiscal.
- **Endpoints:** evolucionar `/api/v1/guests` y `/search` a `/api/v1/customers` y búsqueda equivalente. Retirar `/documents` de identidad, privacy-settings y endpoints GDPR específicos de huéspedes tras migración/retención. Añadir CRUD, búsqueda, archivo y lecturas de contexto necesarias para orden/portal. Durante compatibilidad, el alias `/guests` debe delegar al mismo servicio/DTO adaptador, no guardar duplicado.
- **Estructura/reglas:** mantener controller-service-repository-client, circuit breakers de dependencias útiles, mapper y validación. Eliminar elegibilidad basada en “última estancia”, reserva activa y fecha última factura hotelera; incorporar tenant/branch, deduplicación y visibilidad/archivo de clientes.
- **Pruebas:** adaptar CRUD, tenant isolation, búsqueda, soft-delete, Feign/fallbacks y pruebas de datos personales; crear tests de sucursal, duplicados, conversión solicitud-cliente-orden y scopes del portal.

### frontdesk-service -> núcleo de operaciones de reparación

- **Responsabilidad técnica que conserva:** límite transaccional del ciclo operativo; endpoints, DTOs/validación, repositorios, servicios, coordinación con otros servicios, Feign, estados explícitos, optimistic locking donde proceda, errores, aislamiento tenant y resiliencia.
- **Responsabilidad hotelera que desaparece:** `Room`, `RoomType`, `RateSeason`, `RateCalendar`, `Reservation` de alojamiento, `Stay`/check-in/out, ocupación, housekeeping, Alloggiati, quotation de noches y cargos a habitación.
- **Responsabilidad Fixi:** recepción/solicitud, dispositivos, órdenes, eventos/historial, diagnóstico, presupuestos, autorizaciones, asignación técnica, work logs, tareas, entrega, garantía y evidencia. Mantener AI en el mismo deployable hasta separar sin duplicar autoridad.
- **Entidades:** `Stay` y `Reservation` **se sustituyen** por el agregado físico equivalente a `service_orders` y por `service_requests`; no renombrar tablas hoteleras en sitio ni modelar `Reservation` como solicitud. La orden Fixi actual captura dispositivo inline; empezar así usando los catálogos/campos configurables existentes. El modelo canónico propone `devices`, pero el snapshot/Spec 01 dicen que el equipo vive en `service_orders`; acordar si el historial T09 basta con identificadores o si una tabla normalizada es objetivo posterior. `Quotation` hotelera se sustituye por presupuestos de reparación versionados/autorizados; los campos actuales `estimated_cost/final_cost` y `service_order_authorizations` no prueban por sí solos que exista un agregado físico completo de quotes. Añadir/evolucionar checklist, diagnosis, quote/version/items, authorization, `WorkLog`, `Task`, `Warranty`, `OrderEvent` y `OrderDocument` según el estado físico de cada tabla Fixi, evitando duplicar las ya implementadas.
- **Campos a retirar:** `roomId`, `roomNumber`, `reservationId` hotelera, `guestId` (reemplazar por `customerId`), ocupantes, check-in/out dates/times, `alloggiati*`, estado housekeeping, nightly rate/season y `invoiceId` que presupone folio de estancia. **Añadir/definir:** `tenantId`, `branchId`, `customerId`, campos inline de dispositivo/categoría según catálogo, folio/idempotency key, prioridad, checklist/condición/serie, descripción/falla, estado y `version`, técnico asignado, diagnóstico, presupuesto/version vigente y autorización snapshot, work logs, eventos y timestamps de custodia/entrega/garantía. `deviceId` se incorpora solo tras decisión de normalización/historial.
- **Endpoints:** `/api/v1/stays`, `/reservations`, `/rooms`, `/room-types`, `/rate-calendar`, `/quotations` hoteleras evolucionan o desaparecen. No hacer que esos paths apunten a datos Fixi con DTO hotelero. Contratos nuevos/evolucionados deben cubrir `/api/v1/service-requests` (crear/listar/revisar/rechazar/convertir), `/api/v1/service-orders` (crear/listar/obtener/transicionar/asignar/entregar), subrutas de checklist, diagnosis, quote/authorization, work-logs, tasks, warranty, device catalogs y documents. Verificar contra rutas/migraciones Fixi concretas y usar aliases solo mientras exista consumidor; no presuponer que quote/version o entidad Device ya son físicas.
- **Servicios:** `StayServiceImpl` no se renombra mecánicamente: su orquestación sirve de patrón, pero validadores de check-in, room occupied, factura al check-in y checkout se eliminan. Sus límites colaboradores se transforman en validación de solicitud/cliente/dispositivo, stock, pago y notificación. `QuotationServiceImpl` conserva patrones de edición/PDF/email solo donde el contrato de estimate Fixi lo requiera.
- **Reglas a eliminar/incorporar:** eliminar “checkout exige invoice pagada”, ocupación y check-in válido; incorporar transiciones atómicas/optimistic lock, idempotencia, autorización cliente ligada a versión exacta de presupuesto, consumo de piezas sujeto a stock, historial actor/request id y entrega/garantía.
- **Pruebas:** adaptar controller/service/repository/security, transacciones, fallas Feign, concurrencia y notificación. Sustituir tests de check-in/out, ocupación, reservation/room availability. Crear suite de máquina de estados, conversión transaccional request->customer/order, presupuesto/cambio de versión, doble autorización, work-log, asignación, cancelación/liberación de stock, entrega, garantía, evidencia y cross-tenant/branch.

### billing-service -> cobros, documentos financieros y finanzas operativas

- **Responsabilidad técnica que conserva:** límites de cobro, decimales, secuencias/documentos, pagos, PDF, cliente Feign, errores idempotentes, reportes tenant-scoped y resiliencia.
- **Responsabilidad hotelera que desaparece:** abrir invoice al check-in, resolver invoice por reserva/estancia/huésped, cargos nocturnos, bloqueo de checkout, folios hoteleros y exportación fiscal italiana FatturaPA/SDI donde no sea requerida.
- **Responsabilidad Fixi:** pagos de órdenes/POS, saldo pendiente, reembolsos ligados al pago original, ingresos/gastos operativos, reportes P&L, documentos de venta y caja o interfaz de caja por sucursal.
- **Entidades:** `Payment` puede evolucionar porque sigue siendo un pago monetario, pero sustituir `invoiceId` como relación única por referencia tipada al origen de cobro/orden/venta, preserving idempotency/audit semantics. `Invoice` solo puede evolucionar si Fixi requiere documento fiscal/comprobante; no convertirla en orden ni asumir que el ledger operativo es factura. Añadir `FinanceEntry`/categorías/refunds si no existen como agregado claro y sesiones/cierres de caja en un único dueño definido. Mantener renderer PDF adaptando plantillas.
- **Campos:** retirar `stayId`, `reservationId`, `guestId` como obligatorios, folio por hotel, VAT italiano/SDI/FatturaPA/Alloggiati dependencies y `unitPrice/nights` exclusivos de estancia. Añadir `tenantId`, `branchId` cuando aplique, `customerId`, `serviceOrderId`/sale/purchase references, operation/payment idempotency, origin type, payment/refund parent, expense/income category, reconciliation status, cash shift/user/opening/closing/discrepancy. Campos CFDI y requisitos legales **requieren definición fiscal**, no inferencia desde los campos actuales.
- **Endpoints:** `/api/v1/invoices/stay`, `/stay/{id}/charges`, `/reservation/...`, `/guest/...` desaparecen al eliminar sus consumidores. Mantener temporalmente `/invoices` solo si la factura fiscal sigue siendo producto; definir `/payments`, `/finance/entries`, `/refunds`, `/cash-registers`/shifts, `/reports/finance` según ownership final. Crear cargos desde orden/POS con contrato idempotente; no adaptar parámetro `stayId` a `orderId` sin versionar DTO.
- **Servicios:** `PaymentServiceImpl`, control de versión, lock de secuencia y PDF son candidatos estructurales. Reescribir `InvoiceServiceImpl` y `StayBillingCoordinator` donde su semántica parte de huésped/estancia. Separar factura legal emitida de movimiento de caja/ingreso operativo.
- **Reglas:** eliminar checkout-pagado y invoice abierta por stay; añadir balance/abonos/reembolso, unicidad por tenant, autorización para override, no borrar asientos financieros, recepción de ingresos/gastos con referencias y reconciliación caja/POS/compras.
- **Pruebas:** adaptar payment amount/method/status, concurrency, PDF, reportes, tenant isolation y exception handlers. Crear parciales, refund parent, doble webhook/idempotency, pagos por orden/POS, expense de compra, caja abierta/cierre y conciliación. Añadir tests fiscales solo cuando el requisito CFDI quede definido.

### fb-service -> inventario, procurement y ventas POS

- **Responsabilidad técnica que conserva:** servicio de catálogo/transacciones de venta, precio autoritativo server-side, líneas, cálculo total con `BigDecimal`, tenant scope, control de estados, paginación, cliente HTTP, fallos y controller/service/repository.
- **Responsabilidad hotelera que desaparece:** menú/cocina, `RestaurantOrder`, `OrderItem` con consumo, estado preparado y vínculo obligatorio a estancia/room/guest; cargo de venta F&B a billing invoice de cuarto.
- **Responsabilidad Fixi:** productos/refacciones, SKU/coste, niveles de inventario por sucursal, mínimos, reservas por orden, ledger de movimientos, transferencias, proveedores/PO y recepción, venta POS inmediata, ticket y relación con pagos/caja/finanzas.
- **Entidades:** `MenuItem` y `RestaurantOrder` se sustituyen por `Product`, `Supplier`, `PurchaseOrder`/line items, `BranchInventory`, `InventoryReservation`, `InventoryMovement`, `PosSale`/sale lines. No conservar nombres F&B como fachadas de dominio. Mantener agregados propios y atomicidad local; integración con billing debe ser segura ante retry.
- **Campos:** retirar `stayId`, `guestDisplayName`, `roomNumber`, `menuItemId`, `BILLED_TO_ROOM`, charge description F&B, preparado/cocina. Añadir `tenantId`, `branchId`, `sku`, `cost`, price, unit, category, tax metadata (a definir), `quantity`, `minStock`, source/destination branch, reservation status, supplier/order/status, received quantities, payment method, cash shift, idempotency key y referencias a repair order.
- **Endpoints:** retirar `/api/v1/fb/menu-items`, `/fb/orders`, `/stay/{stayId}`, `/confirm` con semántica de cargo a habitación. Exponer en el mismo servicio rutas de productos, existencias, movimientos, reservations, suppliers, purchase orders/receipts y POS. Conservar gateway, DTO validation y método de lookup server-side de precios, ahora SKU/producto; añadir transacciones PostgreSQL/RPC-like con constraints/locking.
- **Servicios:** `RestaurantOrderServiceImpl` es patrón para total server-side y validación de status, pero eliminar calls a `StayClient` y `BillingClient.addCharge`. Evolucionar la interfaz pública del deployable `fb-service` a inventario/comercio en sitio; tras completar routing, renombrar paquete/artefacto si conviene, sin desplegar un segundo sistema competidor.
- **Reglas:** quitar “solo stay CHECKED_IN”; añadir no negativo/concurrencia de stock, transferencias atómicas, reserva activa->consumida/liberada, receipt parcial/total y gasto asociado, POS inmediato decrementa stock y ventas de efectivo bloqueadas sin caja abierta.
- **Pruebas:** reutilizar tests de precio server-side, tenant IDOR, estados, errores y total. Reemplazar pruebas StayClient/BillingClient F&B. Crear concurrencia/reservation races, atomic transfer, stock floor/min stock, SKU tenant, recepción parcial, PO->stock/expense, idempotent POS, refund/reversal e integración con órdenes.

### notification-service -> notificaciones Fixi multicanal

- **Responsabilidad técnica que conserva:** envío aislado, SMTP, Thymeleaf/plantillas, adjuntos PDF, locale/remitente, mascarado de email, autenticación interna y manejo de errores.
- **Responsabilidad hotelera que desaparece:** plantillas de confirmación de reserva, check-in/out, hotel name como única marca y adjuntar invoice de estancia.
- **Responsabilidad Fixi:** avisar a cliente/técnico sobre solicitud recibida, ingreso/diagnóstico, presupuesto, autorización, cambio de estado, tarea, pago, entrega/garantía; exponer resultados y reintentos con trazabilidad.
- **Entidades/campos:** no hay agregado de cola generic identificado en Camra. Añadir representación de evento/outbox/delivery si se necesita retry durable: tenant, recipient/channel, template, object reference, idempotency, status/attempts/next attempt/safe error. Cambiar `guestEmail`, `hotelName`, `reservation/stay` por cliente/actor Fixi.
- **Endpoints:** evolucionar `/internal/notifications/reservation-confirmed`, `/checkin`, `/checkout`, `/quotation` a contratos internos por evento/plantilla Fixi, conservando auth HMAC. Añadir dispatch status/retry si operaciones Fixi lo requiere. WhatsApp/WebPush se integra como canales, no como endpoint que filtra PII.
- **Servicios/reglas:** conservar renderer y SMTP; sustituir plantillas y fallbacks. Reintentos seguros con idempotencia y consentimiento/preferencias por destinatario; no exponer errores/secretos del proveedor.
- **Pruebas:** adaptar render locale, attachments, sanitización, masking y fallos SMTP. Crear delivery retry/idempotency, tenant template isolation, opt-out, WhatsApp/WebPush result, límites/PII y ausencia de duplicados.

### auth-service -> identidad y organización tenant/sucursal

- **Responsabilidad técnica que conserva:** credenciales, JWT access/refresh, token version, login attempts, refresh revocation, user admin, password rotation, roles y onboarding.
- **Responsabilidad hotelera que desaparece:** `HotelRegistry` entendido como hotel y rol/claims/acoplamiento a hotel único.
- **Responsabilidad Fixi:** registro/onboarding de tenants, owner inicial, sucursales, usuarios, roles, acceso de plataforma/soporte y contexto tenant/branch. La capacidad de planes/capabilities se coordina con billing, no se limita a empleados del taller.
- **Entidades:** `HotelRegistry` evoluciona a `Tenant` en la misma raíz/servicio; añadir/evolucionar `Branch`, plan/status/modules-config y configuración industrial. `UserAccount` evoluciona conservando credenciales/IDs cuando posible. Eliminar perfiles hoteleros `GUEST/KITCHEN/HOUSEKEEPER`; roles Fixi incluyen owner/admin/receptionist/technician/cashier/supervisor y acceso interno separado. La membership multi-tenant del modelo canónico contradice el usuario tenant-scoped del snapshot actual; no añadirla como hecho hasta cerrar decisión.
- **Campos:** claim/header `hotelId` -> `tenantId`; `hotel_name/slug` -> organización/slug; branch scope y plan/status/capabilities según ownership acordado. Mantener contextos de idioma/zona horaria y config legal/privacidad por tenant donde sean genéricos. Revisar unicidad de username/email y JWT antes de alterar constraints.
- **Endpoints:** `/api/v1/auth/login`, refresh/logout/me y usuarios evolucionan preservando flujo. `/platform/hotels` evoluciona a administración de tenants; onboarding Fixi público debe crear tenant, owner y sucursal inicial de forma atómica. Agregar/transformar operaciones de sucursales, estado/plan/capabilities y soporte si auth-service es autoridad. No cambiar claims con JWT activos.
- **Servicios/reglas:** mantener password encoder, login protection, token revocation y refresh. Adaptar onboarding y suspensión/expiración de tenant; plan limita features sin impedir exportación/portabilidad permitida. `HotelSettings` no es el registro SaaS completo.
- **Pruebas:** adaptar login/refresh/logout, token version, must-change-password, user admin, RBAC y onboarding. Crear pruebas de tenant + branches, signup público atomizado, plan activo/suspendido, claims legacy y, solo si se aprueba, usuario multi-tenant/cambio explícito de contexto.

### api-gateway -> conservar edge y evolucionar contratos

- **Responsabilidad técnica que conserva:** routing, JWT verification, RBAC, headers autenticados, HMAC, CSRF, security headers, client IP confiable, Redis rate limits, CORS y docs.
- **Responsabilidad hotelera que desaparece:** path policy `/rooms`, `/reservations`, `/stays`, `/fb`, excepciones de roles de hotel y cabeceras `X-Auth-Hotel`.
- **Responsabilidad Fixi:** misma puerta de entrada a APIs de plataforma (signup, tenants, planes, capabilities, soporte/portabilidad), operación (clientes/solicitudes/órdenes/inventario/POS/finanzas), portal y asistente.
- **Entidades/campos:** sin persistencia de dominio; cambiar claim/header a `tenantId`, añadir branch scope solo desde autorización de servidor y firmarlo también en el payload interno. No confiar en branch enviada por browser.
- **Endpoints:** agregar rutas nuevas al mismo upstream deployable durante rollout; apuntar paths nuevos al servicio dueño. Mantener paths antiguos solo como aliases de transición autorizados y medidos, luego eliminarlos. Ajustar rate limits públicos para intake/portal/webhook y autenticados por usuario/tenant; no abrir webhook como ruta común.
- **Servicios/reglas:** conservar filter chain/KeyResolver. Rehacer allowlists con roles Fixi y garantizar que cada ruta tiene auth/CSRF/rate-limit adecuado. Gateway RBAC es defensa perimetral, no sustituto del control en servicio.
- **Pruebas:** adaptar `AuthenticationFilterTest`, RBAC, CSRF, public booking filter (reemplazar su política por request/portal pública), client IP y rate-limit. Crear route coverage por path Fixi, spoofed tenant/branch header, rollout aliases y rate limits públicos/privados.

### config-service -> configuración runtime común

- **Responsabilidad técnica que conserva:** Spring Cloud Config, autenticación de lectura de config, health/metrics y externalización de parámetros.
- **Responsabilidad hotelera que desaparece:** perfiles/valores específicos de reservation/stay/room/fb y nombres hoteleros incrustados en configuración.
- **Responsabilidad Fixi:** entregar configuración por servicio/ambiente para rutas, JWT, HMAC, DB, Redis, resiliencia, proveedores, notificaciones, límites, CORS y observabilidad. La configuración de tenant/industria y `plan_capabilities` sigue como datos de producto, no como property global de Spring Config.
- **Entidades/campos:** conservar configuración externa versionada; no duplicar preferencias tenant aquí si la app requiere escritura/consulta dinámica. `HotelSettings` actualmente persistido en frontdesk se transforma en settings Fixi de tenant/branch en el servicio dueño, con secretos cifrados y nunca filtrados por API.
- **Endpoints:** Config Server conserva su API interna; actualizar perfiles de `frontdesk-service`, `fb-service`, `billing-service` al rol nuevo. Eliminar flags Alloggiati/F&B/rooms cuando sus consumidores hayan sido retirados.
- **Pruebas:** adaptar auth Config Server, perfiles, health y placeholders; agregar test de config sin claves hoteleras, missing-secret fail-fast y perfiles Fixi para cada servicio.

### frontend -> una SPA Camra convertida al producto Fixi

- **Responsabilidad técnica que conserva:** React, router, auth store, componentes, client services, i18n, forms, manejo de errores, tests unitarios y e2e.
- **Responsabilidad hotelera que desaparece:** pantallas de Rooms/Reservations/Stays/Restaurant/Hotel Settings/rate seasons y textos/roles hoteleros.
- **Responsabilidad Fixi:** experiencias SaaS pública (landing/onboarding/planes), backoffice operativo (solicitudes, recepción, órdenes, clientes/dispositivo, diagnóstico/estimate/approval, técnicos/worklogs, stock/PO/POS/caja, pagos/tareas/garantías), administración/configuración/seguridad y portal cliente.
- **Entidades/types:** `Guest` -> Customer DTO; eliminar tipos Room/Stay/Reservation/F&B. Añadir tipos de dispositivos, solicitud, orden, diagnóstico, estimates versionados, authorization, worklog, stock/movement/reservation, supplier/PO/sale, finance, cash, warranty, evidence y branch.
- **Endpoints/clientes frontend:** `guestService`, `stayService`, `reservationService`, `inventoryService` hoy mezclando habitaciones, `fbService`, `quotationService`, settings y `assistantToolService` deben evolucionar en sitio y consumir APIs nuevas. Retirar client methods antiguos cuando no queden callers.
- **Páginas:** reutilizar shell, navegación, login/settings técnicas, tabla/form/modal donde aplique; sustituir pantallas hoteleras y conservar las tres experiencias Fixi (public, admin y cliente) como rutas/layouts claramente aislados dentro de la SPA Camra. El repo Fixi las distribuye en tres apps; para mantener Camra como base se pueden consolidar en su frontend/deploy actual sin perder landing, signup/planes, backoffice, portal cliente ni separación de sesión. No crear un repo/app paralelo.
- **Pruebas:** mantener auth, accesibilidad, layout, utilidades, API mocks y confirmación IA. Reemplazar tests hoteleros por journeys Fixi; agregar roles, estados, branch scope, formulario intake, transición autorizada, stock, caja, portal/token y mensajes de error/retry.

## Transformación de base de datos

### PostgreSQL y Flyway

- Mantener PostgreSQL, usuarios/DB separados por servicio si así está configurado, pool, backups pgBackRest, healthchecks y políticas de conexión. Fixi actual usa Supabase/Postgres/RLS/Storage; esa es evidencia de garantías y capacidades, no un requisito para sustituir PostgreSQL/Flyway de Camra, porque Camra es la base de ejecución elegida.
- Mantener migrations Flyway **append-only**. No editar V1-V23 desplegadas ni renumerar migraciones. Crear migraciones de expansión: nuevas tablas/columnas nullable, índices, FK compuestas `(tenant_id, id)` donde corresponda y constraints de consistencia. Backfill verificable; luego constraints not-null; finalmente retirar columnas/tablas hoteleras en una migración de contract después de compatibilidad y backup.
- Reemplazar `hotel_id` en todos los roots por `tenant_id` como nuevo concepto. Se puede conservar físicamente el UUID en primera etapa para evitar migración insegura, pero no se considerará terminado hasta que claims, nombres, índices/FKs y política reflejen tenant/sucursal. Si se renombra columna, hacerlo expand/dual-read/backfill/dual-write temporal/switch/contract; nunca aceptar tenant del DTO como autoridad. Camra debe conservar garantías equivalentes a RLS mediante validación de contexto/query/repository y pruebas; no afirmar que el framework Camra es RLS de PostgreSQL.
- Introducir `branch_id` en órdenes, stock, ventas, caja, usuarios y reportes con reglas explícitas. Catalog/producto puede ser tenant-wide mientras stock y POS son branch-scoped. Garantizar que `(tenant_id, branch_id)` pertenece al tenant.
- Definir IDs/relaciones cross-service como referencias API/UUID, evitando FKs que cruzan DB/schema de servicios. Reforzar scoping en repositories, service methods, índices únicos y ArchUnit para cada tenant-root y branch-root.
- Migrar datos útiles: huésped->cliente solo con mapping/conservación de consentimientos válidos; users/owner->tenant/roles; documentos/registros con tratamiento de privacidad decidido. No convertir `Stay` a `ServiceOrder` ni `Room` a `Device` automáticamente. Exportar/archivar datos hoteleros necesarios y eliminar según retención.
- Crear/evolucionar roots según diferencias físicas: customer, service_request, service_order/event/checklist/document/authorization/warranty, inventory reservation/movement/PO/POS/cash/finance ya son capacidades físicas en Fixi, pero deben migrarse a ownership Camra. Dispositivo actualmente inline en orden puede seguir inline hasta decisión de normalización. Presupuesto versionado, work logs y comisiones deben comprobar tablas reales antes de crear; el modelo canónico no prueba que ya existan físicamente. Añadir también tenants, plans, capabilities, config por industria, soporte/portabilidad según alcance aprobado.
- Transacciones atómicas se mantienen dentro de un servicio/DB. Los flujos cross-service (request->order, order->reserve/consume stock, order->charge/notify) necesitan idempotency keys y estado recuperable/outbox o mecanismo durable existente; circuit breaker solo no garantiza entrega/consistencia.

### Redis

- **Conservar** Redis como backend de Spring Cloud Gateway rate limiter y store compartido de nonce anti-replay HMAC. Conservar password, redes privadas, ACL/secret management y healthcheck.
- Cambiar keys/metrics `hotel` por `tenant` donde aparezcan. Añadir sesión de conversación IA con key tenant+user y expiración si no hay ya almacenamiento adecuado.
- No asumir que Redis ya equivale a una cola durable: no convertirlo en sistema de verdad para órdenes/pagos/movimientos sin garantías requeridas. La cola Fixi de notificaciones/automatizaciones requiere persistencia, idempotencia, retries y recuperación validados; puede emplear el patrón/infra existente, con DB como durable source según diseño de fase.
- Tests: nonce atómico y expirable, rate limits por user/IP/tenant, expiración de conversaciones, pérdida/reinicio Redis y fallos del backend sin saltarse auth.

## Transformación del frontend

1. Mantener el frontend/deploy Camra como base y navegación SPA. La navegación ofrece secciones pública, operativa/admin y cliente, con landing/onboarding SaaS, plan/capabilities y sucursal activa según el rol.
2. Reutilizar sesión, guardas de autenticación, componentes de tabla/formulario, i18n y API base; los permisos visibles siguen siendo espejo, nunca autoridad.
3. Sustituir primero páginas hoteleras por los recorridos Fixi manteniendo el shell SPA; incorporar también signup/alta de tenant, elección/estado de plan, configuración por industria y selector de módulos. Flujos operativos: intake/cliente/equipo; diagnóstico/estimate; aprobación; trabajo/piezas; pago/entrega; POS/stock/compra/caja.
4. Añadir branch selector solo si usuario tiene scopes múltiples; al cambiar sucursal actualizar datos y validar en backend, no simplemente filtrar client-side.
5. Portal público SaaS: onboarding/planes/tenant slug; portal cliente separado funcionalmente: token de orden/folio con estado, documentos/evidencias permitidas y autorización. No reutilizar booking de hotel. Restringir enumeración, expiración/revocación, rate limit y datos sensibles.
6. Asistente: misma experiencia de chat y UI de aprobación; enriquecer resúmenes con la operación elegida y mostrar claramente qué lectura se ejecutó, qué mutación está pendiente y resultado.
7. Sustituir rutas, etiquetas y roles en i18n; no mantener componentes hoteleros invisibles que siguen llamando APIs antiguas.

## Transformación de IA y tools

La IA actual permanece como motor conversacional. `AssistantService` conserva cliente Ollama/DeepSeek, timeout, cifrado de credenciales, métricas de tokens, circuit breaker/retry/rate limiter y formato de conversación. `AssistantController` conserva autenticación, obtención server-side de tenant/roles y body validado. `LocalIntentRouter`, `DeterministicParser`, `ConversationSessionStore` y fallback conservan sus patrones, pero dejan de tener intents check-in/batch huéspedes como dominio principal.

### Catálogo destino de herramientas

Seguir con la separación vigente:

- `consultar_fixi`: allow-list de operaciones de lectura, paginación acotada, resultados mínimos y tenant/sucursal derivados de seguridad.
- `proponer_accion_fixi`: allow-list de operaciones mutativas. La IA prepara DTO y explicación; la persona autorizada inspecciona y confirma en UI. El backend vuelve a validar la autorización y el estado al ejecutar.

Grupos de tools que reemplazan las operaciones hoteleras:

| Grupo | Lecturas a habilitar | Mutaciones propuestas sujetas a confirmación |
|---|---|---|
| Clientes | buscar/listar/obtener cliente, historial permitido | crear/actualizar/archivar cliente |
| Dispositivos | buscar catálogo, listar/obtener dispositivos del cliente | registrar/actualizar dispositivo |
| Solicitudes/órdenes/estados | buscar solicitudes, ver orden/timeline, disponibilidad de técnico/estado | crear solicitud, convertir, crear/asignar orden, transicionar/pausar/reanudar/cerrar según reglas |
| Diagnóstico/presupuesto | leer diagnóstico y versiones de estimate, consultar autorización | registrar diagnóstico, proponer/versionar estimate, enviar, cancelar; nunca autoaprobar consentimiento del cliente |
| Inventario/piezas | consultar SKU/stock/sucursal/reservas/movimientos | crear/ajustar/transferir/reservar/liberar/consumir pieza, con reglas y permisos estrictos |
| Técnicos/worklogs/tareas | listar técnicos, asignaciones, tiempos/tareas | asignar técnico, start/pause/stop worklog, crear/asignar/completar task |
| Pagos/POS/finanzas/caja | leer balance, pagos, resumen y estado de caja según rol | registrar pago/reembolso, cerrar caja, crear venta; alto impacto financiero requiere confirmación reforzada |
| Garantías/evidencias | consultar garantía, estado y metadatos permitidos | registrar garantía/evidencia, nunca exponer binarios/PII innecesarios |

### Reglas invariantes de IA

- No confiar en UUID/tenant/branch enviado por LLM; buscar entidades por datos que el usuario proporcione y resolver IDs en el servicio autenticado. Nunca inventar cliente/dispositivo/orden ni disponibilidad.
- Mantener roles en catálogo y autorización real en API; el tool catalog reduce posibilidades, no reemplaza RBAC/tenant/branch checks.
- Validar esquema estricto en servidor para cada tool. Incluir tool registry/dispatch en backend como autoridad; el cliente React puede validar y presentar, pero no ser único ejecutor/autorizador de acciones.
- Lecturas pueden procesarse automáticamente, con límites de tamaño/paginación/PII. Mutaciones deben devolver una propuesta estructurada con operación, campos, objetos afectados, costo/impacto, sucursal y versión del agregado.
- Confirmación ligada a usuario autenticado y versión esperada; volver a evaluar permisos, saldo, stock, estado de orden y autorización vigente al ejecutar. Una confirmación no sustituye aprobación del cliente del presupuesto.
- Idempotencia server-side para ejecución; proteger replay/doble click. Cancelar propuesta no hace cambios. Acciones no reversibles (refund, cierre de caja, consumo/transfers) deben presentar consecuencias y pedir confirmación explícita.
- Limitar cantidad de rondas/tool calls, tokens, timeout y salida; sanear logs y excluir documentos/secretos. Contexto conversacional tenant/user scoped y expiración apropiada.
- Tests: catálogo por rol, tool desconocida/rechazada, JSON/schema malicioso, cross-tenant/branch, stale version, propuesta cancelada, doble ejecución, stock insuficiente, pago/caja inválidos y error del proveedor/fallback sin mutación.

## Transformación de WhatsApp

- Mantener `EvolutionService` como adaptador y conservar QR/status, timeouts, normalización de estado y respuestas que no filtran credenciales.
- Retirar `/api/v1/stays/whatsapp` como ruta de producto y moverlo a una ruta de integraciones/mensajería Fixi. El identificador de instancia debe derivar de tenant/integration, no hotel. Revisar si un número se conecta por tenant o por sucursal; la especificación Fixi no resuelve esa granularidad.
- Completar el hueco explícito de Camra: webhook entrante autenticado/verificado, resolución de integración/tenant, normalización de mensajes, deduplicación/idempotencia, correlación de conversación con cliente/orden, respuesta saliente, estado de entrega y retry durable. No dar por hecho que pairing y QR son mensajería completa.
- Recorrido objetivo: mensaje -> validar origen y rate limit -> resolver tenant/cuenta -> cargar contexto mínimo de cliente/orden -> IA/intent router -> lectura o propuesta tool -> confirmación humana cuando muta -> acción API -> respuesta/notificación -> registro operativo seguro.
- Proteger prompt injection, abuso/volumen, exposición de folios ajenos, teléfonos, documentos y datos fiscales. Las acciones de pago, cierre de caja, garantía/autorización o modificación de orden nunca deben ser ejecutadas únicamente por la inferencia LLM.
- Tests contract/integration de QR, tenant instance isolation, firma del webhook, evento repetido, conversación cruzada, proveedor caído, reply y redacción de logs.

## Compatibilidad y eliminación progresiva del dominio hotelero

1. Establecer contrato Fixi y taxonomía de tenant/branch/roles/estados antes de abrir escrituras nuevas; registrar baseline de rutas, datos y tests Camra.
2. Usar **expand -> migrate -> switch -> contract**. Las migraciones Flyway históricas no se reescriben. Añadir tablas Fixi y cambios compatibles; implementar adapters internos cuando necesite convivir temporalmente un contrato, pero con una única escritura/fuente canónica.
3. En APIs externas, preferir rutas nuevas `/customers`, `/service-requests`, `/service-orders`, `/devices`, `/inventory`, `/pos`, `/finance`. Un alias temporal `/guests` puede mapear Customer sólo si campos/semántica son compatibles y queda marcado deprecated; no aliasar `/stays` a orders ni `/reservations` a stock.
4. Actualizar clients Feign y frontend en la misma fase que provider contracts. Feature flag por endpoint/tenant puede controlar exposición, pero no mantener dos motores de orden/inventario.
5. Backfill datos que tengan destino legítimo con conteos, checksums/invariantes y rollback operacional; bloquear writes/dual-write solo por una ventana controlada. Mantener old readers durante despliegue rolling; eliminar consumidor antiguo antes de quitar columnas/endpoints.
6. Registrar telemetría de llamadas a rutas antiguas y rechazar nuevos writes hoteleros; al llegar a cero consumidores y pasar tests/retención, retirar Gateway route, controller/client/DTO/entity y luego DB en contract migration.
7. Mantener capacidades infra (HMAC, Redis, auth, metrics, backups) activas durante toda la transición. La retirada hotelera no puede reducir auth o aislamiento temporalmente.

## Orden recomendado de transformación

### Fase 0: inventario, decisiones y gates

- Usar el `FIXI_PRODUCT_SPEC.md` del repo Fixi y contrastarlo con rutas/migraciones actuales, `SOURCE_OF_TRUTH.md`, modelo maestro y specs T01-T20. Para cada discrepancia anotar comportamiento implementado, objetivo aprobado, impacto y decisión requerida.
- Cerrar explícitamente: usuario en un tenant vs membership multi-tenant; ciclo físico actual de estados vs nueve estados de Spec 01 vs lista del modelo maestro; equipo inline vs tabla `devices`; presupuestos versionados físicos vs campos/casos actuales; reservas de stock/cola ya presentes vs specs antiguos que las marcan como pendientes.
- Confirmar requisitos fiscales CFDI, pagos, documentos, retención, soporte y scope de portal; inventariar contratos/consumidores Camra de controllers, Feign, gateway y assistant tools; snapshot de schemas, migrations, tenants, pruebas y backup/restore.
- Acordar tenant/branch, ownership de agregados, roles/scopes, plan/capabilities, verticalización, máquina de estados, nomenclatura API y política de compatibilidad. No renombrar tablas históricas Fixi como si se migrara Fixi; en Camra crear el modelo destino vía Flyway.

### Fase 1: preparar columna vertebral multi-sucursal Fixi

- Evolucionar auth/org hacia tenant, sucursal, onboarding público y roles/scopes; conservar primero el contrato single-tenant de usuario si es el comportamiento Fixi confirmado. Propagar contexto `tenantId` firmado por Gateway/HMAC y crear tests tenant + branch.
- Incorporar en los límites existentes Tenant/Plan/plan capabilities, trial/suspensión, signup/tenant slug/owner/sucursal inicial y configuración de industria/módulos. Separar policy de plan SaaS de la configuración global de Spring.
- Conservar runtime/CI/config/observabilidad. Actualizar CORS y rate limits sin quitar defensas. No retirar aún rutas/tablas hoteleras ni activar operaciones de negocio antes del scope de autorización.

### Fase 2: cliente, dispositivo y recepción/solicitud

- Evolucionar guest-service a customer y frontdesk a service-request/order shell con folio e idempotency. Mantener los campos de dispositivo inline al inicio y traer catálogo configurable por tenant/industria; normalizar solo con decisión T09/modelo.
- Habilitar intake público/operativo y conversión transaccional de solicitud->cliente/orden. Alinear experiencia landing/onboarding, admin, portal, UI domain services y rutas gateway. Retirar solo pantallas/endpoints hoteleros que no tengan consumidores activos en el piloto.

### Fase 3: ciclo de orden

- Implementar estados, diagnóstico, estimates versionados, autorizaciones cliente, asignación técnica, work logs/tareas, documentos y garantía en frontdesk.
- Integrar confirmación del cliente/portal y notificaciones. Validar transiciones por rol, versión, tenant/branch y evidencia.

### Fase 4: inventario, compras y POS

- Transformar fb-service in situ a catálogo, proveedores, compra, inventory ledger/reservations y POS; portar de Fixi las garantías de reserva, consumo atómico, transferencias y venta atómica demostradas por migraciones/RPC, no reconstruirlas como feature nueva.
- Integrar consumo/reserva de pieza con orden y recepción de PO. Agregar idempotencia y reconciliación; solo entonces retirar F&B y sus contratos.

### Fase 5: cobros, caja y finanzas

- Reconfigurar billing-service a cargos Fixi, pagos/reembolsos, caja y ledger operativo/reportes; alojar aislado el ciclo de suscripción/plan SaaS si se elige este servicio como dueño. Retener PDF y fiscalidad únicamente tras decisión CFDI.
- Integrar órdenes, POS y compras sin suponer que crear factura equivale a cobrar o que pago equivale a cierre de caja.

### Fase 6: notificaciones, portal y asistente Fixi

- Reemplazar templates/eventos por estados y actores Fixi; completar WhatsApp entrante/saliente y queue/retry.
- Sustituir tool catalog y clients UI con operaciones Fixi; activar primero consultas, después mutaciones de bajo riesgo y finalmente acciones financieras protegidas.
- Completar los tres recorridos funcionales Fixi en la SPA Camra: web-public (signup/planes), web-admin (operación/plataforma) y web-clientes (landing tenant/portal cliente), sin crear repositorio paralelo.
- Portal cliente y customer notifications se prueban con datos anonimizados y pruebas de seguridad; la autorización de presupuesto no comparte el mecanismo de confirmación IA.

### Fase 7: informes, retirada hotelera y estabilización

- Entregar reportes de operación/finanzas por sucursal y tenant, enforcement de plan/capabilities, soporte auditable y portabilidad export/import por tenant; completar observabilidad Fixi, pruebas de carga, aislamiento y backup/restore.
- Retirar rutas/tablas hoteleras después de medir consumidores en cero, exportar/retener datos obligatorios y verificar migraciones de clean install y upgrade. Retirar solo settings hoteleros; conservar configuración genérica tenant/industria.
- Renombrar módulos/artefactos en sitio cuando no quede dependencia hotelera, manteniendo la continuidad del repositorio/deploy.

## Dependencias entre fases

- Cliente/dispositivo preceden a solicitud/orden; la orden precede diagnosis/estimate/worklog/warranty/portal.
- Tenant + branch + roles y onboarding deben preceder a escrituras Fixi, stock por ubicación y reportes multi-sucursal. Plan/capabilities debe preceder a enforcement por feature, no a exportación requerida por baja/suspensión.
- Verticalización/configuración de tenant precede a intake dinámico y a catálogos; no debe cambiar globalmente campos de otro tenant.
- Catálogo/producto y ledger de inventario preceden a reserva/consumo de piezas, compras y POS; las reglas de caja/pagos deben estar definidas antes de habilitar ventas de efectivo.
- Contrato de pagos/CFDI precede a migrar invoice/PDF; caja depende de payment events, POS, órdenes y sucursal.
- Notificaciones dependen de eventos y estados estables; WhatsApp depende además de onboarding/tenant instance, verificación webhook y queue/dedup.
- IA mutation tools dependen de endpoints Fixi, RBAC/tenant/branch, schemas versionados e idempotency. Activar tools antes de enforcement backend está prohibido.
- Migraciones contract hoteleras dependen de completar consumidores nuevos, compatibilidad de integraciones, plan de retención/export, backup y rollback.
- Import/export por tenant depende de ownership estable de agregados, auditabilidad y límites de plan; soporte interno depende de auditoría append-only y contexto tenant explícito.

## Riesgos de regresión

| Riesgo | Por qué aparece en la transformación | Control/gate |
|---|---|---|
| Fuga entre tenants/sucursales | `hotelId` único se convierte en tenant con sub-scope branch; todas las tablas y query paths cambian | Tests de dos tenants y dos sucursales en cada servicio; constraints/ArchUnit; spoofed header y repositorio cross-scope deben fallar |
| Desalineación entre snapshot y canónico Fixi | especificación de implementación, modelo maestro y specs proponen distintos estados/entidades/membership | Matriz de decisión aprobada en Fase 0; usar migrations/código para detectar lo físico y no backfillear hasta fijar target |
| Pérdida de capacidades SaaS al retirar hotelería | Fixi incluye onboarding, planes, módulos, verticalización, soporte y portabilidad además del taller | Suite platform E2E de signup->tenant/owner/sucursal->plan/capabilities, suspensión/export y soporte auditado |
| Pérdida de garantías al salir de Supabase/RLS | Fixi usa RLS/Storage; Camra aplica seguridad por gateway/contexto/repositories y microservicios | Threat model comparativo y pruebas negativas de aislamiento equivalentes por tabla/API/archivo antes de mover tráfico |
| Identidad/tenant membership incompatible | snapshot actual y modelo canónico difieren sobre usuario en uno o varios tenants | No cambiar claim ni diseñar membership implícita; decidir modelo y validar cambio de tenant, roles y refresh JWT |
| Divergencia de workflow | check-in/checkout se reemplaza por orden con muchas transiciones | Máquina de estados centralmente validada; concurrencia/version, transiciones permitidas/prohibidas y eventos idempotentes |
| Pérdida de datos migrados | dominios viejos tienen campos/ciclos no mapeables | Migration report, mapping aprobado, conteos/checksums, backup restore probado; no mapear habitación a dispositivo ni estancia a orden |
| Cobro/inventario duplicado | retries, requests, webhooks y confirmación IA | Idempotency key persistida por operación, atomic stock ledger, payment reconciliation, duplicate webhook tests |
| Inconsistencia entre servicios | order, inventory, billing y notification son deployables separados | Outbox/recovery o mecanismo durable definido; sagas con estados pendientes visibles, retry discriminando 4xx/5xx y reconciliation jobs |
| Caja no conciliable | payment, invoice, POS y efectivo son conceptos separados | Ledger cash session, apertura/cierre por branch, discrepancias auditables, venta efectivo sin caja rechazada |
| Consentimiento incorrecto | confirmación del operador IA no es consentimiento del cliente a estimate | Autorización independiente ligada a versión, actor/canal/time/evidence; tests token replay/expiry |
| Exposición PII/evidencias | identidad hotelera y documentos se reutilizan inapropiadamente | Clasificación de datos, minimización, permisos, acceso al archivo, logs redacted y política de retención |
| JWT/session incompatible | cambia claim hotelId/roles, clientes pueden tener tokens activos | estrategia de transición/expiración de tokens, refresh invalidation controlada y login/rollback tests |
| Ruptura de integraciones F&B/billing | F&B llama a Stay y carga invoice directamente | cambios coordinados Feign+gateway; contract tests; retirar consumer antes de provider endpoint |
| Configuración equivocada por tenant | `HotelSettings` contiene secret y reglas hoteleras | separar secretos/config; no devolver credenciales; tests de tenant leakage y startup fail-fast |
| AI/tool calling inseguro | el catálogo actual puede pedir mutaciones del PMS | servidor es autoridad; confirmación/version/idempotencia; prompt injection, unauthorized role y stale state tests |
| WhatsApp incompleto/abuso | Camra implementa pairing/status; webhook-orchestration es parcial | webhook signature/validation, tenant binding, dedup/rate limit, opt-in, límites de acciones y audit events |
| Reportes financieros erróneos | invoice no equivale a cash ni P&L | definir base contable y origen de cada métrica; reconcile contra payments/refunds/purchases/cash |
| Interrupción de upgrades | migraciones históricas/servicios rolling y schemas separados | CI valida DB vacía y upgrade real; migración forward-only, version compatibility y recovery runbook |
| Quedar con híbrido hotel/Fixi indefinido | aliases y settings antiguos nunca se retiran | métricas y fecha/criterios de sunset por ruta, dueño asignado, contract phase bloqueado por consumidores no cero |

## Criterios para considerar completada la transformación

1. El repositorio, CI y despliegues siguen siendo la línea Camra evolucionada; no existe app/backend Fixi paralelo ni fuente doble de los agregados.
2. Capacidades de plataforma operan end-to-end: landing/signup, tenant/owner/sucursal, configuración vertical, planes/capabilities, soporte auditado y portabilidad por tenant.
3. Flujos de reparación operan por API y SPA: intake, cliente/equipo, orden/diagnóstico/presupuesto, autorización, asignación/work log, stock/pieza, pago/entrega y garantía; POS/compra/caja/finanzas y portal cliente cubiertos.
4. No quedan writes ni rutas hoteleras activas; hotel-specific settings/datos históricos tienen tratamiento documentado, pero configuración genérica/verticalización permanece.
5. Tenant y sucursal se verifican en Gateway, servicios, repositorios, constraints, tools y pruebas; no se confía en IDs del cliente para scope.
6. Transiciones de orden, autorización, stock, pagos, caja, refunds y webhooks son idempotentes/concurrent-safe y recuperables ante caída de dependencias.
7. Cada tool de IA consulta o propone solo operación permitida; mutaciones se confirman por humano y se reautorizan en backend; consentimiento del cliente permanece separado.
8. Evolution API tiene onboarding por scope decidido, webhook validado, envío/entrega/retry/dedup y aislamiento tenant; no solo QR/status.
9. CFDI, privacidad, retención, plan/suspensión y portabilidad Fixi tienen reglas, responsables y pruebas aprobadas.
10. `./gradlew build` y gates frontend lint/build/tests, aislamiento tenant+branch, integración, E2E SaaS + reparación, escaneo y stack E2E pasan en CI.
11. Upgrade Flyway desde instalaciones Camra soportadas, baseline limpio, backup/restore, recuperación, métricas y runbooks se verifican en entorno representativo.

## Matriz final

| Componente Camra actual | Se conserva | Se transforma en | Features Fixi añadidas | Cambios necesarios | Riesgo |
|---|---|---|---|---|---|
| `guest-service` | Servicio de personas, CRUD, búsqueda, tenant scope, privacidad aplicable | Servicio de clientes | Clientes, contactos, tags, sucursal e historial de órdenes | Evolucionar Guest->Customer; retirar identidad/check-in/Alloggiati; adaptar FKs, DTO y retención | Medio |
| `frontdesk-service` | Límite del ciclo operativo, REST/JPA, validación, coordinación, errores y resiliencia | Operaciones Fixi | Dispositivos, solicitudes, órdenes, diagnosis, estimates, autorizaciones, técnicos, work logs, tareas, warranties, evidencias | Sustituir Stay/Reservation/Room/RateSeason; rehacer rutas/estados; contratos idempotentes y tenant/branch | Muy alto |
| `billing-service` | Servicio de pagos/documentos, PDF, secuencias, BigDecimal, reportes | Cobros/finanzas operativas y módulo aislado de monetización SaaS | Pagos de orden/POS, reembolsos, gastos/ingresos, caja, P&L y planes/capabilities | Separar ledger operativo de documento fiscal y de suscripción SaaS; retirar stay hooks; definir CFDI | Muy alto |
| `fb-service` | Deployable, catálogo transaccional, precios server-side, líneas, totales y scoping | Inventario y comercio Fixi | Productos, proveedores, compras, stock, reservas, movimientos, POS | Sustituir MenuItem/RestaurantOrder; quitar Stay/Billing a habitación; nuevos contratos atómicos | Muy alto |
| `notification-service` | SMTP, templates engine, attachments, delivery boundary, sanitización | Notificaciones Fixi multicanal | Avisos de solicitud/orden/estimate/pago/entrega/tareas | Sustituir eventos hoteleros; añadir idempotencia/cola/retry y canales Fixi | Alto |
| `auth-service` | Login/refresh, hashing, token version, user admin y onboarding transaccional | Identidad, tenant/org, sucursales y onboarding SaaS | Signup tenant/owner/sucursal, estado de tenant, usuarios/roles/scopes y administración interna controlada | `hotelId`->tenant scope, `HotelRegistry`->Tenant; resolver usuario single vs multi-tenant y compatibilidad JWT | Muy alto |
| `api-gateway` | Gateway, JWT, HMAC, CSRF, rate limiting, CORS, headers y docs | Edge/API Fixi | Routing de clientes, órdenes, inventario, POS, finanzas y portal | Reescribir paths/RBAC, tenant+branch context firmado, proteger nuevos endpoints públicos | Alto |
| `config-service` | Config Server, auth, health, configuración por ambiente | Configuración runtime Fixi | Límites/proveedores/features/servicios nuevos | Sustituir settings de hotel; separar valores tenant de secretos/deployment config | Bajo-medio |
| `frontend` | SPA, auth store, router, componentes, servicios HTTP, i18n, Vitest/Playwright | Experiencias public/admin/clientes de Fixi dentro del frontend Camra | Landing/signup/planes, operación de reparación, administración y portal cliente | Consolidar rutas/layouts sin perder fronteras de experiencia; sustituir pages/types/services hoteleros; portal seguro | Muy alto |
| PostgreSQL/Flyway | Postgres, separación/ownership de schemas, backups, JPA, migraciones | Persistencia tenant/branch Fixi | Nuevos agregados Fixi y constraints | Expand/backfill/switch/contract; migrations append-only; no mapear semánticas hoteleras | Muy alto |
| Redis | Rate limiter, HMAC anti-replay, cache/sesiones efímeras | Infra compartida Fixi | Contexto conversacional/dispatch efímero | Renombrar keys, revisar eviction/TTL; no tratarlo como queue durable sin garantías | Medio |
| Evolution API | Cliente Evolution, pairing QR/status, HTTP hardening | Integración WhatsApp Fixi | Mensajería entrante/saliente y conversaciones por tenant | Reemplazar path/instance identity; webhook verificado, dedup, retries, vínculo cliente/orden | Alto |
| Asistente IA | Providers, contexto por tenant, timeouts, métricas y fallback | Asistente operativo Fixi | Consultas/acciones de órdenes, clientes, dispositivos, piezas, pagos y tareas | Prompt/policies/config y adapters dejan dominio hotelero; prueba PII e intents Fixi | Alto |
| Tool calling | Allow-list, DTO, lectura/propuesta, confirmación humana | Tool catalog Fixi | Lecturas/mutaciones Fixi con confirmación y revalidación | Reescribir operaciones/schemas/dispatch y autorización backend; idempotencia y stale state | Muy alto |
| Observabilidad | Actuator, Micrometer, Prometheus, Grafana, Loki, Zipkin, Alertmanager | Telemetría Fixi | Métricas SLO por órdenes, stock, pagos, caja y WhatsApp | Rehacer dashboards/alerts/tags; redacción PII y control de cardinalidad | Medio |
| `internal-auth-lib` / HMAC | Firma interna, nonce Redis, filtro compartido, Feign interceptor | Seguridad service-to-service tenant-aware | Protección de tenant/branch y llamadas Fixi | Firmar tenant/branch, validar replay, rotación de secretos y tests spoofing | Alto |
| Docker/infra | Compose, redes, healthchecks, límites, Postgres/Redis, backups | Runtime Fixi sobre la infraestructura Camra | Servicios convertidos y jobs de Fixi | Reemplazar env/config/data/volúmenes hoteleros; validar backup/restore y network boundaries | Alto |
| CI/CD y testing | GitHub Actions, Gradle, Node, Trivy, JUnit, ArchUnit, Vitest, Playwright | Gates para producto Fixi evolucionado | E2E SaaS, orden, stock, caja, portal e IA | Rehacer fixtures/assertions; preservar tenant/security gates y probar upgrade migrations | Alto |
| SaaS onboarding y planes | `HotelOnboardingService`, gateway/auth y reportes; sin plan enforcement equivalente completo | Registro de Tenant/Owner/Branch, lifecycle de tenant y entitlements | Signup, trial/plan/capabilities, suspensión, límites y módulos por tenant | Incorporar agregado SaaS dentro de auth/billing existentes; aislarlo de dominio hotelero y de finanzas del taller | Alto |
| Verticalización configurable | `HotelSettings`, catálogos/tarifas hoteleras | Configuración tenant/industria y feature/module config | Campos de recepción y catálogo de equipo configurables por industria | Separar settings técnicos, preferencias tenant y reglas industriales; retirar solo parámetros hoteleros | Alto |
| Backoffice/portabilidad | Controllers de plataforma, auditoría, jobs y tooling CI | Operación interna y export/import Fixi por tenant | Soporte trazado, exportación e importación con aislamiento | Crear policy de soporte/portabilidad en servicios existentes, autorización explícita, audit trail y límites por plan | Alto |
