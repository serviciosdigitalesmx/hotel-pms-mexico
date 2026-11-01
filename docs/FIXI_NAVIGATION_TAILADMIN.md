# Fixi: navegación compacta y TailAdmin

## Evidencia y alcance

Referencia de organización: reporte local `1-samii-e2e-features.md`, cierre 16/oct/2026, secciones «Inicio / Dashboard», «Órdenes» e «Inventario de Configuración observado». El reporte distingue observado de inferido; no acredita toda la lógica de Samii ni se copió su código. Los permisos y contratos siguen siendo los de Fixi.

Base visual: TailAdmin React gratuito, commit `df1e9f37e56e0bf83030e8063f5353da2e0ecf18`, https://github.com/TailAdmin/free-react-tailwind-admin-dashboard. Se adaptaron la tarjeta de indicadores, los tokens visuales y la composición de navegación. La licencia MIT se distribuye en `frontend/public/licenses/TailAdmin-MIT.txt`. Se conserva React Router 7, Radix, cookies, CSRF y APIs de Fixi; no se importan router, autenticación, estadísticas de demostración ni dependencias de TailAdmin.

## Organización resultante

| Entrada principal | Contenido |
|---|---|
| Dashboard | Métricas reales; compromisos, actividad y carga de técnicos |
| Órdenes de servicio | Todas, nueva recepción y agenda; estados de trabajo en pestañas contextuales |
| Clientes | Directorio, equipos e importación/exportación |
| Inventario | Productos/existencias y compras/recepción |
| Proveedores | Directorio existente |
| Servicios | Catálogo existente |
| Gastos | Flujo existente |
| Reportes | Finanzas, órdenes, inventario, productividad y sucursal según permisos |
| Configuración | Negocio, equipo/permisos, sucursales, políticas, estados, notificaciones e integraciones |

Máximo nueve entradas principales para OWNER con todos los módulos y capacidades; menos según permisos. Sólo se despliega un grupo a la vez. Las rutas `/parts`, `/technicians` y demás rutas existentes se conservan; se retiran accesos redundantes de la barra, no funcionalidades.

Las capacidades se consultan al backend una sola vez por montaje del layout y se comparten entre sidebar, header y pestañas. Cada hijo se filtra individualmente; las restricciones de rol de las rutas se mantienen también en la navegación. Los grupos vacíos desaparecen. El backend y los guards continúan siendo autoridad. SUPER_ADMIN conserva su consola independiente y su grant persistido.

## Validación

Pruebas de navegación: filtrado anidado, capacidad frente a restricciones de rol, nueve entradas para OWNER, estados contextuales, apertura/cierre y URL directa con selección específica. Build, pruebas y E2E se validan en GitHub Actions. No se despliega esta revisión como parte de la preparación local sin revisar su resultado.
