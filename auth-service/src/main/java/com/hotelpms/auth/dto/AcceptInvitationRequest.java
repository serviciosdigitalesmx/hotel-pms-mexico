package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Validated data required to accept a tenant invitation.
 *
 * @param token one-time invitation token
 * @param fullName invited user's display name
 * @param username requested unique account username
 * @param password new account password
 */
public record AcceptInvitationRequest(
    @NotBlank String token,
    @NotBlank @Size(max = MAX_FULL_NAME_LENGTH) String fullName,
    @NotBlank @Size(max = MAX_USERNAME_LENGTH) String username,
    @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{12,}$") String password) {
  private static final int MAX_FULL_NAME_LENGTH = 160;
  private static final int MAX_USERNAME_LENGTH = 50;
}
