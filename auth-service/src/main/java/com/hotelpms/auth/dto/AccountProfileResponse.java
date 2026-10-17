package com.hotelpms.auth.dto;

import com.hotelpms.auth.domain.UserAccount;

/** Safe account profile fields for the authenticated user. */
public record AccountProfileResponse(String username, String fullName, String email, boolean emailVerified) {
  public static AccountProfileResponse from(final UserAccount user) {
    return new AccountProfileResponse(user.getUsername(), user.getFullName(), user.getEmail(), user.isEmailVerified());
  }
}
