# AE02 — Mapa de dependencias de rutas frontend

## Alcance

Inventario de las rutas todavía presentes en `frontend/src/App.tsx` que
pertenecen al flujo hotelero CAMRA o que lo consumen indirectamente. El
objetivo es separar una retirada segura de una eliminación que rompería la
navegación, los contratos HTTP o las pruebas existentes.

| Ruta o grupo | Consumidores directos | Servicios/contratos | Estado para retirada |
|---|---|---|---|
| `/service-orders` | navegación FIXI, `ServiceOrders` y detalle | `/api/v1/service-orders` | Canónica FIXI; conservar |
| `/guests` | navegación y formularios de cliente | guests/customer APIs | Compartida; conservar |
| `/quotations` | cotizaciones y detalle | quotations + rooms + reservations | Compartida; migración pendiente |
| `/billing` | cobros y facturación | billing/payment APIs | Compartida; conservar |
| `/assistant` | asistente operativo | assistant APIs | Compartida; conservar |
| `/reservations`, `/reservations/*` | Dashboard, cotizaciones, `Reservations`, check-in | reservations + rooms | CAMRA; no retirar todavía |
| `/stays`, `/stays/*` | Dashboard, reservations, `Stays`, check-in/walk-in | stays + rooms | CAMRA; no retirar todavía |
| `/rooms` | Dashboard, reservations, quotations, housekeeping | `/api/v1/rooms` | CAMRA; no retirar todavía |
| `/housekeeping` | Dashboard y rol `HOUSEKEEPER` | `/api/v1/rooms` | CAMRA; no retirar todavía |
| `/restaurant` | rol `KITCHEN`, billing y operación | restaurant/F&B APIs | CAMRA; requiere sustituto operativo |
| `/calendar`, `/rates` | planificación y pricing | reservations + rooms + rate APIs | CAMRA; no retirar todavía |
| `/profile/hotel` | Settings y pruebas de perfil | hotel settings API | CAMRA; no retirar todavía |
| `/platform/hotels` | Settings y administración de tenants | `/api/v1/auth/platform/hotels` | Administración de plataforma; fuera de la retirada hotelera |

## Dependencias que bloquean una eliminación inmediata

- `Dashboard.tsx` enlaza directamente a `/stays`, `/reservations`, `/rooms` y
  `/housekeeping`, y carga estadísticas de habitaciones y estancias.
- `QuotationForm` y `QuotationDetail` todavía necesitan habitaciones y pueden
  navegar a una reserva.
- `Reservations` navega al check-in y a edición de reservas; `Stays` navega a
  reservas y walk-in.
- `OperationalHome` redirige los roles `KITCHEN` y `HOUSEKEEPER` a
  `/restaurant` y `/housekeeping`.
- Las páginas retirables tienen suites frontend activas; eliminar sus rutas sin
  reemplazar sus entradas y mocks produciría una regresión funcional y de
  navegación.

## Decisión AE02

No se elimina ninguna ruta en esta pasada. La siguiente costura segura es
reemplazar primero el shell operativo de `Dashboard` por una entrada FIXI que
no dependa de habitaciones/estancias, y después migrar por grupos las rutas de
reservas, estancias y habitaciones. Las rutas de administración de plataforma
se mantienen fuera del alcance de la retirada CAMRA.

## Criterio para la siguiente pasada

Una ruta hotelera solo puede marcarse retirada cuando no tenga referencias en
`App.tsx`, navegación, redirecciones por rol, páginas compartidas, servicios,
pruebas ni contratos backend/migraciones activos.
