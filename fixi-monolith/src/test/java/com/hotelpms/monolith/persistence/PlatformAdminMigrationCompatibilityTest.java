package com.hotelpms.monolith.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PlatformAdminMigrationCompatibilityTest {
  private static final String MIGRATIONS = "classpath:db/monolith-migrations/auth-service";
  private static final String HISTORY_TABLE = "flyway_history_auth_service";
  private static final UUID ACTIVE_TENANT = UUID.fromString("20000000-0000-0000-0000-000000000001");
  private static final UUID INACTIVE_TENANT = UUID.fromString("20000000-0000-0000-0000-000000000002");

  @Container
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Test
  void v24PreservesExistingPlanHistoryAndCreatesNoSuperAdminGrants() {
    final JdbcTemplate jdbc = jdbc();
    flyway(MigrationVersion.fromVersion("23")).migrate();
    insertLegacyTenantAndPlan(jdbc, ACTIVE_TENANT, "legacy-active", true);
    insertLegacyTenantAndPlan(jdbc, INACTIVE_TENANT, "legacy-inactive", false);

    flyway(null).migrate();

    assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM platform_tenant_subscription", Integer.class));
    assertEquals(1, jdbc.queryForObject(
        "SELECT count(*) FROM platform_tenant_subscription WHERE tenant_id = ? AND plan_key = ?"
            + " AND status = 'ACTIVE' AND ends_at IS NULL",
        Integer.class, ACTIVE_TENANT, "legacy-active"));
    assertEquals(1, jdbc.queryForObject(
        "SELECT count(*) FROM platform_tenant_subscription WHERE tenant_id = ? AND plan_key = ?"
            + " AND status = 'ENDED' AND ends_at IS NOT NULL",
        Integer.class, INACTIVE_TENANT, "legacy-inactive"));
    assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM platform_admin_grant", Integer.class));
    assertNotNull(jdbc.queryForObject(
        "SELECT indexname FROM pg_indexes WHERE indexname = 'uq_platform_subscription_active_tenant'",
        String.class));
    assertEquals(1, jdbc.queryForObject(
        "SELECT count(*) FROM flyway_history_auth_service WHERE version = '24' AND success",
        Integer.class));
  }

  private static JdbcTemplate jdbc() {
    final DriverManagerDataSource dataSource = new DriverManagerDataSource(
        POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    dataSource.setDriverClassName("org.postgresql.Driver");
    return new JdbcTemplate(dataSource);
  }

  private static Flyway flyway(final MigrationVersion target) {
    final var configuration = Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .locations(MIGRATIONS)
        .table(HISTORY_TABLE)
        .baselineOnMigrate(true)
        .baselineVersion(MigrationVersion.fromVersion("0"));
    if (target != null) {
      configuration.target(target);
    }
    return configuration.load();
  }

  private static void insertLegacyTenantAndPlan(
      final JdbcTemplate jdbc, final UUID tenantId, final String planKey, final boolean active) {
    jdbc.update("INSERT INTO hotel_registry (id, name, slug) VALUES (?, ?, ?)",
        tenantId, planKey, planKey);
    jdbc.update(
        "INSERT INTO tenant_plan (tenant_id, plan_key, active) VALUES (?, ?, ?)",
        tenantId, planKey, active);
  }
}
