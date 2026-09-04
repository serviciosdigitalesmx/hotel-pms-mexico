# Notification — empaquetado de la referencia validada para Linux amd64

Se reutiliza el piloto Native existente, sin alterar controladores, contratos,
seguridad, plantillas ni el Dockerfile JVM. Este workflow hace portable la
referencia ARM64 anterior al runner Linux amd64 para la integración de los ocho
servicios. No es un despliegue al PMS local ni una migración nueva del servicio.

## Resultado confirmado

- [Run O2 final 33845871639](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33845871639): **success**.
- Código: `b6f4b399575fd779dbc47f96e1466df47566a3e3`.
- [Imágenes pareadas 9926620156](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33845871639/artifacts/9926620156).
- [Evidencia runtime 9926620450](https://github.com/serviciosdigitalesmx/hotel-pms-mexico/actions/runs/33845871639/artifacts/9926620450).
- Archivo `notification-service-images.tar.gz`, acompañado por `SHA256SUMS` y
  `image-metadata.txt`. Contiene `hotel-pms/notification-service-native:validated`,
  el tag Native del SHA completo y `hotel-pms/notification-service-jvm:validated`.

| Métrica | Native O2 | JVM |
|---|---:|---:|
| Docker run hasta health UP | 1345 ms | 8851 ms |
| RAM al estar listo, antes del flujo | 96.25 MiB | 277.4 MiB |
| RAM tras envío SMTP básico | 101.3 MiB | 285.3 MiB |
| Tamaño de imagen | 263164825 bytes | 281539085 bytes |

Ambos modos: health/liveness/readiness UP, Prometheus válido, mensaje real
recibido en el buzón Mailpit correcto, HMAC ausente rechazado, firma válida
aceptada, repetición rechazada y nonce persistido en Redis autenticado. Treinta
comprobaciones de salud a lo largo de sesenta segundos, sin reinicios. Las
mediciones son una ejecución en runner, no percentiles de múltiples arranques ni
una prueba de entrega SMTP externa o de carga masiva.

## Aprendizajes de infraestructura

- La preparación AOT en Gradle usa el directorio del módulo. Las rutas de
  configuración adicional deben ser absolutas respecto a `GITHUB_WORKSPACE`.
- El preflight usa un Config Server real autenticado. No se deshabilita la
  seguridad del servidor ni se cambia el import obligatorio del runtime JVM.
- Las métricas HTTP de negocio son perezosas: se verifican después de la
  petición real al puerto de aplicación. Antes se comprueba la métrica de
  disponibilidad; exigir un contador de negocio antes del primer request era
  un falso fallo del harness.
- `actions/cache@v4` conserva Gradle/Maven; Buildx usa `cache-from: type=gha` y
  `cache-to: type=gha,mode=max`; Gradle compila con un mount `/root/.gradle` que
  no entra en la imagen final. Se conserva el presupuesto del piloto: 4600m,
  cuatro hilos y O2; no se compila en la Mac.

## Límites y reversión

La integración de correos de reservas/checkout desde Frontdesk y la exportación
Zipkin/Loki se verifican en el gate del stack, no quedan demostradas por este
smoke test aislado. Se mantiene la variante opt-in
`docker-compose.notification-native.yml` y el Dockerfile JVM original. No se
modifican APIs, bases de datos, RBAC, JWT, CSRF ni reglas de tenant isolation.
