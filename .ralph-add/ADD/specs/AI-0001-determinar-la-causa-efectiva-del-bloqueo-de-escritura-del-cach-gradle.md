# A.SPEC AI-0001 — Determinar la causa efectiva del bloqueo de escritura del caché Gradle

ID: AI-0001
Mode: READ_ONLY
RISK: LOW

## WHY
El informe confirma que el directorio no es escribible, pero no distingue entre ACL, flags del sistema, montaje o estado de permisos efectivo.

## WHAT
Inspeccionar identidad efectiva, ACL, flags y metadatos del directorio, además de confirmar la ubicación actual de los locks.

## SCOPE
- Lectura de identidad del usuario actual
- Lectura de permisos POSIX y ACL
- Lectura de flags del sistema de archivos
- Inventario de locks existentes

## OUT OF SCOPE
- Modificar permisos, propietarios o ACL
- Eliminar o renombrar locks
- Ejecutar Gradle, compilaciones o pruebas
- Modificar código, configuración, secretos o infraestructura

## CONTRACT
- La investigación debe producir evidencia reproducible sobre la causa del bloqueo de escritura.
- No se debe alterar ningún estado del sistema.

## INVARIANTS
- El directorio y sus archivos permanecen sin cambios.
- No se ejecutan operaciones destructivas ni comandos con efectos secundarios.

## VERIFICATION
- Todos los comandos son locales, deterministas y de solo lectura.
- La salida debe permitir clasificar el bloqueo como POSIX, ACL, flags u otra causa no determinada.

## ROLLBACK
No aplica: esta A.SPEC no modifica archivos, permisos ni configuración.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- /Users/usuario/.gradle/wrapper/dists
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- id
- ls -ldeO /Users/usuario/.gradle/wrapper/dists
- stat -f '%Su:%Sg %Sp %Sf %N' /Users/usuario/.gradle/wrapper/dists
- find /Users/usuario/.gradle/wrapper/dists -maxdepth 2 -name '*.lck' -print
END_VERIFY_COMMANDS
