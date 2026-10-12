# AE05 — Auditoría de migraciones y limpieza de base de datos

La historia Flyway de `frontdesk-service` mezcla el baseline hotelero (`V1` a
`V24`) con workflow/inventario/FIXI (`V25` a `V38`). Las migraciones son
append-only y ya forman parte del historial desplegable.

## Hallazgos

- `V7`–`V16` mantienen columnas y tablas de habitaciones, reservas y estancias
  que siguen siendo referenciadas por código y clientes activos.
- `V25`–`V28` son prerrequisitos de workflows y permisos FIXI.
- `V29`–`V38` crean órdenes, autorización, inventario, garantías, evidencias,
  solicitudes, WhatsApp y tiempos; no deben confundirse con residuos.
- No hay una migración de reemplazo que permita retirar tablas hoteleras sin
  perder datos o romper claves usadas por facturación y notificaciones.

## Decisión AE05

No se modifica ni elimina historial Flyway. La limpieza queda diferida hasta
contar con migración de datos, consumidores migrados y plan de compatibilidad;
eliminar migraciones aplicadas sería inseguro y rompería instalaciones
existentes.

### Terminación y factibilidad

El criterio exige estructuras sin consumidores y una migración compatible,
append-only y operativamente reversible. El consumidor activo es el conjunto
de tablas de alojamiento usado por reservas, estancias, pricing, facturación,
notificaciones y migraciones FIXI posteriores. El reemplazo faltante sería un
backfill versionado que preserve claves, referencias históricas y rollback; no
existe una estructura destino aprobada para ejecutarlo.

Los backfills existentes cubren la parte implementable y Docker confirmó que
Flyway aplica el historial sin reescritura. Retirar V1–V24 o reescribir
checksums rompería instalaciones existentes; conservar el historial
append-only es la terminación correcta bajo el criterio de integridad de datos.
