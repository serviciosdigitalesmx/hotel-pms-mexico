package com.hotelpms.monolith.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resource.LoadableResource;
import org.junit.jupiter.api.Test;

class EmbeddedMigrationResourceProviderTest {

  private static final String INDEX = "db/monolith-migrations/index.txt";
  private static final String AUTH_MIGRATION =
      "db/monolith-migrations/auth-service/V1__create_users.sql";

  @Test
  void exposesOnlyIndexedMigrationsForTheRequestedModule() throws IOException {
    final Map<String, String> resources = new HashMap<>();
    resources.put(
        INDEX,
        AUTH_MIGRATION
            + "\n"
            + "db/monolith-migrations/billing-service/V1__create_invoice.sql\n");
    resources.put(AUTH_MIGRATION, "CREATE TABLE users (id UUID PRIMARY KEY);");
    final EmbeddedMigrationResourceProvider provider =
        new EmbeddedMigrationResourceProvider("auth-service", classLoader(resources));

    final LoadableResource migration = provider.getResource("V1__create_users.sql");
    assertEquals("/" + AUTH_MIGRATION, migration.getAbsolutePath());
    assertEquals("classpath:/" + AUTH_MIGRATION, migration.getAbsolutePathOnDisk());
    assertEquals("V1__create_users.sql", migration.getFilename());
    assertEquals("V1__create_users.sql", migration.getRelativePath());
    assertEquals(
        "CREATE TABLE users (id UUID PRIMARY KEY);", readAll(migration));
    assertNull(provider.getResource("V1__create_invoice.sql"));

    final Collection<LoadableResource> sqlFiles =
        provider.getResources("V1__", new String[] {".sql"});
    assertEquals(1, sqlFiles.size());
    assertEquals("V1__create_users.sql", sqlFiles.iterator().next().getFilename());
    assertEquals(0, provider.getResources("V1__", new String[] {".yaml"}).size());
  }

  @Test
  void rejectsMissingIndexAndMissingIndexedMigration() {
    assertThrows(
        FlywayException.class,
        () -> new EmbeddedMigrationResourceProvider("auth-service", classLoader(Map.of())));

    final EmbeddedMigrationResourceProvider provider =
        new EmbeddedMigrationResourceProvider(
            "auth-service", classLoader(Map.of(INDEX, AUTH_MIGRATION + "\n")));
    assertThrows(
        FlywayException.class,
        () -> readAll(provider.getResource("V1__create_users.sql")));
    assertThrows(
        FlywayException.class,
        () ->
            new EmbeddedMigrationResourceProvider(
                "unknown-service", classLoader(Map.of(INDEX, ""))));
  }

  private static ClassLoader classLoader(final Map<String, String> resources) {
    return new ClassLoader(null) {
      @Override
      public java.io.InputStream getResourceAsStream(final String name) {
        final String contents = resources.get(name);
        return contents == null
            ? null
            : new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
      }
    };
  }

  private static String readAll(final LoadableResource resource) throws IOException {
    try (Reader reader = resource.read()) {
      final StringBuilder contents = new StringBuilder();
      final char[] buffer = new char[128];
      int charactersRead;
      while ((charactersRead = reader.read(buffer)) != -1) {
        contents.append(buffer, 0, charactersRead);
      }
      return contents.toString();
    }
  }
}
