package com.hotelpms.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotelpms.auth.domain.PlatformAdminGrant;
import com.hotelpms.auth.domain.PlatformAuditEvent;
import com.hotelpms.auth.domain.PlatformRole;
import com.hotelpms.auth.domain.UserAccount;
import com.hotelpms.auth.repository.PlatformAdminGrantRepository;
import com.hotelpms.auth.repository.PlatformAuditEventRepository;
import com.hotelpms.auth.repository.UserAccountRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlatformAdminBootstrapRunnerTest {

  private static final String BOOTSTRAP_USERNAME = "verified-platform-operator";

  @Mock private PlatformAdminGrantRepository grants;
  @Mock private UserAccountRepository users;
  @Mock private PlatformAuditEventRepository auditEvents;
  @Mock private SaasIdentityService identities;

  private PlatformAdminBootstrapRunner runner;

  @BeforeEach
  void setUp() {
    runner = new PlatformAdminBootstrapRunner(grants, users, auditEvents, identities);
    ReflectionTestUtils.setField(runner, "username", BOOTSTRAP_USERNAME);
  }

  @Test
  void grantsOnlyActiveVerifiedAccountAndPersistsSafeAudit() {
    final UUID tenantId = UUID.randomUUID();
    final UserAccount account =
        UserAccount.builder()
            .id(UUID.randomUUID())
            .username(BOOTSTRAP_USERNAME)
            .hotelId(tenantId)
            .active(true)
            .emailVerified(true)
            .build();
    when(grants.count()).thenReturn(0L);
    when(users.findByUsernameIncludingInactive(BOOTSTRAP_USERNAME))
        .thenReturn(Optional.of(account));

    runBootstrap();

    final ArgumentCaptor<PlatformAdminGrant> grantCaptor =
        ArgumentCaptor.forClass(PlatformAdminGrant.class);
    verify(grants).save(grantCaptor.capture());
    assertEquals(account, grantCaptor.getValue().getUser());
    assertEquals(PlatformRole.SUPER_ADMIN, grantCaptor.getValue().getPlatformRole());
    assertTrue(grantCaptor.getValue().isActive());

    final ArgumentCaptor<PlatformAuditEvent> auditCaptor =
        ArgumentCaptor.forClass(PlatformAuditEvent.class);
    verify(auditEvents).save(auditCaptor.capture());
    assertEquals("SYSTEM_BOOTSTRAP", auditCaptor.getValue().getActorUsername());
    assertEquals("SUPER_ADMIN_BOOTSTRAPPED", auditCaptor.getValue().getAction());
    assertEquals(tenantId, auditCaptor.getValue().getTargetTenantId());
    assertEquals("{\"platformRole\":\"SUPER_ADMIN\"}", auditCaptor.getValue().getMetadataJson());
  }

  @Test
  void grantsVerifiedTenantlessPlatformIdentityWithoutTenantAuditTarget() {
    final UserAccount account =
        UserAccount.builder()
            .id(UUID.randomUUID())
            .username(BOOTSTRAP_USERNAME)
            .hotelId(null)
            .active(true)
            .emailVerified(true)
            .build();
    when(grants.count()).thenReturn(0L);
    when(users.findByUsernameIncludingInactive(BOOTSTRAP_USERNAME))
        .thenReturn(Optional.of(account));

    runBootstrap();

    final ArgumentCaptor<PlatformAdminGrant> grantCaptor =
        ArgumentCaptor.forClass(PlatformAdminGrant.class);
    verify(grants).save(grantCaptor.capture());
    assertEquals(account, grantCaptor.getValue().getUser());
    final ArgumentCaptor<PlatformAuditEvent> auditCaptor =
        ArgumentCaptor.forClass(PlatformAuditEvent.class);
    verify(auditEvents).save(auditCaptor.capture());
    assertNull(auditCaptor.getValue().getTargetTenantId());
  }

  @Test
  void existingActiveGrantMakesBootstrapIdempotent() {
    when(grants.count()).thenReturn(1L);

    runBootstrap();

    verify(users, never()).findByUsernameIncludingInactive(BOOTSTRAP_USERNAME);
    verify(grants, never()).save(any(PlatformAdminGrant.class));
    verify(auditEvents, never()).save(any(PlatformAuditEvent.class));
  }

  @Test
  void rejectsMissingAccountAndDoesNotCreateGrant() {
    when(grants.count()).thenReturn(0L);
    when(users.findByUsernameIncludingInactive(BOOTSTRAP_USERNAME)).thenReturn(Optional.empty());

    assertThrows(
        IllegalStateException.class,
        () -> runner.run(new DefaultApplicationArguments(new String[0])));

    verify(grants, never()).save(any(PlatformAdminGrant.class));
    verify(auditEvents, never()).save(any(PlatformAuditEvent.class));
  }

  @Test
  void rejectsBlankConfiguredUsernameBeforeLookingUpAnAccount() {
    ReflectionTestUtils.setField(runner, "username", " ");
    when(grants.count()).thenReturn(0L);

    assertThrows(
        IllegalStateException.class,
        () -> runner.run(new DefaultApplicationArguments(new String[0])));

    verify(users, never()).findByUsernameIncludingInactive(any(String.class));
    verify(grants, never()).save(any(PlatformAdminGrant.class));
    verify(auditEvents, never()).save(any(PlatformAuditEvent.class));
  }

  @Test
  void waitsForInactiveAccountToCompleteEmailVerification() {
    final UserAccount account =
        UserAccount.builder()
            .username(BOOTSTRAP_USERNAME)
            .active(false)
            .emailVerified(true)
            .build();
    when(grants.count()).thenReturn(0L);
    when(users.findByUsernameIncludingInactive(BOOTSTRAP_USERNAME))
        .thenReturn(Optional.of(account));

    runBootstrap();

    verify(grants, never()).save(any(PlatformAdminGrant.class));
    verify(auditEvents, never()).save(any(PlatformAuditEvent.class));
  }

  @Test
  void waitsForUnverifiedAccountBeforeCreatingGrant() {
    final UserAccount account =
        UserAccount.builder()
            .username(BOOTSTRAP_USERNAME)
            .active(true)
            .emailVerified(false)
            .build();
    when(grants.count()).thenReturn(0L);
    when(users.findByUsernameIncludingInactive(BOOTSTRAP_USERNAME))
        .thenReturn(Optional.of(account));

    runBootstrap();

    verify(grants, never()).save(any(PlatformAdminGrant.class));
    verify(auditEvents, never()).save(any(PlatformAuditEvent.class));
  }

  @Test
  void provisionsTenantlessIdentityAndWaitsForEmailVerification() {
    ReflectionTestUtils.setField(runner, "email", "operator@example.com");
    ReflectionTestUtils.setField(runner, "fullName", "Platform Operator");
    ReflectionTestUtils.setField(runner, "password", "CI-only-secret");
    when(grants.count()).thenReturn(0L);
    when(users.findByUsernameIncludingInactive(BOOTSTRAP_USERNAME))
        .thenReturn(Optional.empty());

    runBootstrap();

    verify(identities)
        .createPlatformBootstrapIdentity(
            BOOTSTRAP_USERNAME, "operator@example.com", "Platform Operator", "CI-only-secret");
    verify(grants, never()).save(any(PlatformAdminGrant.class));
    verify(auditEvents, never()).save(any(PlatformAuditEvent.class));
  }

  private void runBootstrap() {
    assertDoesNotThrow(() -> runner.run(new DefaultApplicationArguments(new String[0])));
  }
}
