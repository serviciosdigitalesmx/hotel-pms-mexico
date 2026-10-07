# Fixi legacy parity matrix

This matrix records the stable product interpretation before any Web parity
claim is made. Legacy repositories are evidence sources, not architecture
templates.

| Function / need | Sr-Fix validated need | Sdmx evolution | Fixi canonical behavior | Classification | Test |
| --- | --- | --- | --- | --- | --- |
| Reception condition | Record the real condition in which equipment arrived | Broader workflow intent | Branch-scoped order with condition/evidence model | BASELINE FUNCIONAL + EVOLUCIÓN FIXI ACTUAL | WEB-004 |
| Customer/device history | Customer owns repair context and device history | Additional operational views | Customer → device → order | BASELINE FUNCIONAL | WEB-002/003 |
| Folio/status | Staff need an operational identifier and lifecycle | More lifecycle states | Durable order id/status | BASELINE FUNCIONAL + EVOLUCIÓN FIXI ACTUAL | WEB-005/009 |
| Priority/semáforo | Staff must know what risks missing promise date | Possible richer prioritization | Equivalent priority/queue, not legacy colors | BASELINE FUNCIONAL; verification required | WEB-006 |
| Communication | Contact the correct customer for the correct repair | Portal/integration ideas | Evolution/WhatsApp with order context | EVOLUCIÓN FIXI ACTUAL if verified | WEB-007 |
| Public tracking | Customer sees permitted repair progress | Portal/authorization ideas | Token-scoped public projection | EVOLUCIÓN FIXI ACTUAL | WEB-008 |
| Internal notes | Workshop-only information must stay private | Expanded timeline intent | Never public by default | MEJORA SDMX RECUPERABLE | WEB-008 |

Any additional legacy behavior must be added with source evidence and a
corresponding executable test before being classified as a regression.
