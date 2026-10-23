package com.hotelpms.auth.service;

import com.hotelpms.auth.repository.PlatformAdminGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Re-checks platform authority against the active database grant on every protected request. */
@Component("platformAdminGrantGuard")
@RequiredArgsConstructor
public class PlatformAdminGrantGuard {
  private final PlatformAdminGrantRepository grants;

  /** Checks the persisted grant so revocation takes effect without waiting for token expiry.
   * @param username authenticated account name
   * @return whether that account has an active platform grant
   */
  public final boolean isActive(final String username) {
    return username != null && grants.hasActiveGrantForUsername(username);
  }
}
