# CAMRA SYSTEM SPEC

## 1. Features y módulos
El sistema está compuesto por los siguientes submódulos (microservicios):
- **api-gateway**: Spring Cloud Gateway para enrutamiento, rate limiting (Redis) y seguridad.
- **auth-service**: Autenticación, generación de JWT y manejo de usuarios.
- **billing-service**: Facturación, generación de PDFs (facturas).
- **config-service**: Spring Cloud Config server para configuración centralizada.
- **fb-service**: Alimentos y bebidas (Food & Beverage).
- **frontdesk-service**: Módulo principal consolidado (antes inventory, reservation, stay). Maneja habitaciones, tarifas, reservaciones, estancias (stays), integraciones de WhatsApp (Evolution API) y Asistente IA (Ollama/Deepseek/Groq).
- **guest-service**: Gestión de huéspedes, privacidad (GDPR/retención), documentos de identidad.
- **notification-service**: Envío de correos electrónicos transaccionales.
- **frontend**: SPA en React.

## 2. Flujos de trabajo
- **Reservación**: Un usuario crea una reserva, se notifica vía correo/WhatsApp.
- **Check-in/Check-out**: Manejo de estado de la estancia.
- **Facturación**: Al hacer check-out o durante la estancia, se generan cargos y se emite la factura (PDF).
- **Asistencia IA**: Recepción de mensajes vía WhatsApp, parseo de intenciones (LocalIntentRouter, DeterministicParser), consulta al LLM (Ollama/Qwen3) y respuesta.

## 3. Entidades y modelo de datos (Hechos comprobados)
- **Guest**: `Guest`, `GuestPrivacySettings`, `IdentityDocument`.
- **Frontdesk**: `Stay`, `Reservation`, `Room`, `RoomType`, `RateSeason`, `Quotation`, `HotelSettings`.
- **Billing**: Facturas, ítems de factura.
- **Auth**: Usuarios, Roles.

## 4. Reglas de negocio
- **Aislamiento**: Cada hotel tiene su propio `hotelId` (tenant ID) que se extrae del contexto de seguridad (JWT) y se aplica a las consultas (TenantIsolationArch).
- **Precios**: Se manejan por `RateSeason` y calendarios de precios (`RateCalendar`).

## 5. Estados y transiciones
- **Estancias**: `StayStatus` (por comprobar valores exactos, usualmente BOOKED, CHECKED_IN, CHECKED_OUT, CANCELLED).
- **Facturas**: Borrador, Emitida, Cancelada.

## 6. Roles y permisos
- **ADMIN / OWNER**: Pueden configurar WhatsApp y acceder a configuraciones a nivel tenant.
- **RECEPTIONIST**: Operación diaria (implícito en Spring Security).
- Se utiliza `PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")`.

## 7. Pantallas y navegación
- Aplicación React (Frontend) ubicada en `/frontend`.

## 8. Backend, servicios y APIs
- Arquitectura de Microservicios basada en Spring Boot 3.
- Comunicación interna aparentemente mediada por Feign (`InternalFeignAuthInterceptor`) y seguridad por HMAC.

## 9. Arquitectura general
- **Edge**: `api-gateway` expuesto en puerto 8080. Frontend Nginx en puerto 80.
- **Seguridad perimetral**: JWT verificado en el gateway.
- **Seguridad interna**: Firma HMAC para requests entre microservicios (T-GW-08, InternalAuthFilter) + Nonce en Redis para anti-replay.
- **Base de Datos**: PostgreSQL compartida pero separada en esquemas lógicos/bases de datos por microservicio (`hotel_auth`, `hotel_guest`, `hotel_frontdesk`, `hotel_billing`, `hotel_fb`).

## 10. Multi-tenancy y aislamiento de datos
- Verificado mediante pruebas arquitectónicas (`TenantIsolationArchTest.java`).
- El `hotelId` se extrae del JWT y se inyecta en el contexto para filtrar todas las operaciones de la base de datos (Hibernat filtros / Aspectos).

## 11. Autenticación y seguridad
- JWT para clientes externos.
- HMAC (con Nonce en Redis) para tráfico service-to-service.
- Módulo compartido: `internal-auth-lib`.

## 12. Automatizaciones
- Retención y purgado de datos (GDPR): `GuestRetentionJobServiceImpl`.

## 13. Integraciones externas
- SMTP para correos (`notification-service`).
- Alloggiati Web (Portal de policía de Italia): `V3__add_alloggiati_failure_tracking.sql`.

## 14. WhatsApp y Evolution API
- **ESTADO**: IMPLEMENTADO (Parcialmente)
- Código: `EvolutionService.java`, `WhatsAppController.java`.
- Se gestiona el estado de conexión y se puede generar un código QR (`/api/v1/stays/whatsapp/connect`) para emparejar la cuenta del hotel.

## 15. Sistema de inteligencia artificial
- **ESTADO**: IMPLEMENTADO
- Código: `AssistantService`, `LocalIntentRouter`, `ResilientIntentFactory`.
- Modelos implementados a través de migraciones de DB: Deepseek (`V21__migrate_ai_to_deepseek.sql`), Ollama (`V22`), Qwen3-4b (`V23__use_qwen3_4b_instruct_q4.sql`).
- Proveedores de LLM: `OllamaLlmProvider.java`, `DeepseekLlmProvider.java`, `GroqLlmProvider.java`.

## 16. Herramientas/tools disponibles para la IA
- **ESTADO**: PARCIAL/PLANEADO
- Parseo de Check-in en lotes (`BatchCheckInParser.java`).
- Enrutamiento de intenciones (`DeterministicParser.java`, `LocalIntentRouter.java`).

## 17 a 20. IA: Consultas, Acciones, Confirmaciones y Configuración
- La IA está configurada por tenant (`V17__add_tenant_ai_assistant.sql`).
- La interacción se procesa a través de los IntentParsers, que analizan el texto para ejecutar acciones predefinidas (Check-In).
- Las confirmaciones humanas o "Human-in-the-loop" requieren revisión de los objetos DTO generados antes de persistir.

## 21. Base de datos y persistencia
- PostgreSQL (contenedor `postgres`), respaldado vía `pgBackRest` (local e incremental).
- Flyway para migraciones de BD (archivos `V*__*.sql` en `src/main/resources/db/migration/`).

## 22. Observabilidad, auditoría y logs
- Stack: Loki (logs), Grafana (dashboards), Prometheus (métricas), Zipkin (trazas distribuidas).
- Alertas mediante Alertmanager.

## 23. Resiliencia: retries, circuit breakers, rate limits y manejo de errores
- Rate limiting en API Gateway respaldado por Redis.
- Manejadores globales de excepciones (`GlobalExceptionHandler.java`) para estandarizar las respuestas de error en todos los servicios.

## 24. Tests y mecanismos utilizados
- E2E Tests (Playwright en `frontend/e2e`).
- ArchUnit para comprobación de aislamiento (TenantIsolation).
- JUnit/Mockito para tests de servicios y controladores.

## 25. Infraestructura y despliegue
- `docker-compose.yml` para despliegue en producción.
- `docker-compose.config-native.yml` y otros para Native images (GraalVM).
- CI/CD scripts en `/scripts/ci`.

## 26. Funcionalidad parcial, pendiente o incompleta
- WhatsApp y Evolution API están en proceso de integración completa con la IA (los controladores y servicios existen, pero la orquestación completa de intenciones a partir de webhooks puede estar parcial).

## 27. Código legado, duplicado o contradictorio
- Se ha unificado los servicios de `inventory`, `reservation` y `stay` en `frontdesk-service` (mencionado en `docker-compose.yml`).

## 28. Dependencias importantes entre módulos
- `api-gateway` depende de todos.
- `frontdesk-service` depende de `guest-service` para datos de huéspedes.
- `fb-service` depende de `frontdesk-service` y `billing-service` para cargar cargos a habitaciones y facturar.
- Todos dependen de `config-service` y `internal-auth-lib`.

---
*Nota: Este documento ha sido generado con base en la inspección de la estructura de código real.*
