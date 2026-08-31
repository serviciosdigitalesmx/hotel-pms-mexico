# A.SPEC AI-0109 — Auditoría local de backup y restore

**ID:** AI-0109  
**Modo:** READ_ONLY  
**Riesgo:** LOW  
**Resultado:** **PARCIAL / NO aprobado como restore proof vigente**

## Alcance revisado

- `docker/postgres/backup-scheduler.sh`
- `.github/workflows/backup-restore-drill.yml`
- Documentación operativa relacionada, principalmente:
  - `docs/OPERATIONS_RUNBOOK.md`
  - `docs/DEPLOYMENT_GUIDE.md`
  - `docs/ROADMAP.md`

No se ejecutaron backups, restores, consultas PostgreSQL, workflows, despliegues ni operaciones sobre infraestructura.

## Evidencia encontrada

### Backup local

`backup-scheduler.sh` implementa:

- pgBackRest con stanza `hotel-pms`.
- Backup full semanal: `604800` segundos.
- Backup incremental diario: `86400` segundos.
- WAL archiving continuo documentado como responsabilidad de `archive_command`.
- `repo1` siempre configurado.
- `repo2` añadido únicamente cuando existe `S3_BUCKET`.
- Ejecución explícita de `backup` para cada repositorio.
- Ejecución posterior de `pgbackrest check`.
- Alertas HTTP a Alertmanager cuando falla un backup o el `check`.
- El loop continúa después de un fallo y reintenta en el siguiente ciclo.

### Restore drill

El workflow define:

- Ejecución semanal, lunes 04:00 UTC.
- Ejecución manual mediante `workflow_dispatch`.
- Runner GitHub efímero.
- Recuperación únicamente desde el repositorio off-site.
- Configuración de pgBackRest usando secrets de GitHub.
- Restauración del backup más reciente en un directorio temporal.
- Inicio de una instancia PostgreSQL 15 independiente en el puerto `5433`.
- Consulta de cinco bases:
  - `hotel_auth`
  - `hotel_frontdesk`
  - `hotel_guest`
  - `hotel_billing`
  - `hotel_fb`
- Validación de que cada base sea consultable y tenga al menos una fila en `flyway_schema_history`.
- Detención de la instancia restaurada incluso si un paso anterior falla.

El workflow no prueba PITR a un timestamp específico. Eso está explícitamente fuera de este drill y se remite a un procedimiento manual.

## Matriz requisito-evidencia

| Requisito | Evidencia local | Estado | Clasificación |
|---|---|---:|---:|
| Backup programado | Full semanal e incremental diario en `backup-scheduler.sh` | Evidencia estática | LOW |
| WAL / RPO | Comentarios y runbook describen WAL continuo y `archive_timeout=120s` | Documentado, no probado en esta auditoría | MEDIUM |
| Copia local | `repo1` y volumen local documentados | Evidencia estática | LOW |
| Copia off-site | `repo2` condicionado a `S3_BUCKET`; documentación refiere Backblaze B2 | Configuración dependiente de entorno | MEDIUM |
| Retención | No se identifica una política explícita de retención de pgBackRest en los archivos revisados | Falta de evidencia | HIGH |
| Integridad del backup | `pgbackrest check` después de cada ciclo | Control estático | MEDIUM |
| Verificación de contenido | Restore drill consulta las cinco bases y `flyway_schema_history` | Diseñado en workflow | MEDIUM |
| Restore aislado | Directorio scratch y runner efímero; instancia en puerto `5433` | Bien definido estáticamente | LOW |
| Manejo de errores | Alertas por backup/check fallido; workflow detiene ante errores de validación | Evidencia estática | LOW |
| Protección de secretos | Secrets usados mediante `${{ secrets.* }}`; no aparecen valores | Diseño correcto | LOW |
| Restore local | Runbook describe restore y PITR en directorio separado | Procedimiento documentado | MEDIUM |
| Restore off-site | Workflow automatizado y runbook con `repo2` | Existencia estática; ejecución CI no demostrada | HIGH |
| Prueba real de restore | El runbook afirma un PITR real el 2026-08-01, pero el workflow CI figura como no ejecutado | Evidencia documental no independiente | HIGH |
| Verificación PITR | El workflow no ejecuta PITR; solo restaura el último backup | Cobertura parcial | MEDIUM |
| Aislamiento de producción | El workflow usa runner efímero y no producción | Correctamente diseñado | LOW |

## Retención

No se encontró en la evidencia revisada una configuración explícita de:

- `repo*-retention-full`
- Retención diferencial/incremental.
- Retención WAL.
- Política de expiración del bucket off-site.
- Garantía de retención mínima por días o número de backups.

La documentación contiene referencias históricas a `pg_dumpall` con retención de 14 días, pero también declara que ese mecanismo fue reemplazado por pgBackRest. Esa referencia histórica no debe considerarse evidencia de retención vigente para el contrato actual.

## Bloqueadores para aprobar el restore proof

1. **El workflow CI no está probado en vivo según la propia documentación.**  
   `docs/OPERATIONS_RUNBOOK.md` indica que requiere configurar los secrets de GitHub.

2. **No hay evidencia local verificable de una política de retención actual de pgBackRest.**  
   La retención no puede inferirse de la existencia de full/incremental backups.

3. **El restore drill no prueba PITR.**  
   Solo demuestra, cuando se ejecuta, que el backup más reciente puede restaurarse y arrancar.

4. **La supuesta prueba real de PITR del 2026-08-01 está únicamente documentada.**  
   No se revisaron logs, artefactos CI, hashes, identificadores de backup ni resultados persistidos.

5. **La activación de `repo2` depende de variables de entorno.**  
   El script no demuestra por sí solo que el destino off-site esté configurado en el entorno operativo.

## Inconsistencias documentales

`docs/ROADMAP.md` aún contiene descripciones históricas del modelo anterior:

- `pg_dumpall` cada 24 horas.
- Retención de 14 días.
- Ausencia de copia off-site.
- Ausencia de PITR.

Estas afirmaciones contradicen el modelo pgBackRest descrito en `docs/OPERATIONS_RUNBOOK.md` y `docs/DEPLOYMENT_GUIDE.md`. Deben clasificarse como documentación desactualizada, no como evidencia del estado actual.

## Clasificación final

**Clasificación global: MEDIUM**

Motivos:

- Existe una implementación concreta y razonablemente estructurada.
- Existe un workflow de restore aislado con validaciones útiles.
- Hay manejo de errores y uso de secrets sin exponer credenciales.
- Sin embargo, faltan pruebas CI ejecutadas y evidencia vigente de retención.
- El restore proof aprobado no puede declararse únicamente a partir del código y la documentación.

**Bloqueador principal:** no existe evidencia verificable dentro de este alcance de que el workflow off-site haya completado exitosamente ni de cuál es la política efectiva de retención.

## Integridad del worktree

La auditoría no modificó archivos ni Git.

El worktree ya contenía cambios preexistentes, incluyendo archivos modificados y no rastreados fuera del alcance de AI-0109. La comparación específica de las rutas auditadas no mostró diferencias introducidas durante esta ejecución:

```text
allowed_paths_diff_exit=0
```

También se observaron advertencias del entorno al consultar Git por restricciones de escritura en `/tmp`; no implican cambios realizados por esta auditoría.
