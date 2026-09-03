package com.hotelpms.guest.config;

import com.hotelpms.guest.client.AlloggiatiComuniClient;
import com.hotelpms.guest.client.BillingServiceClient;
import com.hotelpms.guest.client.ReservationClient;
import com.hotelpms.guest.client.StayServiceClient;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.DecoratingProxy;
import org.springframework.data.domain.Sort;

import java.util.UUID;

/**
 * Minimal GraalVM reflection metadata required by Hibernate for UUID identifiers.
 */
public final class GuestNativeRuntimeHints implements RuntimeHintsRegistrar {

    private static final BindingReflectionHintsRegistrar BINDING_HINTS =
            new BindingReflectionHintsRegistrar();

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.reflection().registerType(UUID[].class, MemberCategory.UNSAFE_ALLOCATED);
        BINDING_HINTS.registerReflectionHints(hints.reflection(), Sort.Order.class);
        registerFeignProxy(hints, AlloggiatiComuniClient.class);
        registerFeignProxy(hints, BillingServiceClient.class);
        registerFeignProxy(hints, ReservationClient.class);
        registerFeignProxy(hints, StayServiceClient.class);
    }

    private static void registerFeignProxy(final RuntimeHints hints, final Class<?> clientInterface) {
        hints.proxies().registerJdkProxy(
                clientInterface,
                SpringProxy.class,
                Advised.class,
                DecoratingProxy.class);
    }
}
