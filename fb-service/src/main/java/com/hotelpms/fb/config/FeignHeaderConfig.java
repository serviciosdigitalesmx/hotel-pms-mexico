package com.hotelpms.fb.config;

import com.hotelpms.internalauth.feign.FeignAuthContext;
import com.hotelpms.internalauth.feign.InternalFeignAuthInterceptor;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Feign configuration that signs outgoing calls with the internal HMAC
 * signature so that downstream service {@code InternalAuthFilter} instances
 * accept them (T-GW-07 / T-GST-05). See {@link InternalFeignAuthInterceptor}
 * for the shared signing logic.
 */
@Configuration
public class FeignHeaderConfig {

    private final String hmacSecret;

    /**
     * Constructs the Feign configuration with the shared HMAC secret.
     *
     * @param hmacSecret the internal HMAC secret, shared with all microservices
     */
    public FeignHeaderConfig(@Value("${internal.hmac.secret}") final String hmacSecret) {
        this.hmacSecret = hmacSecret;
    }

    /**
     * Registers the shared {@link InternalFeignAuthInterceptor}. This service
     * has no calls originating outside an HTTP request context, so the
     * fallback always resolves to empty.
     *
     * @return the configured interceptor
     */
    @Bean
    public RequestInterceptor authHeaderInterceptor() {
        return new InternalFeignAuthInterceptor(hmacSecret, FeignHeaderConfig::resolveSecurityContext);
    }

    private static Optional<FeignAuthContext> resolveSecurityContext() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null
                || !(authentication.getDetails() instanceof String hotelId)
                || authentication.getAuthorities().isEmpty()) {
            return Optional.empty();
        }
        final String role = authentication.getAuthorities().iterator().next().getAuthority()
                .replaceFirst("^ROLE_", "");
        return Optional.of(new FeignAuthContext(authentication.getName(), role, hotelId));
    }
}
