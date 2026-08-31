# Informe de auditoría — A.SPEC AI-0111

**Modo:** READ_ONLY  
**Riesgo declarado:** LOW  
**Resultado:** **NO CONFORME — requiere correcciones HIGH antes de considerar segura la configuración operativa**

## Alcance revisado

- `docker-compose.yml`
- `config/`
- `.github/workflows/`
- `docs/`

Se ejecutó la búsqueda especificada por el A.SPEC. No se modificaron archivos, Git, secretos, bases de datos, servicios ni infraestructura externa.

## Estado del workspace

El checkout ya contenía numerosos cambios modificados y archivos no rastreados antes de esta auditoría, incluyendo `docker-compose.yml`. No se alteró ninguno.

## Inventario de secretos

| Categoría | Variables detectadas | Fuente declarada | Evaluación |
|---|---|---|---|
| Firma JWT | `JWT_SECRET` | Interpolación desde entorno hacia Config Server y Gateway | Correctamente parametrizada; el valor efectivo no puede confirmarse solo por el Compose |
| Firma interna | `INTERNAL_HMAC_SECRET` | Entorno → Config Server/microservicios | Parametrizada; la documentación declara validación de longitud |
| Base de datos | `POSTGRES_PASSWORD` | Entorno → PostgreSQL y servicios Spring | Parametrizada, sin default visible |
| Config Server | `CONFIG_SERVER_PASSWORD` | Entorno → servicios consumidores | Parametrizada, sin default visible |
| Alloggiati PS | `ALLOGGIATI_USERNAME`, `ALLOGGIATI_PASSWORD`, `ALLOGGIATI_WS_KEY` | Entorno | Tienen placeholders de CI/desarrollo en Compose |
| Cifrado Alloggiati | `ALLOGGIATI_CREDENTIALS_ENCRYPTION_KEY`, `ALLOGGIATI_CREDENTIALS_ENCRYPTION_SALT` | Entorno | Tiene defaults placeholder |
| Backups | `PGBACKREST_CIPHER_PASS`, `S3_ACCESS_KEY_ID`, `S3_SECRET_ACCESS_KEY` y variables `S3_*` | Entorno y GitHub Actions Secrets | Parametrizadas; integración opt-in |
| SMTP | `SMTP_USERNAME`, `SMTP_PASSWORD` | Entorno | Default vacío; SMTP autenticado está desactivado por default |
| Grafana | `GF_SECURITY_ADMIN_PASSWORD` | Hardcoded en Compose | **Hallazgo crítico de exposición operativa** |

## Hallazgos

### HIGH — Credencial administrativa hardcodeada en Grafana

**Evidencia:** `docker-compose.yml:165`

La configuración contiene directamente:

```text
GF_SECURITY_ADMIN_PASSWORD=admin
```

Esto no es un placeholder neutro ni una referencia externa: es un valor efectivo si el servicio Grafana se inicia con esta configuración. El riesgo aumenta porque Grafana expone el puerto `3000`.

**Impacto potencial:**

- Acceso administrativo predecible.
- Compromiso de dashboards, fuentes de datos y configuración de observabilidad.
- Posible exposición indirecta de logs, métricas o información operativa.

**Clasificación recomendada:** HIGH. Puede escalar a CRITICAL si Grafana es accesible fuera de una red interna o mediante proxy público.

### MEDIUM — Placeholders de credenciales Alloggiati incluidos como defaults

**Evidencia:** `docker-compose.yml:510-516`

Se observan defaults para:

- Usuario Alloggiati.
- Password Alloggiati.
- Web Service Key.
- Clave de cifrado de credenciales.
- Salt de cifrado.

Aunque están marcados como placeholders de CI/desarrollo, la mera presencia de defaults permite que el contenedor arranque con material no válido o débil. Además, la aplicación podría no fallar inmediatamente si el modo dry-run permanece activo.

**Evaluación:** No demuestra exposición de credenciales reales, pero sí una configuración insegura si el Compose se reutiliza accidentalmente fuera de CI.

### LOW/MEDIUM — Cifrado de backups desactivado por default

**Evidencia:** `docker-compose.yml:57`

`PGBACKREST_CIPHER_PASS` tiene default vacío. La propia documentación indica que, si no se establece, los repositorios locales pueden quedar sin cifrar.

**Riesgo:** exposición de respaldos que pueden contener datos personales, reservas, facturas y otra información operativa.

**Evaluación:** El comportamiento parece intencional y opt-in, pero no es un baseline seguro para producción.

### LOW — SMTP permite configuración sin autenticación ni TLS

**Evidencia:** `docker-compose.yml:670-675`

Defaults observados:

- Host local.
- Puerto SMTP convencional.
- Usuario y contraseña vacíos.
- Autenticación desactivada.
- STARTTLS desactivado.

Esto no expone una contraseña, pero puede permitir envío inseguro o fallos silenciosos cuando el mismo Compose se usa en entornos no locales.

### MEDIUM — Documentación contiene ejemplos que pueden confundirse con credenciales

**Evidencia:**

- `docs/ALLOGGIATI_README.md:43-46`
- `docs/DOCUMENTAZIONE_TECNICA_ALLOGGIATI_PS.md:340-343`
- `docs/security-report/report-secure-coding.tex:4482-4507`

Los documentos incluyen ejemplos de configuración y fragmentos históricos con valores de credenciales o secretos de prueba. No se consideran configuración efectiva por estar dentro de documentación, pero permanecen en archivos versionables y pueden:

- Ser copiados literalmente a entornos reales.
- Activar detectores de secretos.
- Generar ambigüedad sobre qué valores son históricos, placeholders o válidos.

La documentación de `report-secure-coding.tex` describe explícitamente fallos históricos ya corregidos; no debe interpretarse como evidencia de que esos fallbacks sigan activos en la configuración actual.

### LOW — Riesgo de exposición de secretos en logs de CI

**Evidencia:** `.github/workflows/backup-restore-drill.yml:39-64`

El workflow carga secretos de GitHub Actions y genera un archivo de configuración mediante comandos `echo`. Aunque el archivo se escribe en una ruta temporal protegida por `sudo`, el patrón merece revisión porque incorpora valores secretos en comandos y archivos temporales.

No se observó un `echo` directo del valor sin interpolación controlada en la salida del workflow, pero la protección depende de las reglas de masking de GitHub Actions y del comportamiento del runner.

### MEDIUM — El workflow CI documenta un secreto obligatorio, pero no prueba su presencia

**Evidencia:** `.github/workflows/ci.yml:282`

El workflow declara que `INTERNAL_HMAC_SECRET` debe configurarse en GitHub Actions Secrets, pero la evidencia revisada solo demuestra la documentación de ese requisito. No permite confirmar:

- Que el secreto exista actualmente.
- Que tenga longitud suficiente.
- Que todos los jobs lo reciban.
- Que sea distinto de un placeholder.

## Controles positivos observados

- `JWT_SECRET`, `INTERNAL_HMAC_SECRET`, `POSTGRES_PASSWORD` y `CONFIG_SERVER_PASSWORD` se inyectan mediante variables de entorno, sin valores visibles en Compose.
- No se observó un fallback actual visible para `JWT_SECRET`.
- Las credenciales Alloggiati se mantienen fuera del código de aplicación según la documentación.
- `ALLOGGIATI_DRY_RUN` tiene default seguro (`true`) en Compose.
- Las credenciales de backup se referencian mediante `secrets.*` en GitHub Actions.
- La documentación declara que `SMTP_PASSWORD` no debe almacenarse en configuración de hotel accesible por API.
- La configuración de SMTP no contiene una contraseña real visible.

## Distinción entre placeholder y secreto real

| Elemento | Clasificación |
|---|---|
| `${JWT_SECRET}` | Referencia a secreto externo; valor efectivo no confirmado |
| `${POSTGRES_PASSWORD}` | Referencia a secreto externo; valor efectivo no confirmado |
| `ci_placeholder_*` | Placeholder explícito, no secreto real |
| Salt hexadecimal estático de Compose | Valor hardcodeado de configuración; no debe tratarse como secreto suficiente |
| `GF_SECURITY_ADMIN_PASSWORD=admin` | Valor hardcodeado efectivo; hallazgo de seguridad |
| Valores dentro de documentación histórica | Ejemplos o evidencia histórica; no prueban configuración efectiva |
| `${{ secrets.* }}` | Referencia a GitHub Actions Secret; existencia y contenido no verificables en modo local read-only |

## Asuntos que requieren aprobación HIGH/CRITICAL

1. Eliminar el password administrativo hardcodeado de Grafana y exigir inyección externa o fail-fast.
2. Confirmar que Grafana no sea accesible públicamente; si lo es, elevar el hallazgo a CRITICAL.
3. Impedir que los placeholders de Alloggiati y cifrado sean aceptados fuera de CI/desarrollo.
4. Definir si el cifrado de pgBackRest debe ser obligatorio en producción.
5. Revisar los ejemplos históricos de secretos en documentación y marcar claramente todos los valores no operativos.
6. Validar en GitHub Actions la presencia, longitud y no-placeholder de los secretos requeridos.

## Verificación ejecutada

- Inventario por búsqueda de nombres sensibles.
- Revisión contextual de Compose, workflows y documentación.
- Revisión de estado Git.
- No se ejecutaron servicios, Docker Compose, CI remoto, endpoints, proveedores ni comandos destructivos.

## Conclusión

La mayoría de los secretos principales están correctamente externalizados mediante variables de entorno o GitHub Actions Secrets, pero la auditoría **no puede confirmar la configuración efectiva** de esos valores.

La configuración actual no debe considerarse segura para operación expuesta debido principalmente a:

- `GF_SECURITY_ADMIN_PASSWORD=admin`.
- Defaults de credenciales y cifrado para Alloggiati.
- Cifrado de backups opcional y desactivado por default.
- Material histórico y ejemplos sensibles dentro de documentación.

**Resultado final: FAIL / requiere remediación HIGH antes de producción expuesta.**
