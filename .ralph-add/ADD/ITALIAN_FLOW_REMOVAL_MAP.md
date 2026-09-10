# ADD: Retiro de flujos italianos

## Objetivo

Eliminar del PMS México los flujos Alloggiati/FatturaPA/SDI y dejar operativos los flujos mexicanos de check-in, huéspedes, recibos e invoices. La migración Native se conserva y cada cambio de backend debe pasar JVM y Native.

## Mapa de dependencias

| Área | Entrada actual | Dependencias italianas | Acción |
|---|---|---|---|
| Check-in | `StayController`, `StayServiceImpl` | `StayAlloggiatiCoordinator`, datos Alloggiati | Eliminar coordinación automática y campos PS |
| Catálogos | `/lookup/stati`, `/lookup/tipdoc`, `/comuni` | entidades/repositorios/cargador Alloggiati | Eliminar endpoints, tablas nuevas quedan históricas |
| Reportes | `/reports/alloggiati*` | reportes, SOAP, credenciales y cifrado | Eliminar endpoints y servicios |
| Billing | `InvoiceController` | `FatturaPAService`, XSD italiano, SDI | Sustituir por contrato CFDI México en fase siguiente |
| Datos huésped | `Guest` y DTOs | `fiscalCode`, `vatNumber`, `sdiCode`, `cap`, `comune`, `provincia` | Conservar solo perfil RFC/CFDI mexicano |
| Configuración | `.env.example`, Config Server, Compose | `ALLOGGIATI_*` | Eliminar secretos, URLs y flags italianos |
| Frontend | billing/stays/settings/i18n | botones, descarga XML FatturaPA, reportes y locale italiano | Eliminar acciones y superficie italiana |
| Native | Dockerfiles, hints, workflows | referencias de clases eliminadas | Actualizar reachability/tests; mantener gates |

## Orden de ejecución

1. Cortar rutas y wiring de runtime.
2. Eliminar clases, DTOs, clientes, migraciones de funcionalidad y configuración italiana.
3. Limpiar contratos de frontend y pruebas.
4. Mantener datos históricos en la base; no borrar columnas ni tablas con información existente en esta pasada.
5. Ejecutar `./gradlew clean build`, build frontend y gates Native disponibles.

## Criterio de cierre

- `rg -i 'Alloggiati|FatturaPA|SDI|sdiCode'` no encuentra código runtime mexicano.
- No hay endpoint italiano publicado.
- JVM, frontend y Native compilan.
- El flujo normal de invoice/receipt sigue disponible.
- CFDI 4.0 queda como integración explícita sobre el contrato fiscal mexicano ya existente.
