package com.hotelpms.frontdesk.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Allow-listed PMS operations exposed to the model for the authenticated role. */
@Component
@RequiredArgsConstructor
public class AssistantToolCatalog {

    private static final String TYPE_FIELD = "type";

    private static final Set<String> STAFF_ROLES = Set.of("ADMIN", "OWNER", "RECEPTIONIST");
    private static final Set<String> PRIVILEGED_ROLES = Set.of("ADMIN", "OWNER");

    private static final List<String> STAFF_READS = List.of(
            "resumen_operativo", "buscar_huespedes", "obtener_huesped",
            "buscar_reservaciones", "obtener_reservacion", "listar_estancias",
            "obtener_estancia", "listar_habitaciones", "obtener_habitacion",
            "habitaciones_disponibles", "listar_tipos_habitacion", "buscar_facturas",
            "obtener_factura", "obtener_factura_estancia", "listar_pedidos_fb",
            "pedidos_fb_estancia", "listar_menu", "listar_cotizaciones",
            "obtener_cotizacion", "calendario_tarifas", "listar_temporadas");

    private static final List<String> PRIVILEGED_READS = List.of(
            "reporte_financiero", "listar_usuarios", "obtener_configuracion_hotel");

    private static final List<String> STAFF_ACTIONS = List.of(
            "crear_huesped", "actualizar_huesped", "crear_reservacion",
            "actualizar_reservacion", "cambiar_habitacion_reservacion",
            "reintentar_email_reservacion", "registrar_check_in", "registrar_check_out",
            "reintentar_factura_estancia", "reintentar_email_check_out",
            "actualizar_estado_habitacion", "agregar_cargo", "registrar_pago",
            "crear_pedido_fb", "confirmar_pedido_fb", "crear_cotizacion",
            "actualizar_cotizacion", "duplicar_cotizacion", "enviar_cotizacion",
            "convertir_cotizacion", "rechazar_cotizacion");

    private static final List<String> PRIVILEGED_ACTIONS = List.of(
            "eliminar_huesped", "eliminar_reservacion", "crear_habitacion",
            "actualizar_habitacion", "eliminar_habitacion", "crear_tipo_habitacion",
            "actualizar_tipo_habitacion", "eliminar_tipo_habitacion", "crear_menu_item",
            "actualizar_menu_item", "eliminar_menu_item", "eliminar_cotizacion",
            "aplicar_tarifa", "crear_temporada", "actualizar_temporada",
            "eliminar_temporada", "activar_usuario",
            "desactivar_usuario", "actualizar_configuracion_hotel");

    private final ObjectMapper objectMapper;

    /**
     * Builds the two narrowly scoped tools available to the current role.
     *
     * @param roles authenticated roles
     * @return provider tool definitions
     */
    public ArrayNode toolsFor(final Set<String> roles) {
        final ArrayNode tools = objectMapper.createArrayNode();
        final Set<String> reads = allowed(roles, STAFF_READS, PRIVILEGED_READS);
        final Set<String> actions = allowed(roles, STAFF_ACTIONS, PRIVILEGED_ACTIONS);
        if (!reads.isEmpty()) {
            tools.add(tool("consultar_pms",
                    "Consulta datos reales. Usa parametros según la operación; nunca inventes IDs.", reads));
        }
        if (!actions.isEmpty()) {
            tools.add(tool("proponer_accion_pms",
                    "Prepara una acción. No la ejecuta: el humano revisará los parámetros y dará el clic final.",
                    actions));
        }
        return tools;
    }

    /**
     * Returns whether a tool always requires explicit human confirmation.
     *
     * @param toolName provider tool name
     * @return true for mutation proposals
     */
    public boolean requiresConfirmation(final String toolName) {
        return "proponer_accion_pms".equals(toolName);
    }

    private Set<String> allowed(
            final Set<String> roles,
            final List<String> staff,
            final List<String> privileged) {
        final Set<String> result = new LinkedHashSet<>();
        if (roles.stream().anyMatch(STAFF_ROLES::contains)) {
            result.addAll(staff);
        }
        if (roles.stream().anyMatch(PRIVILEGED_ROLES::contains)) {
            result.addAll(privileged);
        }
        return result;
    }

    private ObjectNode tool(final String name, final String description, final Set<String> operations) {
        final ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put(TYPE_FIELD, "object");
        final ObjectNode properties = parameters.putObject("properties");
        final ObjectNode operation = properties.putObject("operacion");
        operation.put(TYPE_FIELD, "string");
        final ArrayNode values = operation.putArray("enum");
        operations.forEach(values::add);
        final ObjectNode args = properties.putObject("parametros");
        args.put(TYPE_FIELD, "object");
        args.put("description", parameterGuide());
        parameters.putArray("required").add("operacion").add("parametros");
        parameters.put("additionalProperties", false);

        final ObjectNode function = objectMapper.createObjectNode();
        function.put("name", name);
        function.put("description", description);
        function.set("parameters", parameters);
        final ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.put(TYPE_FIELD, "function");
        wrapper.set("function", function);
        return wrapper;
    }

    private String parameterGuide() {
        return "Usa los campos exactos de las APIs del PMS. IDs: id, stayId, invoiceId, roomTypeId. "
                + "Listados: query,page,size,from,to,startDate,endDate,checkInDate,checkOutDate. "
                + "Creaciones y actualizaciones: data con el DTO real. Cambio de habitación: "
                + "id de reservación, roomIdActual y roomIdNuevo. Nunca incluyas contraseñas, llaves API "
                + "ni datos fiscales CFDI 4.0 que el usuario no haya proporcionado. Para registrar_check_in usa exclusivamente parametros.data "
                + "con guestId y roomId como UUID reales, status CHECKED_IN, occupantCount, guests y, si no "
                + "hay reservationId, expectedCheckOutDate. Consulta primero huéspedes y habitaciones; si "
                + "faltan datos del huésped, pregunta al usuario y nunca los inventes. Nunca pidas UUID al "
                + "usuario: busca guestId y roomId con las herramientas de consulta.";
    }
}
