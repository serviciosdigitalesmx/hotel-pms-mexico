package com.hotelpms.monolith;

import java.util.UUID;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Runtime resource hints required by the standalone Fixi Monolith Native Image. */
public final class FixiMonolithNativeRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
    hints.resources().registerPattern("fixi-monolith.yml");
    hints.reflection().registerType(UUID[].class, MemberCategory.UNSAFE_ALLOCATED);
  }
}
