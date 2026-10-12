# AE04 — Mapa de dependencias del dominio hotelero

El módulo `frontdesk-service` contiene dominios FIXI y compatibilidad hotelera
en el mismo servicio. La inspección de paquetes muestra que la eliminación por
directorio no es segura.

| Paquete | Elementos | Dependencias activas | Decisión |
|---|---|---|---|
| `reservations` | Reservation, line items, servicios, controllers | quotations, stays, notifications, billing y frontend | conservar |
| `stays` | Stay, guests, settings, check-in/out | reservations, billing, WhatsApp, assistant y frontend | conservar |
| `rooms` | Room, RoomType, servicios y repositorios | reservations, quotations, pricing, stays y frontend | conservar |
| `pricing` | temporadas, calendario y pricing | quotations, rooms y frontend | conservar |
| `quotations` | cotización y conversión | rooms, reservations, guests y notifications | conservar/migrar gradualmente |
| `serviceorder` | órdenes, requests, tiempos, evidencias, garantías | workflows, inventory y frontend FIXI | canónico FIXI |
| `inventory` | productos, stock, compras y reservas | service orders y frontend FIXI | canónico FIXI |
| `workflow` | definiciones, estados y transiciones | service orders | canónico FIXI |

## Decisión AE04

No se borran entidades, servicios ni repositorios hoteleros. La separación
requiere primero extraer contratos compartidos (guest, billing y notificaciones)
y reemplazar las dependencias de cotización/reserva. Hacerlo ahora rompería
la consistencia de facturación, aislamiento de tenant/branch y conversiones de
cotización.

### Terminación y factibilidad

El criterio exige que ningún dominio duplicado permanezca cuando existe un
dominio FIXI canónico. Esa condición ya se cumple para órdenes de servicio,
solicitudes, inventario y workflows. Reservas, estancias, habitaciones,
pricing y cotizaciones son dominios de alojamiento activos, no duplicados
semánticos de `serviceorder`.

El consumidor que impide extraerlos es la cadena
`quotations -> reservations -> stays/rooms`, además de billing, notificaciones
y frontend. Falta un bounded context de alojamiento con contratos compartidos
versionados, snapshots de precio y eventos de ciclo de estancia. Implementarlo
exigiría una decisión de producto y migración de datos fuera de lo inferible
del repositorio; no se elimina código legítimo por nombre histórico.
