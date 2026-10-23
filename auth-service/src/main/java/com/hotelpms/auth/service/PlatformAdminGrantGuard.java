package com.hotelpms.auth.service;

import com.hotelpms.auth.repository.PlatformAdminGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Re-checks platform authority against the active database grant on every protected request. */
@Component("platformAdminGrantGuard")
@RequiredArgsConstructor
public class PlatformAdminGrantGuard {
  private final PlatformAdminGrantRepository grants;

  public boolean isActive(final String username) {
    return username != null && grants.existsByUser_UsernameAndActiveTrue(username);
  }
}
