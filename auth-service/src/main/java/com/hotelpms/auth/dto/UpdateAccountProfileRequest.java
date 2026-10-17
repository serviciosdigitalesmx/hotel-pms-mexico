package com.hotelpms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Self-service changes to the authenticated account's visible name and contact email.
 *
 * @param fullName display name for the signed-in account
 * @param email contact email address to verify when changed
 */
public record UpdateAccountProfileRequest(
    @NotBlank @Size(max = MAX_FULL_NAME_LENGTH) String fullName,
    @NotBlank @Email @Size(max = 100) String email) {
  static final int MAX_FULL_NAME_LENGTH = 160;
}
