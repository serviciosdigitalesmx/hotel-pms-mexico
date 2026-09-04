package com.hotelpms.frontdesk.config;

import com.hotelpms.frontdesk.assistant.PermanentAiProviderException;
import com.hotelpms.frontdesk.assistant.RetryableAiProviderException;
import com.hotelpms.frontdesk.client.BillingClient;
import com.hotelpms.frontdesk.client.GuestClient;
import com.hotelpms.frontdesk.client.NotificationClient;
import com.hotelpms.frontdesk.reservations.validation.DateRangeValidator;
import org.apache.xmpbox.type.TextType;
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
 * Native Image metadata for the consolidated frontdesk bounded context.
 *
 * <p>Spring AOT covers the entities and controllers. These explicit hints retain
 * the UUID-array binding used by Hibernate, pageable sort binding, Feign JDK
 * proxies, and classpath assets used by quotation PDF rendering.</p>
 */
public final class FrontdeskNativeRuntimeHints implements RuntimeHintsRegistrar {

    private static final BindingReflectionHintsRegistrar BINDING_HINTS =
            new BindingReflectionHintsRegistrar();

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.reflection().registerType(UUID[].class, MemberCategory.UNSAFE_ALLOCATED);
        hints.reflection().registerType(PermanentAiProviderException.class);
        hints.reflection().registerType(RetryableAiProviderException.class);
        hints.reflection().registerType(DateRangeValidator.class,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        // XMPBox creates PDF/UA metadata properties by their public constructor.
        hints.reflection().registerType(TextType.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        BINDING_HINTS.registerReflectionHints(hints.reflection(), Sort.Order.class);

        registerFeignProxy(hints, BillingClient.class);
        registerFeignProxy(hints, GuestClient.class);
        registerFeignProxy(hints, NotificationClient.class);

        // ResourceHint patterns use Spring path wildcards. PDFBox resolves the
        // bundled Unicode CMap by name when embedding the Noto Sans fonts.
        hints.resources().registerPattern("templates/pdf/**");
        hints.resources().registerPattern("fonts/**");
        hints.resources().registerPattern("org/apache/fontbox/cmap/Identity-H");
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
