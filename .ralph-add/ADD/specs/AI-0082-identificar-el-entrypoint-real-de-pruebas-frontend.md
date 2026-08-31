# A.SPEC AI-0082 — Identificar el entrypoint real de pruebas frontend

ID: AI-0082
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0081 quedó bloqueada por el supervisor antes de ejecutar npm test; debe determinarse el comando y la suite reales sin modificar el worktree.

## WHAT
Inspeccionar los scripts npm y localizar pruebas frontend para definir la verificación determinista siguiente.

## SCOPE
- frontend/package.json
- archivos de pruebas existentes bajo frontend

## OUT OF SCOPE
- Modificar código o configuración
- Instalar dependencias
- Ejecutar migraciones
- Cambiar secretos
- Deploy

## CONTRACT
- La inspección debe producir el script de pruebas configurado y las rutas de las suites existentes.
- Si no existe suite o script válido, documentar el hueco como bloqueador.

## INVARIANTS
- No escribir, borrar, limpiar, resetear ni sobrescribir archivos.
- Conservar íntegramente el worktree actual.
- No ejecutar operaciones externas ni destructivas.

## VERIFICATION
- Los dos comandos terminan sin modificar archivos.
- La salida permite seleccionar el comando exacto para la próxima A.SPEC de VERIFY.

## ROLLBACK
No aplica; la A.SPEC es de solo lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- node -e "const p=require('./frontend/package.json'); console.log(JSON.stringify({scripts:p.scripts,devDependencies:p.devDependencies,dependencies:p.dependencies},null,2))"
- rg --files frontend | rg '(^|/)(test|tests|__tests__|.*\.(spec|test)\.)' | sort
END_VERIFY_COMMANDS
