# MASTER CONTRACT — CAMRA -> FIXI
# CONTRATO MAESTRO DE TRANSFORMACIÓN Y EJECUCIÓN MULTIAGENTE

A partir de este momento eres el ORQUESTADOR PRINCIPAL de la transformación completa:

CAMRA -> FIXI

Este documento es el contrato maestro del proyecto.

Debe ser leído y respetado por:

- Antigravity / agente orquestador
- Ralph loop
- Codex workers
- Antigravity/Gemini/Gemma workers
- DeepSeek workers
- integration workers
- review workers
- cualquier agente futuro incorporado a la fábrica

Cuando una instrucción de un worker contradiga este contrato, MANDA ESTE CONTRATO.

No reabras decisiones marcadas aquí como cerradas.

============================================================
0. OBJETIVO
============================================================

Transformar el sistema existente CAMRA hasta convertirlo completamente en FIXI.

NO crear un segundo sistema.

NO construir Fixi desde cero.

NO continuar el repositorio anterior de Fixi como producto principal.

NO fusionar arbitrariamente las dos arquitecturas.

NO transportar Express/Supabase como arquitectura destino.

CAMRA ES EL REPOSITORIO Y SISTEMA BASE.

El Fixi anterior sirve como:

- referencia funcional;
- evidencia de comportamiento;
- referencia de features;
- fuente para descubrir casos de negocio;
- referencia de UX cuando resulte útil.

Pero NO es la arquitectura destino.

Conceptualmente:

CAMRA ACTUAL
    |
    | preservar infraestructura madura
    | sustituir dominio hotelero
    | incorporar dominio Fixi
    | evolucionar capacidades existentes
    v
FIXI NUEVO

La misión no termina en un MVP.

La misión termina cuando Camra haya sido transformado funcionalmente en Fixi y el DAG completo haya pasado sus criterios de aceptación.

============================================================
1. PRINCIPIO FUNDAMENTAL
============================================================

CAMRA ES EL CABRÓN AL QUE VAMOS A OPERAR HASTA QUE SEA FIXI.

No estamos construyendo "Fixi 2" al lado de Camra.

Estamos transformando Camra.

Esto significa:

PRESERVAR cuando tenga sentido:

- arquitectura Spring Boot;
- microservicios/módulos;
- PostgreSQL;
- Flyway;
- Redis;
- API Gateway;
- auth;
- JWT;
- seguridad interna;
- HMAC;
- rate limiting;
- normalized errors;
- observabilidad;
- Prometheus;
- Grafana;
- Loki;
- Zipkin;
- Alertmanager;
- resilience;
- Docker;
- CI/CD;
- frontend React;
- Evolution API;
- AssistantService;
- proveedores IA;
- tool calling;
- confirmation gates;
- tests existentes;
- patrones maduros existentes.

TRANSFORMAR:

- dominio;
- entidades;
- agregados;
- workflows;
- permisos;
- interfaces;
- navegación;
- operaciones;
- automatizaciones;
- herramientas IA;
- experiencia de usuario.

ELIMINAR progresivamente:

- semántica hotelera;
- entidades hoteleras obsoletas;
- rutas hoteleras;
- pantallas hoteleras;
- procesos hoteleros;
- compatibilidad hotelera que deje de ser necesaria.

============================================================
2. PROHIBIDO HACER MAPEOS SEMÁNTICOS FALSOS
============================================================

NO hacer renames mecánicos como:

Room -> Device
Stay -> ServiceOrder
Guest -> Customer

sin comprobar que el modelo realmente sea equivalente.

Reutilizar infraestructura y patrones de Camra NO significa fingir que dos conceptos diferentes son el mismo agregado.

Cuando el dominio Fixi requiera un agregado nuevo:

CREAR EL AGREGADO CORRECTAMENTE.

Después retirar el dominio hotelero reemplazado cuando ya no tenga dependencias.

============================================================
3. MULTI-TENANCY
============================================================

Tenant representa UN NEGOCIO.

El aislamiento de tenant es un INVARIANTE DE SEGURIDAD.

Ninguna operación debe poder leer o modificar información perteneciente a otro tenant.

Toda nueva capacidad debe analizar:

- tenant ownership;
- queries;
- repositories;
- services;
- endpoints;
- events;
- jobs;
- caches;
- AI tools;
- webhooks;
- files;
- imports;
- exports.

Nunca confiar únicamente en tenantId enviado por frontend.

La autorización debe resolverse server-side.

============================================================
4. MULTI-SUCURSAL
============================================================

Un Tenant puede tener múltiples Branches.

El Owner del negocio tiene acceso a todas sus sucursales.

El Owner puede crear usuarios/empleados.

Cada empleado puede tener:

- una sucursal permitida;
- múltiples sucursales permitidas.

Si tiene una sola:

puede entrar directamente.

Si tiene múltiples:

puede seleccionar una Branch activa.

INVARIANTE:

SELECCIONAR UNA BRANCH NO CONCEDE ACCESO A ELLA.

El backend debe comprobar siempre que el usuario tiene acceso a la Branch solicitada.

Modelo conceptual:

Tenant
  |
  +-- Branch A
  +-- Branch B
  +-- Branch C

Owner
  -> todas

Employee 1
  -> A

Employee 2
  -> A + C

Employee 3
  -> B

No es requisito actual soportar que una misma identidad pertenezca simultáneamente a múltiples negocios independientes.

Diseñar sin bloquear una evolución futura razonable, pero NO complicar el producto actual innecesariamente.

============================================================
5. PERMISOS
============================================================

NO utilizar roles rígidos como autoridad principal.

El Owner debe poder definir mediante checklist qué puede:

VER

y qué puede:

HACER

cada empleado.

La seguridad efectiva debe derivarse de:

IDENTITY
+
TENANT
+
ALLOWED BRANCHES
+
EXPLICIT PERMISSIONS

Los roles pueden existir como:

- etiquetas;
- presets;
- templates;
- shortcuts administrativos.

Por ejemplo:

"Técnico"
"Encargado"
"Cajero"

Pero el nombre del rol NO concede autoridad por sí solo.

Ejemplos conceptuales de permissions:

customers.read
customers.create
customers.update
customers.archive

devices.read
devices.create
devices.update

orders.read
orders.create
orders.update
orders.change_status
orders.cancel

quotes.read
quotes.create
quotes.update
quotes.authorize_manual

inventory.read
inventory.adjust
inventory.transfer
inventory.purchase

cash.read
cash.collect
cash.close

refunds.create

finance.read

users.read
users.create
users.update
users.permissions

branches.manage

workflows.manage

plans.manage

El catálogo final puede evolucionar.

El principio NO.

============================================================
6. CUSTOMER
============================================================

Customer será entidad persistente del negocio.

Debe poder mantener:

- información básica;
- contacto;
- historial;
- dispositivos/activos;
- órdenes;
- autorizaciones;
- pagos;
- conversaciones;
- documentos/evidencias cuando corresponda.

Evitar matching ambiguo que pueda asociar incorrectamente personas únicamente porque coincida parcialmente teléfono/email/nombre.

Las estrategias de deduplicación deben ser explícitas y auditables.

============================================================
7. DEVICE / ASSET
============================================================

DECISIÓN CERRADA:

Device será entidad persistente.

NO será únicamente un JSON dentro de ServiceOrder.

Modelo:

Customer
   |
   +---- Device
   |       |
   |       +---- ServiceOrder
   |       +---- ServiceOrder
   |       +---- Warranty
   |
   +---- Device
           |
           +---- ServiceOrder

Device debe tener:

- ID estable;
- tenant;
- owner/customer;
- categoría/tipo;
- identifying attributes;
- serial/IMEI cuando corresponda;
- custom fields;
- historial.

No todos los verticales utilizarán IMEI.

El modelo debe ser multi-industria.

IMPORTANTE:

ServiceOrder debe conservar los snapshots históricos necesarios.

Si mañana alguien cambia:

modelo;
serial;
descripción;
propietario;
atributos;

una orden histórica NO debe reescribirse silenciosamente.

============================================================
8. SERVICE ORDER
============================================================

ServiceOrder será el agregado operacional principal donde corresponda.

Debe integrarse correctamente con:

- Customer;
- Device;
- Workflow;
- Quote;
- Authorization;
- Work logs;
- Technician;
- Inventory;
- Procurement;
- Payments;
- Documents;
- Evidence;
- Warranty;
- Events;
- Notifications;
- Conversations.

No convertir ServiceOrder en un "God Object".

Respetar bounded contexts y ownership entre servicios.

============================================================
9. WORKFLOW ENGINE
============================================================

DECISIÓN CERRADA:

Fixi tendrá un MOTOR DE WORKFLOWS CONFIGURABLE.

NO tendrá únicamente una lista global rígida de estados.

Debe existir una capa de:

SEMANTIC PHASES

común al sistema.

Ejemplos conceptuales:

INTAKE
DIAGNOSIS
QUOTE
AUTHORIZATION
WORK
READY
DELIVERED
WARRANTY
CLOSED
CANCELLED

Los nombres exactos deben definirse técnicamente de forma coherente.

Sobre esas fases:

cada vertical/tenant puede definir estados visibles y transiciones.

Ejemplo:

Reparación electrónica:

Recibido
Diagnóstico
Esperando autorización
En reparación
Listo
Entregado

Automotriz:

Recepción
Inspección
Cotización
Esperando refacción
En taller
Listo para entrega

Ambos pueden mapear internamente a fases semánticas comunes.

Esto permite que:

- reportes;
- IA;
- automatizaciones;
- métricas;
- reglas;
- notificaciones;

funcionen transversalmente.

Las transiciones críticas deben validarse transaccionalmente.

Evitar race conditions del estilo:

leer estado
-> comprobar
-> otro proceso cambia
-> escribir transición inválida.

============================================================
10. MULTI-INDUSTRIA
============================================================

Fixi NO será únicamente software para reparación de celulares.

Debe poder adaptarse a diferentes giros.

Configurable por tenant/vertical:

- workflows;
- estados;
- transiciones;
- fields;
- forms;
- labels;
- catalogs;
- modules;
- rules.

Ejemplo:

REPARACIÓN CELULAR

Device
IMEI
Falla
Diagnóstico

AUTOMOTRIZ

Vehículo
VIN
Placas
Kilometraje
Falla

HVAC

Equipo
Ubicación
BTU
Instalación
Visita técnica

No hardcodear un fallback silencioso a reparación electrónica para cualquier industria desconocida.

============================================================
11. REQUESTS / INBOUND LEADS
============================================================

Una solicitud entrante NO debe convertirse automáticamente en una ServiceOrder real por defecto.

Puede provenir de:

- web;
- WhatsApp;
- portal;
- integración futura;
- captura administrativa.

Lifecycle conceptual:

NEW
  ->
IN_REVIEW
  ->
CONVERTED

o:

REJECTED
DUPLICATE
SPAM
ARCHIVED

No borrar simplemente solicitudes rechazadas.

Guardar auditoría:

- actor;
- timestamp;
- razón;
- resultado.

Conversión:

Request
  ->
Customer
  ->
Device
  ->
ServiceOrder

Customer/Device existentes deben reutilizarse cuando la identidad sea suficientemente comprobable.

============================================================
12. QUOTES / ESTIMATES
============================================================

DECISIÓN CERRADA:

Cotizaciones COMPLETAS Y VERSIONADAS.

Modelo conceptual:

Quote
   |
   +-- QuoteVersion 1
   |      +-- QuoteItem
   |      +-- QuoteItem
   |
   +-- QuoteVersion 2
          +-- QuoteItem
          +-- QuoteItem

Debe soportar:

- parts;
- labor;
- quantities;
- unit price;
- discounts cuando correspondan;
- taxes cuando correspondan;
- totals;
- terms;
- metadata necesaria.

Una QuoteVersion autorizada:

ES INMUTABLE.

Si después cambia:

- pieza;
- precio;
- cantidad;
- mano de obra;
- total;
- términos relevantes;

se crea NUEVA VERSION.

La nueva versión requiere nueva autorización.

Nunca sobrescribir silenciosamente aquello que el cliente autorizó.

============================================================
13. CUSTOMER AUTHORIZATION
============================================================

Toda autorización debe ser AUDITABLE.

Dos caminos válidos:

A. CUSTOMER SELF-AUTHORIZATION

Cliente abre portal/enlace y autoriza una QuoteVersion específica.

B. EMPLOYEE-RECORDED AUTHORIZATION

Cliente autorizó mediante:

- presencial;
- teléfono;
- WhatsApp;
- otro canal permitido.

Empleado registra esa autorización.

Guardar:

- QuoteVersion exacta;
- monto exacto;
- términos;
- timestamp;
- channel;
- actor;
- customer identity cuando corresponda;
- evidence cuando exista.

No permitir:

"poner la orden como autorizada"

sin trazabilidad.

Si QuoteVersion cambia:

AUTORIZACIÓN ANTERIOR NO AUTORIZA AUTOMÁTICAMENTE LA NUEVA.

============================================================
14. WORK LOGS
============================================================

Debe existir trazabilidad real del trabajo.

Registrar cuando corresponda:

- technician;
- order;
- timestamps;
- actividad;
- notas;
- tiempo;
- estado;
- branch;
- tenant.

Definir técnicamente reglas de concurrencia razonables.

No permitir inconsistencias entre frontend/backend contracts.

============================================================
15. INVENTORY ES OPCIONAL
============================================================

DECISIÓN CERRADA:

INVENTARIO NO ES REQUISITO PARA OPERAR.

Muchos negocios no mantienen stock.

Ejemplo real:

cliente llega por pantalla;
negocio no tiene pantalla;
negocio cruza con mayorista;
compra pantalla;
la instala.

Fixi debe soportar perfectamente ese modelo.

Tres modalidades:

A. STOCK

Negocio mantiene inventario.

B. JUST-IN-TIME

Compra piezas específicamente para la orden.

C. HYBRID

Parte stock, parte compra bajo demanda.

Nunca impedir:

- crear orden;
- diagnosticar;
- cotizar;
- autorizar;
- trabajar;

porque una pieza no exista previamente en inventario.

============================================================
16. INVENTORY LIFECYCLE
============================================================

Cuando exista stock:

permitir:

- reservation;
- consume;
- release;
- adjustments;
- transfers;
- purchasing.

Una cotización autorizada puede originar reservas cuando corresponda.

Cuando la pieza se utilice:

consume.

Si se cancela/cambia:

release.

Pero estas automatizaciones deben respetar el modelo opcional.

============================================================
17. PROCUREMENT / JUST-IN-TIME
============================================================

Una pieza puede comprarse específicamente para ServiceOrder.

Debe poder registrar:

- supplier;
- item;
- quantity;
- cost;
- purchase;
- order relationship;
- evidence/document;
- timestamps.

Puede existir estado operacional equivalente a:

PENDING_PURCHASE

cuando el workflow lo requiera.

No exigir stock previo.

============================================================
18. INTELLIGENT INVENTORY INGESTION
============================================================

DECISIÓN CERRADA:

Fixi tendrá ingesta inteligente.

A. DOCUMENT / PHOTO

Usuario puede proporcionar:

- foto de ticket;
- factura;
- PDF;
- documento compatible.

Pipeline:

Document
   ->
OCR / AI extraction
   ->
proposed items
   ->
catalog matching
   ->
preview
   ->
HUMAN CONFIRMATION
   ->
inventory/procurement mutation

Extraer cuando sea posible:

- supplier;
- date;
- products;
- quantities;
- unit costs;
- totals.

IA NO debe modificar stock directamente sin confirmación.

B. MASS IMPORT

Soportar:

- XLSX;
- CSV;
- PDF cuando sea razonablemente interpretable.

Debe permitir:

- column mapping;
- preview;
- validation;
- error reporting;
- human confirmation;
- transactional application.

Debe servir para migrar información desde sistemas externos.

NO hardcodear el importador exclusivamente para un competidor específico.

============================================================
19. FINANCIAL MODEL
============================================================

DECISIÓN CERRADA:

UNA SOLA VERDAD FINANCIERA COHERENTE.

Separar:

QUOTED
COMMITTED / SOLD
COLLECTED
RECEIVABLE
CASH
REFUNDS
EXPENSES
POS

Ejemplo:

Orden: $3,000
Anticipo: $1,000

Entonces:

valor/venta = $3,000
cobrado = $1,000
pendiente = $2,000
cash received = $1,000

NO:

ingreso cobrado = $3,000

solamente porque ServiceOrder.finalCost sea $3,000.

Los movimientos reales de dinero deben gobernar:

- collected;
- cash;
- receivable;
- refunds.

============================================================
20. PAYMENTS
============================================================

Soportar:

- deposits;
- partial payments;
- final payments;
- refunds;
- payment methods;
- references;
- online payments cuando existan;
- POS payments.

Cada movimiento debe ser:

- tenant-scoped;
- branch-scoped cuando corresponda;
- auditable;
- idempotent cuando corresponda;
- reconciliable.

============================================================
21. CASH / POS
============================================================

Caja refleja DINERO REAL.

No simplemente valor de órdenes.

Integrar coherentemente:

- payments;
- refunds;
- expenses;
- POS;
- opening;
- closing;
- movements.

POS y ServiceOrders no deben producir dos verdades financieras independientes.

============================================================
22. WHATSAPP / EVOLUTION
============================================================

DECISIÓN CERRADA:

Conservar y evolucionar Evolution API de Camra.

Cada negocio podrá conectar su WhatsApp.

Objetivo:

Tenant
  ->
WhatsApp Instance
  ->
QR
  ->
CONNECTED

Soportar:

- connection;
- status;
- inbound webhook;
- outbound messages;
- conversations;
- customer correlation;
- order correlation;
- delivery;
- retries;
- idempotency;
- deduplication;
- AI;
- automations.

No reducir WhatsApp a:

wa.me

============================================================
23. AI
============================================================

PRESERVAR y evolucionar la infraestructura IA de Camra.

IA debe poder utilizar herramientas reales sobre Fixi.

Ejemplos:

- buscar cliente;
- consultar Device;
- consultar Order;
- consultar Quote;
- consultar disponibilidad/inventario;
- consultar estado;
- preparar mensajes;
- consultar pagos.

REGLA:

READ operations pueden automatizarse según permisos.

MUTATIONS sensibles requieren las garantías correspondientes.

No permitir que un LLM invente autoridad.

La seguridad de una tool debe comprobar:

- identity;
- tenant;
- branch;
- permission;
- parameters;
- business rules.

La confirmación humana debe conservarse donde corresponda.

============================================================
24. AUTOMATIONS
============================================================

Las automatizaciones deben poder reaccionar a eventos reales.

Ejemplos:

- Order created;
- Quote ready;
- Authorization pending;
- Authorization received;
- Part pending;
- Order ready;
- Payment pending;
- Warranty event.

Diseñar con:

- idempotency;
- retries;
- audit;
- failure handling;
- tenant isolation.

============================================================
25. CLIENT PORTAL
============================================================

Portal del cliente debe evolucionar hacia un acceso seguro para acciones como:

- consultar orden;
- consultar Device;
- consultar Quote;
- autorizar QuoteVersion;
- consultar pagos;
- evidencias/documentos permitidos;
- seguimiento.

No mantener dos mecanismos públicos contradictorios indefinidamente.

Sensitive information debe usar autorización/token adecuado.

============================================================
26. PLANS / CAPABILITIES ENGINE
============================================================

DECISIÓN CERRADA:

CONSTRUIR EL MOTOR AHORA.

NO DEFINIR PRECIOS DEFINITIVOS AHORA.

Debe permitir posteriormente controlar:

- users;
- branches;
- orders;
- storage;
- modules;
- WhatsApp;
- AI;
- inventory;
- POS;
- reports;
- automations;
- limits;
- add-ons.

Debe existir UNA fuente de verdad para enforcement.

No tener:

frontend dice una cosa;
docs otra;
middleware otra;
DB otra.

El motor debe ser técnicamente funcional aunque los paquetes/precios comerciales todavía cambien.

============================================================
27. AUDIT
============================================================

Operaciones sensibles deben ser auditables.

Especialmente:

- permissions;
- users;
- branches;
- workflow changes;
- quote authorization;
- financial movements;
- refunds;
- inventory adjustments;
- imports;
- automation mutations;
- AI mutations.

Guardar actor/contexto/timestamp según corresponda.

============================================================
28. SOFT DELETE / HISTORY
============================================================

Como principio técnico:

NO destruir historial operacional/financiero necesario.

Preferir según entidad:

- inactive;
- archived;
- cancelled;
- reversed;
- anonymized cuando corresponda.

Ejemplos:

Branch cerrada -> inactive
Employee despedido -> disabled
Product descontinuado -> inactive
Order cancelada -> cancelled
Payment incorrecto -> reversal/refund

Nunca destruir un payment histórico para fingir que no ocurrió.

Borrado físico solamente donde sea apropiado y seguro.

============================================================
29. DOCUMENTS / EVIDENCE
============================================================

Diseñar evidencia/documentos como capacidad transversal cuando corresponda.

Ejemplos:

- device intake photos;
- diagnostic photos;
- authorization evidence;
- receipts;
- invoices;
- purchase documents;
- warranty evidence.

Debe respetar:

- tenant;
- access;
- retention;
- ownership;
- security.

============================================================
30. PLATAFORMA CONFIGURABLE, NO MONSTRUO HARD-CODEADO
============================================================

No resolver cada industria con:

if industry == X
else if industry == Y
else if industry == Z

cuando la necesidad pueda representarse mediante:

- configuration;
- schema;
- fields;
- workflow;
- capability;
- module;
- template.

Pero tampoco crear un meta-framework innecesariamente abstracto.

Preferir configuración donde aporte valor real.

============================================================
31. TRANSACCIONES
============================================================

Definir límites transaccionales correctamente.

Operaciones que requieran consistencia deben ser atómicas dentro del bounded context correspondiente.

Ejemplos:

- workflow transition;
- stock movement;
- reservation;
- consume;
- request conversion;
- payment;
- refund;
- cash movement;
- authorization.

No utilizar secuencias:

check
-> mutate
-> hope

cuando exista riesgo real de concurrencia.

============================================================
32. IDEMPOTENCY
============================================================

Aplicar idempotencia especialmente a:

- webhooks;
- payments;
- WhatsApp inbound;
- delivery callbacks;
- imports;
- automation jobs;
- cross-service commands;
- retries.

Un retry NO debe duplicar:

- payment;
- stock movement;
- message;
- order;
- authorization.

============================================================
33. EVENTS / CROSS-SERVICE CONSISTENCY
============================================================

Cuando un proceso cruce servicios:

definir explícitamente:

- source of truth;
- event;
- idempotency;
- retry;
- failure behavior;
- reconciliation.

No crear distributed transactions improvisadas.

Usar los patrones ya maduros de Camra cuando sean apropiados.

============================================================
34. API CONTRACTS
============================================================

Backend y frontend deben compartir contratos coherentes.

Evitar errores históricos como:

frontend:
  /worklogs

backend:
  /work-logs

Toda feature terminada debe incluir cuando corresponda:

- backend;
- endpoint;
- DTO;
- frontend;
- validation;
- permission;
- tests.

No declarar una feature funcional si solo existe la tabla.

============================================================
35. SECURITY INVARIANTS
============================================================

NUNCA comprometer:

TENANT ISOLATION
BRANCH AUTHORIZATION
PERMISSION ENFORCEMENT
SERVER-SIDE VALIDATION
AUDITABILITY
SECRET PROTECTION

Nunca confiar en:

- hidden UI;
- disabled button;
- client-supplied tenant;
- client-supplied permission;
- client-supplied branch;

como mecanismo de seguridad.

============================================================
36. MIGRATIONS
============================================================

Usar nuevas migrations Flyway.

NO reescribir migrations históricas aplicadas.

Toda migration debe:

- ser determinista;
- tener orden correcto;
- preservar datos cuando corresponda;
- ser validada;
- considerar rollback/recovery operacional aunque Flyway no utilice rollback automático.

============================================================
37. FRONTEND
============================================================

No dejar el nuevo Fixi como colección de endpoints sin producto utilizable.

Cuando una capability requiera interacción:

construir UI.

Debe evolucionar navegación, pantallas, formularios y labels hacia Fixi.

Eliminar progresivamente UI hotelera reemplazada.

============================================================
38. TESTING
============================================================

Cada worker debe escribir pruebas apropiadas.

Especial atención:

- tenant isolation;
- branch isolation;
- permissions;
- workflow transitions;
- quote immutability;
- authorization;
- payments;
- refunds;
- inventory concurrency;
- idempotency;
- request conversion.

No escribir tests únicamente para aumentar coverage.

Probar invariantes.

============================================================
39. WORKER CONTRACT
============================================================

TODO WORKER DEBE:

1. leer esta task;
2. leer este MASTER CONTRACT;
3. inspeccionar código antes de modificar;
4. respetar dependencies;
5. respetar owned_paths;
6. no modificar forbidden_paths;
7. no cambiar shared contracts unilateralmente;
8. no inventar arquitectura paralela;
9. preservar tenant isolation;
10. preservar branch isolation;
11. implementar permission enforcement;
12. usar migrations nuevas;
13. mantener auditabilidad;
14. escribir tests;
15. ejecutar validation commands;
16. reportar errores reales;
17. no ocultar failing tests;
18. no declarar DONE por sí mismo.

Formato de salida obligatorio:

STATUS:
TASK_ID:
FILES_CHANGED:
MIGRATIONS:
ENDPOINTS:
EVENTS:
PERMISSIONS:
TESTS_ADDED:
TESTS_RUN:
TEST_RESULTS:
KNOWN_ISSUES:
CONTRACT_CHANGES_REQUESTED:
READY_FOR_INTEGRATION:

============================================================
40. OWNERSHIP
============================================================

Cada task posee paths explícitos.

Un worker NO puede modificar paths fuera de ownership salvo:

A. permiso explícito del orquestador;

o

B. task específica para modificar shared contract.

Esto evita que dos agentes creen simultáneamente:

UserBranchAccess
BranchMembership
EmployeeBranches

para resolver exactamente el mismo problema.

============================================================
41. WORKTREES
============================================================

Cada worker trabaja en:

- branch propia;
- worktree propio.

Nunca varios workers escribiendo simultáneamente sobre integration/main.

El orquestador mantiene:

worker
task
provider
branch
worktree
PID
status
attempt

============================================================
42. ORQUESTADOR
============================================================

ANTIGRAVITY ES EL CEREBRO PRINCIPAL.

Responsabilidades:

- leer DAG;
- detectar READY;
- calcular dependencias;
- seleccionar provider;
- reservar task;
- crear/asignar worktree;
- activar worker;
- entregar contrato + task;
- registrar PID;
- monitorizar ejecución;
- recoger resultado;
- ejecutar validation;
- devolver failures;
- integrar passes;
- desbloquear dependencias;
- lanzar siguiente wave.

No esperar intervención humana entre waves.

============================================================
43. PROVIDERS
============================================================

Usar únicamente providers cuya interfaz haya sido comprobada.

Preferencia general:

1. Antigravity/Gemma para velocidad cuando sea apropiado.
2. Codex para tareas complejas/integración cuando resulte conveniente.
3. DeepSeek para tareas apropiadas si su provider está automatizado.

Pero capability > nombre del proveedor.

No asumir que un modelo es adecuado para una tarea solamente por preferencia global.

============================================================
44. RALPH LOOP
============================================================

Ralph es el loop de ejecución.

No decide producto.

Estados:

READY
  ->
ASSIGNED
  ->
RUNNING
  ->
VALIDATING
  ->
PASSED
  ->
INTEGRATED
  ->
DONE

Failure:

VALIDATING
  ->
FAILED
  ->
RETURN_TO_WORKER

Retries limitados.

No repetir un error determinista sin modificar contexto/código.

============================================================
45. QUALITY GATES
============================================================

"Worker says done" != DONE.

Aplicar gates según task:

- backend compile;
- Gradle tests;
- frontend build;
- frontend tests;
- Flyway validation;
- git diff --check;
- architecture tests;
- tenant isolation;
- branch isolation;
- permission enforcement;
- API contracts;
- transaction tests;
- idempotency;
- Docker/config;
- security checks.

DONE únicamente después de integración validada.

============================================================
46. PARALELISMO
============================================================

Objetivo:

MÁXIMO PARALELISMO SEGURO.

Si cuatro tasks independientes están READY:

NO ejecutarlas secuencialmente.

Ejecutarlas simultáneamente si:

- no comparten ownership incompatible;
- dependencias están satisfechas;
- infraestructura soporta concurrencia.

No maximizar número de procesos por ego.

Maximizar throughput real.

============================================================
47. INTEGRATION WORKER
============================================================

Debe existir función de integración.

Responsabilidades:

- revisar diff;
- verificar ownership;
- verificar shared contracts;
- correr gates;
- integrar;
- resolver conflictos triviales;
- devolver conflictos funcionales al orquestador.

No desarrollar features grandes dentro de integration.

============================================================
48. AUDIT WORKERS
============================================================

Capacidad libre puede utilizarse para auditorías.

Buscar:

- tenant leaks;
- branch leaks;
- permission gaps;
- transaction bugs;
- idempotency gaps;
- hotel leftovers;
- frontend/backend mismatches;
- missing tests;
- dead code;
- security regressions.

Findings reales se convierten en tasks del DAG.

============================================================
49. DAG DINÁMICO
============================================================

El número inicial de tasks NO limita el proyecto.

Si una task es demasiado grande:

SPLIT.

Si aparece trabajo nuevo necesario:

ADD TASK.

Cada task nueva requiere:

- id;
- objective;
- dependencies;
- affected services;
- owned paths;
- acceptance criteria;
- tests;
- status.

No esconder semanas de trabajo dentro de una task para mantener bonito el número del DAG.

============================================================
50. CHECKPOINTS
============================================================

Después de waves importantes registrar:

- completed;
- integrated;
- failed;
- new tasks;
- migrations;
- tests;
- known issues;
- next wave.

CHECKPOINT NO SIGNIFICA DETENERSE.

No preguntar:

"¿Quieres que continúe?"

Continuar.

============================================================
51. BLOCKERS
============================================================

Escalar al humano únicamente cuando exista blocker real.

Ejemplos:

- credencial externa imprescindible;
- servicio externo inaccesible;
- riesgo de pérdida de datos;
- decisión de producto no cubierta;
- conflicto funcional que requiera elegir comportamiento;
- fallo persistente no resoluble razonablemente.

NO escalar:

- error de compilación ordinario;
- test roto;
- lint;
- migration typo;
- merge conflict trivial;
- decisión técnica razonablemente resoluble.

Resolver y continuar.

============================================================
52. GIT SAFETY
============================================================

PROHIBIDO:

- git reset --hard sobre trabajo ajeno;
- force push;
- borrar branches con trabajo no integrado;
- eliminar worktrees dirty;
- destruir cambios del usuario;
- reescribir historia innecesariamente.

Antes de operaciones destructivas:

DETENER y evaluar.

============================================================
53. SECRETS
============================================================

Nunca:

- imprimir API keys;
- committear secrets;
- copiar tokens a logs;
- meter credenciales en DAG.

Usar:

- environment variables;
- secret stores existentes;
- configuración local ignorada.

============================================================
54. HOTEL DOMAIN RETIREMENT
============================================================

Retirar dominio hotelero PROGRESIVAMENTE.

Secuencia:

1. construir capacidad Fixi;
2. integrar;
3. migrar dependencias;
4. comprobar que ya no existe consumidor hotelero;
5. retirar rutas/UI/modelos hoteleros;
6. ejecutar tests.

No empezar destruyendo Camra antes de que exista reemplazo.

============================================================
55. DEFINITION OF DONE GLOBAL
============================================================

El proyecto NO termina cuando:

- foundation funciona;
- órdenes funcionan;
- frontend abre;
- MVP funciona.

Termina cuando:

DAG completo = DONE

y los gates globales pasan.

El sistema final debe demostrar como mínimo:

- tenant isolation;
- multi-branch;
- granular permissions;
- Customer;
- Device;
- configurable workflows;
- Requests;
- ServiceOrders;
- versioned Quotes;
- Authorization;
- Work logs;
- Inventory optional;
- Procurement;
- intelligent ingestion architecture;
- Payments;
- Ledger;
- Cash;
- POS;
- Client Portal;
- WhatsApp/Evolution;
- AI tools;
- Automations;
- Plans/Capabilities;
- multi-industry configuration;
- Audit;
- Documents/Evidence;
- removal of obsolete hotel domain;
- frontend usable;
- backend build;
- frontend build;
- migrations valid;
- critical E2E;
- security invariants.

============================================================
56. TU INSTRUCCIÓN AHORA — ANTIGRAVITY
============================================================

Tú eres el ORQUESTADOR.

No escribas otro plan general.

No me expliques cómo podrías hacerlo.

No vuelvas a hacer discovery que ya existe salvo para verificar información necesaria.

Haz ahora:

1. lee `.agent-orchestration/`;
2. lee DAG;
3. lee state;
4. lee provider capabilities;
5. lee este MASTER CONTRACT;
6. verifica Antigravity CLI;
7. verifica Codex provider;
8. determina max safe concurrency;
9. identifica tasks READY;
10. reserva tasks atómicamente;
11. crea/reutiliza worktrees;
12. ACTIVA WORKERS;
13. entrega a cada worker:
    - MASTER CONTRACT;
    - task;
    - ownership;
    - acceptance criteria;
    - validation commands;
14. monitoriza workers;
15. recoge resultados;
16. ejecuta quality gates;
17. devuelve failures;
18. integra passes;
19. actualiza DAG/state;
20. desbloquea siguiente wave;
21. activa siguiente wave;
22. repite.

============================================================
57. PRIORIDAD DE EJECUCIÓN
============================================================

No seguir necesariamente orden numérico.

Usar DAG.

Priorizar foundations que desbloqueen mayor cantidad de trabajo.

En cuanto existan ramas independientes:

PARALELIZAR.

Ejemplo conceptual:

Foundation contracts
        |
   +----+------+-------------+
   |           |             |
Tenant      Customer      Workflow
Branch      Device        Engine
Permissions
   |           |             |
   +-----------+-------------+
               |
             Orders
        +------+------+------+
        |      |      |      |
      Quote  Work   Inv.   Requests
        |     Logs
      Auth
        |
     Portal

En paralelo donde dependencias reales lo permitan:

Finance
Evolution
Plans
Verticalization
Imports

NO usar este ejemplo como DAG si contradice el DAG real.

El DAG machine-readable es la fuente operacional.

============================================================
58. USO DEL FIXI ANTERIOR
============================================================

Cuando un worker necesite conocer cómo funcionaba una feature del producto anterior:

puede inspeccionar el repositorio Fixi anterior.

ÚNICAMENTE COMO REFERENCIA.

No copiar ciegamente.

Clasificar hallazgo:

- IMPLEMENTED
- PARTIAL
- DOCUMENTED/PLANNED
- LEGACY
- CONTRADICTORY
- INDETERMINATE

Cuando implementación vieja contradiga este MASTER CONTRACT:

MANDA ESTE CONTRATO.

============================================================
59. DECISIONES TÉCNICAS
============================================================

No preguntarme cosas que tú puedes decidir.

Elige técnicamente basándote en:

1. correctness;
2. security;
3. tenant isolation;
4. branch isolation;
5. transactional integrity;
6. auditability;
7. idempotency;
8. extensibility;
9. operational simplicity;
10. consistency with Camra architecture;
11. testability;
12. maintainability.

Solo escalar una decisión cuando cambie realmente EL PRODUCTO.

============================================================
60. MODO DE EJECUCIÓN
============================================================

QUIERO EJECUCIÓN CONTINUA.

No:

"Terminé W1. ¿Quieres que haga W2?"

Sí:

W1 passes
   ->
integrate
   ->
unlock W2/W3/W4
   ->
launch workers
   ->
continue.

Mientras exista:

READY task
+
available worker

debe existir trabajo ejecutándose.

============================================================
61. RESULTADO FINAL
============================================================

Cuando TODO termine, generar un reporte final:

CAMRA -> FIXI TRANSFORMATION COMPLETE

Incluyendo:

- tasks completed;
- tasks added dynamically;
- migrations;
- services transformed;
- hotel components removed;
- APIs;
- frontend;
- permissions;
- tests;
- E2E;
- security checks;
- known residual limitations;
- deployment instructions.

Pero NO generes ese reporte hasta que el DAG esté realmente completado.

============================================================
62. ARRANCA
============================================================

No respondas con otro diseño.

No respondas con una lista de cosas que vas a hacer.

INSPECCIONA LA FÁBRICA EXISTENTE.

CARGA ESTE CONTRATO COMO AUTORIDAD.

IDENTIFICA LA PRIMERA WAVE.

ACTIVA LOS WORKERS DISPONIBLES.

EMPIEZA LA TRANSFORMACIÓN CAMRA -> FIXI.

CONTINÚA AUTOMÁTICAMENTE HASTA COMPLETAR EL DAG O ENCONTRAR UN BLOCKER REAL QUE REQUIERA INTERVENCIÓN HUMANA.

ARRANCA.
