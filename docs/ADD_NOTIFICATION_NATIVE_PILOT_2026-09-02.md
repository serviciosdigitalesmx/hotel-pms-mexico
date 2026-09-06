# ADD - Piloto GraalVM Native Image para notification-service

Fecha de cierre: 2026-09-02

## Resultado

`notification-service` funciona como ejecutable GraalVM Native Image real dentro de Docker, en paralelo a la imagen JVM existente. No se migró ningún otro microservicio y no se modificaron contratos HTTP, puertos, esquemas, RBAC, JWT, aislamiento tenant ni reglas de seguridad.

La variante es reversible: el archivo Compose principal continúa describiendo la imagen JVM y `docker-compose.notification-native.yml` sustituye únicamente el build y la imagen de `notification-service` cuando se incluye explícitamente.

## Mapa ADD del piloto

```text
frontend -> api-gateway -> notification-service:8088
                              |
                              +-> InternalAuthFilter / HMAC-SHA256
                              |      +-> RedisNonceStore -> redis:6379
                              |
                              +-> Spring Cloud Config -> config-server:8888
                              +-> JavaMailSender -> SMTP configurado
                              +-> Thymeleaf -> templates de correo existentes
                              +-> Actuator management:8090
                              |      +-> health/liveness/readiness
                              |      +-> Prometheus
                              +-> Loki (logs)
                              +-> Zipkin (traces)
```

### Cambios del patrón

- Plugin `org.graalvm.buildtools.native` aplicado solo a `notification-service`.
- `notification-service/Dockerfile.native`: compilación multietapa con GraalVM Native Image Community 24.0.2 y runtime Debian sin JVM.
- `docker-compose.notification-native.yml`: override opt-in para conservar intacta la ruta JVM.
- `.dockerignore`: permite fuentes al builder Native y excluye salidas `build/`.
- AOT se ejecuta con la configuración real de `notification-service`, porque Spring decide en build-time beans condicionales como JavaMailSender, management port y Prometheus.
- No hicieron falta hints genéricos de reflection, proxies, resources ni serialization. Se reutilizaron los metadatos AOT de Spring y las configuraciones reales.
- Las tareas QA sobre código AOT generado y el AOT de tests se desactivan; Checkstyle, PMD, SpotBugs, JaCoCo y tests normales sobre el código del proyecto permanecen activos.

La sustitución temporal de Config Server por import opcional y Java 24 ocurre únicamente dentro del contexto aislado del builder Docker. El código fuente conserva Java 21 y Config Server obligatorio para el runtime JVM existente.

## Construcción y ejecución

Construir la variante Native:

```bash
docker compose --env-file .env \
  -f docker-compose.yml \
  -f docker-compose.notification-native.yml \
  build notification-service
```

Ejecutar el stack con la variante Native:

```bash
docker compose --env-file .env \
  -f docker-compose.yml \
  -f docker-compose.notification-native.yml \
  --profile observability up -d --no-build --remove-orphans
```

Rollback inmediato a JVM:

```bash
docker compose --env-file .env \
  -f docker-compose.yml \
  up -d --no-build --force-recreate notification-service
```

## Métricas controladas JVM vs Native

| Métrica | JVM | Native | Diferencia |
|---|---:|---:|---:|
| Arranque reportado por Spring | 42.713 s | 4.267 s | -90.0%, 10.0x más rápido |
| Docker hasta `healthy` | 59 s | 8 s | -86.4%, 7.4x más rápido |
| RAM estabilizada en reposo | 252.4 MiB | 212.9 MiB | -39.5 MiB, -15.7% |
| RAM después de 40 health requests | 258.7 MiB | 214.3 MiB | -44.4 MiB, -17.2% |
| Imagen Docker arm64 | 134,229,342 bytes | 89,454,770 bytes | -44,774,572 bytes, -33.4% |

La compilación Native final tardó 2 h 55 min 54 s en esta Mac; la fase GraalVM consumió 2 h 53 min 36 s y alcanzó aproximadamente 4.99 GB RSS. Es un costo de build, no de arranque/runtime.

## Verificación ejecutada

- Imagen: `hotel-pms/notification-service-native:latest`, Linux arm64.
- Contenedor: `notification-service`, imagen Native, `healthy` y estable por más de 9 horas.
- Config Server: perfiles `default` y `notification-service` cargados.
- Puertos: aplicación 8088 y management 8090 activos.
- Liveness, readiness y Prometheus: respuesta satisfactoria.
- HMAC/InternalAuthFilter: sin headers = 401; firma válida con payload inválido = 400; replay del nonce = 401.
- Redis: `RedisNonceStore` aceptó el nonce una vez y rechazó su repetición.
- Loki: consulta de `service="notification-service"` con estado `success` y stream presente.
- Zipkin: `notification-service` aparece en `/api/v2/services`.
- Build JVM de regresión: `BUILD SUCCESSFUL` con 30 tests, 0 fallos, JaCoCo, Checkstyle, PMD y SpotBugs.
- Contratos de correo y attachments cubiertos por tests de `NotificationServiceImpl`; no se envió correo externo real para evitar un efecto irreversible.

## Patrón reutilizable para los demás servicios

1. Mantener Dockerfile JVM y agregar un Dockerfile Native opt-in.
2. Cargar durante AOT exactamente el perfil/configuración real del servicio para conservar beans condicionales.
3. Construir con un JDK/GraalVM actual sin cambiar el toolchain JVM del repositorio.
4. Resolver únicamente los hints demostrados por errores concretos; evitar listas genéricas.
5. Validar health, seguridad, persistencia/cache, observabilidad y contratos del servicio en el contenedor real.
6. Medir bajo la misma carga y conservar un override Compose reversible.

No aplicar todavía este patrón a `guest-service`, `frontdesk-service`, `billing-service`, `fb-service`, `auth-service`, `api-gateway` ni `config-server`: el criterio de esta iteración termina con el piloto validado.

## Incidencias aprendidas

- GraalVM 21.0.2 se quedó bloqueado al analizar Angus Mail. El builder final usa GraalVM 24.0.2, manteniendo el runtime JVM original en Java 21.
- Un AOT sin el perfil real puede eliminar JavaMailSender, management server y observabilidad por evaluación condicional en build-time.
- Docker Desktop necesitó 6144 MiB de RAM y 2048 MiB de swap para completar la imagen en este host de 8 GB.
- Archivos descargados de iCloud (`dataless`) pueden bloquear silenciosamente Gradle y contenedores con `resource deadlock avoided`; se materializaron desde el mismo commit antes de verificar.
