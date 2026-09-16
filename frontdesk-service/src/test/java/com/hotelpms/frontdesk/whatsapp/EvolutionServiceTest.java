package com.hotelpms.frontdesk.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.frontdesk.exception.ExternalServiceException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvolutionServiceTest {
    private HttpServer server;
    private EvolutionService service;
    private final List<String> paths = new ArrayList<>();
    private String response = "{\"instance\":{\"state\":\"open\"}}";
    private int status = 200;

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            paths.add(exchange.getRequestURI().getPath());
            final byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        service = new EvolutionService("http://127.0.0.1:" + server.getAddress().getPort(),
                "test-only-key", new ObjectMapper());
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void eachHotelUsesItsOwnInstance() {
        final UUID first = UUID.randomUUID();
        final UUID second = UUID.randomUUID();
        assertEquals("connected", service.status(first).state());
        service.status(second);
        assertEquals(List.of("/instance/connectionState/pms-" + first,
                "/instance/connectionState/pms-" + second), paths);
    }

    @Test
    void disabledIntegrationMakesNoNetworkRequest() {
        final EvolutionService disabled = new EvolutionService("", "", new ObjectMapper());
        assertEquals("not_configured", disabled.connect(UUID.randomUUID()).state());
        assertEquals(0, paths.size());
    }

    @Test
    void providerSecretsAreNotReturnedInErrors() {
        status = 401;
        response = "private-provider-token";
        final ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                () -> service.status(UUID.randomUUID()));
        assertEquals("WHATSAPP_PROVIDER_UNAVAILABLE", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void alreadyConnectedDoesNotStartAnotherPairing() {
        assertEquals("connected", service.connect(UUID.randomUUID()).state());
        assertEquals(1, paths.size());
    }

    @Test
    void onlyPngQrIsExposed() throws Exception {
        response = "{\"instance\":{\"state\":\"close\"},\"base64\":\"data:image/png;base64,AAAA\","
                + "\"apikey\":\"never-expose\",\"pairingCode\":\"private\"}";
        final EvolutionService.Connection result = service.connect(UUID.randomUUID());
        assertEquals("data:image/png;base64,AAAA", result.qrCode());
        assertFalse(new ObjectMapper().writeValueAsString(result).contains("never-expose"));
        response = "{\"base64\":\"https://untrusted.example/qr\"}";
        assertNull(service.connect(UUID.randomUUID()).qrCode());
    }
}
