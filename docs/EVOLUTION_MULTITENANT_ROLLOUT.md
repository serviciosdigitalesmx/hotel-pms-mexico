# Evolution y alta de hoteles

## Estado de esta primera etapa

- Ruta OWNER/ADMIN `/settings/whatsapp` y API `/api/v1/stays/whatsapp`.
- El backend toma el hotel del contexto autenticado HMAC/JWT. No acepta hotel ni instancia del cliente.
- Instancia Evolution reservada: `pms-{hotelId}`. Usar un despliegue Evolution dedicado al PMS para evitar colisiones con instancias ajenas.
- Clave global sólo en backend; respuestas limitadas a estado e imagen QR, con `Cache-Control: no-store`.
- Sin sincronización completa de historial, recibos de lectura, webhooks ni respuestas automáticas en esta etapa.
- Pendiente verificar con la versión instalada de Evolution y escanear desde el teléfono del hotel. No se considera vinculado por compilar ni por mostrar esta página.

## Configuración del operador

Configurar `EVOLUTION_BASE_URL` y `EVOLUTION_API_KEY` mediante secretos del entorno de frontdesk. No introducir claves en el chat, frontend ni repositorio. Reiniciar frontdesk después de configurar.
Evolution puede vivir en la infraestructura compartida con almacenamiento persistente propio. Su API administrativa debe permanecer en una red privada; en una instalación externa usar HTTPS. No publicar su clave global a los tenants.

Contrato de referencia: https://github.com/evolution-foundation/docs-evolution/blob/main/openapi/openapi-v2.json
Se usan `POST /instance/create`, `GET /instance/connect/{instance}` y `GET /instance/connectionState/{instance}`. Verificar que la versión desplegada devuelve `base64` PNG en la conexión; las variantes que sólo devuelven `code` necesitan un generador QR local antes de habilitar esta integración.

## Lo que significa crear otro hotel

La gestión actual de usuarios conserva el `hotelId` del administrador autenticado. Crear OWNER en esa pantalla no crea un tenant. ADMIN tampoco es un administrador global de plataforma.

Falta una administración de plataforma separada: alta de hotel con UUID nuevo, estado de aprovisionamiento, perfil/configuración y dueño invitado; activación sólo después de completar todas las etapas. Reintentos idempotentes deben recuperar altas parciales entre servicios. La invitación debe expirar y establecer la contraseña sin exponerla al operador.

Infraestructura compartida no significa datos compartidos. Cada hotel necesita usuarios, habitaciones, tarifas, disponibilidad, documentos, configuración, conversaciones, instancia Evolution y límites aislados. El soporte de un dueño con varios hoteles requeriría membresías y selección explícita de hotel; el modelo actual asigna un hotel por cuenta.

## Bot público: condiciones antes de activarlo

1. Autenticar eventos de Evolution y resolver hotel a partir de una vinculación almacenada y verificada. No confiar en hotelId enviado por el contacto.
2. Rechazar grupos, mensajes propios, eventos antiguos y duplicados; cola con idempotencia por hotel/instancia/messageId y reintentos acotados.
3. Adaptador de consultas públicas separado del asistente del personal. Sólo datos publicados del perfil, servicios, políticas, tarifas y disponibilidad real del hotel. No otorgar OWNER al contacto ni herramientas de huéspedes, finanzas o administración.
4. Los hechos de cada respuesta deben proceder de consultas al PMS. Si faltan datos, responder que no están confirmados y derivar a recepción. Un prompt por sí solo no garantiza esto.
5. Conversaciones y cachés con hotelId. Probar contactos iguales en dos hoteles, manipulación de instancia, instrucciones maliciosas y revocación de acceso.
6. Pausa automática al intervenir un humano, horarios configurables, límites por hotel, auditoría y política de retención de conversaciones.
7. Reservas o cobros requieren un flujo transaccional propio con validación de disponibilidad y confirmación; no activarlos implícitamente al vincular el número.

## Consolidación prioritaria

- Alta/suspensión/baja de tenants y recuperación de altas incompletas.
- Pruebas A/B de aislamiento de API, documentos, cachés, tareas asíncronas y WhatsApp.
- Backups con restauración probada y procedimiento de recuperación por hotel.
- Tarifas y disponibilidad como fuente única, concurrencia de reservas y zona horaria por hotel.
- Vinculación, reconexión y desconexión auditadas; rotación de secretos y límites de consumo.
- Diferenciar el bot público del asistente operativo y definir la información publicable.
