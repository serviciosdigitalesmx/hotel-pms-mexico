# A.SPEC AI-0110 — Auditoría de observabilidad, healthchecks y alertas operativas

**Modo:** READ_ONLY  
**Riesgo:** LOW  
**Resultado:** Auditoría estática completada. No se iniciaron ni reiniciaron servicios y no se modificaron archivos, Git, secretos, bases de datos, redes, volúmenes ni infraestructura externa.

## 1. Evidencia revisada

- `docker-compose.yml`
- `docker/prometheus/prometheus.yml`
- `docker/prometheus/alert_rules.yml`
- Documentación operativa relevante en `docs/`, principalmente `docs/DEPLOYMENT_GUIDE.md`

La configuración fue tratada únicamente como contrato declarado. No se consideró evidencia de ejecución real.

## 2. Servicios, healthchecks y alertas

| Servicio | Healthcheck declarado | Señal Prometheus | Alertas aplicables |
|---|---|---|---|
| `postgres` | `pg_isready -U postgres` | No scrape Prometheus declarado | `BackupCycleFailed` se documenta como mecanismo directo externo a Prometheus; no se verificó runtime |
| `redis` | `redis-cli ping` | No scrape Prometheus declarado | Ninguna específica |
| `loki` | `GET /ready` | No scrape Prometheus declarado | Ninguna específica |
| `grafana` | No healthcheck declarado | No scrape Prometheus declarado | Ninguna específica |
| `zipkin` | `GET /health` | No scrape Prometheus declarado | Ninguna específica |
| `alertmanager` | No healthcheck declarado | No scrape Prometheus declarado | Recibe alertas de Prometheus según configuración declarada |
| `prometheus` | No healthcheck declarado | Es el recolector | Evalúa `alert_rules.yml` |
| `config-server` | `GET :8090/actuator/health/liveness` | `/actuator/prometheus` | `ServiceDown`, `HighErrorRate`, `HighLatencyP99`, `JvmHeapHigh`, `CircuitBreakerOpen`, alertas AI y pool DB si las métricas existen |
| `api-gateway` | `GET :8090/actuator/health/liveness` | `/actuator/prometheus` | Mismas reglas según métricas disponibles |
| `auth-service` | `GET :8090/actuator/health/liveness` | `/actuator/prometheus` | Mismas reglas según métricas disponibles |
| `guest-service` | `GET :8090/actuator/health/liveness` | `/actuator/prometheus` | Mismas reglas según métricas disponibles |
| `frontdesk-service` | `GET :8090/actuator/health/liveness` | `/actuator/prometheus` | Mismas reglas; incluye potencialmente AI y circuit breakers |
| `billing-service` | `GET :8090/actuator/health/liveness` | `/actuator/prometheus` | Mismas reglas según métricas disponibles |
| `fb-service` | `GET :8090/actuator/health/liveness` | `/actuator/prometheus` | Mismas reglas según métricas disponibles |
| `notification-service` | `GET :8090/actuator/health/liveness` | `/actuator/prometheus` | Mismas reglas según métricas disponibles |
| `frontend` | `GET http://127.0.0.1:8080/` | No scrape Prometheus declarado | Ninguna específica |

## 3. Prometheus

Configuración declarada:

- `scrape_interval`: 15 segundos.
- `evaluation_interval`: 15 segundos.
- Alertmanager declarado como `alertmanager:9093`.
- Ocho jobs configurados:
  - `config-server`
  - `api-gateway`
  - `auth-service`
  - `guest-service`
  - `frontdesk-service`
  - `billing-service`
  - `fb-service`
  - `notification-service`
- Todos utilizan `/actuator/prometheus`.
- Prometheus y Alertmanager están bajo el perfil Compose `observability`.

No hay scrape configurado para:

- PostgreSQL
- Redis
- Loki
- Grafana
- Zipkin
- Prometheus
- Alertmanager
- Frontend

## 4. Reglas declaradas

En `docker/prometheus/alert_rules.yml` aparecen nueve reglas:

| Regla | Severidad | Condición declarada |
|---|---:|---|
| `ServiceDown` | critical | `up == 0` durante 1 minuto |
| `HighErrorRate` | warning | Más de 5% de respuestas HTTP 5xx durante 2 minutos |
| `HighLatencyP99` | warning | P99 HTTP superior a 2 segundos durante 5 minutos |
| `JvmHeapHigh` | warning | Heap JVM superior a 85% durante 5 minutos |
| `CircuitBreakerOpen` | critical | Circuit breaker en estado `open` durante 30 segundos |
| `AiProviderHighErrorRate` | warning | Más de 20% de fallos del proveedor AI durante 5 minutos |
| `AiProviderFallbackSpike` | warning | Fallback AI sostenido por encima de 0.1/s durante 5 minutos |
| `AiProviderRateLimiterExhausted` | warning | Permisos disponibles del rate limiter AI iguales a cero durante 1 minuto |
| `DbConnectionPoolNearExhaustion` | warning | Pool HikariCP por encima de 90% durante 2 minutos |

Dependencias métricas explícitas:

- `HighLatencyP99` requiere histogramas de distribución HTTP habilitados.
- Las alertas AI requieren métricas Resilience4j con nombres y etiquetas concretas.
- `DbConnectionPoolNearExhaustion` requiere métricas HikariCP.
- Todas dependen de que el servicio correspondiente sea scrapeable y exponga las métricas esperadas.

## 5. Healthchecks Compose y dependencias

La configuración declara:

- Healthchecks de liveness para los servicios Spring mediante el puerto interno `8090`.
- `api-gateway` depende de PostgreSQL, Redis, Config Server y los servicios de dominio con `condition: service_healthy`.
- `frontend` depende de `api-gateway` saludable.
- Prometheus depende de Config Server saludable y de Alertmanager iniciado.
- Grafana depende de Loki saludable.
- Los componentes de observabilidad son opt-in mediante el perfil `observability`.

Esto expresa orden de arranque y criterios locales de salud, pero no confirma que los endpoints respondan actualmente.

## 6. Destinos y routing de alertas

La documentación operativa indica:

- Prometheus envía alertas a `alertmanager:9093`.
- Alertmanager tiene un receptor por defecto `null`.
- Las alertas pueden ser visibles en la UI local de Alertmanager.
- No existe evidencia estática de notificación activa por email, Slack o PagerDuty.
- La documentación marca la configuración de un receptor real como pendiente antes del go-live.

Por tanto, existe pipeline declarado de evaluación y entrega a Alertmanager, pero el destino operativo final está deshabilitado por defecto.

## 7. Servicios sin señal observable

Los siguientes servicios tienen healthcheck local, pero no tienen scrape Prometheus ni alerta específica declarada:

- PostgreSQL
- Redis
- Loki
- Grafana
- Zipkin
- Alertmanager
- Prometheus
- Frontend

Huecos relevantes:

1. Un frontend caído puede detectarse indirectamente por dependencia de Compose, pero no por alerta Prometheus.
2. Un Alertmanager caído no tiene alerta propia declarada.
3. Prometheus no tiene healthcheck ni alerta self-monitoring declarada.
4. Redis y PostgreSQL solo tienen healthchecks locales; no hay métricas o alertas directas en Prometheus.
5. Loki, Grafana y Zipkin no tienen señal Prometheus declarada.
6. No se observa una regla explícita para pérdida de scraping de todos los componentes de observabilidad.
7. No se observa una alerta específica para fallos SMTP de `notification-service`; solo quedarían cubiertos indirectamente por errores HTTP, circuit breakers o disponibilidad.

## 8. Inconsistencias documentales

`docs/DEPLOYMENT_GUIDE.md` afirma que hay seis alertas Prometheus configuradas, enumerando:

- `ServiceDown`
- `HighErrorRate`
- `HighLatencyP99`
- `JvmHeapHigh`
- `CircuitBreakerOpen`
- `DbConnectionPoolNearExhaustion`

El archivo actual `docker/prometheus/alert_rules.yml` contiene además tres reglas AI:

- `AiProviderHighErrorRate`
- `AiProviderFallbackSpike`
- `AiProviderRateLimiterExhausted`

La documentación no refleja el total actual de reglas.

## 9. Evidencia estática frente a smoke runtime

### Evidencia estática disponible

- Existen healthchecks Compose declarados.
- Existen ocho jobs Prometheus.
- Existe un archivo de reglas de alertas.
- Prometheus declara Alertmanager como destino.
- El perfil `observability` incluye Prometheus, Alertmanager, Grafana, Loki y Zipkin.
- La documentación describe un receptor Alertmanager `null` y deja el receptor real como pendiente.

### Smoke runtime pendiente

No se ejecutaron:

- `docker compose up`
- `docker compose ps`
- Consultas a `/actuator/health/liveness`
- Consultas a `/actuator/prometheus`
- Consultas a Prometheus `/targets`
- Consultas a Prometheus `/api/v1/rules`
- Consultas a Alertmanager
- Pruebas de notificación
- Verificación de métricas reales o firing de alertas

Por ello no se puede afirmar que los servicios estén saludables, que los targets estén `UP`, que las reglas carguen correctamente o que las notificaciones funcionen.

## 10. Hallazgos

### H-01 — Receptores operativos deshabilitados

**Severidad operativa:** Alta  
Alertmanager usa `null` como receptor por defecto. Las alertas pueden visualizarse localmente, pero no generan notificación activa.

### H-02 — Cobertura incompleta de la propia plataforma de observabilidad

**Severidad operativa:** Media  
Prometheus, Alertmanager, Grafana, Loki y Zipkin carecen de healthchecks o alertas Prometheus explícitas.

### H-03 — Frontend sin señal Prometheus

**Severidad operativa:** Media  
El frontend tiene healthcheck HTTP de Compose, pero no existe alerta Prometheus directa para su disponibilidad.

### H-04 — Dependencia de métricas no verificada

**Severidad operativa:** Media  
Varias reglas requieren nombres, etiquetas e histogramas concretos. La presencia de la regla no demuestra que la métrica exista en runtime.

### H-05 — Documentación desactualizada respecto a las reglas

**Severidad operativa:** Baja  
La guía menciona seis reglas, mientras el archivo actual contiene nueve.

### H-06 — Ausencia de alerta específica para dependencias críticas no HTTP

**Severidad operativa:** Media  
PostgreSQL, Redis y SMTP tienen cobertura parcial o indirecta, sin reglas Prometheus específicas visibles en el alcance revisado.

## 11. Conclusión

La configuración contiene una base operativa razonable para los ocho servicios Spring: healthchecks internos, scraping de Actuator, reglas de disponibilidad, errores, latencia, JVM, circuit breakers y pool de conexiones.

Sin embargo, la cobertura no es completa para la plataforma ni para sus dependencias. El hueco más importante es que Alertmanager está configurado para no enviar notificaciones externas. Además, la configuración estática no prueba que los endpoints, targets, métricas o reglas estén funcionando en runtime.

**Estado final:** auditoría estática completada; smoke runtime y validación de alertas reales permanecen pendientes por las restricciones READ_ONLY y OUT OF SCOPE.
