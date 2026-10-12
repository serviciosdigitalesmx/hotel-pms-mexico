# AE06 — Auditoría de terminología

### Terminación y factibilidad

El criterio exige limpiar terminología visible sin romper aislamiento. El
consumidor que impide un reemplazo global es `hotel_id` en entidades, índices,
DTOs, claims y consultas tenant-scoped. El reemplazo faltante es una migración
versionada de columnas/contratos con doble lectura/escritura y una ventana de
compatibilidad; no existe una adaptación
segura que pueda hacerse solo cambiando nombres. Las superficies de usuario y
documentación que admiten equivalencia ya fueron ajustadas, mientras que los
identificadores técnicos legítimos permanecen.

La terminología `hotel` que aparece dentro de FIXI no es automáticamente un
residuo CAMRA: en órdenes, evidencias, garantías, solicitudes y tiempos
representa el tenant propietario (`hotel_id`) y participa en las consultas de
aislamiento.

## Decisión

No se hace un reemplazo global de `hotel` por `tenant`. Cambiar nombres de
columnas, índices, parámetros o rutas alteraría contratos persistidos y podría
debilitar tenant isolation. La limpieza terminológica se limita a superficies
de usuario cuando exista una equivalencia aprobada; los identificadores de
persistencia y API se conservan hasta una migración versionada.
