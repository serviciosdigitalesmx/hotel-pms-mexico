package com.hotelpms.monolith.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;

/** Provides the migrations embedded in a Native Image without classpath directory scanning. */
final class EmbeddedMigrationResourceProvider implements ResourceProvider {

  private static final String INDEX = "db/monolith-migrations/index.txt";
  private static final String MIGRATIONS_ROOT = "db/monolith-migrations/";

  private final ClassLoader classLoader;
  private final List<String> migrationPaths;

  EmbeddedMigrationResourceProvider(final String module, final ClassLoader classLoader) {
    this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    final String modulePrefix = MIGRATIONS_ROOT + module + "/";
    this.migrationPaths = readIndex().stream().filter(path -> path.startsWith(modulePrefix)).toList();
    if (migrationPaths.isEmpty()) {
      throw new FlywayException("No embedded SQL migrations registered for module " + module);
    }
  }

  @Override
  public LoadableResource getResource(final String name) {
    return migrationPaths.stream()
        .filter(path -> path.equals(name) || path.endsWith("/" + name))
        .findFirst()
        .<LoadableResource>map(path -> new EmbeddedMigrationResource(path, classLoader))
        .orElse(null);
  }

  @Override
  public Collection<LoadableResource> getResources(final String prefix, final String[] suffixes) {
    final List<LoadableResource> resources = new ArrayList<>();
    for (final String path : migrationPaths) {
      final String filename = path.substring(path.lastIndexOf('/') + 1);
      final boolean matchesPrefix = filename.startsWith(prefix);
      final boolean matchesSuffix = suffixes == null || suffixes.length == 0
          || java.util.Arrays.stream(suffixes).anyMatch(filename::endsWith);
      if (matchesPrefix && matchesSuffix) {
        resources.add(new EmbeddedMigrationResource(path, classLoader));
      }
    }
    return resources;
  }

  private List<String> readIndex() {
    try (InputStream stream = classLoader.getResourceAsStream(INDEX)) {
      if (stream == null) {
        throw new FlywayException("Embedded Flyway migration index is missing: " + INDEX);
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
        return reader.lines().filter(path -> !path.isBlank()).toList();
      }
    } catch (IOException exception) {
      throw new FlywayException("Unable to read embedded Flyway migration index: " + INDEX, exception);
    }
  }

  private static final class EmbeddedMigrationResource extends LoadableResource {
    private final String path;
    private final ClassLoader classLoader;

    private EmbeddedMigrationResource(final String path, final ClassLoader classLoader) {
      this.path = path;
      this.classLoader = classLoader;
    }

    @Override
    public Reader read() {
      final InputStream stream = classLoader.getResourceAsStream(path);
      if (stream == null) {
        throw new FlywayException("Embedded Flyway migration is missing: " + path);
      }
      return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }

    @Override
    public String getAbsolutePath() {
      return "/" + path;
    }

    @Override
    public String getAbsolutePathOnDisk() {
      return "classpath:/" + path;
    }

    @Override
    public String getFilename() {
      return path.substring(path.lastIndexOf('/') + 1);
    }

    @Override
    public String getRelativePath() {
      return getFilename();
    }
  }
}
