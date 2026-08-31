# A.SPEC V-0000 — Identificar la superficie existente de caja y turnos para implementación inmediata

ID: V-0000
Mode: READ_ONLY
RISK: LOW

## WHY
No hay evidencia reciente suficiente en el checkpoint sobre contratos reales de caja; se requiere una única prevalidación antes de escribir para evitar inventar endpoints o tablas.

## WHAT
Mapear pantallas, servicios, DTOs, endpoints y persistencia ya existentes, devolviendo el primer A.SPEC WRITE acotado y sin migraciones.

## SCOPE
- Contratos existentes de caja
- Flujo de apertura, movimientos, arqueo y cierre
- Históricos disponibles

## OUT OF SCOPE
- Aplicar migraciones
- Crear tablas
- Cambiar secretos
- Modificar código

## CONTRACT
- Reutilizar únicamente contratos encontrados
- Separar lo implementable sin DB de lo bloqueado por aprobación

## INVARIANTS
- No editar archivos
- No ejecutar servicios ni reinicios
- No duplicar blockers de aprobación

## VERIFICATION
- Reporte con rutas y contratos concretos
- Lista de gaps accionables
- Siguiente WRITE definido

## ROLLBACK
No aplica; operación exclusivamente de lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src
- billing-service/src
- frontdesk-service/src
- api-gateway/src
- docs
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n "cash|caja|shift|turno|arqueo|discrepancia|expected|counted|close|historial" frontend/src billing-service/src frontdesk-service/src api-gateway/src docs
END_VERIFY_COMMANDS
