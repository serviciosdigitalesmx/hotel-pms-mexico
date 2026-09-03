package com.hotelpms.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Resolves a public hotel slug server-side and converts the request into the
 * same signed, tenant-scoped internal identity used by authenticated traffic.
 *
 * The browser never chooses X-Auth-Hotel.
 */
@Component
public class PublicBookingFilter
        extends AbstractGatewayFilterFactory<PublicBookingFilter.Config> {

    private static final String HEADER_USER = "X-Auth-User";
    private static final String HEADER_ROLE = "X-Auth-Role";
    private static final String HEADER_HOTEL = "X-Auth-Hotel";
    private static final String HEADER_SIGNATURE = "X-Internal-Signature";
    private static final String HEADER_TIMESTAMP = "X-Auth-Timestamp";
    private static final String HEADER_NONCE = "X-Auth-Nonce";

    private static final String PUBLIC_USER = "public-booking-web";
    private static final String PUBLIC_ROLE = "RECEPTIONIST";

    private static final String PUBLIC_PREFIX =
            "/api/public/hotels/";
    private static final String PUBLIC_SUFFIX =
            "/reservations";

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String hmacSecret;
    private final String resolverUrl;
    private final String resolverIdentityHotelId;
    private final WebClient webClient;

    /**
     * Production constructor.
     */
    @Autowired
    public PublicBookingFilter(
            @Value("${internal.hmac.secret}")
            final String hmacSecret,
            @Value("${public-booking.resolver-url:"
                    + "http://frontdesk-service:8081/internal/public-hotels}")
            final String resolverUrl,
            // Public booking is opt-in and currently has no route in the
            // Config Server contract. Keep the gateway bootable when that
            // optional resolver identity is not configured; the filter still
            // fails closed with 502 if a route is enabled without it.
            @Value("${public-booking.hotel-id:}")
            final String resolverIdentityHotelId,
            final WebClient.Builder webClientBuilder) {

        this(
                hmacSecret,
                resolverUrl,
                resolverIdentityHotelId,
                webClientBuilder.build()
        );
    }

    /**
     * Test constructor.
     */
    PublicBookingFilter(
            final String hmacSecret,
            final String resolverUrl,
            final String resolverIdentityHotelId,
            final WebClient webClient) {

        super(Config.class);

        this.hmacSecret = hmacSecret;
        this.resolverUrl = stripTrailingSlash(resolverUrl);
        this.resolverIdentityHotelId = resolverIdentityHotelId;
        this.webClient = webClient;
    }

    /** {@inheritDoc} */
    @Override
    public GatewayFilter apply(final Config config) {

        return (exchange, chain) -> {

            final String path =
                    exchange.getRequest().getURI().getPath();

            final String slug = extractSlug(path);

            if (slug == null) {
                exchange.getResponse()
                        .setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }

            return resolveHotelId(slug)
                    .flatMap(hotelId ->
                            forwardSigned(
                                    exchange,
                                    chain,
                                    hotelId))
                    .onErrorResume(
                            UnknownHotelException.class,
                            exception -> {
                                exchange.getResponse()
                                        .setStatusCode(
                                                HttpStatus.NOT_FOUND);
                                return exchange
                                        .getResponse()
                                        .setComplete();
                            })
                    .onErrorResume(
                            ResolverUnavailableException.class,
                            exception -> {
                                exchange.getResponse()
                                        .setStatusCode(
                                                HttpStatus.BAD_GATEWAY);
                                return exchange
                                        .getResponse()
                                        .setComplete();
                            });
        };
    }

    private Mono<String> resolveHotelId(final String slug) {

        if (!isValidUuid(resolverIdentityHotelId)) {
            return Mono.error(
                    new ResolverUnavailableException());
        }

        final String timestamp =
                String.valueOf(System.currentTimeMillis());

        final String nonce =
                UUID.randomUUID().toString();

        final String signature =
                computeHmac(
                        resolverIdentityHotelId,
                        timestamp,
                        nonce);

        return webClient
                .get()
                .uri(resolverUrl + "/{slug}", slug)
                .header(HEADER_USER, PUBLIC_USER)
                .header(HEADER_ROLE, PUBLIC_ROLE)
                .header(
                        HEADER_HOTEL,
                        resolverIdentityHotelId)
                .header(
                        HEADER_SIGNATURE,
                        signature)
                .header(
                        HEADER_TIMESTAMP,
                        timestamp)
                .header(
                        HEADER_NONCE,
                        nonce)
                .retrieve()
                .onStatus(
                        status ->
                                status.value()
                                        == HttpStatus.NOT_FOUND.value(),
                        response ->
                                Mono.error(
                                        new UnknownHotelException()))
                .onStatus(
                        status -> status.isError(),
                        response ->
                                Mono.error(
                                        new ResolverUnavailableException()))
                .bodyToMono(String.class)
                .map(String::trim)
                .flatMap(value -> {
                    if (!isValidUuid(value)) {
                        return Mono.error(
                                new ResolverUnavailableException());
                    }
                    return Mono.just(value);
                });
    }

    private Mono<Void> forwardSigned(
            final org.springframework.web.server.ServerWebExchange exchange,
            final org.springframework.cloud.gateway.filter.GatewayFilterChain chain,
            final String hotelId) {

        final String timestamp =
                String.valueOf(System.currentTimeMillis());

        final String nonce =
                UUID.randomUUID().toString();

        final String signature =
                computeHmac(
                        hotelId,
                        timestamp,
                        nonce);

        final ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .headers(headers -> {
                            headers.remove(HEADER_USER);
                            headers.remove(HEADER_ROLE);
                            headers.remove(HEADER_HOTEL);
                            headers.remove(HEADER_SIGNATURE);
                            headers.remove(HEADER_TIMESTAMP);
                            headers.remove(HEADER_NONCE);

                            // Remove the old temporary client-controlled
                            // hotel selector if somebody still sends it.
                            headers.remove("X-Public-Hotel-Id");
                        })
                        .header(
                                HEADER_USER,
                                PUBLIC_USER)
                        .header(
                                HEADER_ROLE,
                                PUBLIC_ROLE)
                        .header(
                                HEADER_HOTEL,
                                hotelId)
                        .header(
                                HEADER_SIGNATURE,
                                signature)
                        .header(
                                HEADER_TIMESTAMP,
                                timestamp)
                        .header(
                                HEADER_NONCE,
                                nonce)
                        .build();

        return chain.filter(
                exchange.mutate()
                        .request(request)
                        .build());
    }

    private static String extractSlug(final String path) {

        if (path == null
                || !path.startsWith(PUBLIC_PREFIX)
                || !path.endsWith(PUBLIC_SUFFIX)) {
            return null;
        }

        final int start = PUBLIC_PREFIX.length();
        final int end =
                path.length() - PUBLIC_SUFFIX.length();

        if (end <= start) {
            return null;
        }

        final String slug =
                path.substring(start, end);

        if (slug.isBlank()
                || slug.length() > 120
                || slug.contains("/")
                || !slug.matches(
                        "[A-Za-z0-9][A-Za-z0-9_-]{0,119}")) {
            return null;
        }

        return slug;
    }

    private String computeHmac(
            final String hotelId,
            final String timestamp,
            final String nonce) {

        try {
            final Mac mac =
                    Mac.getInstance(HMAC_ALGORITHM);

            mac.init(
                    new SecretKeySpec(
                            hmacSecret.getBytes(
                                    StandardCharsets.UTF_8),
                            HMAC_ALGORITHM));

            final String payload =
                    PUBLIC_USER
                            + ":"
                            + PUBLIC_ROLE
                            + ":"
                            + hotelId
                            + ":"
                            + timestamp
                            + ":"
                            + nonce;

            return HexFormat.of()
                    .formatHex(
                            mac.doFinal(
                                    payload.getBytes(
                                            StandardCharsets.UTF_8)));

        } catch (final GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "HMAC_SIGNATURE_FAILED",
                    exception);
        }
    }

    private static boolean isValidUuid(final String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            return UUID.fromString(value)
                    .toString()
                    .equalsIgnoreCase(value);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static String stripTrailingSlash(
            final String value) {

        if (value != null && value.endsWith("/")) {
            return value.substring(
                    0,
                    value.length() - 1);
        }

        return value;
    }

    /** Route configuration marker. */
    public static class Config {
    }

    private static final class UnknownHotelException
            extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    private static final class ResolverUnavailableException
            extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
