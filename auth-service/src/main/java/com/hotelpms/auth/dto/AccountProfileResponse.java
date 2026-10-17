package com.hotelpms.auth.dto;

import com.hotelpms.auth.domain.UserAccount;

/** Safe account profile fields for the authenticated user.
 *
 * @param username stable login name
 * @param fullName display name
 * @param email contact email address
 * @param emailVerified whether the contact email was verified
 */
public record AccountProfileResponse(String username, String fullName, String email, boolean emailVerified) {
  /** Projects only fields safe for the authenticated account's profile screen.
   *
   * @param user authenticated account
   * @return safe profile response
   */
  public static AccountProfileResponse from(final UserAccount user) {
    return new AccountProfileResponse(user.getUsername(), user.getFullName(), user.getEmail(), user.isEmailVerified());
  }
}
