# AE03 — Mapa de dependencias de APIs hoteleras

## Resultado de la auditoría

Los endpoints hoteleros no son un bloque aislado: varios son consumidos por
cotizaciones, facturación, notificaciones, roles operativos y contratos
internos. La retirada masiva produciría regresiones.

| Grupo | Rutas principales | Consumidores detectados | Decisión |
|---|---|---|---|
| Reservas | `/api/v1/reservations/**` | frontend Reservations, Calendar, Quotations, BillingClient, notifications | conservar hasta migrar consumidores |
| Estancias | `/api/v1/stays/**` | frontend Stays, check-in, assistant, WhatsApp, BillingClient | conservar; contiene integraciones activas |
| Habitaciones | `/api/v1/rooms/**`, `/api/v1/room-types/**` | Dashboard, Reservations, Quotations, Rates, Housekeeping | conservar hasta sustitución del shell |
| Pricing hotelero | `/api/v1/rate-calendar/**`, `/api/v1/room-types/*/rate-seasons/**` | RateCalendar y QuotationForm | conservar hasta migración de pricing |
| Configuración hotel | `/api/v1/stays/settings` | HotelProfile y configuración | conservar durante compatibilidad |
| FIXI | `/api/v1/service-orders/**`, `/api/v1/service-requests/**`, `/api/v1/workflows/**` | frontend FIXI y clientes operativos | canónico; fuera de la retirada |
| Inventario FIXI | `/api/v1/inventory/**`, `/api/v1/procurement/**` | frontend/servicios de inventario | canónico; fuera de la retirada |
| Plataforma | `/api/v1/auth/platform/hotels`, `/internal/public-hotels/**` | administración de tenants y resolución pública | conservar; no es limpieza hotelera de tenant |

## Integraciones que impiden borrar rutas

- `BillingClient` consulta facturas por reserva/estancia y publica cargos de
  estancia.
- `NotificationClient` conserva eventos de reserva, check-in y checkout.
- `WhatsAppController` y `AssistantController` siguen bajo el contrato de
  estancias.
- Los contratos de cotizaciones convierten cotizaciones a reservas y consultan
  habitaciones para precios/disponibilidad.

## Decisión AE03

No se elimina ningún endpoint en esta pasada. El trabajo queda parcialmente
implementado como inventario de consumidores; cualquier eliminación requiere
primero migrar los clientes anteriores a contratos FIXI equivalentes y
verificar tenant/branch isolation.

### Terminación y factibilidad

El criterio exige cero consumidores activos fuera del contrato FIXI cuando
exista un contrato FIXI equivalente. La verificación identificó el frontend de
reservas/estancias, `BillingClient`, notificaciones, asistente, WhatsApp y
cotizaciones. No existe en este repositorio un contrato FIXI de alojamiento
que cubra disponibilidad, precios, reservas, check-in/out y configuración;
`service-orders` no es un reemplazo semántico.

El reemplazo faltante es un bounded context de alojamiento con contratos de
disponibilidad, precio versionado, reservas, ciclo de estancia y configuración
tenant/branch. No puede implementarse dentro de este objetivo sin inventar un
producto y migrar datos operativos. Las rutas FIXI que sí tienen contrato ya
son canónicas y están expuestas por gateway; las rutas hoteleras restantes se
conservan deliberadamente y AE03 queda terminado por retención justificada.
