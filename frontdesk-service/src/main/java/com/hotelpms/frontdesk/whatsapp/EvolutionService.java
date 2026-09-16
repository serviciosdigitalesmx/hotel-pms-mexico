package com.hotelpms.frontdesk.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelpms.frontdesk.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/** Server-side Evolution adapter. Instance identity is derived exclusively from the authenticated hotel. */
@Service
public class EvolutionService {
    private static final int CONNECT_TIMEOUT_SECONDS = 5;
    private static final int REQUEST_TIMEOUT_SECONDS = 15;
    private static final String CONNECTING = "connecting";
    private static final String PROVIDER_UNAVAILABLE = "WHATSAPP_PROVIDER_UNAVAILABLE";

    private final String baseUrl;
    private final String apiKey;
    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS)).build();

    /**
     * Creates an adapter using server-side provider settings.
     *
     * @param baseUrl private Evolution API origin
     * @param apiKey private Evolution API key
     * @param mapper JSON mapper
     */
    public EvolutionService(@Value("${evolution.base-url:}") final String baseUrl,
                            @Value("${evolution.api-key:}") final String apiKey,
                            final ObjectMapper mapper) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey;
        this.mapper = mapper.copy();
    }

    /**
     * Reads connection state for the authenticated hotel.
     *
     * @param hotelId authenticated hotel identifier
     * @return safe public connection state
     */
    public Connection status(final UUID hotelId) {
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return new Connection("not_configured", null);
        }
        final JsonNode result = request("GET", "/instance/connectionState/" + instance(hotelId), null, true);
        if (result == null) {
            return new Connection("not_linked", null);
        }
        final String state = result.path("instance").path("state").asText();
        return new Connection(switch (state) {
            case "open" -> "connected";
            case CONNECTING -> CONNECTING;
            case "close", "closed" -> "disconnected";
            default -> "unknown";
        }, null);
    }

    /**
     * Starts or resumes pairing for the authenticated hotel.
     *
     * @param hotelId authenticated hotel identifier
     * @return connection state and optional PNG QR image
     */
    public Connection connect(final UUID hotelId) {
        final Connection current = status(hotelId);
        if ("not_configured".equals(current.state()) || "connected".equals(current.state())) {
            return current;
        }
        if ("not_linked".equals(current.state())) {
            request("POST", "/instance/create", Map.of(
                    "instanceName", instance(hotelId), "integration", "WHATSAPP-BAILEYS",
                    "qrcode", false, "groupsIgnore", true, "readMessages", false,
                    "syncFullHistory", false), false);
        }
        final JsonNode result = request("GET", "/instance/connect/" + instance(hotelId), null, false);
        // Return only the QR image, never Evolution tokens, pairing codes or full provider responses.
        final String qr = result.path("base64").asText("");
        return new Connection(CONNECTING, qr.startsWith("data:image/png;base64,") ? qr : null);
    }

    private static String instance(final UUID hotelId) {
        return "pms-" + hotelId;
    }

    // Evolution errors may include credentials or message content. Deliberately
    // discard provider exception causes before crossing the tenant-facing API.
    @SuppressWarnings("PMD.PreserveStackTrace")
    private JsonNode request(final String method, final String path,
                             final Map<String, Object> body, final boolean allowMissing) {
        try {
            final HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                    .header("apikey", apiKey)
                    .header("Content-Type", "application/json")
                    .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (allowMissing && response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return null;
            }
            if (response.statusCode() < HttpURLConnection.HTTP_OK
                    || response.statusCode() >= HttpURLConnection.HTTP_MULT_CHOICE) {
                throw new ExternalServiceException(PROVIDER_UNAVAILABLE);
            }
            return mapper.readTree(response.body());
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException(PROVIDER_UNAVAILABLE);
        } catch (final IOException | IllegalArgumentException exception) {
            throw new ExternalServiceException(PROVIDER_UNAVAILABLE);
        }
    }

    /**
     * Public, provider-secret-free connection result.
     *
     * @param state normalized connection state
     * @param qrCode optional PNG data URL
     */
    public record Connection(String state, String qrCode) { }
}
