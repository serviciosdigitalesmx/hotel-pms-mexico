package com.hotelpms.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PublicBookingFilterTest {

    private static final String RESOLVER_HOTEL =
            "00000000-0000-0000-0000-000000000001";

    private static final String TARGET_HOTEL =
            "00000000-0000-0000-0000-000000000002";

    private static final String SECRET =
            "0123456789012345678901234567890123456789";

    @Test
    void shouldResolveSlugServerSideAndSignTargetHotel() {

        final ExchangeFunction exchangeFunction = request -> {

            assertThat(request.url().getPath())
                    .endsWith("/internal/public-hotels/palmas");

            assertThat(
                    request.headers()
                            .getFirst("X-Auth-Hotel"))
                    .isEqualTo(RESOLVER_HOTEL);

            assertThat(
                    request.headers()
                            .getFirst("X-Internal-Signature"))
                    .isNotBlank();

            return Mono.just(
                    ClientResponse
                            .create(HttpStatus.OK)
                            .body(TARGET_HOTEL)
                            .build());
        };

        final WebClient webClient =
                WebClient.builder()
                        .exchangeFunction(exchangeFunction)
                        .build();

        final PublicBookingFilter filter =
                new PublicBookingFilter(
                        SECRET,
                        "http://resolver/internal/public-hotels",
                        RESOLVER_HOTEL,
                        webClient);

        final AtomicReference<ServerWebExchange> forwarded =
                new AtomicReference<>();

        final GatewayFilterChain chain = exchange -> {
            forwarded.set(exchange);
            return Mono.empty();
        };

        final MockServerWebExchange exchange =
                MockServerWebExchange.from(
                        MockServerHttpRequest
                                .post(
                                        "/api/public/hotels/"
                                                + "palmas/reservations")
                                // Deliberately malicious/incorrect:
                                // the browser must NOT control tenant UUID.
                                .header(
                                        "X-Public-Hotel-Id",
                                        RESOLVER_HOTEL)
                                .build());

        filter.apply(new PublicBookingFilter.Config())
                .filter(exchange, chain)
                .block();

        assertThat(forwarded.get()).isNotNull();

        assertThat(
                forwarded.get()
                        .getRequest()
                        .getHeaders()
                        .getFirst("X-Auth-Hotel"))
                .isEqualTo(TARGET_HOTEL);

        assertThat(
                forwarded.get()
                        .getRequest()
                        .getHeaders()
                        .getFirst("X-Public-Hotel-Id"))
                .isNull();

        assertThat(
                forwarded.get()
                        .getRequest()
                        .getHeaders()
                        .getFirst("X-Auth-User"))
                .isEqualTo("public-booking-web");
    }

    @Test
    void shouldReturnNotFoundForUnknownSlug() {

        final ExchangeFunction exchangeFunction = request ->
                Mono.just(
                        ClientResponse
                                .create(HttpStatus.NOT_FOUND)
                                .build());

        final PublicBookingFilter filter =
                new PublicBookingFilter(
                        SECRET,
                        "http://resolver/internal/public-hotels",
                        RESOLVER_HOTEL,
                        WebClient.builder()
                                .exchangeFunction(exchangeFunction)
                                .build());

        final AtomicReference<ServerWebExchange> forwarded =
                new AtomicReference<>();

        final MockServerWebExchange exchange =
                MockServerWebExchange.from(
                        MockServerHttpRequest
                                .post(
                                        "/api/public/hotels/"
                                                + "no-existe/reservations")
                                .build());

        filter.apply(new PublicBookingFilter.Config())
                .filter(
                        exchange,
                        request -> {
                            forwarded.set(request);
                            return Mono.empty();
                        })
                .block();

        assertThat(forwarded.get()).isNull();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRejectMalformedPublicPath() {

        final PublicBookingFilter filter =
                new PublicBookingFilter(
                        SECRET,
                        "http://resolver/internal/public-hotels",
                        RESOLVER_HOTEL,
                        WebClient.create());

        final MockServerWebExchange exchange =
                MockServerWebExchange.from(
                        MockServerHttpRequest
                                .post(
                                        "/api/public/hotels/"
                                                + "bad/slug/reservations")
                                .build());

        filter.apply(new PublicBookingFilter.Config())
                .filter(
                        exchange,
                        request -> Mono.empty())
                .block();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
