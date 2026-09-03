package com.hotelpms.gateway.config;

import com.hotelpms.gateway.filter.AuthenticationFilter;
import com.hotelpms.gateway.filter.PublicBookingFilter;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * Native metadata for Spring Cloud Gateway's WebFlux route filter factories.
 *
 * <p>The route definitions instantiate their filter {@code Config} objects from
 * the generic factory type at runtime. These are gateway-specific constructor
 * hints; no MVC controller, JPA entity, or servlet metadata is carried into the
 * reactive gateway image.</p>
 */
public final class GatewayNativeRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.reflection().registerType(
                AuthenticationFilter.Config.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(
                PublicBookingFilter.Config.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
}
