# Adopción de RepairShop como nuevo Fixi

## Decisión

Por instrucción del propietario, la base completa de RepairShop pasa a ser el nuevo producto Fixi en `codex/fixi-repairshop`. No es un frontend para el backend Spring anterior: incluye su propio backend .NET, esquema PostgreSQL, autenticación y frontend.

## Archivo del producto anterior

- Checkout conservado: `D:\Projects\hotel-pms-mexico`.
- Rama conservada: `codex/fixi-monolith`.
- Base remota al iniciar la adopción: `15761a0e1f3d22b5b689467c7db636584bb8d257`.
- HEAD local de ese checkout: `2b885ae57ad9007b09e2d9bf9ecb1621ae5fd973`, con sus cambios locales preexistentes.
- El historial anterior sigue siendo ancestro de esta rama; no se reescribió ni fusionó en main.
- Sus cambios no rastreados y stash permanecen en el checkout anterior. No forman parte de esta importación.
- Producción anterior continúa operando; no se ha realizado el corte.

## Incorporado

Backend completo, migraciones EF Core, frontend completo, órdenes/diagnóstico/presupuestos/QA/garantías, clientes/equipos, inventario/compras/proveedores/transferencias, POS/caja, reportes, portal, mensajes/outbox, usuarios/sucursales, suscripciones, pruebas y CI upstream. Marca visible Fixi en la UI, PWA, invitaciones y descripciones de suscripción. Los identificadores técnicos `RepairShop.*`, issuer JWT y propósitos criptográficos se conservan deliberadamente.

## Diferencias que impiden un corte ciego de producción

1. No existe migración de datos desde Spring/Flyway a EF Core. No apuntar esta aplicación a la base anterior ni copiar hashes/grants automáticamente.
2. El modelo upstream tiene Admin/Tech/Reception/Cashier. No hereda el SUPER_ADMIN tenantless ni `platform_admin_grant` anterior; se requiere decidir/validar la administración global antes del corte.
3. La fiscalidad ARCA/AFIP, CUIT, textos legales, valores regionales y cobros argentinos son funcionalidad upstream; no equivalen a CFDI ni a condiciones legales/comerciales de México. No se reemplazan con una implementación ficticia.
4. Signup admite México; verificar moneda operativa, zona horaria, precios y canales reales antes de habilitar altas comerciales. No se cambiaron importes ni conversiones masivamente.
5. Los proveedores simulados son exclusivos de CI/demo. Ninguna prueba demo acredita entrega real de email/WhatsApp ni pagos.
6. Configurar explícitamente integraciones, frontend/backend del nuevo target, almacenamiento, backups y rollback antes de un despliegue autorizado. No reutilizar el proxy Vercel del backend Spring.

## Validación

La fuente de verdad es el workflow `Fixi RepairShop CI`: backend completo con PostgreSQL efímero, frontend lint/types/unit/build, imágenes/Compose y browser E2E contra .NET real. Consultar el resultado terminal para el SHA correspondiente; la presencia de pruebas no demuestra que pasaron.
