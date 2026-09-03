package com.hotelpms.guest.config;

import java.util.UUID;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * Minimal GraalVM reflection metadata required by Hibernate for UUID identifiers.
 */
public final class GuestNativeRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.reflection().registerType(UUID[].class, MemberCategory.UNSAFE_ALLOCATED);
    }
}
