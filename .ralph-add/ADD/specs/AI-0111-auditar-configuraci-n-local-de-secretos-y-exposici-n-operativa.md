# A.SPEC AI-0111 — Auditar configuración local de secretos y exposición operativa

ID: AI-0111
Mode: READ_ONLY
RISK: LOW

## WHY
La seguridad de secretos es requisito de V1 y existen cambios de configuración que pueden auditarse sin rotar ni modificar credenciales.

## WHAT
Clasificar nombres de secretos, fuentes, defaults, manejo y riesgos de exposición sin mostrar valores completos.

## SCOPE
- Docker Compose
- Configuración de servicios
- CI/CD
- Documentación de secretos

## OUT OF SCOPE
- Cambiar o rotar secretos
- Modificar RBAC
- Deploy
- Acceder a proveedores externos

## CONTRACT
- No imprimir valores sensibles
- No asumir configuración efectiva por la mera existencia de una variable
- Distinguir placeholder de secreto real

## INVARIANTS
- Solo lectura
- No modificar archivos
- No ejecutar acciones externas

## VERIFICATION
- Inventario de nombres y fuentes
- Defaults peligrosos o valores hardcodeados
- Asuntos que requieren aprobación HIGH/CRITICAL

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
