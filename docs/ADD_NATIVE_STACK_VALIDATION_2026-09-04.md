# ADD — migración Native e integración controlada del PMS

## Estado y alcance

Trabajo sobre `serviciosdigitalesmx/hotel-pms-mexico`. Las seis migraciones
mantienen ramas y PR independientes; esta rama solo orquesta imágenes ya
construidas y sus controles JVM, sin fusionar implementaciones ni hacer merge
en `main`. Los pilotos Guest y Notification se reutilizan. El empaquetado Linux
amd64 de Notification permite usar el piloto originalmente validado en ARM64.

**Validación integrada cerrada el 2026-09-05:**
[run 33940555256](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33940555256),
commit `e2dc2185`. Los ocho servicios Native y sus controles JVM pasan juntos.
Se verificaron tres recorridos Playwright por modo (cero fallos ni omisiones),
PostgreSQL, igualdad de las cinco historias Flyway, Config autenticado,
health/readiness/liveness, Redis/HMAC/replay, RBAC, tenant, flujos entre servicios,
factura PDF y adjunto SMTP con texto y fuentes, Prometheus, Zipkin y Loki.
Estabilidad: 30 comprobaciones por servicio durante cinco minutos, sin reinicios
ni OOM en ambos modos. Esto no equivale a una prueba prolongada en producción.

| Medición conjunta en runner | Native | JVM |
|---|---:|---:|
| Stack disponible (ms, infraestructura incluida; descarga excluida) | 34508 | 97786 |
| RAM backend en reposo (bytes) | 1065101558 | 2623327437 |
| RAM backend tras uso básico (bytes) | 1164936479 | 2842899251 |
| Suma de picos por contenedor durante E2E (bytes; no pico simultáneo) | 1168994468 | 2856635597 |

Evidencia descargada: `/tmp/native-evidence-33940555256-final`;
artefacto remoto `native-stack-integration-evidence` del run enlazado.
El healthcheck Docker de Config queda verificado por este gate.
El PDF de cotizaciones Frontdesk está validado individualmente en O2 por
[run 33855201348](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33855201348),
SHA `38fd5a2bad5ca33071b362d95fda48a20d5bf68f`: dos renders por modo,
texto esperado y NotoSans Regular/Bold embebidas, Native y JVM PASS.
El manifest se actualizó al artefacto `9930690941` con esa corrección AWT/PDF.
La integración conjunta de este nuevo manifest requiere el resultado de la
corrida disparada por `bf4d5973`; las métricas anteriores pertenecen al manifest
previo. No atribuirlas a las nuevas imágenes hasta cerrar esa corrida.
No se realizó merge en main ni despliegue en la Mac.

## Mapa funcional y evidencia individual O2

| Servicio / PR | Ejecución final | Contratos e integraciones comprobados |
|---|---|---|
| Guest / #17 | [33830026445](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830026445) | PostgreSQL, Flyway 10, paginación, Redis/HMAC/replay, tenant, Feign, control JVM |
| F&B / #21 | [33829870882](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33829870882) | Menú/precios, pedidos, cargo real en Billing, estancia real en Frontdesk, aislamiento, fallbacks, persistencia tras reinicio |
| Billing / #24 | [33830302204](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830302204) | Factura/cargo/pago persistidos, Flyway 12, tenant, HMAC/replay, Feign, IVA y rechazo de sobrepago, texto/fuentes del PDF en Native y JVM |
| Frontdesk / #22 | [33830356702](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830356702) | Habitaciones, reservas, check-in/out, Flyway 20, downstream Guest/Billing, tenant, RBAC, HMAC/replay, carga sostenida |
| Gateway / #18 | [33830442810](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830442810) | WebFlux, login/refresh/me, routing real, JWT/CSRF/RBAC, eliminación de headers falsificados, rate limit Redis, firma y replay |
| Config / #20 | [33830247446](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33830247446) | O2 final, 39 combinaciones de perfiles autenticados, igualdad de contenido JVM/Native, secretos resueltos en runtime, Actuator y carga |
| Auth / #23 | [33844799765](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33844799765) | O2, login/refresh/me, JWT inválido/expirado/falsificado, replay, RBAC/tenant, HMAC, Flyway 8, persistencia y cinco minutos de carga por modo sin 5xx |
| Notification / referencia | [33845871639](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33845871639) | Implementación del piloto conservada; O2 Linux amd64, SMTP real en Mailpit, Redis/HMAC/replay, health/probes/Prometheus y control JVM |

## Mediciones individuales confirmadas

Una ejecución por imagen O2. Son mediciones en runners, no benchmarks de la Mac.
Las ventanas y métodos difieren entre servicios: los artefactos detallan si RAM
cargada es pico durante carga o memoria después del uso básico. No se suman estas
mediciones independientes para inventar un total del stack.

| Servicio | Arranque Native / JVM (ms) | RAM reposo Native / JVM (MiB) | RAM con uso Native / JVM (MiB) | Imagen Native / JVM (bytes) |
|---|---:|---:|---:|---:|
| Guest | 2293 / 12718 | 121.1 / 406.3 | 125.4 / 418.8 | 328504217 / 311212791 |
| F&B | 2303 / 14041 | 120.1 / 461.7 | 117.4 / 511.0 | 324834201 / 312200953 |
| Billing | 4312 / 17142 | 140.2 / 518.1 | 141.0 / 545.2 | 359806578 / 320582598 |
| Frontdesk | 10373 / 25349 | 137.8 / 512.9 | 123.5 / 567.1 | 358126489 / 320946301 |
| Gateway | 2258 / 8820 | 93.7 / 276.4 | 118.3 / 305.7 | 252154777 / 274783412 |
| Config | 616 / 3765 | 60.27 / 217.3 | 98.85 / 283.0 | 180867639 / 262590263 |
| Auth | 4277 / 15026 | 127.77 / 433.65 | 280.91 / 567.24 | 324244377 / 312365701 |
| Notification | 1345 / 8851 | 96.25 / 277.4 | 101.3 / 285.3 | 263164825 / 281539085 |

RAM puede bajar durante la carga por recolección de basura; no implica un error
si la medición de reposo fue anterior al primer ciclo de GC. Algunas imágenes
Native son mayores que JVM: no se promete reducción de tamaño donde no ocurrió.
Auth registra mediana bajo carga en la tabla (picos: 445.84 / 654.75 MiB).
En cinco minutos completó 2271 ciclos Native y 2924 JVM con tres clientes; esta
prueba no demuestra mayor throughput Native. El beneficio medido es de arranque
y memoria, sin afirmar aceleración universal de todas las operaciones.

## Diseño de la integración final

```text
Chromium → frontend existente → Gateway → Auth (JWT/refresh/CSRF)
                                      → Guest ↔ Frontdesk ↔ Billing
                                                    ↑        ↑
                                                    └── F&B ─┘
                                            Frontdesk → Notification → Mailpit
Los ocho servicios ← Config autenticado; servicios de datos → PostgreSQL
Gateway/Auth/servicios internos → Redis (sesión, rate limit y nonces HMAC)
Actuator/Prometheus + exportación Zipkin y Loki
```

- `docs/native-stack-images.json`: fija ID de artefacto, run y SHA de origen;
  exige exactamente ocho pares Native/JVM y un run final exitoso.
- `scripts/ci/load-native-stack-images.sh`: descarga desde este repositorio,
  verifica checksums, arquitectura Linux amd64 y tags explícitos. No compila ni
  sustituye la imagen por una distinta si falta algún artefacto.
- `docker-compose.native-stack-ci.yml`: override opt-in de la composición real,
  conserva segmentación de redes, puertos internos y secretos de runtime.
  Solo publica puertos de prueba en loopback. No toca los volúmenes de la Mac.
- `scripts/ci/verify-native-stack-runtime.sh`: arranca un proyecto aislado nuevo
  para Native y otro para JVM, ejecuta la misma prueba de navegador/APIs,
  comprueba salud de los ocho, HMAC/replay directo y observabilidad. Captura RAM
  antes, durante y después del recorrido, y hace un soak de cinco minutos.
- Los correos de pruebas solo llegan a Mailpit dentro del runner. No se envían
  reservas/correos de prueba a clientes, SMTP externos ni el PMS del usuario.

El cronómetro de disponibilidad incluye inicialización de las bases de datos
vacías y arranque de todos los contenedores; excluye descargas de registros y
compilación. Ambas variantes usan imágenes previamente cargadas, límites iguales
y datos nuevos. El control JVM corre aun si el gate Native falla, para distinguir
regresiones de comportamiento preexistente.

## Hallazgo real resuelto en Billing

Un gate previo aceptaba HTTP 200 y encabezado PDF, pero el archivo estaba en
blanco: PDFBox no podía cargar `Identity-H` en Native. Se añadió exclusivamente
`org/apache/fontbox/cmap/Identity-H` a los recursos de runtime; se conservan los
hints precisos existentes de XMPBox. Ahora tests JVM y ambos gates exigen texto
legible (incluidos acentos), fuentes NotoSans incrustadas y el total esperado.
Los PDFs Native/JVM de evidencia se renderizaron y compararon visualmente.
No se cambió el contrato, el template ni la regla de facturación para pasar.

## Reutilización, reversibilidad y limitaciones

- GraalVM 24, AOT antes de compilación costosa, Java 21 como fallback, caché
  Buildx `gha` y Gradle descargado fuera de la imagen final son patrones comunes.
- Los hints de PDFBox/XMPBox solo son pertinentes a los renderizadores de PDF;
  no se agregan indiscriminadamente a Gateway/WebFlux ni a otros servicios.
- El primer contenedor Config O2 respondía al healthcheck HTTP externo, pero no
  contenía `wget`, requerido por Compose. Su empaquetado debe corregirse antes
  de fijar el artefacto definitivo del stack; una sonda externa verde no cubría
  esta dependencia del contenedor.
- Cada Dockerfile JVM permanece disponible; `NATIVE_STACK_MODE=jvm` selecciona
  los controles JVM en este gate. No se cambió ningún despliegue local.
- El override usa PostgreSQL 15 estándar y datos efímeros. No valida pgBackRest,
  copias remotas ni restauración de backups de producción.
- Las imágenes finales aquí son Linux amd64. Validación en GitHub no equivale a
  comprobar el rendimiento en Apple Silicon ni a desplegar en la Mac.
- Los fallos globales preexistentes de PMD/Checkstyle/Vitest se separan como
  `UNRELATED_GLOBAL_CI_FAIL`; no se desactivan controles de seguridad para ocultar
  un `NATIVE_GATE_FAIL` o `NATIVE_STACK_GATE_FAIL`.
- Auth corrige la numeración duplicada de la semilla del segundo hotel de V7 a
  V8, conservando el SQL ejecutable. La validación usa bases nuevas. Antes de
  desplegar sobre una base existente hay que revisar `flyway_schema_history`,
  scripts y checksums; no se afirma que ese historial tenga impacto cero ni se
  ejecuta `repair` automáticamente.
- Las cotizaciones PDF no están cubiertas por el gate individual de Frontdesk.
- Nada se considera integrado en `origin/main`: los PR permanecen sin merge,
  conforme a la instrucción de revisión independiente.

## Reproducir

En GitHub Actions, ejecutar `All Native PMS stack integration` sobre la rama
`codex/native-stack-validation` una vez completo el manifiesto. Los scripts se
niegan a correr fuera de GitHub Actions para proteger los datos de la Mac.
La evidencia se publica como `native-stack-integration-evidence`, incluidas
trazas de Playwright, PDFs, logs, estados de contenedores, métricas y procedencia
de las imágenes. La caducidad normal de artifacts exige conservarlos o repetir
el empaquetado antes de que expiren; nunca se sustituye silenciosamente un SHA.
