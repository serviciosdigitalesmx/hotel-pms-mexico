# Fixi Android

Cliente Android nativo de Fixi. Consume exclusivamente el API Gateway existente; no contiene backend, base de datos ni reglas de negocio propias.

## Entornos

- `local`: `http://10.0.2.2:8080/` para el emulador Android y el stack local.
- `debugApi`: mismo endpoint local, configurable en `app/build.gradle.kts`.
- `releaseApi`: `https://api.fixi.mx/` como endpoint de producción.

La sesión usa cookies persistentes de OkHttp; las contraseñas no se almacenan. El backend sigue siendo la autoridad para tenant, branch, roles y capabilities.
