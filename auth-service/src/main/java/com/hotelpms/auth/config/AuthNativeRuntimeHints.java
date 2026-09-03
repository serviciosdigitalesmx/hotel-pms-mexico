package com.hotelpms.auth.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.data.domain.Sort;

import java.util.UUID;

/**
 * Runtime metadata for the auth-service Native Image.
 *
 * <p>Spring AOT supplies the application bean metadata. These explicit hints
 * cover the UUID array binding used by Hibernate and the pageable sort binding
 * used by Spring MVC/Data Commons.</p>
 */
public final class AuthNativeRuntimeHints implements RuntimeHintsRegistrar {

    private static final BindingReflectionHintsRegistrar BINDING_HINTS =
            new BindingReflectionHintsRegistrar();

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.reflection().registerType(UUID[].class, MemberCategory.UNSAFE_ALLOCATED);
        BINDING_HINTS.registerReflectionHints(hints.reflection(), Sort.Order.class);
    }
}
