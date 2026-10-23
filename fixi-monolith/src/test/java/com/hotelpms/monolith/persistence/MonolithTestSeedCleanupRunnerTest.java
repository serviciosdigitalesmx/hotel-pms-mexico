package com.hotelpms.monolith.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

class MonolithTestSeedCleanupRunnerTest {

  @Test
  void removesOnlyKnownSeedAccountDependenciesBeforeDeletingAccounts() {
    final JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
    final List<String> statements = new ArrayList<>();
    doAnswer(invocation -> {
      statements.add(invocation.getArgument(0));
      return 1;
    }).when(jdbcTemplate).update(anyString(), any(Object[].class));
    final MonolithTestSeedCleanupRunner runner = new MonolithTestSeedCleanupRunner(jdbcTemplate);

    runner.run(new DefaultApplicationArguments(new String[0]));

    assertEquals(12, statements.size());
    for (int accountIndex = 0; accountIndex < 2; accountIndex++) {
      final int statementIndex = accountIndex * 6;
      assertEquals(
          "DELETE FROM user_capability_grant WHERE user_id = ?",
          statements.get(statementIndex));
      assertEquals(
          "DELETE FROM user_branch_membership WHERE user_id = ?",
          statements.get(statementIndex + 1));
      assertEquals("DELETE FROM account_token WHERE user_id = ?", statements.get(statementIndex + 2));
      assertEquals(
          "DELETE FROM tenant_invitation WHERE created_by = ?",
          statements.get(statementIndex + 3));
      assertEquals(
          "DELETE FROM platform_admin_grant WHERE user_id = ?",
          statements.get(statementIndex + 4));
      assertEquals(
          "DELETE FROM user_account WHERE id = ? AND username = ?",
          statements.get(statementIndex + 5));
    }
    assertTrue(statements.get(5).contains("user_account"));
    verify(jdbcTemplate, org.mockito.Mockito.times(12)).update(anyString(), any(Object[].class));
  }
}
