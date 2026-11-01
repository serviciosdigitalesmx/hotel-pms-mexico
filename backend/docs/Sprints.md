# Sprints (14 dÃ­as) â€” RepairShop

Este repo ya incluye el cÃ³digo implementado para **Sprint 1 + Sprint 2**.

Las tareas originales estÃ¡n en `docs/repairshop_project1_tasks.csv`.

## Sprint 1 â€” Foundations + Auth + CRUD base

- **RS-001** â€” Create solution + projects structure (2 pts)
  _Epic:_ Foundations
  _AC:_ La soluciÃ³n compila; proyectos referenciados correctamente; estructura de carpetas creada.

- **RS-002** â€” Add docker-compose with PostgreSQL (2 pts)
  _Epic:_ Foundations
  _AC:_ docker compose up levanta Postgres; puerto expuesto; datos persisten en volumen.

- **RS-003** â€” Configure EF Core + DbContext (3 pts)
  _Epic:_ Foundations
  _AC:_ DbContext conecta; se puede ejecutar migration; app arranca sin errores.

- **RS-004** â€” Initial migration + database creation (2 pts)
  _Epic:_ Foundations
  _AC:_ Migration creada; database creada con tablas iniciales; comando documentado.

- **RS-005** â€” Global error handling (ProblemDetails) (3 pts)
  _Epic:_ API Quality
  _AC:_ Errores devuelven ProblemDetails con status, title, detail, traceId.

- **RS-006** â€” User model + password hashing (3 pts)
  _Epic:_ Auth & Security
  _AC:_ Se puede crear/verificar password; no se guardan passwords en texto plano.

- **RS-007** â€” JWT issuing endpoint (/auth/login) (3 pts)
  _Epic:_ Auth & Security
  _AC:_ Login correcto devuelve token; login invÃ¡lido devuelve 401 con ProblemDetails.

- **RS-008** â€” Authorize + role policies (2 pts)
  _Epic:_ Auth & Security
  _AC:_ Endpoints protegidos requieren JWT; reglas de rol funcionan.

- **RS-009** â€” Seed admin/tech users (2 pts)
  _Epic:_ Auth & Security
  _AC:_ Al levantar, existen usuarios seed y se puede loguear con ambos.

- **RS-010** â€” Swagger + Bearer authentication (2 pts)
  _Epic:_ DevEx & Docs
  _AC:_ Swagger permite Authorize con token; endpoints muestran cÃ³digos/DTOs.

- **RS-011** â€” Customer entity + configuration (2 pts)
  _Epic:_ Customers
  _AC:_ Tabla Customer creada; restricciones de longitudes/required aplicadas.

- **RS-012** â€” CustomersController CRUD (5 pts)
  _Epic:_ Customers
  _AC:_ CRUD funciona; status codes correctos; validaciÃ³n devuelve 400 con detalles.

- **RS-013** â€” Customer DTOs + validation (2 pts)
  _Epic:_ Customers
  _AC:_ Errores de validaciÃ³n consistentes; campos obligatorios validados.

- **RS-014** â€” Device entity + relation to Customer (3 pts)
  _Epic:_ Devices
  _AC:_ Tabla Device creada; FK enforce; no permite device sin customer.

- **RS-015** â€” DevicesController CRUD (5 pts)
  _Epic:_ Devices
  _AC:_ CRUD funciona; valida customer existente; devuelve 404 si customer no existe.

- **RS-016** â€” RepairOrder entity (base fields) (3 pts)
  _Epic:_ Orders
  _AC:_ Tabla RepairOrder creada; default status Entered; FK a Customer y Device.

- **RS-017** â€” OrdersController basic CRUD (no workflow yet) (5 pts)
  _Epic:_ Orders
  _AC:_ Crear orden exige customer+device existentes; status inicial Entered; list funciona.


## Sprint 2 â€” Workflow de estados + notas/adjuntos + tests + docs

- **RS-018** â€” Order status history entity (3 pts)
  _Epic:_ Orders
  _AC:_ Al cambiar estado se inserta una fila en history; migraciÃ³n aplicada.

- **RS-019** â€” Implement status transition service (5 pts)
  _Epic:_ Orders
  _AC:_ Reglas aplicadas: Ready requiere diagnosis+bujet; Delivered requiere Ready; InProgress requiere approved.

- **RS-020** â€” Endpoint POST /orders/{id}/status (3 pts)
  _Epic:_ Orders
  _AC:_ Endpoint cambia status vÃ¡lido; invalid transitions devuelven 400/409 con ProblemDetails.

- **RS-021** â€” Order notes: entity + endpoints (3 pts)
  _Epic:_ Orders
  _AC:_ Se pueden agregar notas; guardan createdBy/createdAt; list devuelve ordenadas.

- **RS-022** â€” Order attachments (links): entity + endpoints (3 pts)
  _Epic:_ Orders
  _AC:_ Adjuntos guardan URL vÃ¡lida; URL invÃ¡lida devuelve 400.

- **RS-023** â€” Consistent response contracts + status codes (2 pts)
  _Epic:_ API Quality
  _AC:_ Todos endpoints usan patrones consistentes; swagger refleja respuestas.

- **RS-024** â€” Unit tests for status rules (set 1) (5 pts)
  _Epic:_ Testing
  _AC:_ MÃ­nimo 6 tests pasan; no tocan DB (testean services/domain).

- **RS-025** â€” Unit tests for history + invariants (set 2) (5 pts)
  _Epic:_ Testing
  _AC:_ MÃ­nimo 6 tests adicionales pasan; total 12+ tests.

- **RS-026** â€” README pro + curl examples (3 pts)
  _Epic:_ DevEx & Docs
  _AC:_ Un tercero puede levantar el proyecto en < 10 min siguiendo README.

- **RS-027** â€” Postman/HTTP file for manual testing (optional) (2 pts)
  _Epic:_ DevEx & Docs
  _AC:_ Requests cubren login + CRUD + status transition + notes/attachments.

- **RS-028** â€” CI pipeline (optional) - build + test (2 pts)
  _Epic:_ DevEx & Docs
  _AC:_ Pipeline pasa en repo; falla si tests fallan.
