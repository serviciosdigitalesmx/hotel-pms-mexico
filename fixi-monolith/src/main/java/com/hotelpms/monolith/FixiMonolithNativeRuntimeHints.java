package com.hotelpms.monolith;

import java.util.UUID;
import com.hotelpms.auth.config.AuthNativeRuntimeHints;
import com.hotelpms.billing.config.BillingNativeRuntimeHints;
import com.hotelpms.fb.config.FbNativeRuntimeHints;
import com.hotelpms.frontdesk.config.FrontdeskNativeRuntimeHints;
import com.hotelpms.guest.config.GuestNativeRuntimeHints;
import com.hotelpms.notification.config.NotificationRuntimeHints;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Runtime resource hints required by the standalone Fixi Monolith Native Image. */
public final class FixiMonolithNativeRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
    hints.resources().registerPattern("fixi-monolith.yml");
    hints.resources().registerPattern("db/monolith-migrations/**");
    hints.resources().registerPattern("fonts/**");
    hints.reflection().registerType(UUID[].class, MemberCategory.UNSAFE_ALLOCATED);
    new AuthNativeRuntimeHints().registerHints(hints, classLoader);
    new FrontdeskNativeRuntimeHints().registerHints(hints, classLoader);
    new GuestNativeRuntimeHints().registerHints(hints, classLoader);
    new BillingNativeRuntimeHints().registerHints(hints, classLoader);
    new FbNativeRuntimeHints().registerHints(hints, classLoader);
    new NotificationRuntimeHints().registerHints(hints, classLoader);
  }
}
