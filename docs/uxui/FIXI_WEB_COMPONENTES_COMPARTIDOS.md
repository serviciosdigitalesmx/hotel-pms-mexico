# Componentes compartidos: auditoría UX/UI v1.1

## Base existente

| Componente | Archivo | Comportamiento actual | Decisión |
|---|---|---|---|
| Button | `frontend/src/components/ui/index.tsx` | Tamaños 32/40/48px; estado loading conserva children y bloquea | Mantener; adaptar tamaño táctil vía clases responsive y variantes por pantalla |
| Input/Select/Textarea | mismo | Input y Select 40px; `fieldClass` común; Textarea sin mínimo declarado | Subir a 44px; preservar overrides explícitos sólo donde sean intencionales |
| Field | mismo | Label arriba; `aria-describedby`/`aria-invalid` para error; no foco automático | Preservar, corregir asociación en children compuestos y mejorar espacio; enfocar en formularios multipaso al validar |
| PageHeader | mismo | H1 + subtítulo + acciones responsive | Reducir margen/densidad, permitir acciones con wrap y touch targets |
| Card/Stat | mismo | Contenedores reutilizados en pantallas | Reducir espaciados excesivos sin cambiar contenido/datos |
| Table/Th/Td | mismo | Tabla con scroll horizontal | No cambiar semántica; habilitar wrapper/presentación mobile por CSS mediante opt-in/lista correspondiente |
| Tabs | mismo | ARIA básica, scroll horizontal | Añadir teclado flecha/Home/End y `tabIndex`, respetar componente consumidor |
| Modal/ConfirmDialog | mismo | `<dialog>` centrado, Escape/click backdrop, foco manejado nativamente | Añadir componente Drawer reutilizable y estilos mobile full-screen; no reemplazar diálogos de confirmación |
| Loading/EmptyState/ErrorState | mismo | Genéricos, empty ilustrativo pequeño, error con reintento opcional | Compactar empty; skeleton table/list reutilizable donde sea apropiado; conservar retry real |
| Toast Sonner | layout/proveedor existente | mensajes de éxito/error | mantener avisos con decisión del caller; no autoocultar errores que requieran intervención |
| Sidebar/topbar | `components/layout/AppLayout.tsx`, `styles.css` | 232px, móvil drawer <=1023, CTA nueva orden global | quitar CTA global, CTA contextual en órdenes/tablero; intermedio compacto con etiquetas accesibles |
| SubscriptionBanner | `components/layout/SubscriptionBanner.tsx` | avisos condicionados por API/estado, no sticky | conservar enlaces, reducir densidad sin cambiar condiciones |

## Tokens y responsive

Los tokens están en `frontend/src/styles.css` como variables `--fx-*`, con tema claro/oscuro y brand violeta actual. Mantenerlos hasta que exista fuente de marca Android disponible. Breakpoints existentes: 1180, 1023, 760, 640 y 420px. El movimiento reducido ya tiene regla global. Remover la animación `fx-page-in` de entrada completa, sin quitar interacciones breves de diálogos.

## Reglas de integración

- Los componentes compartidos sólo reorganizan presentación y accesibilidad.
- No construyen clientes API ni duplican modelos.
- El Drawer informa si está abierto; la pantalla consumidora conserva el formulario y decide cuándo puede cerrarse. No descarta datos automáticamente.
- Las variantes móviles se activan de forma explícita; no se ocultarán datos/acciones esenciales mediante selectores globales frágiles.
- Navegación por tabs respeta labels/visibilidad de cada página y su URL actual.
