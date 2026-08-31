# A.SPEC AI-0001 — Diagnosticar permisos del caché local de Gradle

ID: AI-0001
Mode: READ_ONLY
RISK: LOW

## WHY
La verificación quedó bloqueada antes de compilar porque Gradle no pudo crear o abrir gradle-9.3.1-bin.zip.lck. Hace falta evidencia local del propietario, permisos y existencia del lock antes de definir una remediación.

## WHAT
Inspeccionar de forma no destructiva los permisos del directorio de distribuciones del Gradle Wrapper y del archivo de bloqueo relacionado.

## SCOPE
- Propietario y grupo del directorio de caché
- Permiso de escritura del usuario actual
- Existencia y permisos de archivos .lck dentro del caché

## OUT OF SCOPE
- Modificar permisos o propietarios
- Eliminar archivos .lck o distribuciones
- Cambiar Gradle, el wrapper o el código fuente
- Ejecutar compilaciones, pruebas, Docker, migraciones o proveedores de IA

## CONTRACT
- No alterar el checkout ni el caché local
- No imprimir secretos ni contenido de archivos de configuración
- Reportar claramente si la evidencia confirma o no un bloqueo de permisos

## INVARIANTS
- El repositorio /Users/usuario/Desktop/HOTEL-PMS permanece sin modificaciones
- No se eliminan ni sobrescriben archivos
- La conclusión se limita a evidencia observable del sistema de archivos

## VERIFICATION
- Los tres comandos terminan sin modificar archivos
- Se registra el código de salida de la prueba de escritura
- Se identifica el propietario y los permisos del directorio y locks relevantes

## ROLLBACK
No aplica: este paso es estrictamente READ_ONLY y no realiza cambios.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- /Users/usuario/.gradle/wrapper/dists
- /Users/usuario/Desktop/HOTEL-PMS/gradle
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- test -d /Users/usuario/.gradle/wrapper/dists && stat -f '%Sp %Su %Sg %N' /Users/usuario/.gradle/wrapper/dists
- test -w /Users/usuario/.gradle/wrapper/dists; printf 'writable_exit=%s\n' "$?"
- find /Users/usuario/.gradle/wrapper/dists -maxdepth 3 -name '*.lck' -exec stat -f '%Sp %Su %Sg %N' {} \;
END_VERIFY_COMMANDS
