# AE01 — Hotel-domain dependency audit

Date: 2026-10-04

This audit is an inventory only. It does not remove routes or tables before their
consumers and migration ownership are verified.

## Findings

| Area | Current evidence | Classification | Required follow-up |
|---|---|---|---|
| Frontend | `frontend/src/App.tsx` still mounts reservations, rooms, stays, housekeeping, hotel settings, and platform-hotels routes. | Legacy hotel surface | AE02 must remove or replace routes after FIXI navigation consumers are mapped. |
| Frontdesk backend | `frontdesk-service` contains rooms, reservations, stays, pricing, quotations, and hotel settings packages alongside service-order/workflow packages. | Shared runtime plus legacy hotel domains | AE03/AE04 require route-by-route and bean-by-bean dependency review. |
| FIXI backend | Service orders, requests, devices, workflow, inventory, procurement, WhatsApp, and assistant packages are active and tenant-scoped. | Canonical FIXI surface | Preserve while removing only unreferenced hotel-only consumers. |
| Persistence | Frontdesk migrations contain both hotel tables and FIXI tables (`V29` service orders, `V30` authorization, `V31` inventory, `V37` requests, `V38` time entries). | Mixed migration history | AE05 must use dependency/production-data evidence before deleting anything. |
| Terminology | `hotelId` remains the tenant identifier in Java APIs and database columns; frontend still uses hotel settings and hotel profile terminology. | Compatibility terminology | AE06 should distinguish tenant identity from user-facing legacy hotel concepts. |

## Safety conclusion

No deletion is safe from this inventory alone. The next implementation pass must
map frontend routes to API consumers, map controller beans to repositories and
migrations, and identify dead code with compilation/tests before AE02–AE07.
