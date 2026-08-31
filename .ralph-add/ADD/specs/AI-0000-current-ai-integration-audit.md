# A.SPEC AI-0000 — Audit current AI integration

Mode: READ-ONLY AUDIT
Mutation authorization: NONE
Deployment authorization: NONE

## WHY
Antes de construir el AI Gateway debemos conocer exactamente como consume IA el Hotel PMS actualmente.

## WHAT
Inventariar toda integración IA existente sin modificar absolutamente nada.

## SCOPE
- Java / Spring Boot
- Next.js / React / TypeScript
- Docker Compose
- Ollama
- Groq
- Gemini
- OpenAI
- OpenRouter
- Anthropic
- otros proveedores encontrados
- modelos configurados
- nombres de variables de entorno, nunca valores
- AssistantController / AssistantService / routers / tools
- SDKs y clientes HTTP
- timeouts
- retries
- manejo HTTP 429
- manejo HTTP 5xx
- fallback
- circuit breaker
- rate limiter
- métricas y logging

## OUT OF SCOPE
- NO modificar código
- NO modificar configuración
- NO instalar dependencias
- NO modificar PostgreSQL
- NO modificar Redis
- NO ejecutar migraciones
- NO modificar Flyway
- NO reiniciar ni recrear containers
- NO realizar peticiones reales a proveedores IA
- NO leer ni imprimir valores secretos
- NO commit
- NO push
- NO reset
- NO clean
- NO stash
- NO deploy

## REQUIRED OUTPUT
Crear .ralph-add/REPORTS/AI-0000-report.md

El reporte debe cubrir:
1. Proveedores IA encontrados.
2. Microservicios que los consumen.
3. Archivos responsables.
4. SDK o protocolo utilizado.
5. Modelos configurados.
6. Variables requeridas, SOLO NOMBRES.
7. Flujo actual de una petición IA.
8. Timeouts actuales.
9. Retries actuales.
10. Manejo de 429.
11. Manejo de errores 5xx.
12. Fallback existente.
13. Circuit breaker existente.
14. Rate limiter existente.
15. Métricas y logging.
16. Riesgos detectados.
17. Change Surface probable de AI-0001.
18. Propuesta arquitectónica inicial.

Clasificar cada punto como PASS, WARNING, BLOCKER o UNKNOWN.

## SECURITY INVARIANTS
- Never print API key values.
- Never print secret values.
- Never copy .env contents into reports.
- Never test leaked or historical credentials.
- Never send requests to external AI providers.
- Never mutate PMS source code.
- Never mutate PostgreSQL.
- Never mutate Redis.
- Never mutate Docker volumes.
- Never modify an applied Flyway migration.
- Never destroy or hide uncommitted work.
- Tenant isolation must remain untouched.

## REQUIRED SEARCH TERMS
AI, LLM, Assistant, Groq, Gemini, Google GenAI, OpenAI, OpenRouter,
Anthropic, Ollama, generateContent, generate_content, chat.completions,
apiKey, API_KEY, 429, rate limit, retry, backoff, timeout,
circuit breaker, resilience4j, WebClient, RestClient, RestTemplate, Feign.

## TARGET ARCHITECTURE TO EVALUATE
PMS / Assistant
-> AI Gateway
-> Provider Registry
-> Rate Limiter
-> Retry + Backoff + Jitter
-> Circuit Breaker
-> Authorized Provider Failover
-> Usage / Metrics
-> Secret Provider

Failover must not rotate accounts for the purpose of evading provider quotas.

## DEFINITION OF DONE
- Integraciones IA inventariadas.
- Providers y modelos identificados.
- Variables identificadas sin valores.
- Flujo actual documentado.
- 429 / 5xx / retry / timeout / fallback documentados.
- Riesgos clasificados.
- AI-0001 Change Surface propuesto.
- Ningún secret revelado.
- Ningún archivo del PMS modificado.
- Ninguna petición externa realizada.
- Reporte generado.

Completion signal: <promise>COMPLETE</promise>
