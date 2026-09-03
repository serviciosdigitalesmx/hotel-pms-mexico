# ADD — piloto Native de `config-service`

## Delta real identificado

El módulo existente es el subproyecto Gradle `config-service`, con
`ConfigServiceApplication` + `@EnableConfigServer`. Su contrato conservado es:

- Config Server Native en `:8888`, sirviendo los YAML de
  `src/main/resources/config/`.
- HTTP Basic obligatorio para los endpoints de configuración.
- Actuator aislado en `:8090`, con health, liveness, readiness e info.
- Prometheus en `:8090/actuator/prometheus`, que ya era el destino declarado
  por `docker/prometheus/prometheus.yml`.
- Credenciales exclusivamente por variables de entorno; los YAML contienen
  placeholders y no valores operativos.

No aplica a este servicio: PostgreSQL/Flyway/JPA, Redis, HMAC/InternalAuthFilter,
RBAC/JWT o Feign. Esos contratos pertenecen a los consumidores y no se
introducen artificialmente en Config Server.

## Implementación

- Se agregó el plugin Native sólo a `config-service`.
- `config-service/Dockerfile` permanece como la ruta JVM reversible.
- `config-service/Dockerfile.native` compila en GraalVM dentro de Docker y
  ejecuta como usuario no root en un runtime sin JVM.
- `config-service/Dockerfile.native.dockerignore` mantiene fuera del contexto
  los outputs, secretos locales y artefactos no necesarios.
- `.github/workflows/config-native.yml` usa un solo runner y una sola
  invocación de `nativeCompile`/`native-image` por ejecución. El gate de PR usa
  `-Ob`; el gate final se solicita manualmente con `optimized` (`-O2`).
- La compatibilidad Native conserva el repositorio classpath `native` y excluye
  del módulo los artefactos JGit/SSHD del backend Git opcional no utilizado; no
  activa ni introduce un backend Git.
- La caché es únicamente `actions/cache@v4` para `~/.gradle/caches`,
  `~/.gradle/wrapper` y `~/.m2/repository`; BuildKit usa además la caché GHA
  requerida para las capas Docker.

## Gate y evidencia

Antes del Native build se ejecutan tests JVM, `processAot` y `bootJar`. El script
de runtime levanta la imagen Native y una JVM de control y comprueba:

- salud general, liveness, readiness y Prometheus;
- HTTP 401 sin Basic Auth y HTTP 200 con credenciales CI;
- perfiles reales `guest-service/default` y `api-gateway/prod`;
- arranque, tamaño de imagen y memoria en reposo JVM vs Native;
- ausencia de credenciales operativas en la imagen: sólo se inyectan placeholders
  efímeros del CI.

La evidencia se publica como artifact y en `GITHUB_STEP_SUMMARY`. Los fallos de
este flujo se identifican como `NATIVE_GATE_FAIL`; un fallo ajeno del workflow
global `ci.yml` no se atribuye a este gate.

La compilación Native no se ejecuta en macOS; sólo el runner `ubuntu-latest` del
workflow construye la imagen. La validación local queda limitada a la ruta JVM,
tests y procesamiento AOT barato.
