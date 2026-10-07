# Fixi Business Contract (Web)

This is the versioned contract used by the Web Business E2E laboratory. It is
an operational contract, not a pixel-level legacy clone.

Sr-Fix was built from real repair-shop requirements. Its demonstrated
operational capabilities are therefore validated business evidence, not merely
legacy UI choices. Fixi may implement them with a different model or UI only
when the same business need is preserved or improved.

| ID | Business need | Sr-Fix evidence | Canonical Fixi behavior | Web implementation | E2E |
| --- | --- | --- | --- | --- | --- |
| WEB-001 | Staff must enter the operation securely. | Legacy authenticated operation | Real session, `/auth/me`, protected dashboard | Fixi login and httpOnly session | login |
| WEB-002 | The workshop must retain customer contact/history. | Customer records used by repairs | Tenant-scoped customer | Customers Web + API persistence | customer |
| WEB-003 | The received device must remain tied to its owner. | Device identity in repair context | Customer → device relation | Equipment Web + customer device read | device |
| WEB-004 | Reception must record how a device arrived and what it reports. | Reception, failure, checklist/evidence requirements | Branch + customer + device + reported failure become an order | Reception Web | reception |
| WEB-005 | Every repair needs an operational folio and lifecycle state. | Folio/status workflow | Durable service-order id/status and tenant context | Order detail Web + API read | order |
| WEB-006 | The workshop must prioritize work before promised dates. | Legacy semáforo/queue requirement | Status/priority surface equivalent must remain testable | Contract gap test pending full priority UI | priority |
| WEB-007 | Staff and customer communications must address the right repair. | Customer contact/WhatsApp behavior | Tenant-scoped Evolution integration with order context | Integration/API contract | whatsapp |
| WEB-008 | Customers may see public progress, never internal notes. | Public tracking need | Token-scoped portal/public projection | Portal route and boundary | portal |
| WEB-009 | The complete repair must remain auditable after reload/restart. | Follow-up, solution, history, delivery | Timeline/history and persisted order state | Reload/API persistence checks | traceability |

Rules WEB-006 through WEB-009 remain explicit acceptance criteria. They must
not be marked PASS solely because an endpoint returns HTTP 200; the test must
verify the business effect and the public/internal boundary.

The contract deliberately does not certify Android. Android uses this same
contract only after the Web laboratory is green.
