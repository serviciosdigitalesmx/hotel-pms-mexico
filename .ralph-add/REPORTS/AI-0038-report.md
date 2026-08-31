# A.SPEC AI-0038 — Diagnóstico de `UserManagementServiceImplTest`

## Resultado

**Clasificación general: BLOCKER**

Los cinco fallos se explican por una desincronización entre `UserManagementServiceImpl` y los stubs usados por `UserManagementServiceImplTest`. La implementación usa métodos `IncludingInactive`, mientras que las pruebas continúan configurando y verificando los métodos antiguos.

## Evidencia observada

### 1. Fallos en listado de usuarios — BLOCKER

`UserManagementServiceImpl.listUsers()` invoca:

```java
userRepository.findAllByHotelIdIncludingInactive(hotelId)
```

Observado en `UserManagementServiceImpl.java:38-43`.

Las dos pruebas de listado configuran y, en un caso, verifican:

```java
userRepository.findAllByHotelId(HOTEL_ID)
```

Observado en `UserManagementServiceImplTest.java:87`, `95` y `100`.

Consecuencia inferida:

- `findAllByHotelIdIncludingInactive(...)` no tiene stub y Mockito devuelve `null`.
- La llamada a `.stream()` puede provocar `NullPointerException`, o la prueba no recibe el resultado esperado.
- El `verify` de `findAllByHotelId(...)` tampoco corresponde a la implementación actual.

**Clasificación: BLOCKER**

### 2. Fallos en creación de usuarios — BLOCKER

`UserManagementServiceImpl.createUser()` invoca:

```java
existsByUsernameIncludingInactive(request.username())
existsByEmailIncludingInactive(request.email())
```

Observado en `UserManagementServiceImpl.java:48-54`.

Las tres pruebas de creación configuran los métodos antiguos:

```java
existsByUsername(...)
existsByEmail(...)
```

Observado en `UserManagementServiceImplTest.java:110-111`, `126`, `136-137`.

El repositorio confirma que ambos grupos de métodos existen actualmente:

- Métodos antiguos globales: `existsByUsername`, `existsByEmail`.
- Métodos nuevos que incluyen cuentas inactivas: `existsByUsernameIncludingInactive`, `existsByEmailIncludingInactive`.

Observado en `UserAccountRepository.java:44-104`.

Consecuencias inferidas:

- Los stubs de `existsByUsername(...)` y `existsByEmail(...)` no son consumidos por la implementación.
- Bajo Mockito strict stubbing, esos stubs generan `UnnecessaryStubbingException`.
- Las llamadas `IncludingInactive` no están configuradas; para métodos booleanos Mockito devuelve `false` por defecto.
- Las pruebas que esperan `DuplicateResourceException` pueden continuar indebidamente hacia la creación del usuario y fallar sus aserciones.

**Clasificación: BLOCKER**

### 3. Strict Mockito — PASS respecto al diagnóstico

La prueba está anotada con:

```java
@ExtendWith(MockitoExtension.class)
```

Observado en `UserManagementServiceImplTest.java:34`.

La prueba usa `when(...)` para configurar métodos que ya no son invocados por el servicio. Con la configuración strict predeterminada de `MockitoExtension`, los stubs no utilizados producen `UnnecessaryStubbingException`.

**Clasificación: PASS**

Esto confirma que el error de strict stubbing es consistente con la discrepancia de nombres de métodos observada.

### 4. DTOs y mapeo — PASS

`CreateUserRequest` contiene los campos usados por la implementación:

```java
username, password, email, role
```

Observado en `CreateUserRequest.java:16-26`.

`UserResponse` contiene los campos que `toResponse(...)` construye:

```java
id, username, email, role, active, mustChangePassword, createdAt
```

Observado en `UserResponse.java:14-22` y `UserManagementServiceImpl.java:156-164`.

No se observa una incompatibilidad entre DTOs, entidad y el mapeo privado `toResponse(...)`.

**Clasificación: PASS**

### 5. Entidad usada en las aserciones — PASS

Las pruebas inicializan `activeUser` e `inactiveUser` con `role`, `active`, `mustChangePassword`, `id`, `username`, `email` y `hotelId`.

La implementación lee esos mismos campos para construir `UserResponse` y modificar el estado de la cuenta.

Observado en `UserManagementServiceImplTest.java:60-82` y `UserManagementServiceImpl.java:56-68`, `83-99`, `156-164`.

No se observa que los fallos principales provengan de DTOs, mappers o de la entidad.

**Clasificación: PASS**

## Mapeo de los cinco fallos esperados

| Área | Pruebas afectadas | Causa observada |
|---|---:|---|
| Listado | 2 | Stub/verificación de `findAllByHotelId`, pero el servicio usa `findAllByHotelIdIncludingInactive` |
| Creación | 3 | Stubs de `existsByUsername`/`existsByEmail`, pero el servicio usa las variantes `IncludingInactive`; strict stubbing detecta stubs no usados |

## Inferencia de causa raíz

La causa raíz más probable es que la implementación fue actualizada para incluir usuarios inactivos, pero `UserManagementServiceImplTest.java` conserva stubs y verificaciones de la API anterior del repositorio.

Esta conclusión es una **INFERENCE** basada en la coincidencia directa entre:

- Métodos invocados por la implementación.
- Métodos configurados por las pruebas.
- Excepción reportada: `UnnecessaryStubbingException`.
- Aserciones esperadas en las pruebas.

## Alcance y cambios realizados

**OBSERVED:**

- No se modificaron archivos.
- No se ejecutaron pruebas ni comandos de build.
- No se modificaron Git, bases de datos, Redis, Docker, migraciones ni secretos.
- El repositorio ya contenía cambios y archivos no rastreados preexistentes; permanecieron intactos.

## Acción correctiva objetivo

Actualizar únicamente `UserManagementServiceImplTest.java` para que sus stubs y verificaciones correspondan a:

```java
findAllByHotelIdIncludingInactive(...)
existsByUsernameIncludingInactive(...)
existsByEmailIncludingInactive(...)
```

No se recomienda modificar `UserManagementServiceImpl.java` con base en esta inspección, porque sus llamadas coinciden con los métodos existentes en `UserAccountRepository` y preservan la inclusión de usuarios inactivos y la unicidad global documentada.
