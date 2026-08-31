# A.SPEC AI-0108 — Auditar configuración local de secretos y exposición operativa

ID: AI-0108
Mode: READ_ONLY
RISK: LOW

## WHY
La seguridad de secretos es parte de V1 y el worktree contiene múltiples cambios de configuración; una auditoría estática acotada puede detectar defaults inseguros o secretos versionados sin modificarlos.

## WHAT
Clasificar referencias a secretos por fuente, default, manejo y riesgo, sin mostrar valores completos ni realizar rotaciones.

## SCOPE
- Compose
- configuración de servicios
- CI/CD
- documentación de secretos

## OUT OF SCOPE
- Cambiar secretos
- Rotar credenciales
- Modificar RBAC
- Deploy
- Acceder a proveedores externos

## CONTRACT
- No imprimir valores sensibles
- No asumir que una variable está configurada solo porque existe
- Distinguir placeholder de secreto real

## INVARIANTS
- Solo lectura
- No modificar archivos
- No ejecutar acciones externas

## VERIFICATION
- Inventario de nombres y fuentes
- Detección de valores hardcodeados o defaults peligrosos
- Lista de asuntos que requieren aprobación HIGH/CRITICAL

## ROLLBACK
No aplica; auditoría read-only.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- docker-compose.yml
- config
- .github
- docs
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n --hidden -g '!node_modules/**' -g '!*.backup-*' "(PASSWORD|SECRET|TOKEN|API_KEY|PRIVATE_KEY|ENCRYPTION_KEY|JWT)" docker-compose.yml config .github docs
END_VERIFY_COMMANDS
