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

/** One-time, operator-enabled provisioning or grant for the platform operator identity. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fixi.platform.bootstrap.enabled", havingValue = "true")
public class PlatformAdminBootstrapRunner implements ApplicationRunner {
  private final PlatformAdminGrantRepository grants;
  private final UserAccountRepository users;
  private final PlatformAuditEventRepository auditEvents;
  private final SaasIdentityService identities;

  @Value("${fixi.platform.bootstrap.username:}")
  private String username;

  @Value("${fixi.platform.bootstrap.email:}")
  private String email;

  @Value("${fixi.platform.bootstrap.full-name:}")
  private String fullName;

  @Value("${fixi.platform.bootstrap.password:}")
  private String password;

  /**
   * Provisions an isolated identity for verification, then grants it after verification.
   *
   * @param args Spring application arguments
   * @implSpec Keep this operation one-time; only an active, verified account receives the grant.
   */
  @Override
  @Transactional
  public void run(final ApplicationArguments args) {
    if (grants.count() > 0) {
      return;
    }
    if (!StringUtils.hasText(username)) {
      throw new IllegalStateException("Platform bootstrap requires a configured username.");
    }
    final var configuredUsername = username.trim();
    final var account = users.findByUsernameIncludingInactive(configuredUsername);
    if (account.isEmpty()) {
      if (!StringUtils.hasText(email)
          || !StringUtils.hasText(fullName)
          || !StringUtils.hasText(password)) {
        throw new IllegalStateException(
            "Platform bootstrap needs an existing verified account or complete identity settings.");
      }
      identities.createPlatformBootstrapIdentity(configuredUsername, email, fullName, password);
      return;
    }
    if (!account.get().isActive() || !account.get().isEmailVerified()) {
      return;
    }
    final PlatformAdminGrant grant = new PlatformAdminGrant();
    grant.setUser(account.get());
    grant.setPlatformRole(PlatformRole.SUPER_ADMIN);
    grant.setActive(true);
    grant.setGrantedAt(Instant.now());
    grant.setGrantedBy("SYSTEM_BOOTSTRAP");
    grants.save(grant);

    final PlatformAuditEvent event = new PlatformAuditEvent();
    event.setId(UUID.randomUUID());
    event.setActorUsername("SYSTEM_BOOTSTRAP");
    event.setAction("SUPER_ADMIN_BOOTSTRAPPED");
    event.setTargetTenantId(account.get().getHotelId());
    event.setOccurredAt(Instant.now());
    event.setMetadataJson("{\"platformRole\":\"SUPER_ADMIN\"}");
    auditEvents.save(event);
  }
}
