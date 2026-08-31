# A.SPEC AI-0104 — Inventario read-only de migraciones

**Modo:** READ_ONLY  
**Riesgo:** LOW  
**Resultado:** Completado con evidencia local. No se ejecutaron Flyway ni Docker.

## 1. Verificación de integridad operativa

- No se modificaron archivos.
- No se modificó Git.
- No se accedió a bases de datos, servicios ni infraestructura externa.
- `git status` reportó únicamente archivos no versionados dentro del alcance indicado.
- Git mostró advertencias locales de macOS relacionadas con `xcrun_db`, sin impacto en el inventario.

## 2. Versiones existentes por servicio

### `frontdesk-service`

Secuencia encontrada:

```text
V1, V2, V3, V4, V5, V6, V7, V8, V9,
V10, V11, V12, V13, V14, V15, V16, V17,
V18, V19, V20, V21, V22, V23
```

No se detectaron duplicados ni saltos de versión.

Archivos no versionados:

- `V12__add_version_to_rate_seasons.sql`
- `V13__add_version_to_quotations.sql`
- `V14__add_reservation_overlap_exclusion.sql`
- `V21__migrate_ai_to_deepseek.sql`
- `V22__use_ollama_by_default_keep_deepseek_option.sql`
- `V23__use_qwen3_4b_instruct_q4.sql`

### `guest-service`

Secuencia encontrada:

```text
V1, V2, V3, V4, V5, V6, V7, V8, V9, V10
```

No se detectaron duplicados ni saltos de versión.

Archivo no versionado:

- `V10__add_mexico_cfdi_guest_profile.sql`

### `billing-service`

Secuencia encontrada:

```text
V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12
```

No se detectaron duplicados ni saltos de versión.

No se detectaron archivos no versionados en este servicio.

## 3. Resumen de migraciones nuevas

| Servicio | Versión | Alcance | Operaciones observadas |
|---|---:|---|---|
| frontdesk | V12 | Control optimista de `rate_seasons` | Añade `rate_seasons.version` |
| frontdesk | V13 | Control optimista de `quotations` | Añade `quotations.version` |
| frontdesk | V14 | Prevención de doble reserva | Añade columnas, backfill, funciones, triggers y constraint `EXCLUDE` |
| frontdesk | V21 | Cambio de proveedor AI a DeepSeek | Cambia default y normaliza modelos no permitidos; desactiva AI |
| frontdesk | V22 | Ollama como default local | Cambia default a `qwen3:4b`; habilita determinados registros |
| frontdesk | V23 | Modelo Qwen explícito | Cambia default a `qwen3:4b-instruct-2507-q4_K_M` |
| guest | V10 | Datos fiscales CFDI México | Añade seis columnas fiscales opcionales a `guests` |

## 4. Dependencias y riesgos identificados

### Frontdesk V12

- Depende de que exista la tabla `rate_seasons`.
- Añade una columna `version` obligatoria con `DEFAULT 0`.
- Riesgo operativo bajo, pero requiere que la entidad JPA correspondiente use el mismo campo para que el control optimista sea efectivo.

### Frontdesk V13

- Depende de que exista la tabla `quotations`.
- Añade `quotations.version` como `BIGINT NOT NULL DEFAULT 0`.
- Su objetivo es evitar conversiones concurrentes duplicadas.
- Requiere compatibilidad con la entidad JPA de quotations.

### Frontdesk V14

Es la migración de mayor alcance y riesgo relativo dentro del conjunto:

- Requiere `reservation_line_items`, `reservations` y sus columnas relacionadas.
- Requiere PostgreSQL con soporte para `btree_gist`, mediante `CREATE EXTENSION IF NOT EXISTS`.
- Realiza backfill de fechas y estado de bloqueo.
- Convierte las columnas backfilled en `NOT NULL`.
- Crea dos funciones y dos triggers.
- Crea una restricción `EXCLUDE USING gist` para impedir solapamientos de habitación.

Riesgos para aprobación posterior:

- El backfill fallaría si existen líneas sin una reserva correspondiente o con fechas nulas.
- La restricción final puede fallar si ya existen reservas activas solapadas.
- La migración contiene operaciones de datos y cambios de integridad, no solamente DDL.
- Debe comprobarse previamente la compatibilidad de triggers existentes y la capacidad de la base para crear índices GiST.

La propia migración declara que `btree_gist` ya habría sido habilitada por `frontdesk V9`; esto es una dependencia de secuencia lógica observada en el contenido local.

### Frontdesk V21 → V22 → V23

La cadena es secuencial y modifica el mismo campo `hotel_settings.ai_model`:

1. **V21**  
   - Default: `deepseek-v4-flash`.
   - Reemplaza modelos no permitidos.
   - Desactiva AI para esos registros.

2. **V22**  
   - Default: `qwen3:4b`.
   - Cambia a Ollama determinados registros DeepSeek `deepseek-v4-flash`, vacíos o nulos.
   - Habilita AI para esos registros.

3. **V23**  
   - Default: `qwen3:4b-instruct-2507-q4_K_M`.
   - Reemplaza `qwen3:4b`, además de valores vacíos o nulos.
   - Mantiene intactos los modelos DeepSeek existentes.

Riesgos:

- Son migraciones dependientes del estado de datos dejado por las anteriores.
- V22 no reemplaza todos los modelos DeepSeek, únicamente `deepseek-v4-flash` y valores vacíos/nulos.
- V23 tampoco reemplaza modelos DeepSeek.
- La semántica de `ai_enabled` cambia entre V21 y V22/V23; debe aprobarse junto con la lógica actual de `AssistantService`.
- No se observaron secretos ni valores de credenciales en el contenido inspeccionado.

### Guest V10

- Añade seis columnas opcionales a `guests`.
- No contiene backfill ni restricciones `NOT NULL`.
- No se observan conflictos de secuencia.
- Riesgo estructural bajo; requiere compatibilidad posterior con validaciones de RFC, régimen fiscal, uso CFDI y código postal.

## 5. Duplicados y saltos

Resultado por servicio:

| Servicio | Primera versión | Última versión | Duplicados | Saltos |
|---|---:|---:|---:|---:|
| frontdesk-service | V1 | V23 | No observados | No observados |
| guest-service | V1 | V10 | No observados | No observados |
| billing-service | V1 | V12 | No observados | No observados |

No existen conflictos de nombres dentro del mismo servicio según la lista ordenada de archivos.

Las mismas versiones numéricas entre servicios no constituyen conflicto Flyway porque cada servicio mantiene su propio directorio de migraciones.

## 6. Estado Git local

Archivos reportados como no versionados:

```text
frontdesk-service/src/main/resources/db/migration/V12__add_version_to_rate_seasons.sql
frontdesk-service/src/main/resources/db/migration/V13__add_version_to_quotations.sql
frontdesk-service/src/main/resources/db/migration/V14__add_reservation_overlap_exclusion.sql
frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql
frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql
frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql
guest-service/src/main/resources/db/migration/V10__add_mexico_cfdi_guest_profile.sql
```

## 7. Conclusión para aprobación posterior

- La numeración local es continua en los tres servicios.
- No se detectaron duplicados ni saltos.
- Las migraciones pendientes identificadas son siete.
- El principal punto de revisión es `frontdesk V14`, por su backfill, triggers, extensión GiST y constraint de exclusión.
- La cadena `V21–V23` requiere aprobación funcional porque cambia defaults, habilitación de AI y selección de proveedor/modelo.
- `guest V10`, `frontdesk V12` y `frontdesk V13` presentan cambios más acotados, aunque deben verificarse contra las entidades y datos existentes antes de aplicar.
- No se puede afirmar desde este inventario si alguna migración ya fue aplicada en una base de datos, porque la A.SPEC prohibió acceder o ejecutar operaciones contra bases de datos.
