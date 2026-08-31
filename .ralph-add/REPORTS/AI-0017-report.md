# A.SPEC AI-0017 — Report

## Resultado

**WARNING** — La ejecución fue estrictamente de solo lectura y no modificó archivos, Git, bases de datos, Redis, Docker ni secretos.

## Evidencia observada

### Estructura Gradle

`settings.gradle.kts` define el proyecto raíz `hotel-pms` e incluye, cuando existen:

- `api-gateway`
- `common-web-lib`
- `config-service`
- `frontdesk-service`
- otros servicios PMS

`api-gateway`, `common-web-lib` y `config-service` tienen configuración estándar de pruebas JUnit mediante `useJUnitPlatform()`.

### Componentes AI / intent router encontrados

No existen componentes de intent routing dentro del alcance solicitado:

- `api-gateway`: no contiene `LocalIntentRouter`, `LocalIntent` ni `DeterministicParser`.
- `common-web-lib`: no contiene componentes AI o de intent routing.
- `config-service`: no contiene componentes AI o de intent routing.

Los componentes reales están en `frontdesk-service`:

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntent.java`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/DeterministicParser.java`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ConversationSession.java`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ConversationSessionStore.java`

`LocalIntentRouter` es un componente Spring (`@Component`) que recibe `hotelId`, identidad, roles y una solicitud de conversación. Usa `DeterministicParser` y mantiene sesiones conversacionales.

### Suite de pruebas existente

La prueba buscada sí existe:

- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java`

La clase está declarada como:

```java
class LocalIntentRouterTest
```

También existen pruebas relacionadas:

- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/DeterministicParserTest.java`
- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/BatchCheckInParserTest.java`
- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/AssistantServiceTest.java`
- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/AssistantToolCatalogTest.java`

La prueba `LocalIntentRouterTest` usa JUnit 5, Mockito y AssertJ. Construye directamente un `LocalIntentRouter` con dependencias simuladas para probar el fallback determinista y los flujos de conversación.

### Evidencia de compilación previa

Existen artefactos generados previamente relacionados con la prueba:

- `frontdesk-service/build/classes/java/test/.../LocalIntentRouterTest.class`
- `frontdesk-service/build/test-results/test/TEST-com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest.xml`
- reportes JaCoCo para `LocalIntentRouter`

Esto demuestra que el proyecto ha compilado o ejecutado anteriormente esa clase, pero no constituye una ejecución nueva en esta A.SPEC.

## Verificación solicitada

Se ejecutó:

```text
./gradlew projects
```

Resultado observado:

```text
java.io.FileNotFoundException:
.../.gradle/wrapper/dists/gradle-9.3.1-bin/.../gradle-9.3.1-bin.zip.lck
(Operation not permitted)
```

La verificación quedó bloqueada por permisos del caché/distribución local de Gradle. No se modificó el repositorio para resolverlo.

## Estado de hallazgos

| Hallazgo | Clasificación |
|---|---|
| La A.SPEC fue tratada como solo lectura | PASS |
| No se modificaron archivos ni configuración | PASS |
| `LocalIntentRouterTest` existe | PASS |
| El router y sus interfaces están en `frontdesk-service` | PASS |
| No hay router AI dentro de `api-gateway`, `common-web-lib` o `config-service` | PASS |
| El alcance declarado no incluye el módulo que contiene el router real | WARNING |
| `./gradlew projects` pudo ejecutarse correctamente | BLOCKER |
| Ejecución actual de `LocalIntentRouterTest` verificada | UNKNOWN |
| Pruebas verdes actuales confirmadas | UNKNOWN |

## Inferencias

- El fallo de AI-0016 probablemente se relaciona con ejecutar la prueba desde el módulo o ruta incorrectos: `LocalIntentRouterTest` pertenece a `frontdesk-service`, no a `api-gateway`, `common-web-lib` ni `config-service`.
- El comando esperado para aislar esa prueba sería conceptualmente `:frontdesk-service:test --tests ...LocalIntentRouterTest`, pero no se ejecutó porque la A.SPEC solo autoriza `./gradlew projects` como comando de verificación.
- Los artefactos existentes no prueban el estado actual de la prueba ni sustituyen una ejecución reproducible.

## Conclusión

**PASS parcial / WARNING operativo**

La ubicación del router y de la suite existente quedó identificada: ambos están en `frontdesk-service`. Dentro de los tres módulos especificados no existe el componente buscado. La verificación de proyectos Gradle no pudo completarse por un bloqueo de permisos en el caché local de Gradle.
