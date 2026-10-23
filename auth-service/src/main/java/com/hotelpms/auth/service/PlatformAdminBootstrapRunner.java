package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.PlatformAdminGrant;
import com.hotelpms.auth.domain.PlatformAuditEvent;
import com.hotelpms.auth.domain.PlatformRole;
import com.hotelpms.auth.repository.PlatformAdminGrantRepository;
import com.hotelpms.auth.repository.PlatformAuditEventRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** One-time, operator-enabled grant for an existing verified Fixi login. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fixi.platform.bootstrap.enabled", havingValue = "true")
public class PlatformAdminBootstrapRunner implements ApplicationRunner {
  private final PlatformAdminGrantRepository grants;
  private final UserAccountRepository users;
  private final PlatformAuditEventRepository auditEvents;

  @Value("${fixi.platform.bootstrap.username:}")
  private String username;

  /**
   * Grants the configured existing account once, only when bootstrap is explicitly enabled.
   *
   * @param args Spring application arguments
   * @implSpec Keep this operation idempotent and require an active, verified account.
   */
  @Override
  @Transactional
  public void run(final ApplicationArguments args) {
    if (grants.countByActiveTrue() > 0) {
      return;
    }
    if (!StringUtils.hasText(username)) {
      throw new IllegalStateException("Platform bootstrap requires an existing username.");
    }
    final var account = users.findByUsername(username.trim())
        .filter(user -> user.isActive() && user.isEmailVerified())
        .orElseThrow(() -> new IllegalStateException("Bootstrap account must exist and be active."));
    final PlatformAdminGrant grant = new PlatformAdminGrant();
    grant.setUser(account);
    grant.setPlatformRole(PlatformRole.SUPER_ADMIN);
    grant.setActive(true);
    grant.setGrantedAt(Instant.now());
    grant.setGrantedBy("SYSTEM_BOOTSTRAP");
    grants.save(grant);

    final PlatformAuditEvent event = new PlatformAuditEvent();
    event.setId(UUID.randomUUID());
    event.setActorUsername("SYSTEM_BOOTSTRAP");
    event.setAction("SUPER_ADMIN_BOOTSTRAPPED");
    event.setTargetTenantId(account.getHotelId());
    event.setOccurredAt(Instant.now());
    event.setMetadataJson("{\"platformRole\":\"SUPER_ADMIN\"}");
    auditEvents.save(event);
  }
}
