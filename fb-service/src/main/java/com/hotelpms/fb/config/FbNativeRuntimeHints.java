package com.hotelpms.fb.config;

import com.hotelpms.fb.client.BillingClient;
import com.hotelpms.fb.client.StayClient;
import com.hotelpms.fb.client.dto.ChargeResponse;
import com.hotelpms.fb.client.dto.StayResponse;
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
 * Runtime metadata required by the F&B Native Image for Feign proxies and
 * Spring Data's pageable/sort binding.
 */
public final class FbNativeRuntimeHints implements RuntimeHintsRegistrar {

    private static final BindingReflectionHintsRegistrar BINDING_HINTS =
            new BindingReflectionHintsRegistrar();

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.reflection().registerType(UUID[].class, MemberCategory.UNSAFE_ALLOCATED);
        BINDING_HINTS.registerReflectionHints(hints.reflection(), Sort.Order.class);
        BINDING_HINTS.registerReflectionHints(hints.reflection(), ChargeResponse.class);
        BINDING_HINTS.registerReflectionHints(hints.reflection(), StayResponse.class);
        registerFeignProxy(hints, BillingClient.class);
        registerFeignProxy(hints, StayClient.class);
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
