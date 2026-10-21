package com.hotelpms.monolith;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Runtime resource hints required by the standalone Fixi Monolith Native Image. */
public final class FixiMonolithNativeRuntimeHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
    hints.resources().registerPattern("fixi-monolith.yml");
  }
}
