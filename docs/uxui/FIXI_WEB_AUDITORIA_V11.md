# Auditoría técnica Fixi Web UX/UI v1.1

Fecha: 2026-11-07  
Rama auditada: `codex/fixi-web-uxui-v11`  
Commit base: `13621171b1567213b193757018361be62ec135e4` (`codex/fixi-repairshop`)  
Alcance: frontend React 18, TypeScript, Vite; API ASP.NET consumida por `frontend/src/api/endpoints.ts`.

## Método y límites

Se recorrieron las rutas activas en `frontend/src/App.tsx`, el layout, los componentes compartidos, las 17 pantallas operativas, sus adaptadores y tipos de API, estilos y breakpoints. Esta es una auditoría estática del código conectado a las rutas; no se usó una cuenta ni se modificó producción. Por tanto, las operaciones remotas, el aspecto renderizado en un navegador real y los permisos efectivos del servidor quedan **no verificados en runtime**. GitHub Actions es el mecanismo de verificación definido por el repositorio; no se ejecutan builds ni pruebas locales.

La auditoría no encontró evidencia para afirmar fallos de servidor. Los adaptadores declaran respuestas tipadas, pero los contratos HTTP sólo se confirman plenamente contrastando controladores backend o un entorno aislado autorizado. La autorización frontend es una guía de interfaz; el backend debe seguir validando cada acción.

## Hallazgos globales

- La aplicación usa layout único y navegación por grupos; los permisos filtran navegación y algunas acciones. La sucursal activa se cambia mediante `authApi.switchShop`; no se deben alterar esos flujos.
- El encabezado global añade «Nueva orden» en todas las rutas a quien tenga `orders.manage`, aunque Órdenes y Tablero ya tienen acciones contextuales. Recepción tiene su propio formulario.
- El sidebar se oculta en anchos <=1023 px y se sustituye por drawer. En escritorio no hay sidebar compacto intermedio.
- El contenido aplica una animación de entrada de página de 220 ms. Hay una preferencia `prefers-reduced-motion`, pero la animación completa sigue presente para otros usuarios.
- Los controles compartidos Input/Select/Button median 40 px; el objetivo móvil pide controles táctiles de al menos 44 px. `Field` asocia errores accesibles cuando se usa; no enfoca automáticamente el primer error.
- `Table` siempre envuelve tabla en scroll horizontal. No existe un patrón reutilizable de lista móvil ni panel lateral; varios flujos usan `Modal` centrado.
- `Tabs` usa `tablist`/`tab` y estado seleccionado, sin navegación por flechas ni relación `tabpanel` común.
- El tema ya centraliza paleta en tokens CSS y admite claro/oscuro. La marca Android no está disponible en este repositorio; no se cambia la paleta en esta implementación.
- Hay estados Loading/EmptyState/ErrorState reutilizables. Loading es genérico y no reproduce estructura de tabla/tablero. Las pantallas combinan estados propios y compartidos.
- El tablero móvil conserva columnas kanban de ancho aproximado 83vw con scroll horizontal. Debe adaptarse sin introducir estados.
- La búsqueda/filtros viven mayormente en estado local de pantalla; abrir el detalle de órdenes cambia URL y el detalle guarda pestaña en query string. No existe persistencia global uniforme de filtros/scroll.

## Matriz de pantallas

Detalle de rutas, modelos, endpoints declarados, permisos visuales, observaciones, objetivos y criterios por pantalla: [FIXI_WEB_MATRIZ_PANTALLAS_V11.md](./FIXI_WEB_MATRIZ_PANTALLAS_V11.md).

## Integridad y riesgos

1. Cobros, reembolsos, caja, inventario, compras, transferencias y facturación son acciones con efecto. Sólo se modificará presentación y accesibilidad alrededor de las operaciones existentes; no se ejecutan en producción.
2. El detalle de orden muestra estado de reparación y saldo en secciones distintas. La acción de estado incluye un bloqueo visible de saldo en ciertos casos. Este trabajo no cambia condiciones, balances ni transiciones.
3. Recepción crea primero la orden y luego persiste checklist, desbloqueo y firma por separado; si fallan pasos secundarios, la UI reporta una orden creada con detalles pendientes. Se preservará ese comportamiento y su idempotencia.
4. Configuración contiene secretos de integración y datos administrativos; no se imprimen ni se copian en documentación.
5. Compras, transferencias, caja y suscripción dependen de permisos/estado del taller además de lo visible en la UI. No se agregarán botones o criterios no respaldados.
6. No se obtuvo sesión autorizada para validación runtime multitenant, sucursal, suscripción, finanzas ni móvil. Quedará explícito en la entrega.

## Estado inicial de verificación

- Rutas/componentes/adaptadores/tipos: inspección estática completada.
- Perfiles responsive reales: no ejecutados localmente por instrucción de repositorio.
- Build, lint, unit y E2E: pendientes de GitHub Actions en la rama/PR.
- Acceso real a API y aislamiento: no verificados durante esta auditoría.
- Cambios en producción: ninguno.
