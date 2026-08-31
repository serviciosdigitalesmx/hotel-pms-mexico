package com.hotelpms.frontdesk.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantToolCatalogTest {

    private final AssistantToolCatalog catalog = new AssistantToolCatalog(new ObjectMapper());

    @Test
    void receptionistCannotReceivePrivilegedOperations() {
        final ArrayNode tools = catalog.toolsFor(Set.of("RECEPTIONIST"));
        final String json = tools.toString();

        assertThat(json).contains("registrar_check_in", "registrar_pago", "crear_pedido_fb");
        assertThat(json).doesNotContain("listar_usuarios", "eliminar_habitacion", "reporte_financiero");
    }

    @Test
    void ownerReceivesOperationalAndPrivilegedOperations() {
        final String json = catalog.toolsFor(Set.of("OWNER")).toString();

        assertThat(json).contains("registrar_check_out", "reporte_financiero", "aplicar_tarifa");
        assertThat(json).doesNotContain(
                "crear_usuario", "password", "aiApiKey", "fatturaPA", "alloggiati");
    }

    @Test
    void onlyMutationToolRequiresConfirmation() {
        assertThat(catalog.requiresConfirmation("consultar_pms")).isFalse();
        assertThat(catalog.requiresConfirmation("proponer_accion_pms")).isTrue();
    }

    @Test
    void checkInGuideRequiresRealIdentifiersAndDtoEnvelope() {
        final String json = catalog.toolsFor(Set.of("RECEPTIONIST")).toString();

        assertThat(json).contains(
                "registrar_check_in usa exclusivamente parametros.data",
                "guestId y roomId como UUID reales",
                "nunca los inventes",
                "Nunca pidas UUID al usuario");
    }
}
