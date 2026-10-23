package com.hotelpms.monolith.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Removes only the fixed development/E2E accounts seeded by the legacy auth migrations. */
@Component
@ConditionalOnProperty(
    prefix = "fixi.bootstrap",
    name = "remove-test-users-on-startup",
    havingValue = "true")
public class MonolithTestSeedCleanupRunner implements ApplicationRunner {

  private static final List<SeedAccount> TEST_ACCOUNTS =
      List.of(
          new SeedAccount(
              UUID.fromString("00000000-0000-0000-0000-000000000000"), "admin"),
          new SeedAccount(
              UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
              "e2e-live-other-hotel-admin"));

  private final JdbcTemplate jdbcTemplate;

  public MonolithTestSeedCleanupRunner(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  @Transactional
  public void run(final ApplicationArguments args) {
    for (final SeedAccount account : TEST_ACCOUNTS) {
      jdbcTemplate.update(
          "DELETE FROM user_capability_grant WHERE user_id = ?",
          account.id());
      jdbcTemplate.update(
          "DELETE FROM user_branch_membership WHERE user_id = ?",
          account.id());
      jdbcTemplate.update("DELETE FROM account_token WHERE user_id = ?", account.id());
      jdbcTemplate.update(
          "DELETE FROM tenant_invitation WHERE created_by = ?",
          account.id());
      jdbcTemplate.update(
          "DELETE FROM platform_admin_grant WHERE user_id = ?",
          account.id());
      jdbcTemplate.update(
          "DELETE FROM user_account WHERE id = ? AND username = ?",
          account.id(),
          account.username());
    }
  }

  private record SeedAccount(UUID id, String username) {}
}
