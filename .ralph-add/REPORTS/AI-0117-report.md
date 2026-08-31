# A.SPEC AI-0117 — Auditoría del contrato local de backup y restore

**Modo:** `READ_ONLY`  
**Riesgo:** `LOW`  
**Resultado:** **PARCIAL — no aprobado todavía como restore proof operativo completo**

## 1. Alcance y método

Se realizó únicamente inspección estática de:

- `docker/postgres/backup-scheduler.sh`
- `.github/workflows/backup-restore-drill.yml`
- Documentación operativa relevante dentro de `docs`

No se ejecutaron backups, restores, workflows, consultas PostgreSQL ni servicios. No se modificaron archivos, Git, secretos, bases de datos ni infraestructura.

## 2. Resumen ejecutivo

Existe una implementación documentada para:

- Backups full semanales e incrementales diarios.
- Archivado continuo de WAL, según la documentación y comentarios del scheduler.
- Uso de `repo1` local y `repo2` off-site condicionado a `S3_BUCKET`.
- Alertas ante fallos del ciclo de backup y de `pgbackrest check`.
- Restore aislado en un runner efímero de GitHub Actions.
- Arranque de PostgreSQL restaurado y consulta de cinco bases de datos.
- Verificación de que `flyway_schema_history` no esté vacía.

Sin embargo:

- El workflow CI no tiene evidencia estática de una ejecución exitosa reciente.
- El runbook declara que el drill CI aún no ha sido ejecutado en vivo.
- No se observa una política explícita de retención de backups vigente en el scheduler ni en el workflow.
- La documentación contiene referencias históricas contradictorias sobre el modelo anterior de backup de 14 días.
- No existe evidencia en esta auditoría de un resultado verificable actual contra el repositorio off-site.
- El workflow prueba restore del último backup, pero no prueba PITR; el PITR se describe como un procedimiento separado y manual.

## 3. Matriz requisito-evidencia

| Requisito | Evidencia encontrada | Estado | Clasificación |
|---|---|---:|---:|
| Retención | El scheduler define periodicidad full/incremental, pero no `retention-full`, `retention-diff`, `retention-archive` ni equivalente. El workflow tampoco configura retención. `docs/ROADMAP.md` conserva una referencia histórica a `pg_dumpall` con 14 días. | No demostrada | **HIGH** |
| Integridad | El scheduler ejecuta `pgbackrest check` después de cada ciclo. El workflow restaura el backup, inicia PostgreSQL y consulta las cinco bases de datos. | Parcialmente cubierta estáticamente | **MEDIUM** |
| Aislamiento | El workflow restaura en `${{ runner.temp }}/restore`, usa un runner GitHub efímero, inicia PostgreSQL en puerto `5433` y no toca la instancia de producción. El runbook también describe restauración en `/tmp/pitr-restore`. | Documentada | **LOW** |
| Restore verificable | El workflow ejecuta `info`, `restore`, `pg_ctl start`, consulta `hotel_auth`, `hotel_frontdesk`, `hotel_guest`, `hotel_billing` y `hotel_fb`, y valida `flyway_schema_history`. | Implementado estáticamente; ejecución no demostrada | **MEDIUM** |
| Manejo de errores | El scheduler continúa ante fallos, registra el error, envía alerta crítica a Alertmanager y vuelve a intentar en el siguiente ciclo. El workflow falla si una base no es consultable o no tiene migraciones; detiene la instancia con `if: always()`. | Documentado e implementado estáticamente | **LOW/MEDIUM** |
| Separación local/off-site | El scheduler itera explícitamente sobre `repo1` y, si existe `S3_BUCKET`, sobre `repo2`. El workflow configura únicamente el repositorio off-site como `repo1` dentro del runner. | Documentada | **LOW** |
| Prueba de disponibilidad off-site | El workflow contiene comandos para consultar y restaurar desde el repositorio remoto, pero no hay resultado de ejecución en los artefactos inspeccionados. El runbook dice que el drill CI aún no se ha ejecutado en vivo. | No demostrada | **HIGH** |
| Prueba PITR | El runbook describe un procedimiento PITR manual. El workflow explícitamente no usa `--type` ni `--target`; prueba solamente restore del último backup. | Parcial; PITR CI fuera del workflow | **MEDIUM** |
| No exposición de credenciales | Los secretos se referencian mediante `${{ secrets.* }}` y no aparecen valores concretos. El workflow escribe configuración temporal durante la ejecución. | Correcto estáticamente | **LOW** |

## 4. Evidencia principal

### Scheduler local

`docker/postgres/backup-scheduler.sh`:

- Define full semanal mediante `BACKUP_FULL_INTERVAL_SECONDS:=604800`.
- Define incremental diario mediante `BACKUP_INCR_INTERVAL_SECONDS:=86400`.
- Ejecuta:

  ```text
  pgbackrest ... --repo="${repo}" --type="${type}" backup
  ```

- Usa `repo1` siempre y agrega `repo2` únicamente cuando existe `S3_BUCKET`.
- Registra errores y envía alertas `BackupCycleFailed` con severidad crítica.
- Ejecuta `pgbackrest check` al finalizar cada ciclo.
- No termina el proceso completo ante un fallo de backup, porque no usa `set -e`.

Esto demuestra control de ejecución y manejo documentado de errores, pero no demuestra que exista una política de expiración o retención efectiva.

### Workflow de drill

`.github/workflows/backup-restore-drill.yml`:

- Está programado semanalmente y permite `workflow_dispatch`.
- Usa un runner GitHub-hosted nuevo.
- Configura el bucket remoto mediante secretos de GitHub.
- Ejecuta `pgbackrest info`.
- Restaura el último backup en un directorio temporal.
- Inicia PostgreSQL 15 en una instancia separada, puerto `5433`.
- Consulta cinco bases de datos.
- Exige al menos una fila en `flyway_schema_history`.
- Detiene la instancia incluso si pasos anteriores fallan.

El workflow constituye un diseño de restore verificable, pero el diseño estático no equivale a una prueba real aprobada.

## 5. Retención

La retención es el principal vacío contractual.

Se observa periodicidad:

- Full semanal.
- Incremental diario.
- WAL continuo descrito en la documentación.

Pero no se observa en los tres artefactos auditados una configuración explícita equivalente a:

- `repo*-retention-full`
- `repo*-retention-diff`
- Retención de WAL.
- Política de expiración automática.
- Capacidad, antigüedad o número máximo de cadenas conservadas.

Además, `docs/ROADMAP.md` todavía describe el modelo anterior de `pg_dumpall` con retención de 14 días, mientras que `docs/OPERATIONS_RUNBOOK.md` y `docs/DEPLOYMENT_GUIDE.md` describen el modelo actual de pgBackRest. Esa referencia histórica no prueba la retención actual y puede inducir a una falsa garantía operativa.

**Conclusión:** retención no demostrada.

## 6. Integridad

La integridad está cubierta de forma parcial:

- `pgbackrest check` se ejecuta después de cada ciclo.
- El workflow restaura una copia.
- PostgreSQL se inicia correctamente.
- Se consulta cada base de datos.
- Se valida la existencia de historial Flyway.

Esto prueba una forma útil de legibilidad funcional, pero no constituye por sí solo una verificación completa de:

- Checksums explícitos del repositorio.
- Validación de todos los archivos o bloques del backup.
- Consistencia de datos de negocio.
- Existencia de una cadena completa de backups y WAL.
- Resultado real del restore contra datos actuales.

**Conclusión:** integridad estáticamente razonable, pero no probada en vivo en esta auditoría.

## 7. Aislamiento

El diseño de aislamiento es sólido a nivel estático:

- Runner efímero.
- Directorio de restore separado.
- Puerto alternativo `5433`.
- No se detiene ni sobrescribe la instancia de producción.
- El runbook diferencia el restore scratch del restore destructivo sobre `PGDATA`.

El procedimiento manual de restore productivo sí incluye detener PostgreSQL y sobrescribir datos corrientes. Está documentado con advertencia, pero no forma parte del drill aislado.

**Conclusión:** aislamiento documentado y adecuado para el drill; el restore productivo sigue siendo una operación de alto impacto que requiere controles adicionales.

## 8. Manejo de errores

### Scheduler

Correctamente documentado estáticamente:

- Registra backup exitoso o fallido.
- Envía alerta a Alertmanager.
- Continúa el loop ante errores.
- No actualiza `last_full` si el ciclo full no fue exitoso en todos los repositorios.
- Alerta si falla `pgbackrest check`.

### Workflow

También contiene controles adecuados:

- Los comandos de restore o arranque fallan el job si no son exitosos.
- El script de validación acumula fallos por base de datos.
- Se ejecuta cleanup con `if: always()`.
- Se detecta una base restaurada sin migraciones.

No se observa un mecanismo explícito de:

- Reintento del workflow.
- Notificación operativa posterior al fallo.
- Conservación de artefactos/logs de restore para auditoría.
- Registro formal del RTO alcanzado.

**Conclusión:** manejo de errores funcionalmente documentado; observabilidad y evidencia histórica del resultado son incompletas.

## 9. Bloqueadores para aprobar un restore proof

Antes de considerar aprobado el restore proof de V1, quedan estos bloqueadores:

1. **Ejecutar realmente el workflow CI** contra el repositorio off-site configurado.
2. Conservar evidencia del run exitoso: fecha, commit, duración, repositorio usado y validación de las cinco bases.
3. Confirmar que `repo2` contiene una cadena válida de backup y WAL.
4. Definir y verificar una política explícita de retención para backups y WAL.
5. Resolver la contradicción documental entre el modelo antiguo de 14 días y el modelo actual pgBackRest.
6. Registrar si el restore proof requerido incluye solamente “latest backup restore” o también un PITR real.
7. Para un aprobado de disaster recovery, demostrar que el restore desde off-site funciona sin depender del host de producción.

## 10. Clasificación final

| Área | Nivel |
|---|---:|
| Retención no demostrada | **HIGH** |
| Restore CI no ejecutado en vivo | **HIGH** |
| Integridad parcialmente demostrada | **MEDIUM** |
| PITR separado del workflow | **MEDIUM** |
| Manejo de errores del scheduler | **LOW** |
| Aislamiento del drill | **LOW** |
| Exposición de secretos en los artefactos inspeccionados | **LOW** |

## Veredicto

**Estado general: MEDIUM/HIGH — contrato parcialmente implementado, restore proof aún no aprobado.**

La arquitectura y el workflow necesarios existen de forma estática, incluyendo aislamiento, validación funcional y manejo básico de errores. No obstante, la auditoría no puede afirmar que el restore off-site funcione actualmente porque el workflow CI no tiene evidencia de ejecución real y el propio runbook indica que esa ejecución está pendiente. La retención tampoco está definida de forma verificable en el contrato actual.
