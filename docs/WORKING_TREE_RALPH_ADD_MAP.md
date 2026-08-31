# Mapa completo del working tree y Ralph/ADD

Fecha del inventario: 2026-08-31

## Resultado de la consolidación

La rama local `main` contiene la consolidación funcional en seis commits sobre
`origin/main`: seguridad/concurrencia, expediente, asistente tenant, perfil
fiscal mexicano y aislamiento operativo del legado italiano.

## Material que se integra

- Código fuente y configuración versionada pendiente.
- Pruebas versionadas pendientes.
- `docs/ADD_IMPLEMENTATION_CONSOLIDATION_MAP.md`.
- `docs/ADD_MULTITENANT_MEXICO_MAP.md`.
- Especificaciones ADD/Ralph en texto para conservar trazabilidad.
- Reportes y decisiones textuales de Ralph que no contengan secretos ni binarios.
- Scripts fuente de supervisión (`.py`) y configuraciones JSON/YAML de control,
  cuando no sean credenciales ni estado efímero.

## Material que no se integra deliberadamente

- `.env.backup-*`, backups `.bak-*` y cualquier archivo con credenciales.
- `*.jar`, `*.class`, `*.pyc`, `__pycache__`, `target`, `runtime` y cachés.
- PIDs, índices y estados efímeros generados por procesos.
- Evidencia binaria compilada dentro de `.add/evidence`.
- `argos` y `argos-local` como scripts operativos no parte del producto.
- `docker-compose.ollama.yml` y su backup: integración experimental de IA,
  fuera del bloque PMS México solicitado.

## Conteo del inventario previo

- 16 archivos versionados modificados antes de la consolidación final.
- 1,508 archivos no rastreados.
- `.add`: 496 archivos, principalmente evidencias y artefactos.
- `.ralph-add`: 516 archivos, principalmente especificaciones, logs, estados y
  resultados de ejecución.
- `docs/freeze-2026-08-22`: 491 archivos de congelamiento operativo.

## Regla de cierre

La ausencia de un archivo binario o secreto del commit no significa pérdida de
trabajo de producto: el código fuente, las pruebas, las especificaciones y la
trazabilidad textual quedan integrados o referenciados aquí. Los builds, lint,
Docker y E2E deben ejecutarse sobre esta rama antes de declarar producción.
