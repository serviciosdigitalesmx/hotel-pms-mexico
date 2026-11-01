# Fixi — nueva base RepairShop

Lee y aplica primero el AGENTS.md global de Codex del usuario.

- Este checkout es el nuevo Fixi: React/TypeScript/Vite y ASP.NET Core 8/PostgreSQL.
- Rama de adopción: `codex/fixi-repairshop`. No modificar `main` ni `codex/fixi-monolith`.
- El Fixi anterior permanece en `D:\Projects\hotel-pms-mexico`; no limpiar sus cambios, untracked ni stash.
- GitHub Actions (`.github/workflows/ci.yml`) es la validación autoritativa. No ejecutar builds/tests locales de esta adopción. Se permite una vista de desarrollo local conectada exclusivamente a la base demo aislada.
- No usar la base, credenciales, cookies, tokens ni SMTP de Fixi anterior. Las migraciones EF Core no se ejecutan sobre PostgreSQL de producción existente.
- No desplegar ni cambiar VPS/Vercel/DNS hasta una autorización explícita para el corte del nuevo producto.
- Preservar autorización server-side, aislamiento de organización/sucursal, reglas de órdenes, control de stock y rotación de sesión. No suponer equivalencia con `platform_admin_grant` del sistema anterior.
- Conservar procedencia y permiso documentados en `docs/UPSTREAM_AUTHORIZATION.md`. No atribuir una licencia pública que el upstream no haya publicado.
- Nunca incorporar `.env`, uploads, dumps, paquetes descargados ni material local de agentes.
