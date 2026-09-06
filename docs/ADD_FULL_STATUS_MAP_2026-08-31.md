# Mapa completo ADD — backend, frontend y runtime

Fecha de corte: 2026-08-31.

## Adenda verificada 2026-09-04 — solo Config Native

`config-service` completó su gate individual JVM/tests/AOT/bootJar → Native -Ob
→ Native -O2 en [run 33830247446](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830247446),
SHA `a44f8fee350231be67e3218c53dd5bb00995a939`, rama
`codex/config-native-direct` / PR #20. La comprobación incluye contenido real de
39 perfiles por runtime, autenticación/rechazos, probes/Prometheus, secretos
sin resolver y 180 segundos bajo carga sin errores, reinicios ni OOM.

Native/JVM: arranque **616/3765 ms**, RAM idle **60.27/217.3 MiB**, máximo
muestreado bajo carga **98.85/283 MiB** e imágenes **180867639/262590263 bytes**.
El artefacto pareado **9921959128** conserva ambas imágenes con `:validated`,
`:ci` y el SHA completo; evidencia O2 **9921959629**. El
[reporte final](CONFIG_NATIVE_FINAL_EVIDENCE.md) registra etiquetas exactas,
image IDs, checksums diferenciados del digest ZIP y el procedimiento de carga.

Estado: Config validado individualmente, sin merge. El manifest, arranque de
clientes Native y E2E/RAM del stack integrado corresponden a la tarea principal.
Esta adenda no actualiza ni recertifica los estados históricos que siguen.

## Estado de integración

| Área | Evidencia | Estado |
|---|---|---|
| Rama local vs `origin/main` | Ambos apuntaban a `0673a89` al iniciar este mapa | Verificado |
| Código funcional consolidado | `origin/main` contiene la consolidación y la corrección Redis | Verificado |
| ADD/Ralph local | 283 especificaciones y 506 reportes bajo `.ralph-add` | Verificado |
| Backups/snapshots/supervisor | Fuera del índice; no son módulos ejecutables | Verificado |

## Inventario técnico

- Backend: 7 directorios `*-service`, 467 archivos Java en `src/main/java`.
- Frontend: 253 archivos bajo `frontend/src`.
- Módulos: gateway, auth, config, frontdesk, guest, billing, fb y
  notification, además de librerías compartidas y motor PDF.

## ADD/Ralph clasificado

### Integrado o existente

- Expediente de estancia y perfil mexicano de huésped.
- Aislamiento de Alloggiati/FatturaPA.
- Asistente IA de frontdesk, sesión tenant/operador y fallback determinista.
- Lint frontend, Checkstyle de auth y binding Redis para Config Server/auth/gateway.

### PASS histórico, no certificación actual

`AI-0000`, `AI-0002`, `AI-0004`, `AI-0006`, `AI-0008`, `AI-0033`, `AI-0052`,
`AI-0134`, `AI-0148`, `AI-0151` y `AI-0155` registran PASS de tareas concretas.
Deben repetirse para certificar el estado actual.

### Pendiente o fallido

- `AI-0009`, `AI-0010`, `AI-0014`: fallos de `LocalIntentRouterTest`.
- `AI-0071`: 7 archivos de test y 37 tests frontend fallidos.
- `AI-0132`: suite frontend bloqueada por supervisor.
- `FRONTEND-0007`: marcado `NOT_APPLIED` aunque su prueba aislada pasó.
- `AI-0156`–`AI-0159`: requieren revisión del resultado efectivo.

## Runtime observado

- i18next/Locize: mensaje informativo.
- CSP de Google Translate: bloquea un recurso externo; no explica el RBAC.
- Frontend carga, pero `settings`, `reservations`, `rooms`, `availability` y
  `stays` devuelven `403`.
- `/api/v1/auth/me` sin sesión puede devolver `401`.
- Se observó `502` cuando el gateway estaba `created`; auth posteriormente llegó
  a `healthy`.
- Redis registró `NOAUTH`; Redis acepta la contraseña del `.env` y se añadió
  `SPRING_DATA_REDIS_PASSWORD` directo en auth y gateway.
- PostgreSQL y Redis estuvieron `healthy`.
- Flyway reportó esquema `hotel_frontdesk` en versión 23 frente a migraciones
  disponibles hasta 14; informó que no había migración pendiente, pero la
  divergencia requiere reconciliación formal.

## Qué sigue tronando

1. Probar login autenticado real y confirmar respuesta de auth sin 502.
2. Comparar claims no sensibles del JWT contra roles y tenant activo.
3. Correlacionar los 403 con las reglas de settings/reservations/rooms/
   availability/stays; no relajar RBAC.
4. Repetir tests backend de frontdesk, auth y guest indicados por ADD.
5. Ejecutar lint, TypeScript/Vite y Vitest separando runner de producto.
6. Reconciliar Flyway y validar E2E tenant A→B.

## Conclusión

El código consolidado está en `origin/main`. No falta meter ADD
indiscriminadamente: falta cerrar la verificación actual de auth/RBAC/tenant,
repetir las suites fallidas y reconciliar Flyway. Supervisor, backups y
snapshots no deben entrar en la imagen ni en la rama principal.
