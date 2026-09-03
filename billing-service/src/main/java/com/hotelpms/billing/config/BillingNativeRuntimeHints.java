package com.hotelpms.billing.config;

import com.hotelpms.billing.client.GuestClient;
import com.hotelpms.billing.client.HotelSettingsClient;
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
 * Runtime metadata for the billing Native Image boundary.
 *
 * <p>Spring AOT covers the application beans and JPA entities. These hints
 * cover the dynamic edges that are otherwise only discovered at runtime:
 * Feign's JDK proxies, pageable sort binding, Hibernate UUID arrays, and the
 * dynamically resolved PDF/XML classpath resources.
 */
public final class BillingNativeRuntimeHints implements RuntimeHintsRegistrar {

    private static final BindingReflectionHintsRegistrar BINDING_HINTS =
            new BindingReflectionHintsRegistrar();

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.reflection().registerType(UUID[].class, MemberCategory.UNSAFE_ALLOCATED);
        BINDING_HINTS.registerReflectionHints(hints.reflection(), Sort.Order.class);
        registerFeignProxy(hints, GuestClient.class);
        registerFeignProxy(hints, HotelSettingsClient.class);
        // ResourceHint patterns use Spring's path wildcards, not regexes.
        hints.resources().registerPattern("templates/pdf/**");
        hints.resources().registerPattern("xsd/**");
    }

    private static void registerFeignProxy(final RuntimeHints hints, final Class<?> clientInterface) {
        hints.reflection().registerType(
                clientInterface,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_DECLARED_METHODS);
        hints.proxies().registerJdkProxy(
                clientInterface,
                SpringProxy.class,
                Advised.class,
                DecoratingProxy.class);
    }
}
