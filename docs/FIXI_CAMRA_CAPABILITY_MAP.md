# Mapa de capacidades Fixi -> Camra

## Alcance y método

- **Fixi:** descripción tomada del `FIXI PRODUCT SPEC (Exhaustive Analysis)` adjunto por el usuario. El repositorio Fixi no está montado en este workspace; sus rutas se consignan tal como aparecen en esa especificación y no se inspeccionaron directamente.
- **Camra:** contraste con `docs/CAMRA_SYSTEM_SPEC.md` y código Java/React, migraciones SQL, configuración, pruebas, Docker Compose y workflows presentes en este repositorio.
- Se considera **EQUIVALENTE** solo una coincidencia sustancial de propósito, datos y ciclo de vida. La mera semejanza de nombres no es evidencia de equivalencia.
- Para capacidades ausentes, se buscaron clases, rutas, entidades y migraciones Camra con los nombres y conceptos del dominio Fixi. La ausencia significa que no se encontró implementación en los módulos y esquemas inspeccionados, no una afirmación sobre sistemas externos no incluidos en el workspace.
- **Complejidad:** Baja / Media / Alta / Muy alta; estima adaptar Camra a la capacidad Fixi, no construir Fixi desde cero.
- **Confianza:** mide la solidez de la comparación. Las rutas Fixi se apoyan en la especificación suministrada; sin acceso a su repo no se pudo validar independientemente ese lado.

## Comparación por capacidad

### 1. Órdenes de reparación
1. **Capacidad Fixi:** ciclo de reparación de dispositivo desde recepción hasta entrega, con diagnóstico, presupuesto, estados, work logs, pagos, piezas, autorizaciones y garantías.
2. **Implementación actual en Fixi:** `service_orders`, eventos, documentos, autorizaciones y garantías; creación con cliente y tenant, clave de idempotencia, estados atómicos, costes estimado/final y efectos en caja e inventario.
3. **Pieza Camra relacionada:** `Stay` y su servicio de check-in/check-out; `Reservation` es una entidad previa al alojamiento, no una orden de servicio.
4. **Qué se puede reutilizar:** persistencia de estados, validaciones transaccionales, control de concurrencia/versión, coordinación entre servicios y manejo de fallos externos como patrones técnicos.
5. **Qué tendría que cambiar:** entidad, estados, transiciones, reglas de presupuesto, técnicos, consumo de piezas, entrega, cobro y relaciones con documentos/garantías se tendrían que crear para reparación.
6. **Dependencias hoteleras a eliminar:** huésped, habitación, reserva, estancia, ocupación, check-in/out, housekeeping, factura de estancia y reporte Alloggiati.
7. **Riesgos:** convertir `Stay` en `ServiceOrder` conservaría invariantes erróneas (una habitación ocupada, checkout pagado) y dañaría los límites de facturación y auditoría.
8. **Resultado:** **NO EXISTE** | Complejidad **Muy alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/orders.ts`, `20260424_baseline_schema.sql`, migraciones `t10_service_order_warranties`, `t11_service_order_authorizations`, `t14_work_logs_commissions`; Camra `frontdesk-service/.../stays/domain/Stay.java`, `frontdesk-service/.../stays/service/impl/StayServiceImpl.java`, `frontdesk-service/.../reservations/domain/Reservation.java`.
10. **Confianza:** **ALTO** en que Camra no tiene orden de reparación; el lado Fixi se basa en el documento adjunto.

### 2. Solicitudes de servicio
1. **Capacidad Fixi:** recibe leads/cotizaciones públicas y permite aprobar, rechazar o convertirlos en una orden.
2. **Implementación actual en Fixi:** `service_requests`; creación pública/anónima y conversión transaccional que crea cliente y orden.
3. **Pieza Camra relacionada:** `Quotation` es una cotización de estancia hotelera; `Reservation` ya representa una reserva de alojamiento con fechas y huéspedes.
4. **Qué se puede reutilizar:** patrones de endpoints públicos, validación, estados, notificación y transacción distribuida/local donde aplique.
5. **Qué tendría que cambiar:** inbox de solicitudes de reparación, su origen público, revisión, rechazo y conversión a orden y cliente.
6. **Dependencias hoteleras a eliminar:** tipo de habitación, disponibilidad, calendario de tarifas, noches, huéspedes y reserva de alojamiento.
7. **Riesgos:** una cotización o reservación no es un lead de reparación; reutilizarlas mezcla venta de alojamiento con recepción de trabajo aún no aceptado.
8. **Resultado:** **NO EXISTE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/requests.ts`, `20260728074005_convert_service_request_transaction.sql`; Camra `frontdesk-service/.../quotations/`, `frontdesk-service/.../reservations/`.
10. **Confianza:** **ALTO**.

### 3. Clientes
1. **Capacidad Fixi:** directorio CRM de clientes del tenant, con contacto, sucursal, etiquetas y archivo lógico.
2. **Implementación actual en Fixi:** tabla `customers`, CRUD, `is_active`, relaciones con órdenes y finanzas; un técnico puede crear un cliente desde una orden.
3. **Pieza Camra relacionada:** `Guest` en `guest-service`, con datos de contacto, fiscales, GDPR, documentos de identidad e historial hotelero consultado por estancia/reserva/facturación.
4. **Qué se puede reutilizar:** CRUD, búsquedas, aislamiento por tenant, soft delete, cliente de servicio entre microservicios y parte del manejo de datos personales.
5. **Qué tendría que cambiar:** renombrar semántica y campos, definir identidad de cliente, deduplicación/contactos, etiquetas, relación a sucursal y acceso desde técnicos/portal.
6. **Dependencias hoteleras a eliminar:** huéspedes de estancias, documentos de identidad, Alloggiati, consentimiento/retención hotelera y datos fiscales específicos de factura hotelera.
7. **Riesgos:** un huésped puede ser viajero temporal y tener varios ocupantes de una estancia; un cliente Fixi es contraparte comercial estable. La lógica GDPR y fiscal no es intercambiable sin revisar obligaciones.
8. **Resultado:** **ADAPTABLE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/customers.ts`, `20260424_baseline_schema.sql`, `20260609100000_add_customer_fk_finances.sql`; Camra `guest-service/.../model/Guest.java`, `guest-service/.../service/impl/GuestServiceImpl.java`, `guest-service/.../controller/GuestController.java`.
10. **Confianza:** **ALTO**.

### 4. Dispositivos
1. **Capacidad Fixi:** captura de marca, modelo, número de serie y atributos por industria para un aparato que se diagnostica y repara.
2. **Implementación actual en Fixi:** datos de dispositivo en la orden y catálogos configurables de dispositivos/campos.
3. **Pieza Camra relacionada:** `Room` y `RoomType` representan inventario físico hotelero, disponibilidad, ocupación y estado de limpieza.
4. **Qué se puede reutilizar:** solo técnicas genéricas de formularios, validación de campos configurables y endpoints CRUD.
5. **Qué tendría que cambiar:** entidad, atributos, identidad/serie, catálogos por industria, asociación cliente-orden, búsqueda y ciclo de vida del equipo.
6. **Dependencias hoteleras a eliminar:** habitación, tipo/capacidad de habitación, disponibilidad de alojamiento, estado ocupado/sucio/limpio y tarifas.
7. **Riesgos:** **no conviene reutilizar `Room`**: la habitación es un recurso reservable del hotel, no el bien propiedad del cliente que entra a reparación.
8. **Resultado:** **NO CONVIENE REUTILIZAR** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/device-catalogs.ts`, migraciones `20260724000000_phase1_catalogs.sql`, `20260527050000_tenant_field_definitions_phase2.sql`; Camra `frontdesk-service/.../rooms/domain/Room.java`, `frontdesk-service/.../rooms/domain/RoomType.java`.
10. **Confianza:** **ALTO**.

### 5. Recepción de dispositivos
1. **Capacidad Fixi:** alta inicial de aparato, cliente, condición/diagnóstico y generación de orden/folio antes del trabajo técnico.
2. **Implementación actual en Fixi:** flujo de órdenes y pantalla de órdenes, respaldado por capturas y datos de dispositivo.
3. **Pieza Camra relacionada:** recepción hotelera en `StayServiceImpl.checkIn`, que valida reserva/huésped/habitación, ocupa la habitación, abre factura y sincroniza reserva.
4. **Qué se puede reutilizar:** estructura de formulario, paginación, validación de entrada y patrón de orquestación con compensación/fallo.
5. **Qué tendría que cambiar:** el flujo de intake completo, impresión/folio, checklist de condición, pertenencias, autorización, fotos y transición a diagnóstico.
6. **Dependencias hoteleras a eliminar:** validar reserva de hotel, asignar cuarto, limitar ocupación, marcar cuarto ocupado y abrir factura de estancia.
7. **Riesgos:** “recepción” es aquí una función y ciclo completamente distintos; adaptar check-in podría heredar efectos irreversibles y datos personales hoteleros.
8. **Resultado:** **NO CONVIENE REUTILIZAR** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/web-admin/src/app/dashboard/ordenes/page.tsx`, `apps/api/src/routes/orders.ts`; Camra `frontdesk-service/.../stays/service/impl/StayServiceImpl.java`, `frontdesk-service/.../stays/service/impl/StayCheckInValidator.java`.
10. **Confianza:** **ALTO**.

### 6. Técnicos
1. **Capacidad Fixi:** usuarios de plantilla con rol de técnico, acceso operativo a órdenes y registro de trabajo propio.
2. **Implementación actual en Fixi:** `users` tenant-scoped, rol `technician`, sucursal asociada y restricciones RLS; módulo admin puede invitar, revocar y cambiar rol.
3. **Pieza Camra relacionada:** `UserAccount` y `Role`; roles operativos existentes son `RECEPTIONIST`, `KITCHEN`, `HOUSEKEEPER`, además de `ADMIN`/`OWNER`.
4. **Qué se puede reutilizar:** gestión de identidad, invitación/activación, credenciales, pertenencia a tenant, comprobaciones de rol y sesión.
5. **Qué tendría que cambiar:** rol técnico, perfil/capacidades, sucursal, asignación de órdenes, propiedad de work logs, comisiones y permisos de lectura/escritura.
6. **Dependencias hoteleras a eliminar:** recepción, cocina, housekeeping, acceso a huéspedes, reservas, habitaciones y facturación de hotel.
7. **Riesgos:** mapear `HOUSEKEEPER` o `RECEPTIONIST` a técnico concede permisos incorrectos; no hay perfil ni cola de asignaciones técnicas.
8. **Resultado:** **ADAPTABLE** | Complejidad **Media**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/users.ts`, `apps/web-admin/src/app/dashboard/tecnico/page.tsx`; Camra `auth-service/.../domain/UserAccount.java`, `auth-service/.../domain/Role.java`, `auth-service/.../service/UserManagementServiceImpl.java`.
10. **Confianza:** **ALTO**.

### 7. Work logs
1. **Capacidad Fixi:** iniciar, pausar y terminar actividad técnica por orden, guardando tiempos y estado del trabajo.
2. **Implementación actual en Fixi:** endpoints de work logs en `orders.ts`, estados `in_progress/paused/completed`, reglas para evitar logs activos simultáneos.
3. **Pieza Camra relacionada:** marcas de tiempo de estancia y entidades Spring Data auditadas con `created_at/updated_at`; no existe temporizador de trabajo por empleado.
4. **Qué se puede reutilizar:** timestamps, autenticación, validación y persistencia estándar como infraestructura.
5. **Qué tendría que cambiar:** construir entidad, transiciones, pausas, atribución a técnico/orden, duración y consulta de productividad.
6. **Dependencias hoteleras a eliminar:** ninguna entidad de trabajo análoga; el modelo de eventos de estancia no debe sustituir horas de técnico.
7. **Riesgos:** timestamps de auditoría de entidad no equivalen a jornada o tiempo facturable; derivarlo de estados de orden pierde pausas y autoría.
8. **Resultado:** **NO EXISTE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/orders.ts`, `20260625075448_t14_work_logs_commissions.sql`; Camra `frontdesk-service/.../stays/domain/Stay.java` (solo timestamps hoteleros; no work logs).
10. **Confianza:** **ALTO**.

### 8. Comisiones
1. **Capacidad Fixi:** cálculo de comisión técnica según reglas administradas por owner y trabajo realizado.
2. **Implementación actual en Fixi:** reglas de comisión y montos asociados a work logs, con impacto en métricas de productividad.
3. **Pieza Camra relacionada:** `OwnerReportService` agrega ingresos/facturas; no hay modelo de comisiones ni pagos a empleados.
4. **Qué se puede reutilizar:** aritmética de montos/decimales, reportes por tenant y permisos owner como componentes genéricos.
5. **Qué tendría que cambiar:** reglas configurables, base comisionable, aprobación, corte, redondeo, reversas y enlace a técnico/work log.
6. **Dependencias hoteleras a eliminar:** ingresos por estancia, habitación, factura fiscal y roles de hotel.
7. **Riesgos:** confundir venta/facturación hotelera con liquidación de nómina produce diferencias y obligaciones contables incorrectas.
8. **Resultado:** **NO EXISTE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `20260625075448_t14_work_logs_commissions.sql`, `apps/api/src/routes/orders.ts`; Camra `billing-service/.../service/impl/OwnerReportServiceImpl.java`.
10. **Confianza:** **ALTO**.

### 9. Inventario por sucursal
1. **Capacidad Fixi:** existencia por producto y sucursal, mínimo de stock, disponibilidad para consumo y valuación.
2. **Implementación actual en Fixi:** `products`, `sucursal_inventory`, `inventory_movements`, RLS y transferencias atómicas.
3. **Pieza Camra relacionada:** habitaciones y menú/pedidos F&B; no hay inventario de refacciones ni stock por ubicación.
4. **Qué se puede reutilizar:** PostgreSQL, transacciones, scoping de tenant, controles de concurrencia y formato de APIs.
5. **Qué tendría que cambiar:** modelo SKU/stock por sucursal, cantidades, mínimos, coste, ajuste, transferencia, recuento y valoración.
6. **Dependencias hoteleras a eliminar:** habitaciones, disponibilidad hotelera, menú de restaurante y cargos a estancia.
7. **Riesgos:** pedidos F&B y estado de habitaciones no implementan ledger de existencias, coste de mercancía ni reserva concurrente.
8. **Resultado:** **NO EXISTE** | Complejidad **Muy alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/inventory.ts`, migraciones `20260624120000_t07_inventory_reservations.sql`, `20260728123342_transactional_inventory_transfer.sql`; Camra `fb-service/.../domain/MenuItem.java`, `frontdesk-service/.../rooms/` (no stock de piezas).
10. **Confianza:** **ALTO**.

### 10. Reservas de inventario
1. **Capacidad Fixi:** reservar stock para una orden, consumirlo al usar la pieza o liberarlo al cancelar.
2. **Implementación actual en Fixi:** `inventory_reservations`, estados `active/consumed/released` y consumo transaccional.
3. **Pieza Camra relacionada:** `Reservation` reserva alojamiento para fechas; no bloquea productos ni controla disponibilidad de refacciones.
4. **Qué se puede reutilizar:** locking optimista/pesimista y disciplina transaccional, no la entidad ni las transiciones.
5. **Qué tendría que cambiar:** disponibilidad reservable por SKU/sucursal, expiración, cancelación y enlace a línea de orden.
6. **Dependencias hoteleras a eliminar:** fechas de check-in/out, huésped, habitación y ocupación.
7. **Riesgos:** **no mapear Reservation hotelera a reserva de stock**; cambia unidad reservada, concurrencia y cuándo se confirma/libera.
8. **Resultado:** **NO CONVIENE REUTILIZAR** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `20260624120000_t07_inventory_reservations.sql`, `apps/api/src/routes/inventory.ts`; Camra `frontdesk-service/.../reservations/domain/Reservation.java`, `frontdesk-service/.../reservations/service/impl/ReservationServiceImpl.java`.
10. **Confianza:** **ALTO**.

### 11. Movimientos de inventario
1. **Capacidad Fixi:** registrar entradas, salidas, transferencias y consumo con trazabilidad de cantidad/ubicación.
2. **Implementación actual en Fixi:** `inventory_movements`; reservas consumidas actualizan stock y movimiento en forma atómica.
3. **Pieza Camra relacionada:** cambios de estado de habitación, cargos de factura y transiciones de estancia; no hay ledger de movimientos de mercancía.
4. **Qué se puede reutilizar:** transacciones, timestamps, identidad de actor desde seguridad y manejo de errores.
5. **Qué tendría que cambiar:** tipos de movimiento, cantidades firmadas, sucursal origen/destino, motivo, referencias a orden/compra y balance.
6. **Dependencias hoteleras a eliminar:** ocupación/housekeeping y cargos por noches.
7. **Riesgos:** logs de aplicación o estados actuales no sustituyen un ledger inmutable conciliable; no reconstruyen saldo por SKU.
8. **Resultado:** **NO EXISTE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/inventory.ts`, `20260728123342_transactional_inventory_transfer.sql`; Camra `frontdesk-service/.../rooms/service/impl/RoomServiceImpl.java`, `billing-service/.../domain/InvoiceCharge.java`.
10. **Confianza:** **ALTO**.

### 12. Productos y refacciones
1. **Capacidad Fixi:** catálogo vendible/consumible con SKU y coste, utilizado por inventario, POS, compras y órdenes.
2. **Implementación actual en Fixi:** tabla `products`, asociada a inventario y ventas/órdenes.
3. **Pieza Camra relacionada:** `MenuItem` del restaurante y líneas de pedidos F&B; su propósito es menú/servicio de alimentos, no refacciones almacenadas.
4. **Qué se puede reutilizar:** validación de DTO, CRUD de catálogo y patrones de precios, solo como referencia de interfaz.
5. **Qué tendría que cambiar:** SKU, coste, unidad, categoría, impuestos, seguimiento de stock, sucursal, proveedor y relaciones de consumo/venta.
6. **Dependencias hoteleras a eliminar:** platos, menú, pedido de restaurante y cargo a habitación/estancia.
7. **Riesgos:** **no conviene reutilizar MenuItem**: su ciclo de pedido/consumo no conserva coste, serialidad, stock físico ni compra de repuestos.
8. **Resultado:** **NO CONVIENE REUTILIZAR** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/inventory.ts`, `apps/web-admin/src/app/dashboard/stock/page.tsx`; Camra `fb-service/.../domain/MenuItem.java`, `fb-service/.../domain/OrderItem.java`.
10. **Confianza:** **ALTO**.

### 13. Proveedores
1. **Capacidad Fixi:** directorio tenant-scoped de mayoristas de refacciones, contacto y términos de pago.
2. **Implementación actual en Fixi:** `suppliers`, CRUD y unicidad de nombre por tenant, relacionado con compras.
3. **Pieza Camra relacionada:** no se encontró entidad ni servicio de proveedores en compras; `Guest` y servicios externos no son proveedores comerciales.
4. **Qué se puede reutilizar:** patrón genérico de CRUD y validación tenant-scoped.
5. **Qué tendría que cambiar:** implementar proveedor, contactos, condiciones de pago, estado, referencias, deduplicación y acceso a procurement.
6. **Dependencias hoteleras a eliminar:** ninguna contraparte hotelera; no asociar proveedores con huéspedes ni Alloggiati.
7. **Riesgos:** inventar relación con invitados o proveedores externos de hotel ampliaría permisos y mezcla PII con compras.
8. **Resultado:** **NO EXISTE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/suppliers.ts`; Camra `docs/CAMRA_SYSTEM_SPEC.md`, `billing-service/` y `fb-service/` (sin módulo de suppliers/procurement identificado).
10. **Confianza:** **ALTO**.

### 14. Compras
1. **Capacidad Fixi:** órdenes de compra a proveedor con recepción parcial/total, aumento de stock y registro de gasto.
2. **Implementación actual en Fixi:** `purchase_orders`, APIs procurement/purchase-orders y automatización de entrada a stock/finanzas.
3. **Pieza Camra relacionada:** no hay flujo de compra de bienes; cargos de facturación y órdenes de restaurante no son órdenes de compra.
4. **Qué se puede reutilizar:** persistencia, transacciones, generación PDF y patrones de integración entre servicios como infraestructura.
5. **Qué tendría que cambiar:** cabecera/líneas/estado de PO, proveedor, recepción, diferencias, coste y asientos de gasto/stock.
6. **Dependencias hoteleras a eliminar:** cargos de habitación, menú y facturas a huésped.
7. **Riesgos:** recibir un pedido F&B o aplicar cargos de factura no asegura actualización de stock comprable ni conciliación proveedor.
8. **Resultado:** **NO EXISTE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/procurement.ts`, `apps/api/src/routes/purchase-orders.ts`; Camra `fb-service/.../domain/RestaurantOrder.java`, `billing-service/.../domain/Invoice.java`.
10. **Confianza:** **ALTO**.

### 15. POS
1. **Capacidad Fixi:** venta directa de producto/accesorio sin orden de reparación, cobro inmediato, ticket y descuento de stock.
2. **Implementación actual en Fixi:** pantalla POS y transacción atómica de venta, registro de ingreso y stock.
3. **Pieza Camra relacionada:** POS de restaurante/F&B crea `RestaurantOrder`, `OrderItem` y puede cargar consumo a estancia/factura.
4. **Qué se puede reutilizar:** carrito/estado de líneas y transacciones como ideas de implementación, generación PDF si se adapta y autorización por rol.
5. **Qué tendría que cambiar:** productos, pago inmediato, caja, ticket, inventario por sucursal y venta sin asociación a estancia.
6. **Dependencias hoteleras a eliminar:** menú, cocina, huésped, mesa/habitación, consumo y cargo al folio de estancia.
7. **Riesgos:** el pedido de restaurante no garantiza captura de forma de pago, cambio de caja, decremento de SKU o comprobante de venta Fixi.
8. **Resultado:** **NO CONVIENE REUTILIZAR** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/web-admin/src/app/dashboard/pos/page.tsx`, `20260726000000_execute_pos_sale_transaction.sql`; Camra `fb-service/.../domain/RestaurantOrder.java`, `fb-service/.../domain/OrderItem.java`, `billing-service/.../domain/Invoice.java`.
10. **Confianza:** **ALTO**.

### 16. Caja
1. **Capacidad Fixi:** apertura/cierre por turno y sucursal, arqueo, discrepancia y bloqueo de ventas en efectivo si no hay caja abierta.
2. **Implementación actual en Fixi:** cash registers y validación de caja abierta; integra cobros de órdenes y ventas POS.
3. **Pieza Camra relacionada:** `Payment` registra pago de factura; no se encontró caja física, turno, saldo inicial/final o arqueo.
4. **Qué se puede reutilizar:** tipos de pago y transacción de pago, no el cierre de efectivo.
5. **Qué tendría que cambiar:** sesión de caja, depósitos/retiros, conciliación, discrepancia, cierre y relación con caja de sucursal/usuario.
6. **Dependencias hoteleras a eliminar:** invoice/checkout de estancia y roles de recepción hotelera.
7. **Riesgos:** una factura pagada no demuestra que el efectivo fue recibido en una caja/turno específico ni resuelve diferencias.
8. **Resultado:** **NO EXISTE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/cash.ts`, `20260724000001_phase3_pos_cash.sql`; Camra `billing-service/.../domain/Payment.java`, `billing-service/.../service/impl/PaymentServiceImpl.java`.
10. **Confianza:** **ALTO**.

### 17. Pagos
1. **Capacidad Fixi:** registrar pagos/cobros de órdenes y ventas, con método, balance pendiente y reembolsos.
2. **Implementación actual en Fixi:** pagos enlazados a órdenes/finanzas y reembolsos enlazados a pago padre; override de saldo requiere permiso.
3. **Pieza Camra relacionada:** `Payment` enlazado a `Invoice`, `PaymentService` y control de factura pagada antes del checkout.
4. **Qué se puede reutilizar:** almacenamiento de monto/fecha/método/referencia, asociación padre-hijo, estados de pago y validación de saldo como patrón.
5. **Qué tendría que cambiar:** fuente de cargo, pagos parciales, saldo por orden, reembolso, caja, POS y reglas de autorización Fixi.
6. **Dependencias hoteleras a eliminar:** factura fiscal del huésped, factura de estancia, checkout y `hotelId` como contexto de hotel.
7. **Riesgos:** el pago Camra satisface una factura hotelera; no representa ledger de órdenes, ventas POS ni turno de caja. Confirmar también requisitos de CFDI/tax antes de reutilizar.
8. **Resultado:** **ADAPTABLE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/orders.ts`, `apps/api/src/routes/pos.ts`, `20260622000000_t06_refund_parent_payment.sql`; Camra `billing-service/.../domain/Payment.java`, `billing-service/.../service/impl/PaymentServiceImpl.java`.
10. **Confianza:** **ALTO**.

### 18. Finanzas
1. **Capacidad Fixi:** ingresos, gastos manuales/automáticos, reembolsos, conciliación y P&L operativo de negocio.
2. **Implementación actual en Fixi:** `finance.ts` y tablas financieras; ingresos desde órdenes/POS, gastos desde compras y vistas/resúmenes.
3. **Pieza Camra relacionada:** billing administra factura fiscal y pagos; `OwnerReportServiceImpl` suma ingresos de facturas por fecha.
4. **Qué se puede reutilizar:** aritmética de agregaciones, filtros de fechas, exportación/renderizado de documentos y scoping por tenant.
5. **Qué tendría que cambiar:** libro de ingresos/gastos, categorías, asientos, conciliación, origen, reembolsos, P&L y vínculos con compra/orden/caja.
6. **Dependencias hoteleras a eliminar:** huésped, estancia, cargos nocturnos, reservas, cumplimiento fiscal hotelero y folios de factura.
7. **Riesgos:** el reporte de ingresos facturados no es contabilidad de caja/P&L; confundir facturas fiscales con cobros o costes oculta gastos y saldos.
8. **Resultado:** **NO EXISTE** como finanzas operativas Fixi | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/finance.ts`, `apps/web-admin/src/app/dashboard/finanzas/page.tsx`, `20260622000000_t06_refund_parent_payment.sql`; Camra `billing-service/.../service/impl/OwnerReportServiceImpl.java`, `billing-service/.../domain/Invoice.java`.
10. **Confianza:** **ALTO**.

### 19. Sucursales
1. **Capacidad Fixi:** varias ubicaciones operativas bajo un tenant, con inventario, caja, usuarios y órdenes ligados a sucursal.
2. **Implementación actual en Fixi:** `sucursales`, migración de `branches`, y `sucursal_id` en inventario/clientes/usuarios.
3. **Pieza Camra relacionada:** `hotelId` funciona como tenant y propiedad hotelera; no se encontró entidad `Branch/Sucursal` dentro de un hotel ni scoping operacional multi-sucursal.
4. **Qué se puede reutilizar:** separación tenant y relaciones por identificador como base del aislamiento superior.
5. **Qué tendría que cambiar:** jerarquía tenant -> sucursal, reglas de acceso por sucursal, inventario/caja multiubicación y reportes agregados/separados.
6. **Dependencias hoteleras a eliminar:** suponer que cada tenant equivale a una sola propiedad/hotel y definir propiedades como `HotelRegistry`.
7. **Riesgos:** un hotel Camra por tenant no equivale a un negocio Fixi con sucursales compartiendo catálogo, usuarios e inventario.
8. **Resultado:** **ADAPTABLE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/sucursales.ts`, migraciones `20260527091000_cutover_branches_to_sucursales.sql`, `20260527093000_migrate_branch_fks_to_sucursales.sql`; Camra `auth-service/.../domain/HotelRegistry.java`, `auth-service/.../domain/UserAccount.java`.
10. **Confianza:** **ALTO**.

### 20. Tareas internas
1. **Capacidad Fixi:** crear/asignar/completar tareas como llamar al cliente o conseguir pieza, opcionalmente vinculadas a orden.
2. **Implementación actual en Fixi:** tabla/endpoints `tasks`, estados open/in_progress/completed y notificación al usuario asignado.
3. **Pieza Camra relacionada:** tareas de retención GDPR programadas y operaciones pendientes/reintentos de check-in, sin entidad de tareas asignables a empleados.
4. **Qué se puede reutilizar:** scheduler, ejecución background, identidad de usuarios, notificaciones y estados comunes como patrón.
5. **Qué tendría que cambiar:** CRUD de tareas, asignación, fecha de vencimiento, prioridad, enlaces a orden, permisos y avisos de vencimiento.
6. **Dependencias hoteleras a eliminar:** trabajos de retención GDPR o tareas ligadas a estancia/check-in no deben ser tareas Fixi.
7. **Riesgos:** logs/reintentos técnicos no son trabajo humano asignado y no ofrecen lifecycle ni notificación apropiados.
8. **Resultado:** **NO EXISTE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/tasks.ts`, `20260609100001_create_tasks_table.sql`; Camra `guest-service/.../service/impl/GuestRetentionJobServiceImpl.java`, `notification-service/.../service/impl/NotificationServiceImpl.java`.
10. **Confianza:** **ALTO**.

### 21. Portal cliente
1. **Capacidad Fixi:** cliente consulta estado de reparación con folio/token, descarga comprobante, conversa y aprueba presupuesto.
2. **Implementación actual en Fixi:** `web-clientes` ruta por tenant/folio, token público de orden, backend `public-portal.ts` y consentimiento persistido.
3. **Pieza Camra relacionada:** reserva pública/onboarding hotelero; el rol `GUEST` está reservado para portal futuro y actualmente no tiene acceso API. No se encontró portal de consulta de estado de estancia por token.
4. **Qué se puede reutilizar:** generación/validación de token público, frontend React, protección anti-CSRF/rate-limit y diseño de endpoints de lectura pública si aplican.
5. **Qué tendría que cambiar:** portal tokenizado de orden, timeline técnico, descarga de recibo/evidencia, conversación y aprobación/refutación de presupuesto.
6. **Dependencias hoteleras a eliminar:** búsqueda de disponibilidad, habitaciones, tarifas, reserva y datos de estancia.
7. **Riesgos:** una página de reserva pública busca vender alojamiento; no resuelve autenticación/privacidad de reparación ni autorización de cambios.
8. **Resultado:** **NO CONVIENE REUTILIZAR** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/web-clientes/src/app/t/[tenantSlug]/portal/[folio]/page.tsx`, `apps/api/src/routes/public-portal.ts`, `20260530143000_add_public_token_to_service_orders.sql`; Camra `api-gateway/.../filter/PublicBookingFilter.java`, `auth-service/.../domain/Role.java`.
10. **Confianza:** **ALTO**.

### 22. Autorizaciones de presupuesto
1. **Capacidad Fixi:** registrar consentimiento del cliente al presupuesto/diagnóstico y evidencias de decisión ligadas a orden.
2. **Implementación actual en Fixi:** `service_order_authorizations`, estado de orden aprobado y evidencia visible desde portal.
3. **Pieza Camra relacionada:** el asistente propone mutaciones que el operador confirma; no hay consentimiento de huésped a un presupuesto de reparación. Aprobación de quotation/reservation tiene otra finalidad.
4. **Qué se puede reutilizar:** patrón de propuesta, revisión de parámetros, actor autenticado, registro temporal y rechazo/cancelación.
5. **Qué tendría que cambiar:** actor cliente, presupuesto/versionado, consentimiento persistente, trazabilidad, revocación y transición segura de la orden.
6. **Dependencias hoteleras a eliminar:** usuario empleado, rol receptionist y cotización de alojamiento.
7. **Riesgos:** confirmación de staff a una acción de IA no prueba consentimiento legal/comercial del cliente.
8. **Resultado:** **NO EXISTE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi migraciones `t11_service_order_authorizations`, `20260622001000_t02_consent_evidence_visibility.sql`; Camra `frontend/src/pages/Assistant.tsx`, `frontdesk-service/.../quotations/`.
10. **Confianza:** **ALTO**.

### 23. Garantías
1. **Capacidad Fixi:** gestionar garantía post-entrega vinculada al servicio/dispositivo y sus condiciones/periodo.
2. **Implementación actual en Fixi:** tabla y estados mediante `t10_service_order_warranties`.
3. **Pieza Camra relacionada:** estancia/factura y soft delete hotelero; no se encontró garantía de reparación o producto.
4. **Qué se puede reutilizar:** IDs, fechas, tenant scope, estados y relaciones como infraestructura de persistencia.
5. **Qué tendría que cambiar:** condiciones/cobertura, fechas, reclamaciones, órdenes cubiertas, exclusiones y resolución.
6. **Dependencias hoteleras a eliminar:** estancia, reserva, habitación, huésped y obligaciones Alloggiati.
7. **Riesgos:** obligaciones/periodo de garantía y evidencia por reparación no se derivan de factura hotelera ni de política de cancelación.
8. **Resultado:** **NO EXISTE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `t10_service_order_warranties`; Camra `billing-service/.../domain/Invoice.java`, `frontdesk-service/.../stays/domain/Stay.java` (sin garantía).
10. **Confianza:** **ALTO**.

### 24. Documentos y evidencias de reparación
1. **Capacidad Fixi:** adjuntar y consultar documentos/evidencias asociados a orden, autorización, diagnóstico y entrega.
2. **Implementación actual en Fixi:** `service_order_documents`, comprobantes y evidencia de consentimiento.
3. **Pieza Camra relacionada:** `IdentityDocument` guarda datos estructurados de identificación de huésped; billing genera PDF de factura/quotation. No es repositorio de fotos/evidencia de reparación.
4. **Qué se puede reutilizar:** generación PDF y entrega como adjunto; validación tenant y controles de privacidad como patrones.
5. **Qué tendría que cambiar:** carga/almacenamiento de archivos, metadatos, límites MIME/tamaño, vínculo a orden/tipo de evidencia, retención y acceso del portal.
6. **Dependencias hoteleras a eliminar:** números/fechas de identidad, huésped y documentos regulatorios de estancia.
7. **Riesgos:** confundir PII de documentos de identidad con pruebas de estado del dispositivo expone datos altamente sensibles; PDF generado no equivale a almacenamiento de adjuntos.
8. **Resultado:** **ADAPTABLE** solo para generación/descarga PDF; la gestión de evidencia es **NO EXISTE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `service_order_documents`, `service_order_authorizations`; Camra `guest-service/.../model/IdentityDocument.java`, `billing-service/.../service/impl/PdfInvoiceServiceImpl.java`, `pdf-template-engine/`.
10. **Confianza:** **ALTO**.

### 25. Notificaciones
1. **Capacidad Fixi:** notificar cambios de orden por WhatsApp/WebPush/correo a cliente y empleados.
2. **Implementación actual en Fixi:** cambios de estado disparan avisos; cola de mensajes con retry y notificación de tareas WebPush según especificación.
3. **Pieza Camra relacionada:** `notification-service` renderiza plantillas Thymeleaf y envía correo SMTP para reservación, check-in/out y quotation; circuit breakers desde clientes. Evolution solo conecta WhatsApp, no se encontró envío de mensajes de negocio completo.
4. **Qué se puede reutilizar:** SMTP, remitente, adjuntos PDF, plantillas/renderizado, clientes Feign y aislamiento de fallo de proveedor.
5. **Qué tendría que cambiar:** eventos Fixi, destinatarios, plantillas de reparación, canales WhatsApp/WebPush y preferencias/consentimientos de notificación.
6. **Dependencias hoteleras a eliminar:** plantillas en italiano/inglés hoteleras, huéspedes, nombre de hotel, checkout e invoice de estancia.
7. **Riesgos:** Camra notifica reservas/estancias; no ofrece la cola multipropósito ni garantiza actualmente WhatsApp de avisos Fixi.
8. **Resultado:** **ADAPTABLE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/automation.ts`, `apps/api/src/routes/orders.ts`, `20260530150000_add_pwa_push_subscriptions.sql`; Camra `notification-service/.../service/impl/NotificationServiceImpl.java`, `frontdesk-service/.../client/NotificationClient.java`, `frontdesk-service/.../whatsapp/EvolutionService.java`.
10. **Confianza:** **ALTO**.

### 26. Automatizaciones
1. **Capacidad Fixi:** cola de mensajes/acciones en background con estados, retry, reintento manual, purge y workers.
2. **Implementación actual en Fixi:** message queue con pending/processing/failed/completed, retry_count, webhook de pagos y reglas de automatización.
3. **Pieza Camra relacionada:** jobs programados (p. ej. retención GDPR), sagas check-in/out y retry/resiliencia de llamadas; no se encontró cola genérica de trabajos de negocio.
4. **Qué se puede reutilizar:** scheduler, configuración Resilience4j, circuit breakers, métricas y patrones de estados/error.
5. **Qué tendría que cambiar:** cola durable, workers, idempotencia, retry/backoff, dead-letter/observabilidad, controles de retry/purge y triggers de Fixi.
6. **Dependencias hoteleras a eliminar:** retención GDPR, check-in/out, Alloggiati e invoice de estancia.
7. **Riesgos:** reintentar un endpoint no es una cola durable; los efectos de terceros y la entrega al menos una vez necesitan idempotencia explícita.
8. **Resultado:** **NO EXISTE** como motor/cola de automatizaciones | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/automation.ts`, `20260625070326_t13_message_queue.sql`; Camra `guest-service/.../service/impl/GuestRetentionJobServiceImpl.java`, `frontdesk-service/.../stays/service/impl/StayServiceImpl.java`.
10. **Confianza:** **ALTO**.

### 27. Reportes y dashboard
1. **Capacidad Fixi:** métricas BI sobre órdenes, técnicos, tiempos, stock, ventas, finanzas, filtros fecha/sucursal y exportación.
2. **Implementación actual en Fixi:** `reports.ts`, agregaciones/vistas y restricción por plan y rango.
3. **Pieza Camra relacionada:** `OwnerReportServiceImpl` suma facturas por hotel/fecha y reporta cantidad/estado de facturas; frontdesk tiene resumen operativo hotelero.
4. **Qué se puede reutilizar:** filtros temporales, agregaciones, DTO/export y patrón de tenant-scoped reporting.
5. **Qué tendría que cambiar:** métricas, tablas fuente, drill-down, sucursal, productividad, inventario y planes/capabilities Fixi.
6. **Dependencias hoteleras a eliminar:** noches, huéspedes, habitaciones, estancias, invoices de alojamiento y ocupación.
7. **Riesgos:** ingresos de facturas no equivalen a ventas cobradas; reportes actuales no calculan productividad técnica, costes de pieza o P&L Fixi.
8. **Resultado:** **ADAPTABLE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/reports.ts`, `apps/web-admin/src/app/dashboard/reportes/page.tsx`; Camra `billing-service/.../service/impl/OwnerReportServiceImpl.java`, `billing-service/.../controller/OwnerReportController.java`.
10. **Confianza:** **ALTO**.

### 28. Usuarios
1. **Capacidad Fixi:** empleados tenant-scoped, invitación/revocación/cambio de rol, estado y sucursal.
2. **Implementación actual en Fixi:** tabla `users`, `auth_user_id`, rol, `sucursal_id`, MFA, estado, reglas RLS y alta de tenant en signup.
3. **Pieza Camra relacionada:** `UserAccount`, `UserManagementService`, login/refresh, invitación/creación de cuenta de hotel y `hotelId`.
4. **Qué se puede reutilizar:** credenciales, alta/desactivación, JWT, versionado de tokens, gestión admin y pertenencia tenant.
5. **Qué tendría que cambiar:** identidad/proveedor actual, enlace con Supabase `auth_user_id`, roles Fixi, sucursal, MFA y flujo de invitación/registro específico.
6. **Dependencias hoteleras a eliminar:** `HotelRegistry` y supuestos de owner/empleado de un hotel.
7. **Riesgos:** incompatibilidad de usuarios/password hashes/tokens; sincronización parcial entre dos identity providers puede dejar accesos huérfanos.
8. **Resultado:** **ADAPTABLE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/users.ts`, `20260530120000_expand_users_admin_module.sql`, `20260531063240_add_users_mfa_enabled_compatibility.sql`; Camra `auth-service/.../domain/UserAccount.java`, `auth-service/.../service/UserManagementServiceImpl.java`.
10. **Confianza:** **ALTO**.

### 29. Roles y permisos
1. **Capacidad Fixi:** jerarquía owner > manager > technician > receptionist, permisos granulares y restricciones por módulo/sucursal.
2. **Implementación actual en Fixi:** backend routes/RLS, `tenant-roles.ts`, permisos especiales para override de balance y separación técnico/manager.
3. **Pieza Camra relacionada:** enum de roles hoteleros, reglas de gateway por rutas y `@PreAuthorize` en endpoints.
4. **Qué se puede reutilizar:** RBAC en gateway/Spring Security, anotaciones, extracción de rol y pruebas de autorización.
5. **Qué tendría que cambiar:** catálogo de roles, matriz endpoint/acción, alcance por sucursal, permisos granulares y políticas RLS equivalentes.
6. **Dependencias hoteleras a eliminar:** ADMIN/OWNER/RECEPTIONIST/KITCHEN/HOUSEKEEPER y accesos hoteleros a huésped/F&B/estancia.
7. **Riesgos:** igual nombre de rol no implica privilegios iguales; además Fixi combina RLS y lógica API y no debe depender solo del gateway/UI.
8. **Resultado:** **ADAPTABLE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/security.ts`, `apps/api/src/routes/tenant-roles.ts`, `apps/api/src/routes/orders.ts`; Camra `auth-service/.../domain/Role.java`, `api-gateway/.../filter/AuthenticationFilter.java`, controllers con `@PreAuthorize`.
10. **Confianza:** **ALTO**.

### 30. Multi-tenancy y aislamiento
1. **Capacidad Fixi:** tenant RLS robusto, identidad obligatoria, datos cruzados protegidos y sub-ámbito por sucursal.
2. **Implementación actual en Fixi:** `tenant_id`, Supabase RLS, FKs tenant-aligned y policies endurecidas.
3. **Pieza Camra relacionada:** `hotelId` en JWT/contexto, filtros/queries hotel-scoped, separación de schemas/servicios y pruebas ArchUnit de aislamiento.
4. **Qué se puede reutilizar:** validación de contexto, propagación segura, scoping de repositorios, test arquitectónico y patrón de separación lógica por servicio.
5. **Qué tendría que cambiar:** generalizar `hotelId` a `tenantId`, eliminar reglas semánticas de hotel y agregar `sucursalId` como ámbito subordinado donde Fixi lo requiere.
6. **Dependencias hoteleras a eliminar:** registro de hotel, claims `hotelId`, vocabulario/llaves hoteleras y cualquier supuesto de una propiedad por tenant.
7. **Riesgos:** una query no scopeada filtra datos; Camra no debe presentarse como equivalente exacto a RLS PostgreSQL si su enforcement actual depende de contexto y queries Java.
8. **Resultado:** **ADAPTABLE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi migraciones `20260528000600_harden_live_inventory_rls.sql`, `20260530193000_audit_hardening_multitenant.sql`; Camra `api-gateway/.../filter/AuthenticationFilter.java`, `internal-auth-lib/.../security/InternalAuthFilter.java`, `billing-service/src/test/java/com/hotelpms/billing/architecture/TenantIsolationArchTest.java`.
10. **Confianza:** **ALTO**.

### 31. Autenticación
1. **Capacidad Fixi:** autenticar empleados/usuarios con identidad vinculada a `auth_user_id`; la especificación señala Supabase Auth/RLS.
2. **Implementación actual en Fixi:** usuarios tenant-scoped, MFA habilitado por esquema y rutas de autenticación según modelo Supabase.
3. **Pieza Camra relacionada:** `auth-service` con login, JWT access/refresh, rotación/invalidación de tokens y gateway que verifica cookie JWT.
4. **Qué se puede reutilizar:** validación/generación JWT, refresh rotation, expiración, token version, hashing de password y guardas de sesión si se acepta el cambio de identidad.
5. **Qué tendría que cambiar:** integración con identidad existente, cookies/CSRF, claims, MFA y ciclo de registro/recuperación para reproducir la experiencia Fixi.
6. **Dependencias hoteleras a eliminar:** claim `hotelId`, roles de hotel y asociación con `HotelRegistry`.
7. **Riesgos:** cambiar de Supabase Auth a auth-service afecta sesiones, contraseñas, reset, MFA y tokens de clientes; no es una migración transparente de identidad.
8. **Resultado:** **ADAPTABLE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/users.ts`, esquema `users.auth_user_id`; Camra `auth-service/.../service/JwtService.java`, `auth-service/.../controller/AuthController.java`, `api-gateway/.../filter/AuthenticationFilter.java`.
10. **Confianza:** **MEDIO** sobre detalles exactos del flujo Fixi (repositorio no accesible); **ALTO** sobre Camra.

### 32. Auditoría de negocio
1. **Capacidad Fixi:** registrar actor, acción, request id y cambios mutativos; audit logs inmutables respaldados por triggers/RLS.
2. **Implementación actual en Fixi:** `audit_logs`, hardening de mutaciones, inmutabilidad y `audit_request_id` requerido.
3. **Pieza Camra relacionada:** logs de aplicación, correlation ID/MDC y `created_at/updated_at` de JPA; Loki agrega logs en perfil observabilidad.
4. **Qué se puede reutilizar:** logs estructurados, correlation id, MDC, sanitización y pipeline Loki/Grafana.
5. **Qué tendría que cambiar:** crear registro de auditoría de negocio con actor/acción/objeto/antes-después, integridad/inmutabilidad, request id, consulta y retención.
6. **Dependencias hoteleras a eliminar:** etiquetas/identidades `hotelId` y nombres de entidades hoteleras; conservar contexto tenant genérico.
7. **Riesgos:** **logs y timestamps no son audit log**: no garantizan evento por mutación, actor, contenido inmutable ni búsqueda legal. La especificación Camra denomina “audit” a la agregación de logs, no a una tabla equivalente.
8. **Resultado:** **NO EXISTE** como auditoría de cambios Fixi | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/security.ts`, migraciones `20260530193000_audit_hardening_multitenant.sql`, `20260619152119_harden_audit_logs_immutable.sql`, `20260619152143_enforce_audit_request_id_and_immutability.sql`; Camra `api-gateway/.../filter/CorrelationIdFilter.java`, `docker-compose.yml`, `auth-service/src/main/resources/logback-spring.xml`.
10. **Confianza:** **ALTO**.

### 33. Seguridad
1. **Capacidad Fixi:** RLS, aislamiento tenant, inmutabilidad de auditoría, permisos por operación, tokens públicos y MFA.
2. **Implementación actual en Fixi:** policies endurecidas, roles/policies y controles de token para portal; detalles no validados fuera de la especificación aportada.
3. **Pieza Camra relacionada:** validación JWT/RBAC en gateway, headers internos firmados HMAC, nonce anti-replay Redis, filtros Spring Security, rate limiting, validación y cabeceras CSRF/security.
4. **Qué se puede reutilizar:** controles y librería de autenticación interna, reglas de gateway, configuración segura, escaneo Trivy y pruebas de seguridad/tenant como prácticas.
5. **Qué tendría que cambiar:** política de acceso Fixi, membership tenant/sucursal, credenciales, endpoints públicos, datos PCI/fiscales y threat model específico.
6. **Dependencias hoteleras a eliminar:** rutas/roles/claims y autorización por huésped, estancia, habitación y facturación hotelera.
7. **Riesgos:** diferencias de plataforma (Supabase RLS vs Spring services) y un shared HMAC secret de despliegue requieren revisar amenazas, rotación, red interna y controles equivalentes; no asumir paridad automática.
8. **Resultado:** **REUTILIZABLE** como controles técnicos con ajuste de políticas | Complejidad **Media**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/security.ts`, `apps/api/src/routes/public-portal.ts`, migraciones RLS/audit; Camra `api-gateway/.../filter/AuthenticationFilter.java`, `internal-auth-lib/.../security/InternalAuthFilter.java`, `auth-service/.../config/SecurityConfig.java`, `THREAT_MODEL.md`.
10. **Confianza:** **ALTO**.

### 34. Catálogos configurables
1. **Capacidad Fixi:** catálogo de marcas/modelos/campos de dispositivo ajustables por tenant/industria y usados dinámicamente en recepción.
2. **Implementación actual en Fixi:** rutas de device catalogs, field definitions y validaciones requeridas como serial number por industria.
3. **Pieza Camra relacionada:** `RoomType`, `RateSeason`, `RateCalendar`, tipos/estados hoteleros; no se identificó catálogo genérico definido por tenant.
4. **Qué se puede reutilizar:** CRUD, validación de esquema y frontend de formularios solo como patrón técnico.
5. **Qué tendría que cambiar:** metadata configurable de industria/dispositivo, versionado, campos, reglas y compatibilidad de órdenes existentes.
6. **Dependencias hoteleras a eliminar:** tipos de cuarto, temporadas/tarifas, estados de habitación y referencia Alloggiati.
7. **Riesgos:** **no conviene reutilizar RoomType/RateSeason**: son datos operativos hoteleros, no catálogo extensible de dispositivos.
8. **Resultado:** **NO CONVIENE REUTILIZAR** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/device-catalogs.ts`, `20260724000000_phase1_catalogs.sql`, `20260527050000_tenant_field_definitions_phase2.sql`; Camra `frontdesk-service/.../rooms/domain/RoomType.java`, `frontdesk-service/.../pricing/domain/RateSeason.java`.
10. **Confianza:** **ALTO**.

### 35. Configuración por tenant
1. **Capacidad Fixi:** configuración de sucursal, roles, módulos/capabilities, campos y opciones del tenant.
2. **Implementación actual en Fixi:** configuración tenant/industria/capabilities según rutas y migraciones de la especificación.
3. **Pieza Camra relacionada:** `HotelSettings`, fila por `hotelId`, con nombre/dirección, fiscalidad, locale, moneda, zona horaria, email, AI y credenciales Alloggiati.
4. **Qué se puede reutilizar:** patrón de settings por tenant, DTO/validación, cifrado de secretos, lectura desde contexto y valores por defecto.
5. **Qué tendría que cambiar:** separar configuración SaaS común, branding, sucursales, flags Fixi, catálogo configurable, límites/planes y secretos por proveedor.
6. **Dependencias hoteleras a eliminar:** Alloggiati, fiscalidad/localidades italianas, hotel name, habitaciones, huéspedes y configuración de emails de estancias.
7. **Riesgos:** `HotelSettings` mezcla preferencias, cumplimiento legal y credenciales; replicarla tal cual acoplaría Fixi a regulación hotelera italiana.
8. **Resultado:** **ADAPTABLE** | Complejidad **Media**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/public.ts`, `apps/api/src/routes/device-catalogs.ts`, migración `20260527030000_tenant_industry_config_phase1.sql`; Camra `frontdesk-service/.../stays/domain/HotelSettings.java`, `frontdesk-service/src/main/resources/db/migration/V17__add_tenant_ai_assistant.sql`.
10. **Confianza:** **ALTO**.

### 36. WhatsApp como canal de Fixi
1. **Capacidad Fixi:** notificaciones transaccionales de reparación a cliente/empleado mediante WhatsApp, junto a WebPush y correo.
2. **Implementación actual en Fixi:** actualización de estado dispara notificaciones según especificación; no se aporta la implementación completa del conector en este documento.
3. **Pieza Camra relacionada:** endpoint de conexión/status de WhatsApp y adaptador Evolution; especificación marca integración con IA/webhooks parcialmente pendiente.
4. **Qué se puede reutilizar:** conexión de cuenta, protección de secretos, identificación por tenant y QR de pairing.
5. **Qué tendría que cambiar:** plantillas, eventos, destinatarios cliente/empleado, envío, recepción/webhook, deduplicación, consentimiento y trazabilidad de entrega Fixi.
6. **Dependencias hoteleras a eliminar:** namespace `stays`, propietario hotel, mensaje de huésped y eventos de reservación/check-in/out.
7. **Riesgos:** no afirmar que Camra envía notificaciones Fixi: el código verificado solo expone estado/conexión, no un flujo completo de mensajes de negocio.
8. **Resultado:** **ADAPTABLE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/orders.ts`, `apps/api/src/routes/automation.ts`; Camra `frontdesk-service/.../whatsapp/WhatsAppController.java`, `frontdesk-service/.../whatsapp/EvolutionService.java`.
10. **Confianza:** **MEDIO** sobre alcance de WhatsApp Fixi; **ALTO** sobre alcance verificado Camra.

### 37. Evolution API
1. **Capacidad Fixi:** proveedor/canal externo necesario para mensajería, con conexión individual asociada al tenant.
2. **Implementación actual en Fixi:** la especificación menciona integración WhatsApp/WebPush y avisos; no identifica Evolution como proveedor actual.
3. **Pieza Camra relacionada:** `EvolutionService` construye instance name `pms-{hotelId}`, consume API key/base URL del servidor y entrega QR/status sanitizados.
4. **Qué se puede reutilizar:** cliente HTTP, protocolo Evolution, provisioning/status/QR, límites/timeout, sanitización de errores y separación de API key.
5. **Qué tendría que cambiar:** nombre neutro de instancia, asociación a `tenantId`, almacenamiento/configuración por cuenta y endpoints de envío/webhook Fixi.
6. **Dependencias hoteleras a eliminar:** prefijo de instancia y rutas bajo `/stays/whatsapp`; no trasladar hotel ID como dueño conceptual.
7. **Riesgos:** API key configurada globalmente no equivale a credenciales/cuenta aisladas por tenant; pairing operativo no prueba envío entrante/saliente ni integración con cola.
8. **Resultado:** **REUTILIZABLE** como adaptador de proveedor, con desacople y extensión funcional | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/automation.ts` (WhatsApp según especificación); Camra `frontdesk-service/.../whatsapp/EvolutionService.java`, `frontdesk-service/.../whatsapp/WhatsAppController.java`, `docker-compose.evolution.yml`.
10. **Confianza:** **ALTO** sobre implementación Camra; **MEDIO** sobre proveedor actual Fixi.

### 38. IA operativa
1. **Capacidad Fixi:** automatización/asistencia relacionada con órdenes, clientes, dispositivos y tareas según capacidades y permisos del tenant.
2. **Implementación actual en Fixi:** la especificación describe automatizaciones; no describe explícitamente un asistente LLM propio de Fixi.
3. **Pieza Camra relacionada:** asistente tenant-configured con Ollama/DeepSeek, system prompt hotelero, tools allow-listed, guardas de rol y Resilience4j.
4. **Qué se puede reutilizar:** cliente de proveedor, límites/timeout, cifrado de API key, manejo de mensajes, métricas de tokens, resiliencia y control de contexto tenant.
5. **Qué tendría que cambiar:** prompt, modelos autorizados, uso de datos Fixi, tool catalog, permisos, protección PII e instrucciones/medición por tenant.
6. **Dependencias hoteleras a eliminar:** nombre/horario/locale hotelero, huéspedes, check-in, reservaciones, habitaciones, facturas y F&B.
7. **Riesgos:** la presencia de IA en Camra no demuestra capacidad IA equivalente en Fixi; las tools actuales operan sobre PMS de hotel y exponen datos/acciones de ese dominio.
8. **Resultado:** **ADAPTABLE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/automation.ts` (solo automatizaciones indicadas); Camra `frontdesk-service/.../assistant/AssistantService.java`, `frontdesk-service/.../stays/domain/HotelSettings.java`, migraciones `V21`, `V22`, `V23`.
10. **Confianza:** **MEDIO**.

### 39. Tool calling
1. **Capacidad Fixi:** exponer operaciones/consultas del sistema a automatizaciones/agentes con validación de rol.
2. **Implementación actual en Fixi:** no hay descripción de tools LLM en la especificación; automatización se implementa mediante endpoints/colas.
3. **Pieza Camra relacionada:** `AssistantToolCatalog` declara operaciones por rol; proveedor OpenAI-compatible devuelve tool calls y el frontend ejecuta consultas/acciones mediante `assistantToolService`.
4. **Qué se puede reutilizar:** DTO provider-neutral, allow-list de operaciones, separación read/action, parser de tool calls, límite de rondas y adaptador UI/API.
5. **Qué tendría que cambiar:** cada operación, esquema/validación de parámetros, backends, roles, límites, política de datos y lógica de idempotencia Fixi.
6. **Dependencias hoteleras a eliminar:** todos los nombres/DTOs de huésped, reserva, estancia, habitación, invoice, tarifa y pedidos F&B.
7. **Riesgos:** ejecutar tools del modelo sin validar parámetros/tenant en servidor; catálogo por rol no sustituye autorización de cada endpoint.
8. **Resultado:** **ADAPTABLE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/automation.ts`; Camra `frontdesk-service/.../assistant/AssistantToolCatalog.java`, `frontdesk-service/.../assistant/AssistantService.java`, `frontend/src/services/assistantToolService.ts`.
10. **Confianza:** **ALTO**.

### 40. Confirmaciones humanas
1. **Capacidad Fixi:** autorización humana de acciones relevantes; además el cliente aprueba presupuesto de reparación, que es un consentimiento de negocio distinto.
2. **Implementación actual en Fixi:** portal guarda aprobación del diagnóstico/presupuesto en `service_order_authorizations`.
3. **Pieza Camra relacionada:** el modelo solo prepara mutaciones; frontend presenta parámetros y exige “Confirmar y ejecutar”; consulta de lectura se ejecuta automáticamente.
4. **Qué se puede reutilizar:** separación lectura/propuesta, pantalla de revisión/cancelación, protección contra doble ejecución y estados de conversación.
5. **Qué tendría que cambiar:** integración a acciones Fixi, política de qué requiere confirmación y almacenamiento servidor-side de consentimiento durable donde tenga efecto contractual.
6. **Dependencias hoteleras a eliminar:** descripciones/acciones específicas de hotel y rol receptionist; no confundirlo con aceptación por el cliente.
7. **Riesgos:** la confirmación IA de un empleado no sustituye una autorización del cliente, ni crea por sí sola auditoría durable o idempotencia backend.
8. **Resultado:** **REUTILIZABLE** como patrón de interacción humana; consentimiento de cliente Fixi **NO EXISTE** en Camra | Complejidad **Media**.
9. **Archivos relevantes:** Fixi `apps/web-clientes/src/app/t/[tenantSlug]/portal/[folio]/page.tsx`, `service_order_authorizations`; Camra `frontend/src/pages/Assistant.tsx`, `frontend/src/pages/Assistant.test.tsx`, `frontdesk-service/.../assistant/AssistantToolCatalog.java`.
10. **Confianza:** **ALTO**.

### 41. Resiliencia
1. **Capacidad Fixi:** reintentos e idempotencia de operaciones transaccionales/notificaciones e integraciones, manejo de fallos y límites.
2. **Implementación actual en Fixi:** transactions RPC para stock, conversiones y ventas; cola con retry_count y workers según especificación.
3. **Pieza Camra relacionada:** Resilience4j circuit breaker/retry/rate limiter en llamadas de IA y Feign; estado visible para algunos fallos de factura/email; timeouts externos.
4. **Qué se puede reutilizar:** librería/configuración de circuit breakers, backoff/retry para fallos transitorios, timeouts y patrones de degradación.
5. **Qué tendría que cambiar:** nombres/config por dependencia, estrategia de idempotencia, límites de retries, cola durable, recuperación de transacciones e impacto en caja/inventario Fixi.
6. **Dependencias hoteleras a eliminar:** named breakers y coordinadores para guest/billing/reservation/stay/Alloggiati.
7. **Riesgos:** reintentar una mutación de pago, venta o stock sin clave de idempotencia puede duplicar cargo/decremento; circuit breaker no es garantía de entrega.
8. **Resultado:** **REUTILIZABLE** como infraestructura con políticas por operación | Complejidad **Media**.
9. **Archivos relevantes:** Fixi `20260727000002_add_idempotency_key_orders.sql`, `20260728123342_transactional_inventory_transfer.sql`, `20260726000000_execute_pos_sale_transaction.sql`; Camra `frontdesk-service/.../assistant/AssistantService.java`, `frontdesk-service/.../client/BillingClient.java`, `config-service/src/main/resources/config/frontdesk-service.yml`.
10. **Confianza:** **ALTO**.

### 42. Observabilidad
1. **Capacidad Fixi:** logs/auditabilidad operativa, métricas, reportes y seguimiento de fallos de módulos.
2. **Implementación actual en Fixi:** audit log de negocio por separado; especificación no detalla stack de métricas/trazas.
3. **Pieza Camra relacionada:** Actuator/Micrometer, Prometheus, Grafana, Loki, Zipkin, Alertmanager y correlation ID configurados para despliegue.
4. **Qué se puede reutilizar:** exporters, dashboards/alertas base, propagación de correlation ID, scraping y agregación de logs.
5. **Qué tendría que cambiar:** etiquetas tenant/sucursal seguras, SLO y métricas de órdenes/stock/pagos/colas, alarmas y redacción de PII.
6. **Dependencias hoteleras a eliminar:** etiquetas de hotel, métricas de check-in/occupancy/Alloggiati y dashboards hoteleros.
7. **Riesgos:** logs no sustituyen el audit log de Fixi; cardinalidad por tenant puede encarecer Prometheus y logs pueden exponer PII si no se sanea.
8. **Resultado:** **REUTILIZABLE** stack técnico; auditoría funcional es aparte | Complejidad **Media**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/security.ts` (auditoría de negocio), `apps/api/src/routes/reports.ts`; Camra `docker/prometheus/`, `docker/grafana/`, `docker/loki/`, `config-service/src/main/resources/config/`, `api-gateway/.../filter/CorrelationIdFilter.java`.
10. **Confianza:** **ALTO**.

### 43. Infraestructura de ejecución
1. **Capacidad Fixi:** ejecutar API/web, DB, Redis/colas, jobs y servicios multi-tenant desplegables.
2. **Implementación actual en Fixi:** backend/web-admin/web-clientes y Supabase según rutas y esquemas descritos; orquestación de colas.
3. **Pieza Camra relacionada:** microservicios Spring Boot, PostgreSQL, Redis, Config Server, gateway, contenedores, networks segmentadas y respaldos pgBackRest.
4. **Qué se puede reutilizar:** patrones Docker, healthchecks, límites de recursos, redes, Postgres, Redis, configuración externa y backup/restore si la plataforma destino lo permite.
5. **Qué tendría que cambiar:** servicios/builds/secretos, migraciones, topology, DB ownership, endpoints, web stack y aprovisionamiento de tenant.
6. **Dependencias hoteleras a eliminar:** nombres, contenedores, esquemas `hotel_*`, Alloggiati/FatturaPA y configuración PMS específica.
7. **Riesgos:** cambiar Supabase/RLS por DB compartida con microservicios mueve el punto de enforcement; backups, restauración y secretos deben conservar aislamiento.
8. **Resultado:** **REUTILIZABLE** como patrones de plataforma, no como despliegue listo para Fixi | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi referencias a `apps/api`, `apps/web-admin`, `apps/web-clientes`, migraciones Supabase; Camra `docker-compose.yml`, `docker-compose.prod.yml`, `docker/postgres/`, `THREAT_MODEL.md`.
10. **Confianza:** **MEDIO** (topología Fixi se describe pero no se inspeccionó su código/compose).

### 44. Testing
1. **Capacidad Fixi:** pruebas de reglas RLS/tenant, transacciones atómicas, APIs, portales y flujos frontend.
2. **Implementación actual en Fixi:** la especificación refiere pruebas de transacciones y hardening, pero no inventaría framework/coverage no consignado.
3. **Pieza Camra relacionada:** JUnit/Mockito, ArchUnit `TenantIsolationArchTest`, Vitest/frontend tests, Playwright E2E de tenant onboarding, Gradle JaCoCo/PMD/Checkstyle.
4. **Qué se puede reutilizar:** principios y esqueletos de tests de aislamiento, contratos, pruebas de integración/E2E, quality gates y generación de reportes.
5. **Qué tendría que cambiar:** fixtures/DB Supabase, identidad, entidades, rutas, roles Fixi y asserts de sus invariantes transaccionales.
6. **Dependencias hoteleras a eliminar:** fixtures de hotel/huésped/estancia/habitación y pruebas de check-in/F&B/factura.
7. **Riesgos:** reutilizar solo tests Camra dejaría sin cobertura stock concurrente, autorización de cliente, caja, multi-sucursal e idempotencia de venta.
8. **Resultado:** **REUTILIZABLE** herramientas/patrones; suites concretas requieren reescritura | Complejidad **Media**.
9. **Archivos relevantes:** Fixi rutas/migraciones identificadas en las fichas (repo no disponible); Camra `build.gradle.kts`, `.github/workflows/ci.yml`, `.github/workflows/tenant-isolation.yml`, `frontend/src/pages/Assistant.test.tsx`, `billing-service/src/test/java/com/hotelpms/billing/architecture/TenantIsolationArchTest.java`.
10. **Confianza:** **ALTO** sobre Camra; **MEDIO** sobre cobertura Fixi.

### 45. Onboarding público y provisioning SaaS
1. **Capacidad Fixi:** signup público, selección de planes, provisión de tenant/sucursal/admin y capabilities por plan.
2. **Implementación actual en Fixi:** `public.ts`, RPC de onboarding, slug, primer admin/sucursal y plan_capabilities según especificación.
3. **Pieza Camra relacionada:** `HotelOnboardingService` crea `HotelRegistry` y usuario OWNER en transacción; no es por sí mismo funnel web/planes/subscription provisioning.
4. **Qué se puede reutilizar:** creación transaccional de registro tenant + owner, validación de slug/email duplicados y cambio de contraseña inicial.
5. **Qué tendría que cambiar:** signup público, términos, planes, pago/suscripción, sucursal, capabilities/módulos, branding y generación de slug Fixi.
6. **Dependencias hoteleras a eliminar:** registro/slug y copy de hotel; configurar negocio Fixi y primera sucursal en lugar de hotel/owner.
7. **Riesgos:** provisionar tenant sin consistencia entre suscripción, plan/capabilities y recursos deja cuentas fuera de plan o sin owner/sucursal.
8. **Resultado:** **ADAPTABLE** para el paso de creación base; el funnel/planes Fixi **NO EXISTE** | Complejidad **Media-alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/public.ts`, `20260514150000_add_tenant_onboarding.sql`, `20260713134254_normalize_tenant_slug_generation.sql`; Camra `auth-service/.../service/HotelOnboardingService.java`, `auth-service/.../controller/PlatformHotelController.java`.
10. **Confianza:** **ALTO**.

### 46. Movivendor/terceros de venta
1. **Capacidad Fixi:** integración opt-in por tenant para recargas/servicios externos desde POS.
2. **Implementación actual en Fixi:** ruta `movivendor.ts`, módulo/capability habilitable y consulta/solicitud de transacción/saldo.
3. **Pieza Camra relacionada:** clientes HTTP externos y Evolution/Alloggiati; no se encontró adapter de Movivendor ni integración POS equivalente.
4. **Qué se puede reutilizar:** timeouts, circuit breakers, manejo de secretos, cliente HTTP, registro de integración y métricas.
5. **Qué tendría que cambiar:** protocolo/autenticación del proveedor, catálogo de productos externos, transacción/compensación y relación con POS/caja Fixi.
6. **Dependencias hoteleras a eliminar:** ninguna integración hotelera específica debe quedar en el adapter Movivendor.
7. **Riesgos:** saldo/transacciones de proveedor externo pueden quedar inconsistentes con POS si no hay idempotencia y conciliación.
8. **Resultado:** **NO EXISTE** integración Movivendor; infraestructura de conexión **REUTILIZABLE** | Complejidad **Alta**.
9. **Archivos relevantes:** Fixi `apps/api/src/routes/movivendor.ts`, `20260617100000_add_movivendor_module.sql`; Camra `frontdesk-service/.../whatsapp/EvolutionService.java`, `frontdesk-service/.../client/` (solo patrones HTTP).
10. **Confianza:** **ALTO**.

## Infraestructura reutilizable independientemente del dominio

Esta sección clasifica bloques transversales. “Reutilizable” significa que su responsabilidad técnica no depende de hotelería; no implica compatibilidad directa con el proveedor/stack actual de Fixi ni que esté listo sin configuración.

| Pieza Camra | Evidencia/estado observado | Uso potencial para Fixi | Dependencia hotelera o adaptación | Clasificación |
|---|---|---|---|---|
| JWT access/refresh | `JwtService`, refresh tokens, token version, `jti` y expiración en `auth-service` | Autenticación de APIs, expiración/rotación y invalidación | Cambiar claim `hotelId`, cookie/session, login y compatibilidad con identidad Fixi/Supabase | ADAPTABLE |
| Aislamiento tenant | Contexto por `hotelId`, queries tenant-scoped y pruebas ArchUnit | Patrón para garantizar scoping tenant y pruebas de repositorio | Generalizar identidad a `tenantId`, sumar scope de sucursal y validar RLS equivalente; no afirmar que reemplaza Supabase RLS | ADAPTABLE |
| Seguridad service-to-service | HMAC-SHA256, timestamp/nonce, Redis anti-replay en `internal-auth-lib` | Autenticar llamadas internas y evitar spoofing/replay | Externalizar hotel claim; revisar claves, red, rotación y confianza entre servicios | REUTILIZABLE |
| API Gateway | Spring Cloud Gateway, JWT, RBAC, CSRF/security filters, rutas y headers de identidad | Entrada unificada, seguridad perimetral, normalización de errores/rutas | Quitar rutas hoteleras, acomodar aplicaciones/identidad Fixi y no duplicar policies RLS | REUTILIZABLE |
| Rate limiting | Redis Gateway con resolvers pre-auth por IP y autenticado por user | Proteger APIs públicas y sesiones autenticadas | Definir buckets por tenant/usuario/IP y endpoints/planes Fixi | REUTILIZABLE |
| Auditoría de negocio | No se halló ledger inmutable de mutaciones equivalente; hay logs/correlation/timestamps | Infra de logs puede apoyar diagnósticos, no el requisito de auditoría Fixi | Implementar eventos inmutables con actor/acción/request id y proteger PII | NO EXISTE como capacidad de auditoría |
| Observabilidad | Actuator, Micrometer, Prometheus, Grafana, Loki, Zipkin, Alertmanager | Métricas, logs, trazas, dashboards y alertas | Etiquetas Fixi, redacción PII, SLO, alertas y evitar cardinalidad excesiva por tenant | REUTILIZABLE |
| Resiliencia | Resilience4j circuit breaker/retry/rate limiter y fallbacks Feign | Aislar indisponibilidad de proveedores/microservicios | Configurar por transacción y combinar con claves idempotentes Fixi/cola durable | REUTILIZABLE |
| Configuración central | Spring Cloud Config y `HotelSettings` | Configuración externa, defaults y secretos por ambiente | Config Server es genérico; `HotelSettings` es hotelero y no debe copiarse literal | ADAPTABLE |
| Notificaciones | `notification-service`: SMTP, Thymeleaf, adjuntos y clientes resilientes | Email transaccional y PDF adjunto | Reemplazar templates/eventos de hotel y sumar WhatsApp/WebPush/cola según alcance | ADAPTABLE |
| Proveedores LLM | Ollama local y DeepSeek en `AssistantService`; API keys cifradas por hotel | Conector/protocolo LLM, timeout, métricas de tokens y manejo de errores | Quitar prompt/config hotelera, revisar región/PII, modelos, tenant config y secretos | ADAPTABLE |
| Tool calling | Allow-list y herramientas restringidas por rol para operaciones hoteleras | Patrón de tools tipadas, lectura vs mutación y rondas controladas | Reescribir tools, DTO, validación y autorización de cada operación Fixi | ADAPTABLE |
| Confirmación humana | UI revisa parámetros y exige clic para ejecutar mutación IA | Patrón UI de aprobación/rechazo y doble ejecución protegida | Acciones Fixi; mantener separado del consentimiento contractual del cliente | REUTILIZABLE como patrón |
| Evolution API | Adaptador HTTP, QR/status y nombre de instancia derivado de hotelId | Conectar cuentas WhatsApp e identificar instancias | Global API key/base URL, prefijo `pms-` y ruta `/stays`; falta flujo verificado de envío/webhook | ADAPTABLE |
| Docker/infra | Compose con healthchecks, límites, redes separadas, Postgres/Redis, backup pgBackRest | Base de empaquetado y despliegue reproducible | Reemplazar servicios/secretos/volúmenes/DB y validar segregación Fixi | REUTILIZABLE como patrón |
| CI/CD | GitHub Actions Gradle Java 21, Node 24, quality checks, Trivy, tenant E2E | Modelo de gates, artefactos, análisis de imágenes y stack integration tests | Cambiar comandos/builds, framework web y pruebas de Supabase/Fixi | REUTILIZABLE como plantilla |
| Testing | JUnit/Mockito, ArchUnit, Vitest, Playwright, JaCoCo, tests de aislamiento | Herramientas/patrones para API, frontend, tenant y stack integrado | Tests de hotel no validan órdenes, reservas de stock, caja ni RLS Fixi | REUTILIZABLE como tooling |

## Criterios de riesgo relevantes

- **No son equivalencias:** `Guest != Customer`; `Room != Device`; `Reservation != Repair Order`; `Stay/check-in != recepción de dispositivo`; `RestaurantOrder != POS de refacciones`; `Payment != Cash register`; `Invoice != Finance ledger`.
- Camra es un PMS hotelero multitenant con `hotelId`; Fixi es un SaaS de reparación con varias sucursales por tenant. La separación tenant en ambos no demuestra misma jerarquía ni mismas garantías de aislamiento.
- Camra contiene datos/reglas de hotel y cumplimiento de Italia (Alloggiati, CAP/Comune/Provincia, FatturaPA heredada) además de campos mexicanos de CFDI; esas obligaciones requieren análisis separado, no migración automática.
- Donde se clasifica una base técnica como reutilizable, se identifican por separado los cambios para desacoplarla del dominio. No se propone aquí una arquitectura final ni un plan de migración.

## Matriz final

Complejidad de adaptación para obtener la capacidad Fixi partiendo de Camra.

| Capacidad Fixi | Camra relacionado | Clasificación | Complejidad de adaptación | Confianza |
|---|---|---|---|---|
| Órdenes de reparación | `Stay`, `Reservation` (ciclos hoteleros distintos) | NO EXISTE | Muy alta | ALTO |
| Solicitudes de servicio | `Quotation`, `Reservation` (no son leads) | NO EXISTE | Alta | ALTO |
| Clientes | `Guest` | ADAPTABLE | Media-alta | ALTO |
| Dispositivos | `Room`, `RoomType` | NO CONVIENE REUTILIZAR | Alta | ALTO |
| Recepción de dispositivos | `StayService.checkIn` | NO CONVIENE REUTILIZAR | Alta | ALTO |
| Técnicos | `UserAccount`, roles hoteleros | ADAPTABLE | Media | ALTO |
| Work logs | Timestamps de entidades, sin temporizador | NO EXISTE | Alta | ALTO |
| Comisiones | Reporte de facturas, sin reglas de comisión | NO EXISTE | Alta | ALTO |
| Inventario por sucursal | Room/F&B, sin stock de refacciones | NO EXISTE | Muy alta | ALTO |
| Reservas de inventario | `Reservation` hotelera | NO CONVIENE REUTILIZAR | Alta | ALTO |
| Movimientos | Estados hoteleros/cargos, sin ledger de stock | NO EXISTE | Alta | ALTO |
| Productos/refacciones | `MenuItem`, `OrderItem` de restaurante | NO CONVIENE REUTILIZAR | Alta | ALTO |
| Proveedores | Sin módulo de procurement | NO EXISTE | Media-alta | ALTO |
| Compras | `RestaurantOrder`/facturas no son PO | NO EXISTE | Alta | ALTO |
| POS | Pedidos F&B y cargos a estancia | NO CONVIENE REUTILIZAR | Alta | ALTO |
| Caja | Pagos de invoice, sin turnos/caja | NO EXISTE | Alta | ALTO |
| Pagos | `Payment` ligado a invoice | ADAPTABLE | Media-alta | ALTO |
| Finanzas | Reporte de ingresos por invoice | NO EXISTE | Alta | ALTO |
| Sucursales | `hotelId` como hotel/tenant | ADAPTABLE | Alta | ALTO |
| Tareas | Scheduler/reintentos, sin tareas de empleados | NO EXISTE | Media-alta | ALTO |
| Portal cliente | Booking público, rol GUEST sin API portal | NO CONVIENE REUTILIZAR | Alta | ALTO |
| Autorizaciones de presupuesto | Confirmación de acción IA por empleado | NO EXISTE | Alta | ALTO |
| Garantías | Sin entidad/ciclo de garantía | NO EXISTE | Alta | ALTO |
| Documentos/evidencias | Identidad guest y PDFs fiscales | ADAPTABLE parcial | Alta | ALTO |
| Notificaciones | SMTP/Thymeleaf para eventos hoteleros | ADAPTABLE | Media-alta | ALTO |
| Automatizaciones/colas | Jobs programados y resiliencia, sin cola genérica | NO EXISTE | Alta | ALTO |
| Reportes | Reporte owner de facturas | ADAPTABLE | Alta | ALTO |
| Usuarios | `UserAccount` tenant-scoped | ADAPTABLE | Media-alta | ALTO |
| Roles/permisos | RBAC de hotel | ADAPTABLE | Media-alta | ALTO |
| Multi-tenancy | Aislamiento por `hotelId` | ADAPTABLE | Alta | ALTO |
| Autenticación | JWT/refresh vs identidad Supabase descrita | ADAPTABLE | Alta | MEDIO |
| Auditoría | Logs y timestamps, sin audit ledger inmutable | NO EXISTE | Alta | ALTO |
| Seguridad | Gateway/JWT/HMAC/RBAC/Redis anti-replay | REUTILIZABLE con políticas nuevas | Media | ALTO |
| Catálogos configurables | `RoomType`, tarifas hoteleras | NO CONVIENE REUTILIZAR | Media-alta | ALTO |
| Configuración por tenant | `HotelSettings` | ADAPTABLE | Media | ALTO |
| WhatsApp | Pairing/status; sin mensajería Fixi completa | ADAPTABLE | Alta | MEDIO |
| Evolution API | Adaptador/provider HTTP y QR | REUTILIZABLE como adaptador, requiere desacople | Media-alta | ALTO |
| IA | Asistente hotelero con Ollama/DeepSeek | ADAPTABLE | Media-alta | MEDIO |
| Tool calling | Allow-list de tools PMS | ADAPTABLE | Media-alta | ALTO |
| Confirmaciones humanas | UI confirma mutaciones IA | REUTILIZABLE como patrón | Media | ALTO |
| Resiliencia | Resilience4j y fallbacks | REUTILIZABLE | Media | ALTO |
| Observabilidad | Prometheus/Grafana/Loki/Zipkin | REUTILIZABLE | Media | ALTO |
| Infraestructura de ejecución | Compose, PostgreSQL, Redis, backups | REUTILIZABLE como patrón | Alta | MEDIO |
| Testing | JUnit/ArchUnit/Vitest/Playwright/JaCoCo | REUTILIZABLE como tooling | Media | ALTO |
| Onboarding público/planes | Alta transaccional hotel + owner | ADAPTABLE parcial | Media-alta | ALTO |
| Movivendor/terceros | Sin integración Movivendor; HTTP resiliente | NO EXISTE integración | Alta | ALTO |
| CI/CD | Workflows GitHub Actions Camra | REUTILIZABLE como plantilla | Media | ALTO |
