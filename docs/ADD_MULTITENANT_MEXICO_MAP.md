# ADD — Mapa de Multitenancy Total y Desitalianización del PMS

## Objetivo

Convertir el PMS en una plataforma mexicana con aislamiento completo por hotel
(tenant). Ninguna lectura, mutación, caché, mensaje, documento, integración o
pantalla podrá usar datos de otro hotel. Las dependencias regulatorias italianas
deben retirarse o quedar aisladas y apagadas; no se reemplazan por reglas SAT/CFDI
inventadas.

Este documento es el contrato de trabajo para la siguiente fase. No autoriza
borrados masivos ni cambios de esquema sin migración, pruebas y una decisión
fiscal mexicana verificable.

---

## Bloque B — Matriz de trazabilidad multitenant verificada

Cada fila está respaldada por evidencia real (archivo:línea). Los ítems sin localizar
se marcan explícitamente como **pendiente de localizar**.

### Endpoints y repositorios por servicio

| Servicio | Endpoint / Evento | Recurso | Origen de `hotelId` | DB Tabla / Repo | `hotel_id NOT NULL` | `(id, hotel_id)` | Vulnerabilidad / Hueco | Prioridad | Evidencia (archivo:línea) |
|---|---|---|---|---|---|---|---|---|---|
| `api-gateway` | `AuthenticationFilter` — extrae claim JWT | JWT claims | JWT claim `hotelId` | – | – | – | Auditar: ¿qué pasa si falta el claim? | P0 | `api-gateway/.../filter/AuthenticationFilter.java` |
| `api-gateway` | `PublicBookingFilter` — resuelve slug server-side | Slug → hotelId | Slug resuelto por `PublicHotelResolverController` vía llamada interna; strip de `X-Public-Hotel-Id` del cliente | – | – | – | Protegido: el gateway elimina header malicioso del navegador e inyecta hotel resuelto | P0 | `api-gateway/.../filter/PublicBookingFilter.java` |
| `auth-service` | `UserAccountRepository.findByIdAndHotelId` | User accounts | Request attribute del gateway | `user_account` | ✅ | ✅ (`:132`) | Ninguno detectado | P0 | `auth-service/.../repository/UserAccountRepository.java:132` |
| `frontdesk-service` | `ReservationRepository` queries | Reservations | Request attribute | `reservation` | ✅ (`:59`) | ✅ | Ninguno detectado | P0 | `frontdesk-service/.../reservations/domain/Reservation.java:59` |
| `frontdesk-service` | `Stay` entity CRUD | Stays | Request attribute | `stay` | ✅ (`:60`) | ✅ | Ninguno detectado | P0 | `frontdesk-service/.../stays/domain/Stay.java:60` |
| `frontdesk-service` | `HotelSettings` CRUD | Settings | JWT | `hotel_settings` | ✅ (`:51`) | ✅ (PK = hotel_id) | Ninguno detectado | P0 | `frontdesk-service/.../stays/domain/HotelSettings.java:51` |
| `frontdesk-service` | `Room` entity | Rooms | Request attribute | `room` | ✅ (`:53`) | ✅ | Ninguno detectado | P0 | `frontdesk-service/.../rooms/domain/Room.java:53` |
| `frontdesk-service` | `RoomType` entity | Room types | Request attribute | `room_type` | ✅ (`:54`) | ✅ | Unicidad `(hotel_id, name)` | P0 | `frontdesk-service/.../rooms/domain/RoomType.java:54` |
| `frontdesk-service` | `Quotation` entity | Quotations | Request attribute | `quotation` | ✅ (`:65`) | ✅ | Ninguno detectado | P0 | `frontdesk-service/.../quotations/domain/Quotation.java:65` |
| `frontdesk-service` | `RateSeason` entity | Rate seasons | Request attribute | `rate_season` | ✅ (`:54`) | ✅ | Ninguno detectado | P0 | `frontdesk-service/.../pricing/domain/RateSeason.java:54` |
| `billing-service` | `InvoiceServiceImpl.getInvoice(id)` | Invoices | `resolveHotelId()` + `findByIdAndHotelId()` | `invoice` | ✅ (`:67`) | ✅ (`:141`) | Protegido: `resolveHotelId()` antes de fetch | P0 | `billing-service/.../service/impl/InvoiceServiceImpl.java:140-141` |
| `billing-service` | `InvoiceController.getFatturaPAXml(id)` | FatturaPA XML | Recibe solo `id`; delega a `FatturaPAServiceImpl.generateXml` que llama `invoiceService.getInvoice` (tenant-safe) | `invoice` + `invoice_fiscal_export` | ✅ | ✅ (vía getInvoice) | Cadena verificada: `getInvoice` aplica `resolveHotelId` antes de pasar datos a FatturaPA | P0 | `billing-service/.../controller/InvoiceController.java:248` → `FatturaPAServiceImpl.java:118` → `InvoiceServiceImpl.java:140` |
| `billing-service` | `PaymentServiceImpl` | Payments | `resolveHotelId()` + `findByIdAndHotelId` | `payment` (vía invoice) | ✅ | ✅ | Ninguno detectado | P0 | `billing-service/.../service/impl/PaymentServiceImpl.java:46-47` |
| `billing-service` | `OwnerReportController` | Reports | `extractHotelId()` | `invoice` | ✅ | ✅ | Ninguno detectado | P0 | `billing-service/.../controller/OwnerReportController.java:49` |
| `guest-service` | Guest CRUD | Guests | Request attribute | `guest` | ✅ | ✅ | Ninguno detectado | P0 | `guest-service/.../model/Guest.java` |
| `fb-service` | Menu items, restaurant orders | F&B | Request attribute | Tablas fb | ✅ | **Pendiente de verificar** | Auditar repos y queries | P0 | Pendiente de localizar |
| `notification-service` | Notification CRUD | Notifications | Pendiente de verificar origen | Pendiente | Pendiente | Pendiente | Auditar controller y servicio | P1 | Pendiente de localizar |

### Jobs y schedulers (verificados)

| Servicio | Job / Scheduler | Tipo | Datos operativos | Filtro por tenant | Evidencia (archivo:línea) |
|---|---|---|---|---|---|
| `guest-service` | `GuestRetentionJobServiceImpl.runRetentionJob()` | `@Scheduled(cron = "0 0 2 * * *")` | Sí (tabla guest) | ✅ Agrupa por `hotelId` (línea 82-83) y aplica settings por hotel (línea 88) | `guest-service/.../service/impl/GuestRetentionJobServiceImpl.java:70-83` |
| **Otros servicios** | — | — | — | — | **No se encontró `@Scheduled` en frontdesk, billing, notification, auth, fb ni api-gateway** |
| **Docker Compose** | — | — | — | — | **No se encontraron entradas `cron` en docker-compose.yml** |
| **Kafka / RabbitMQ** | — | — | — | — | **No se encontró `@KafkaListener`, `KafkaTemplate`, `@RabbitListener` ni `RabbitTemplate` en ningún servicio** |

### Inventario de claves Redis (verificado)

| Servicio | Consumidor | Prefijo de clave | Construcción | Datos por tenant | TTL / Lock | Legacy / Compartido | Evidencia (archivo:línea) |
|---|---|---|---|---|---|---|---|
| `frontdesk-service` | `ConversationSessionStore` | `assistant:conversation:` | `SESSION_PREFIX + hotelId + ":" + sha256(userId)` | ✅ hotelId en la clave | TTL 10 min | No (tenant-scoped) | `frontdesk-service/.../assistant/engine/ConversationSessionStore.java:28,106-107` |
| `frontdesk-service` | `ConversationSessionStore` (lock) | `assistant:lock:` | `LOCK_PREFIX + hotelId + ":" + sha256(userId)` | ✅ hotelId en la clave | TTL 45 seg, Lua release script | No (tenant-scoped) | `ConversationSessionStore.java:29,93` |
| `auth-service` | `RefreshTokenServiceImpl` (blacklist) | `rt:blacklist:` | `PREFIX + jti` | ❌ **Sin hotelId** — clave global por JTI | TTL = remaining token lifetime | Compartido (global por JTI) — aceptable: JTI es UUID único global | `auth-service/.../service/impl/RefreshTokenServiceImpl.java:24,41` |
| `auth-service` | `RefreshTokenServiceImpl` (token version) | `user:tv:` | `TV_PREFIX + username` | ❌ **Sin hotelId** — clave global por username | TTL configurable | Compartido — **RIESGO si username no es único entre hoteles** | `RefreshTokenServiceImpl.java:27,58` |
| `internal-auth-lib` | `RedisNonceStore` (todos los servicios) | `internal-auth:nonce:` | `KEY_PREFIX + nonce` | ❌ **Sin hotelId** — nonce es UUID global | TTL configurable | Compartido (global por nonce) — aceptable: nonce es UUID único | `internal-auth-lib/.../security/RedisNonceStore.java:16,31` |
| `frontdesk-service` | `AlloggiatiCacheConfig` (Caffeine, NO Redis) | N/A (in-memory) | Caches: `CACHE_STATI`, `CACHE_COMUNI`, `CACHE_TIPDOC` | ❌ Sin tenant (datos globales de lookup italiano) | Sin TTL (permanent, in-memory) | Legacy italiano — datos de catálogo global | `frontdesk-service/.../stays/config/AlloggiatiCacheConfig.java:28-34` |

#### ✅ Verificado: `user:tv:{username}` es seguro bajo unicidad global actual

`username` es único globalmente en `user_account` (no solo por hotel), por lo que `user:tv:{username}` no puede colisionar entre hoteles. **Nota prospectiva:** si en el futuro se migra a usernames únicos solo por hotel, Redis deberá migrar a `user:tv:{hotelId}:{username}` **antes** de ese cambio.

### Variables Docker Compose para Alloggiati (verificadas)

| Variable | Línea en docker-compose.yml | Servicio | Descripción |
|---|---|---|---|
| `ALLOGGIATI_USERNAME` | 530 | `frontdesk-service` | Credencial portal Polizia di Stato |
| `ALLOGGIATI_PASSWORD` | 531 | `frontdesk-service` | Credencial portal |
| `ALLOGGIATI_WS_KEY` | 532 | `frontdesk-service` | Web Service key |
| `ALLOGGIATI_DRY_RUN` | 533 | `frontdesk-service` | Modo seco (default true) |
| `ALLOGGIATI_CREDENTIALS_ENCRYPTION_KEY` | 535 | `frontdesk-service` | AES key para cifrado at rest |
| `ALLOGGIATI_CREDENTIALS_ENCRYPTION_SALT` | 536 | `frontdesk-service` | Salt hex para cifrado |

**No existe `ALLOGGIATI_ENDPOINT`.**

---

## Bloque C — Residuos italianos confirmados (inventario por archivo)

### frontdesk-service (48 archivos Java con "Alloggiati")

| Categoría | Archivos verificados | Evidencia |
|---|---|---|
| **Dominio (entidades)** | `AlloggiatiComune.java`, `AlloggiatiStato.java`, `AlloggiatiTipdoc.java` | `.../stays/domain/` |
| **Repositorios** | `AlloggiatiComuneRepository.java`, `AlloggiatiStatoRepository.java`, `AlloggiatiTipdocRepository.java` | `.../stays/repository/` |
| **Servicios** | `AlloggiatiLookupService.java` (interfaz), `AlloggiatiLookupServiceImpl.java` (impl), `AlloggiatiReportService.java` (interfaz), `AlloggiatiReportServiceImpl.java` (impl), `AlloggiatiWebSenderService.java` (interfaz), `AlloggiatiWebSenderServiceImpl.java` (impl) | `.../stays/service/` y `.../stays/service/impl/` |
| **Controladores** | `AlloggiatiLookupController.java`, `StayController.java` (endpoints Alloggiati: downloadAlloggiatiReport L166, downloadAlloggiatiJson L192, submitAlloggiatiReport L213, getAlloggiatiFailureSummary L229) | `.../stays/controller/` |
| **Config** | `AlloggiatiCacheConfig.java`, `AlloggiatiCsvParser.java`, `AlloggiatiLookupDataLoader.java`, `AlloggiatiWebConfig.java` | `.../stays/config/` |
| **Seguridad** | `AlloggiatiCredentialEncryptor.java` — **Nota: también usado por `AssistantService` para descifrar `aiApiKeyEncrypted` por hotel, NO solo para credenciales Alloggiati** | `.../stays/security/AlloggiatiCredentialEncryptor.java` |
| **DTOs** | `AlloggiatiRowDto.java`, `AlloggiatiFailureSummaryResponse.java`, campos alloggiati* en `HotelSettingsRequest.java`, `HotelSettingsResponse.java`, `StayRequest.java`, `StayResponse.java` | `.../stays/dto/` |
| **Excepciones** | `AlloggiatiRowLimitExceededException.java`, `AlloggiatiValidationException.java` + handlers en `GlobalExceptionHandler.java` (L119-146) | `.../exception/` |
| **Dominio Stay** | `Stay.java` campos: `alloggiatiSent` (L144), `alloggiatiSendFailed` (L151), `alloggiatiLastError` (L161) | `.../stays/domain/Stay.java` |
| **Dominio HotelSettings** | Campos: `alloggiatiAutoSend` (L55), `alloggiatiUsername` (L141), `alloggiatiPasswordEncrypted` (L148), `alloggiatiWsKeyEncrypted` (L156), `hasAlloggiatiCredentials()` (L217) | `.../stays/domain/HotelSettings.java` |
| **Enums** | `TravellerType.java` — códigos TIPALLOG italianos | `.../stays/domain/TravellerType.java` |
| **StayGuest** | Referencia Alloggiati compliance (L29) | `.../stays/domain/StayGuest.java` |

### Migraciones frontdesk (verificadas)

| Migración | Contenido Alloggiati | Evidencia |
|---|---|---|
| `V1__frontdesk_baseline.sql` | Crea tablas `alloggiati_stati`, `alloggiati_comuni`, `alloggiati_tipdoc` | `.../db/migration/V1__frontdesk_baseline.sql` |
| `V3__add_alloggiati_failure_tracking.sql` | Agrega tracking de fallos Alloggiati en stays | `.../db/migration/V3__...sql` |
| `V4__add_alloggiati_credentials.sql` | Agrega credenciales cifradas por hotel | `.../db/migration/V4__...sql` |
| **`V10__add_quotations.sql`** | **NO es de Alloggiati** — es de cotizaciones | `.../db/migration/V10__add_quotations.sql` |

### billing-service (15 archivos Java con "Fattura")

| Categoría | Archivos verificados | Evidencia |
|---|---|---|
| **Servicio** | `FatturaPAService.java` (interfaz), `FatturaPAServiceImpl.java` (impl local, NO cliente externo) | `.../service/FatturaPAService.java`, `.../service/impl/FatturaPAServiceImpl.java` |
| **Validador** | `FatturaPaXsdValidator.java` — **está en `.../service/`, NO en `.../validation/`** | `.../service/FatturaPaXsdValidator.java` |
| **Controller** | `InvoiceController.java`: `getFatturaPAXml` (L246), `validateFatturaPAXml` (L271), batch export (L297) | `.../controller/InvoiceController.java` |
| **Dominio** | `DocumentType.java` (enum FATTURA/RICEVUTA), `SdiStatus.java`, `InvoiceFiscalExport.java`, `InvoiceSequence.java` | `.../domain/` |
| **DTOs** | `InvoiceResponse.java` (documentType, sdiStatus) | `.../dto/InvoiceResponse.java` |
| **Servicios impl** | `InvoiceServiceImpl.java` (refs FATTURA), `PaymentServiceImpl.java` (guard post-export), `PdfInvoiceServiceImpl.java` (template FATTURA), `VatBreakdownCalculator.java` | `.../service/impl/` |

### Tests Alloggiati existentes (verificados)

| Test | Archivo |
|---|---|
| `AlloggiatiReportServiceImplTest.java` | `frontdesk-service/src/test/java/.../stays/service/impl/` |
| `AlloggiatiWebSenderServiceImplTest.java` | `frontdesk-service/src/test/java/.../stays/service/impl/` |
| `AlloggiatiLookupServiceImplTest.java` | `frontdesk-service/src/test/java/.../stays/service/impl/` |
| `AlloggiatiLookupControllerTest.java` | `frontdesk-service/src/test/java/.../stays/controller/` |
| `AlloggiatiCsvParserTest.java` | `frontdesk-service/src/test/java/.../stays/config/` |
| `AlloggiatiComuneRepositoryIntegrationTest.java` | `frontdesk-service/src/test/java/.../integration/` |
| `FatturaPAServiceImplTest.java` | `billing-service/src/test/java/.../service/impl/` |
| `FatturaPaXsdValidatorTest.java` | `billing-service/src/test/java/.../service/` |

---

## Nota sobre AssistantService y AlloggiatiCredentialEncryptor

`AssistantService.java:16` importa `AlloggiatiCredentialEncryptor` y lo usa (L70) para **descifrar `aiApiKeyEncrypted` por hotel** — la clave de IA, no credenciales Alloggiati. Este uso **no debe tocarse** al retirar la integración italiana; es infraestructura de cifrado reutilizada. El nombre de la clase es un residuo de nomenclatura, pero la funcionalidad es operativa y vigente.
