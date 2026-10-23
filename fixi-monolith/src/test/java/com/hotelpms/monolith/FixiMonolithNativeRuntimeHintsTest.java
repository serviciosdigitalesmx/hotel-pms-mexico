package com.hotelpms.monolith;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

class FixiMonolithNativeRuntimeHintsTest {

  @Test
  void registersMonolithResourcesAndSharedServiceHints() {
    final RuntimeHints hints = new RuntimeHints();
    new FixiMonolithNativeRuntimeHints().registerHints(hints, getClass().getClassLoader());

    assertTrue(RuntimeHintsPredicates.resource().forResource("fixi-monolith.yml").test(hints));
    assertTrue(
        RuntimeHintsPredicates.resource()
            .forResource("db/monolith-migrations/V24__platform_super_admin_console.sql")
            .test(hints));
    assertTrue(RuntimeHintsPredicates.resource().forResource("fonts/DejaVuSans.ttf").test(hints));
    assertTrue(RuntimeHintsPredicates.reflection().onType(UUID[].class).test(hints));
  }
}
