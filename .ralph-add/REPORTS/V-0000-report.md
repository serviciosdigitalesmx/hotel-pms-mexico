# Reporte A.SPEC V-0000

**Modo:** READ_ONLY  
**Resultado:** Superficie de caja parcial existente; no existe contrato implementado de turnos, apertura, arqueo o cierre.

## 1. Resumen ejecutivo

El PMS ya dispone de una superficie real para:

- Facturas.
- Cargos asociados a estancias.
- Registro de pagos, incluido `CASH`.
- Consulta histórica de facturas.
- Reporte financiero por rango de fechas.
- Pantalla frontend de facturación.

No se encontró evidencia de:

- Entidad o tabla de turnos/cajas.
- Apertura de caja.
- Movimientos manuales de caja.
- Saldo inicial o saldo esperado.
- Arqueo.
- Importe contado.
- Discrepancia.
- Cierre de turno.
- Histórico de turnos o arqueos.

Por tanto, no es válido implementar apertura, arqueo o cierre reutilizando únicamente los contratos actuales.

## 2. Pantallas existentes

### Facturación

[frontend/src/pages/Billing.tsx](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/frontend/src/pages/Billing.tsx:113)

La pantalla existente permite:

- Buscar facturas.
- Filtrar por estado.
- Filtrar por rango de fechas.
- Consultar detalle.
- Registrar pagos.
- Ver facturas `ISSUED`, `PAID` y `CANCELLED`.

El pago se habilita solamente para facturas que no estén pagadas o canceladas:

[frontend/src/pages/Billing.tsx](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/frontend/src/pages/Billing.tsx:74)

### Modal de pago

[frontend/src/pages/Billing/PaymentModal.tsx](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/frontend/src/pages/Billing/PaymentModal.tsx:26)

El modal reutiliza el contrato de pagos existente y permite seleccionar métodos definidos por `PaymentMethod`, incluyendo `CASH`.

No existe una pantalla independiente de caja o turnos.

## 3. Servicios y endpoints existentes

### Facturas

Controlador:

[billing-service/src/main/java/com/hotelpms/billing/controller/InvoiceController.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/controller/InvoiceController.java:49)

Contratos relevantes:

| Método | Endpoint | Uso |
|---|---|---|
| `GET` | `/api/v1/invoices/{id}` | Obtener factura |
| `GET` | `/api/v1/invoices` | Listar facturas paginadas |
| `GET` | `/api/v1/invoices/search` | Buscar por estado, texto y fechas |
| `GET` | `/api/v1/invoices/reservation/{reservationId}/latest` | Última factura de una reservación |
| `POST` | `/api/v1/invoices/stay` | Crear factura para una estancia |
| `POST` | `/api/v1/invoices/stay/{stayId}/charges` | Agregar cargo a la factura abierta |
| `GET` | `/api/v1/invoices/guest/{guestId}/history` | Historial de facturas del huésped |
| `GET` | `/api/v1/invoices/{id}/pdf` | Descargar PDF |

La búsqueda histórica ya soporta:

- `status`
- `query`
- `dateFrom`
- `dateTo`
- Paginación ordenada por fecha de emisión

[billing-service/src/main/java/com/hotelpms/billing/controller/InvoiceController.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/controller/InvoiceController.java:96)

### Pagos

Controlador:

[billing-service/src/main/java/com/hotelpms/billing/controller/PaymentController.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/controller/PaymentController.java:23)

Contrato encontrado:

```http
POST /api/v1/invoices/{invoiceId}/payments
```

[billing-service/src/main/java/com/hotelpms/billing/controller/PaymentController.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/controller/PaymentController.java:38)

### Reporte financiero

Controlador:

[billing-service/src/main/java/com/hotelpms/billing/controller/OwnerReportController.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/controller/OwnerReportController.java:27)

Contrato:

```http
GET /api/v1/reports/owner?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
```

Está restringido a roles `OWNER` y `ADMIN`:

[billing-service/src/main/java/com/hotelpms/billing/controller/OwnerReportController.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/controller/OwnerReportController.java:44)

Este reporte es financiero agregado, no un cierre de caja por turno.

## 4. DTOs y enums existentes

### PaymentRequest

[billing-service/src/main/java/com/hotelpms/billing/dto/PaymentRequest.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/dto/PaymentRequest.java:20)

Contrato real:

```java
BigDecimal amount;
PaymentMethod paymentMethod;
String transactionReference;
```

`amount` es obligatorio y positivo. `transactionReference` es opcional y está documentado como no necesario para efectivo.

### PaymentMethod

[billing-service/src/main/java/com/hotelpms/billing/domain/PaymentMethod.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/domain/PaymentMethod.java:1)

Valores existentes:

```text
CREDIT_CARD
DEBIT_CARD
CASH
BANK_TRANSFER
CHECK
```

### InvoiceResponse

[billing-service/src/main/java/com/hotelpms/billing/dto/InvoiceResponse.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/dto/InvoiceResponse.java:29)

Incluye:

- `id`
- `hotelId`
- `invoiceNumber`
- `issueDate`
- `totalAmount`
- `status`
- `reservationId`
- `guestId`
- `stayId`
- `payments`
- `charges`

No incluye:

- Usuario que cobró.
- Turno.
- Caja.
- Saldo esperado.
- Importe contado.
- Discrepancia.
- Estado de cierre.

## 5. Persistencia existente

### Facturas

La entidad `Invoice` persiste:

- Factura por hotel.
- Fecha de emisión.
- Total.
- Estado.
- Huésped.
- Estancia.
- Cargos.
- Pagos.

[billing-service/src/main/java/com/hotelpms/billing/domain/Invoice.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/domain/Invoice.java:57)

### Pagos

La entidad `Payment` persiste:

- Fecha del pago.
- Importe.
- Método.
- Referencia de transacción.
- Factura relacionada.
- Auditoría de creación/modificación.
- Soft-delete mediante `active`.

[billing-service/src/main/java/com/hotelpms/billing/domain/Payment.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/domain/Payment.java:33)

La migración inicial confirma la tabla `payments` y su relación con `invoices`:

[billing-service/src/main/resources/db/migration/V1__init_schema.sql](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/resources/db/migration/V1__init_schema.sql:77)

La migración posterior hizo opcional la referencia de transacción para permitir pagos en efectivo:

[billing-service/src/main/resources/db/migration/V4__make_payment_transaction_reference_optional.sql](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/resources/db/migration/V4__make_payment_transaction_reference_optional.sql:1)

## 6. Flujo real identificado

### Cobro en efectivo existente

```text
Factura ISSUED
    ↓
PaymentModal
    ↓
POST /api/v1/invoices/{invoiceId}/payments
    ↓
Payment { amount, CASH, transactionReference opcional }
    ↓
Factura con lista de pagos actualizada
```

Este flujo permite registrar un cobro, pero no lo vincula a un turno o caja.

### Cargos de estancia

```text
Estancia
    ↓
POST /api/v1/invoices/stay/{stayId}/charges
    ↓
InvoiceCharge
    ↓
totalAmount de la factura
```

Estos cargos representan consumos o conceptos facturables, no movimientos de caja.

### Históricos

Hay dos tipos de histórico disponibles:

1. Histórico paginado y filtrable de facturas:

```http
GET /api/v1/invoices/search
```

2. Histórico de facturas por huésped:

```http
GET /api/v1/invoices/guest/{guestId}/history
```

No existe histórico por:

- Turno.
- Cajero.
- Caja.
- Fecha de apertura.
- Fecha de cierre.
- Arqueo.
- Discrepancia.

## 7. Gaps accionables

### Gap crítico: no existe dominio de turnos

No se encontró entidad, DTO, repositorio, servicio ni endpoint para:

- `CashShift`
- `CashRegister`
- `Turn`
- `Caja`
- `Turno`

### Gap crítico: no existe apertura

No existe contrato para registrar:

- Usuario responsable.
- Caja física o terminal.
- Fecha/hora de apertura.
- Saldo inicial.
- Moneda.
- Estado abierto.

### Gap crítico: no existen movimientos independientes

Los pagos existentes pertenecen exclusivamente a una factura:

[Payment.java](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/V-0000-moa6n_ry/billing-service/src/main/java/com/hotelpms/billing/domain/Payment.java:61)

No cubren:

- Entradas manuales.
- Retiros.
- Ajustes.
- Gastos.
- Devoluciones de caja.
- Movimientos sin factura.

### Gap crítico: no existe arqueo

No existen campos ni contratos para:

- Efectivo esperado.
- Efectivo contado.
- Diferencia.
- Observaciones.
- Responsable del arqueo.
- Fecha/hora del arqueo.

### Gap crítico: no existe cierre

No existe transición de estado de un turno ni endpoint para cerrar una caja.

### Gap medio: autorización de caja no definida

El reporte financiero existente restringe por `OWNER` y `ADMIN`, pero no define permisos específicos para:

- Abrir turno.
- Registrar movimientos.
- Arquear.
- Cerrar.
- Aprobar discrepancias.

### Gap medio: trazabilidad operativa insuficiente

Los pagos tienen fecha y auditoría técnica, pero el contrato expuesto no incluye expresamente el usuario operativo que realizó el cobro ni un identificador de turno.

## 8. Qué se puede implementar sin base de datos nueva

Se puede realizar un WRITE frontend acotado que reutilice exclusivamente contratos existentes:

- Pantalla o sección de “Cobros registrados”.
- Filtro por fechas.
- Filtro por `CASH`.
- Consulta de facturas y pagos existentes.
- Registro de pagos mediante `POST /api/v1/invoices/{invoiceId}/payments`.
- Consulta del reporte financiero existente para usuarios autorizados.
- Exportación o presentación de un resumen derivado de datos existentes, sin llamarlo arqueo ni cierre.

Esto no permitiría implementar correctamente:

- Apertura de turno.
- Saldo inicial.
- Movimientos manuales.
- Arqueo.
- Discrepancias.
- Cierre.

## 9. Lo bloqueado por aprobación

Requiere aprobación explícita antes de escribir:

- Crear el modelo persistente de turnos/cajas.
- Crear tablas o migraciones.
- Definir si un pago puede pertenecer a un turno.
- Definir movimientos sin factura.
- Definir roles y permisos operativos.
- Definir reglas de cierre y reapertura.
- Definir tratamiento de discrepancias.
- Definir si existe una caja por hotel, terminal o usuario.

No se duplican estos blockers: todos corresponden a la ausencia de un contrato persistente de caja/turnos.

## 10. Primer A.SPEC WRITE recomendado

### A.SPEC WRITE V-0000.1 — Superficie de cobros existente, sin migraciones

**Objetivo:** habilitar una pantalla operativa de cobros basada únicamente en facturas y pagos ya existentes.

**Alcance:**

- Reutilizar `billingService.searchInvoices`.
- Reutilizar `billingService.getInvoiceById`.
- Reutilizar `billingService.processPayment`.
- Mostrar pagos existentes incluidos en `InvoiceResponse`.
- Filtrar por fecha, estado y método `CASH`.
- Mantener el registro asociado a una factura.
- No crear endpoints.
- No crear tablas.
- No agregar concepto de turno o arqueo.

**Fuera de alcance:**

- Apertura.
- Cierre.
- Arqueo.
- Saldo inicial.
- Movimientos manuales.
- Discrepancias.
- Migraciones.

**Aprobación necesaria para un WRITE posterior:**

Definir y aprobar el contrato persistente de `turnos/cajas` antes de implementar cualquier flujo de apertura, arqueo o cierre.

## 11. Verificación realizada

Se ejecutó únicamente la búsqueda estática indicada por la A.SPEC dentro de:

- `frontend/src`
- `billing-service/src`
- `frontdesk-service/src`
- `api-gateway/src`
- `docs`

No se ejecutaron:

- Servicios.
- Reinicios.
- Migraciones.
- Consultas de base de datos.
- Cambios de archivos.
- Cambios de Git.
- Operaciones externas.

**Conclusión:** la superficie existente permite operar cobros ligados a facturas y consultar históricos financieros, pero no contiene un sistema de caja por turnos.
