# Android para Fixi 4V

Evaluación inicial, 2026-11-01. No se declara un APK integrado ni probado.

## Candidata reutilizable

- Fixora: https://github.com/mr-kumuditha/fixora-mobile-app
- Snapshot inspeccionado: `224335a75813b5c3cdc7104682ee9f539da42338`.
- Clon aislado: `D:\Projects\reference-apps\fixora-android`.
- Licencia MIT presente: conservar copyright y permiso al reutilizar.
- Android nativo Kotlin/Compose, flujos de reparación para clientes, técnicos y administración.
- No es un cliente de RepairShop: usa Firebase Auth/Firestore, almacenamiento externo y pagos simulados. No conectar a servicios de terceros del autor ni copiar credenciales.
- Para reutilizarla deben adaptarse repositorios/auth/DTOs/subidas/estados al API .NET de Fixi, eliminar pagos simulados y probar roles/sucursal/tenant. La licencia permite reutilización, pero no prueba compatibilidad ni seguridad.

## Alternativas descartadas para adopción inmediata

- RepairStoreManager: README limita uso a educación/presentación; no se encontró autorización comercial equivalente.
- FixTrack: modelo LAN con servidor Windows/SQLite y activación propia; no se encontró licencia pública identificable. No es intercambiable con el backend SaaS .NET.

## Acceso Android disponible con la web

El frontend de Fixi 4V incluye manifest y service worker PWA. Usar el mismo sitio HTTPS desde Chrome/Android e instalarlo desde el navegador evita duplicar datos/autenticación. Esto es una PWA, no un APK ni una app nativa publicada. La instalación y recorrido en un dispositivo Android real aún deben comprobarse.
