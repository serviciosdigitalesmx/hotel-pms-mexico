package com.hotelpms.auth.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlatformPageTest {
  @Test
  void pageCopiesAndProtectsItsContent() {
    final List<String> source = new ArrayList<>(List.of("tenant-a"));
    final PlatformPage<String> page = new PlatformPage<>(source, 0, 25, 1, 1);

    source.add("tenant-b");

    assertEquals(List.of("tenant-a"), page.content());
    assertThrows(UnsupportedOperationException.class, () -> page.content().add("tenant-c"));
  }
}
