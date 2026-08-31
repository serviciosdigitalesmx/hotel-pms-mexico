# A.SPEC AI-0118 — Auditoría de observabilidad, healthchecks y alertas operativas

**Modo:** READ_ONLY  
**Riesgo:** LOW  
**Resultado:** Auditoría estática completada. No se modificaron archivos, Git, secretos, bases de datos, servicios, redes, volúmenes ni infraestructura externa.

## Alcance revisado

- `docker/prometheus/prometheus.yml`
- `docker/prometheus/alert_rules.yml`
- `docker-compose.yml`
- Documentación operativa y auditorías relevantes bajo `docs/`

No se ejecutaron servicios ni pruebas contra infraestructura externa.

## Resumen ejecutivo

El checkout contiene:

- 8 servicios scrapeados por Prometheus.
- 9 reglas Prometheus activas declaradas.
- Healthchecks Docker para 12 de 15 servicios Compose.
- Alertmanager configurado como destino estático de Prometheus.
- Endpoints Actuator de liveness, health y métricas Prometheus declarados.
- Documentación operativa para revisar healthchecks, Actuator y logs.

Persisten estos huecos estáticos:

1. Grafana, Alertmanager y Prometheus no tienen `healthcheck` Compose.
2. `frontend`, PostgreSQL y Redis tienen healthcheck, pero no aparecen como targets de Prometheus.
3. No hay evidencia dentro del alcance de destinos efectivos de notificación de Alertmanager; solo existe el montaje de su archivo de configuración.
4. No hay alertas explícitas para frontend, PostgreSQL, Redis, Alertmanager, Prometheus, Grafana, Loki, Zipkin ni reinicios de contenedores.
5. La documentación presenta contradicciones históricas sobre la existencia de alert rules y un perfil `backup`.
6. La configuración estática no demuestra que los endpoints respondan en runtime.

## Inventario de servicios Compose y healthchecks

| Servicio | Perfil | Healthcheck declarado | Señal estática |
|---|---|---:|---|
| `postgres` | Core | Sí | `pg_isready -U postgres` |
| `redis` | Core | Sí | `redis-cli ping` |
| `loki` | `observability` | Sí | `GET /ready` |
| `grafana` | `observability` | No | Sin healthcheck Compose |
| `zipkin` | `observability` | Sí | `GET /health` |
| `alertmanager` | `observability` | No | Sin healthcheck Compose |
| `prometheus` | `observability` | No | Sin healthcheck Compose |
| `config-server` | Core | Sí | `/actuator/health/liveness` |
| `api-gateway` | Core | Sí | `/actuator/health/liveness` |
| `auth-service` | Core | Sí | `/actuator/health/liveness` |
| `guest-service` | Core | Sí | `/actuator/health/liveness` |
| `frontdesk-service` | Core | Sí | `/actuator/health/liveness` |
| `billing-service` | Core | Sí | `/actuator/health/liveness` |
| `fb-service` | Core | Sí | `/actuator/health/liveness` |
| `notification-service` | Core | Sí | `/actuator/health/liveness` |
| `frontend` | Core | Sí | `GET /` vía Nginx |

Evidencia: [`docker-compose.yml`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docker-compose.yml:71).

Todos los microservicios Spring principales usan el puerto de management interno `8090` para liveness. La documentación indica que esta puerta no está publicada directamente al host y debe consultarse desde dentro de la red Docker.

Evidencia: [`OPERATIONS_RUNBOOK.md`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docs/OPERATIONS_RUNBOOK.md:30).

## Prometheus

Prometheus declara:

- `scrape_interval`: 15 segundos.
- `evaluation_interval`: 15 segundos.
- Reglas desde `/etc/prometheus/alert_rules.yml`.
- Alertmanager en `alertmanager:9093`.

Targets configurados:

| Job | Target | Endpoint |
|---|---|---|
| `config-server` | `config-server:8090` | `/actuator/prometheus` |
| `api-gateway` | `api-gateway:8090` | `/actuator/prometheus` |
| `auth-service` | `auth-service:8090` | `/actuator/prometheus` |
| `guest-service` | `guest-service:8090` | `/actuator/prometheus` |
| `frontdesk-service` | `frontdesk-service:8090` | `/actuator/prometheus` |
| `billing-service` | `billing-service:8090` | `/actuator/prometheus` |
| `fb-service` | `fb-service:8090` | `/actuator/prometheus` |
| `notification-service` | `notification-service:8090` | `/actuator/prometheus` |

Evidencia: [`prometheus.yml`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docker/prometheus/prometheus.yml:1).

## Alertas declaradas

| Regla | Señal | Umbral / duración | Severidad |
|---|---|---|---|
| `ServiceDown` | `up == 0` | 1 minuto | Critical |
| `HighErrorRate` | HTTP 5xx | >5% durante 2 minutos | Warning |
| `HighLatencyP99` | Latencia HTTP P99 | >2 s durante 5 minutos | Warning |
| `JvmHeapHigh` | Heap JVM | >85% durante 5 minutos | Warning |
| `CircuitBreakerOpen` | Circuit breaker abierto | 30 segundos | Critical |
| `AiProviderHighErrorRate` | Fallos del proveedor AI | >20% durante 5 minutos | Warning |
| `AiProviderFallbackSpike` | Fallos AI sostenidos | >0.1/s durante 5 minutos | Warning |
| `AiProviderRateLimiterExhausted` | Permisos disponibles AI | 0 durante 1 minuto | Warning |
| `DbConnectionPoolNearExhaustion` | Pool HikariCP | >90% durante 2 minutos | Warning |

Evidencia: [`alert_rules.yml`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docker/prometheus/alert_rules.yml:1).

## Tabla servicio-healthcheck-alerta

| Servicio | Healthcheck local | Scrape Prometheus | Alertas aplicables |
|---|---:|---:|---|
| `config-server` | Sí | Sí | `ServiceDown`, errores, latencia, JVM |
| `api-gateway` | Sí | Sí | `ServiceDown`, errores, latencia, JVM, circuit breakers, DB pool si expuesto |
| `auth-service` | Sí | Sí | `ServiceDown`, errores, latencia, JVM, DB pool |
| `guest-service` | Sí | Sí | `ServiceDown`, errores, latencia, JVM, DB pool |
| `frontdesk-service` | Sí | Sí | `ServiceDown`, errores, latencia, JVM, DB pool, circuit breakers |
| `billing-service` | Sí | Sí | `ServiceDown`, errores, latencia, JVM, DB pool |
| `fb-service` | Sí | Sí | `ServiceDown`, errores, latencia, JVM, DB pool |
| `notification-service` | Sí | Sí | `ServiceDown`, errores, latencia, JVM, circuit breakers |
| `postgres` | Sí | No | Sin alerta Prometheus directa declarada |
| `redis` | Sí | No | Sin alerta Prometheus directa declarada |
| `frontend` | Sí | No | Sin alerta Prometheus directa declarada |
| `loki` | Sí | No | Sin alerta Prometheus directa declarada |
| `zipkin` | Sí | No | Sin alerta Prometheus directa declarada |
| `grafana` | No | No | Sin healthcheck ni alerta declarada |
| `alertmanager` | No | No | Sin healthcheck; destino configurado pero sin alerta propia |
| `prometheus` | No | No | Sin healthcheck ni alerta propia |

La aplicabilidad de cada regla depende de que las métricas requeridas existan realmente en runtime. La configuración declara los nombres de métricas, pero no prueba que estén siendo exportadas.

## Alertmanager, destinos y routing

Prometheus apunta estáticamente a:

```text
alertmanager:9093
```

Compose monta:

```text
./docker/alertmanager/alertmanager.yml
```

en Alertmanager y expone el puerto `9093`.

Sin embargo, `docker/alertmanager/alertmanager.yml` está fuera de los `BEGIN_ALLOWED_PATHS`, por lo que no fue inspeccionado. En consecuencia:

- El destino lógico Prometheus → Alertmanager está confirmado estáticamente.
- No puede confirmarse el routing interno.
- No puede confirmarse ningún receptor efectivo como email, webhook, Slack u otro.
- No puede confirmarse que las notificaciones sean entregables.

Evidencia: [`prometheus.yml`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docker/prometheus/prometheus.yml:8) y [`docker-compose.yml`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docker-compose.yml:214).

## Documentación operativa encontrada

`docs/OPERATIONS_RUNBOOK.md` documenta:

- Arranque del perfil `observability`.
- Consulta de `docker compose ps`.
- Consulta de `/actuator/health`.
- Verificación de los ocho microservicios.
- Consulta de logs `ERROR|WARN`.
- Uso de Grafana/Loki y LogQL.
- Diagnóstico de fallos de arranque.
- Correlation ID para rastrear solicitudes.

Evidencia: [`OPERATIONS_RUNBOOK.md`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docs/OPERATIONS_RUNBOOK.md:14).

### Inconsistencias documentales

El runbook indica:

```text
docker compose --profile observability --profile backup up -d
```

Pero el encabezado actual de `docker-compose.yml` indica que ya no existe el perfil `backup` y que el backup de PostgreSQL forma parte del servicio permanente. Esto requiere reconciliación documental antes de usar el comando como procedimiento operativo.

Además, `docs/FINAL_AUDIT_ULTRA_SEVERE.md` afirma que no existen alert rules Prometheus, mientras que `docker/prometheus/alert_rules.yml` contiene nueve reglas. Esa afirmación documental parece estar desactualizada respecto a los archivos actuales.

Evidencia:

- [`docker-compose.yml`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docker-compose.yml:12)
- [`OPERATIONS_RUNBOOK.md`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docs/OPERATIONS_RUNBOOK.md:17)
- [`FINAL_AUDIT_ULTRA_SEVERE.md`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0118-lslv7q4h/docs/FINAL_AUDIT_ULTRA_SEVERE.md:248)

## Servicios sin señal observable suficiente

### Sin healthcheck Compose

- `grafana`
- `alertmanager`
- `prometheus`

### Con healthcheck, pero sin scrape Prometheus

- `postgres`
- `redis`
- `frontend`
- `loki`
- `zipkin`

### Sin alerta específica de dependencia

No hay reglas específicas visibles para:

- PostgreSQL caído o no disponible.
- Redis caído o degradado.
- Loki no disponible.
- Zipkin no disponible.
- Grafana no disponible.
- Alertmanager no disponible.
- Prometheus no disponible.
- Reinicios de contenedores.
- Saturación de disco o volúmenes.
- Fallos de entrega de notificaciones.
- Expiración o incumplimiento operativo de Alloggiati.

El `ServiceDown` cubre únicamente targets scrapeados por Prometheus; no cubre automáticamente los servicios que no aparecen en `scrape_configs`.

## Evidencia estática frente a smoke runtime

| Aspecto | Evidencia estática | Verificación runtime |
|---|---|---|
| Healthchecks Docker | Declarados en Compose | Pendiente |
| Actuator liveness | URLs declaradas | Pendiente |
| Métricas Prometheus | Jobs y paths declarados | Pendiente |
| Reglas de alerta | YAML presente | Pendiente de evaluar Prometheus |
| Alertmanager | Host y puerto declarados | Pendiente |
| Receptores finales | No verificables dentro del alcance | Pendiente |
| Grafana/Loki | Servicios y documentación presentes | Pendiente |
| Dependencias DB/Redis | Healthchecks presentes | Pendiente |
| Disponibilidad real | No demostrada | Smoke runtime pendiente |

No se debe interpretar la presencia de una regla, un healthcheck o un archivo de configuración como prueba de que el servicio está saludable o que las alertas están llegando.

## Conclusión

La base estática de observabilidad existe y cubre los ocho microservicios principales mediante Actuator y Prometheus. También existen reglas para disponibilidad, errores HTTP, latencia, JVM, circuit breakers y pool de conexiones.

La cobertura operativa es incompleta para la propia plataforma de observabilidad y para dependencias como PostgreSQL, Redis, frontend, Loki y Zipkin. Tampoco queda demostrado dentro del alcance que Alertmanager tenga receptores efectivos o que las reglas se evalúen correctamente en un entorno vivo.

**Estado de readiness de observabilidad:** parcialmente preparado estáticamente; no validado en runtime.
