# A.SPEC AI-0116 — Auditoría de consistencia de migraciones no aplicadas

**Modo:** `READ_ONLY`  
**Riesgo:** `LOW`  
**Resultado:** Auditoría completada sin modificaciones.

## 1. Límites y verificación

Se ejecutaron únicamente inspecciones de archivos:

- Inventario Flyway con `find`.
- Búsqueda de referencias con `rg`.
- Lectura de migraciones y código relacionado.
- Consulta de estado Git.

No se ejecutaron:

- Migraciones.
- Comandos contra bases de datos.
- Servicios o Docker.
- Deploys.
- Operaciones destructivas.
- Escrituras en archivos, Git o infraestructura.

El checkout ya contenía numerosos cambios modificados y no rastreados antes de la auditoría. No fueron alterados.

El path indicado `.ralph-add/ADD/specs` no existe en este checkout. Existe `.add/`, pero no contiene `APPROVAL-0000` ni `APPROVAL-0001` en las rutas inspeccionadas.

## 2. Inventario de migraciones

### frontdesk-service

| Versión | Archivo | Estado observado |
|---:|---|---|
| V1 | `V1__frontdesk_baseline.sql` | rastreada |
| V2–V11 | migraciones existentes | rastreadas |
| V12 | `V12__add_version_to_rate_seasons.sql` | no rastreada |
| V13 | `V13__add_version_to_quotations.sql` | no rastreada |
| V14 | `V14__add_reservation_overlap_exclusion.sql` | no rastreada |
| V15–V20 | migraciones existentes | rastreadas |
| V21 | `V21__migrate_ai_to_deepseek.sql` | no rastreada |
| V22 | `V22__use_ollama_by_default_keep_deepseek_option.sql` | no rastreada |
| V23 | `V23__use_qwen3_4b_instruct_q4.sql` | no rastreada |

La secuencia numérica es continua de `V1` a `V23`; no se observaron huecos de versión.

### guest-service

| Versión | Archivo | Estado observado |
|---:|---|---|
| V1–V9 | migraciones existentes | rastreadas |
| V10 | `V10__add_mexico_cfdi_guest_profile.sql` | no rastreada |

La secuencia numérica es continua de `V1` a `V10`.

## 3. Dependencias y referencias

### V12 — versión de temporadas

Modifica:

```sql
ALTER TABLE rate_seasons ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

Dependencias:

- Requiere `rate_seasons`, creada en `frontdesk V9`.
- La entidad `RateSeason` y la lógica de administración ya hacen referencia al dominio `rate_seasons`.
- El código documenta la exclusión de solapamientos y el control optimista.

Conclusión: dependencia estructural válida sobre `V9`.

### V13 — versión de cotizaciones

Modifica:

```sql
ALTER TABLE quotations ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

Dependencias:

- Requiere `quotations`, creada en `frontdesk V10`.
- Requiere la estructura ampliada por `V11`, que añade opciones y relaciones de cotización.
- El código de cotizaciones ya existe y el flujo `convertToReservation()` depende conceptualmente de control de concurrencia.

Conclusión: dependencia estructural válida sobre `V10` y compatible con `V11`.

### V14 — prevención de doble reserva

Modifica `reservation_line_items`:

- Añade `check_in_date`.
- Añade `check_out_date`.
- Añade `booking_blocking`.
- Hace backfill desde `reservations`.
- Añade triggers de sincronización.
- Añade una restricción `EXCLUDE USING gist`.

Dependencias:

- `reservation_line_items` y `reservations` provienen de `frontdesk V1`.
- Usa `btree_gist`, ya habilitada por `V9`.
- La restricción usa `room_id` y rangos de fechas.
- La lógica de aplicación ya utiliza rangos semiabiertos (`check-in` inclusivo, `check-out` exclusivo).

Conflictos potenciales concretos:

1. La migración realiza backfill y después establece `NOT NULL`; fallará si existen líneas huérfanas o reservas sin correspondencia.
2. La creación de la restricción fallará si ya existen reservas activas solapadas para el mismo cuarto.
3. El trigger actualiza filas mediante `UPDATE reservation_line_items SET id = id`; esto puede provocar actividad adicional y debe validarse sobre datos reales antes de aprobación.
4. La migración no usa `IF NOT EXISTS` para las columnas nuevas ni para la restricción; no es reejecutable frente a un esquema parcialmente aplicado.

Conclusión: dependencia válida, pero con riesgo de datos existentes y sin evidencia de aplicación segura.

### V21–V23 — configuración de IA

#### V21

Establece por defecto:

```text
deepseek-v4-flash
```

También normaliza modelos desconocidos y desactiva la IA en esas filas.

#### V22

Cambia el default a:

```text
qwen3:4b
```

Y convierte determinados registros `deepseek-v4-flash` a Ollama, activando la IA.

#### V23

Cambia el default a:

```text
qwen3:4b-instruct-2507-q4_K_M
```

Y convierte registros `qwen3:4b` al modelo nuevo.

Dependencias:

- Todas requieren `hotel_settings`, creada en `frontdesk V1`.
- V22 depende semánticamente de V21.
- V23 depende semánticamente de V22.

Conflicto concreto:

- V22 declara conservar la opción DeepSeek, pero modifica las filas con `deepseek-v4-flash` y las cambia a Ollama. Por tanto, la conservación real queda limitada a otros valores DeepSeek, como `deepseek-v4-pro`.
- V21–V23 son migraciones de datos, no solo cambios de esquema. Su efecto depende del contenido actual de `hotel_settings`.
- El código actual tiene como valor Java por defecto `qwen3:4b-instruct-2507-q4_K_M`, lo que coincide con V23, pero esto no prueba que V23 esté aplicada en la base de datos.

Conclusión: la cadena es numéricamente ordenada, pero representa decisiones de proveedor/modelo distintas y debería aprobarse como una unidad explícita o dividirse por decisión de proveedor.

### guest V10 — perfil fiscal CFDI México

Añade a `guests`:

- `rfc`
- `fiscal_name`
- `fiscal_postal_code`
- `fiscal_regime`
- `cfdi_use`
- `billing_email`

Dependencias:

- Requiere `guests`, creada en `guest V1`.
- Es compatible con las ampliaciones previas de `V8` y `V9`.
- El modelo, DTOs y validaciones de `guest-service` ya referencian estos campos.

Conflictos observados:

- No se observaron columnas duplicadas con `V8` o `V9`.
- No se observaron cambios destructivos.
- No se observó `IF NOT EXISTS`; una aplicación parcial provocaría error si alguna columna ya existiera.

## 4. Relación con APPROVAL-0000 y APPROVAL-0001

Resultado de búsqueda en las rutas permitidas:

```text
APPROVAL-0000: no encontrado
APPROVAL-0001: no encontrado
```

Además:

```text
.ralph-add/ADD/specs: inexistente
```

No es posible confirmar desde este checkout:

- El contenido de las aprobaciones.
- Qué migraciones están clasificadas como HIGH o CRITICAL.
- Si V12–V14 corresponden exactamente a `APPROVAL-0000` o `APPROVAL-0001`.
- Si V21–V23 tienen una aprobación independiente.

Por contrato, la existencia de los scripts en el checkout no se considera evidencia de que estén aplicados.

## 5. Estado de aplicación

No se consultó ninguna base de datos y no se ejecutó Flyway.

Por lo tanto, el estado real de aplicación de todas las migraciones queda:

```text
NO DETERMINADO
```

La documentación contiene referencias históricas a `flyway_schema_history`, pero no constituye evidencia actual del estado de este checkout.

## 6. Hallazgos

### Hallazgo H1 — Migraciones no rastreadas

No rastreadas:

- Frontdesk V12–V14.
- Frontdesk V21–V23.
- Guest V10.

Se confirma su presencia física, pero no su aplicación.

### Hallazgo H2 — V14 requiere validación de datos antes de aprobarse

V14 puede fallar por:

- Solapamientos existentes.
- Datos huérfanos.
- Filas sin fechas válidas.
- Aplicación parcial previa.

Recomendación: aprobación separada de V14 o A.SPEC específica de prevalidación read-only sobre datos reales.

### Hallazgo H3 — V21–V23 mezclan decisiones de proveedor

La cadena DeepSeek → Ollama → modelo Qwen es coherente por versión, pero cambia comportamiento y datos existentes en `hotel_settings`.

Recomendación: dividir en A.SPECs o aprobar como una secuencia explícita de configuración de IA, con revisión de compatibilidad operativa.

### Hallazgo H4 — Aprobaciones no disponibles

No se encontraron `APPROVAL-0000` ni `APPROVAL-0001` en las rutas permitidas. No debe inferirse aprobación pendiente específica sin esos artefactos.

## 7. Recomendación

No recomendaría una aprobación global de las migraciones.

Recomiendo dividir la decisión así:

1. **Grupo estructural de bajo riesgo**
   - V12.
   - V13.
   - Guest V10.

2. **Grupo de integridad/concurrencia**
   - V14, con una A.SPEC previa de validación de datos y solapamientos.

3. **Grupo de configuración de IA**
   - V21–V23, con aprobación funcional explícita sobre DeepSeek, Ollama y Qwen.

Antes de aplicar cualquier migración, debe obtenerse evidencia independiente de `flyway_schema_history` por servicio/base de datos. La auditoría actual no confirma ninguna migración como aplicada.

## 8. Verificación final

- No se modificaron archivos.
- No se modificaron migraciones.
- No se modificó Git.
- No se usaron bases de datos.
- No se iniciaron ni detuvieron servicios.
- No se ejecutaron comandos destructivos.
- El estado Git final observado conserva los cambios preexistentes del checkout.

**Conclusión:** `AI-0116` queda completada como auditoría read-only, con aprobación global no recomendada hasta resolver la ausencia de aprobaciones y separar V14 y V21–V23 por riesgo y decisión funcional.
