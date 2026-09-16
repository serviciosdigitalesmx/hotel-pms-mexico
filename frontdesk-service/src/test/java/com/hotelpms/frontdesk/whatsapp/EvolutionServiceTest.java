package com.hotelpms.frontdesk.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hotelpms.frontdesk.exception.ExternalServiceException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvolutionServiceTest {
    private HttpServer server;
    private EvolutionService service;
    private final List<String> paths = new ArrayList<>();
    private final AtomicReference<String> response = new AtomicReference<>("{\"instance\":{\"state\":\"open\"}}");
    private int status = HttpURLConnection.HTTP_OK;

    @BeforeEach
    void start() throws IOException {
        final InetAddress loopback = InetAddress.getLoopbackAddress();
        server = HttpServer.create(new InetSocketAddress(loopback, 0), 0);
        server.createContext("/", exchange -> {
            paths.add(exchange.getRequestURI().getPath());
            final byte[] bytes = response.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        final String address = loopback.getHostAddress();
        final String host = address.contains(":") ? "[" + address + "]" : address;
        service = new EvolutionService("http://" + host + ":" + server.getAddress().getPort(),
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
        status = HttpURLConnection.HTTP_UNAUTHORIZED;
        response.set("private-provider-token");
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
    void onlyPngQrIsExposed() throws JsonProcessingException {
        response.set("{\"instance\":{\"state\":\"close\"},\"base64\":\"data:image/png;base64,AAAA\","
                + "\"apikey\":\"never-expose\",\"pairingCode\":\"private\"}");
        final EvolutionService.Connection result = service.connect(UUID.randomUUID());
        assertEquals("data:image/png;base64,AAAA", result.qrCode());
        assertFalse(new ObjectMapper().writeValueAsString(result).contains("never-expose"));
        response.set("{\"base64\":\"https://untrusted.example/qr\"}");
        assertNull(service.connect(UUID.randomUUID()).qrCode());
    }
}
