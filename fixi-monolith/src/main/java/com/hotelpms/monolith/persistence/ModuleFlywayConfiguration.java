package com.hotelpms.monolith.persistence;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Runs each existing bounded-context migration chain against the shared PostgreSQL database. */
@Configuration(proxyBeanMethods = false)
public class ModuleFlywayConfiguration {

  private static final String[] MODULES = {
    "auth-service",
    "frontdesk-service",
    "guest-service",
    "billing-service",
    "fb-service",
    "notification-service"
  };

  @Bean
  FlywayMigrationStrategy monolithFlywayMigrationStrategy(final DataSource dataSource) {
    return ignored -> {
      for (final String module : MODULES) {
        migrateModule(dataSource, module);
      }
    };
  }

  private static void migrateModule(final DataSource dataSource, final String module) {
    final String historyTable = "flyway_history_" + module.replace('-', '_');
    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/monolith-migrations/" + module)
        .table(historyTable)
        .load()
        .migrate();
  }
}
