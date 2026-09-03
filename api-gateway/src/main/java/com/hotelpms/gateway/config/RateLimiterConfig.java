package com.hotelpms.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Configures rate-limiting key resolver beans for the Spring Cloud Gateway.
 *
 * <p>Two resolvers are provided:
 * <ul>
 *   <li>{@code remoteAddrKeyResolver} — for pre-authentication routes (e.g. /auth/**).
 *       Uses the gateway-injected {@code X-Client-IP} value, which is stripped and
 *       rebuilt by {@link com.hotelpms.gateway.filter.ClientIpFilter}; if the global
 *       filter has not run, it falls back to the TCP peer. It never reads the
 *       client-forgeable {@code X-Forwarded-For} header.</li>
 *   <li>{@code userKeyResolver} — for authenticated routes. Uses the {@code X-Auth-User}
 *       header injected by {@link com.hotelpms.gateway.filter.AuthenticationFilter} after
 *       JWT validation. Per-user buckets prevent a single compromised or malicious account
 *       from flooding the API and causing a denial-of-service for other tenants. Falls back
 *       to the trusted {@code X-Client-IP} value (or TCP peer as a defensive
 *       fallback) when the header is absent.</li>
 * </ul>
 *
 * <p>Both beans are referenced by name in the {@code api-gateway.yml} rate-limiter
 * filter definitions:
 * <pre>
 *   key-resolver: "#{@remoteAddrKeyResolver}"   # pre-auth routes
 *   key-resolver: "#{@userKeyResolver}"          # authenticated routes
 * </pre>
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Resolves the rate-limit bucket key from the client IP address.
     *
     * <p>{@link com.hotelpms.gateway.filter.ClientIpFilter} runs as a global filter
     * before route filters and creates the trusted {@code X-Client-IP} header. The
     * TCP address fallback keeps this resolver safe in isolated/unit-test use when
     * the global filter has not been applied.
     *
     * <p>Marked {@code @Primary} so that {@code RequestRateLimiterGatewayFilterFactory}
     * can auto-wire a single default resolver without ambiguity. Routes that need
     * per-user buckets reference {@code userKeyResolver} explicitly via SpEL.
     *
     * @return a proxy-aware {@link KeyResolver} backed by client IP
     */
    @Bean
    @Primary
    public KeyResolver remoteAddrKeyResolver() {
        return exchange -> Mono.just(resolveTrustedClientIp(exchange));
    }

    /**
     * Resolves the rate-limit bucket key for authenticated routes.
     *
     * <p>The {@code X-Auth-User} header is injected by
     * {@link com.hotelpms.gateway.filter.AuthenticationFilter} after successful JWT
     * validation, so this resolver must run after that filter in the route filter chain.
     * When the header is present, each authenticated user receives an independent token
     * bucket (prefixed {@code "user:"}). When absent, the resolver falls back to the
     * trusted client IP (prefixed {@code "ip:"}) — never {@code X-Forwarded-For}.
     *
     * @return a {@link KeyResolver} that keys by authenticated username, or by IP as fallback
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            final String user = exchange.getRequest().getHeaders().getFirst("X-Auth-User");
            if (user != null && !user.isBlank()) {
                return Mono.just("user:" + user);
            }
            return Mono.just("ip:" + resolveTrustedClientIp(exchange));
        };
    }

    private static String resolveTrustedClientIp(
            final org.springframework.web.server.ServerWebExchange exchange) {
        final String trustedClientIp = exchange.getRequest().getHeaders()
                .getFirst(com.hotelpms.gateway.filter.ClientIpFilter.CLIENT_IP_HEADER);
        if (trustedClientIp != null && !trustedClientIp.isBlank()) {
            return trustedClientIp;
        }
        return Objects.requireNonNull(
                exchange.getRequest().getRemoteAddress(),
                "Remote address must not be null").getAddress().getHostAddress();
    }
}
