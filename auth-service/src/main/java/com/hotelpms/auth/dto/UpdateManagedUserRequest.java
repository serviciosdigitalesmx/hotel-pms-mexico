package com.hotelpms.auth.dto;

import com.hotelpms.auth.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Editable non-privileged team profile fields.
 *
 * @param email replacement email address, if provided
 * @param role non-privileged role to assign
 */
public record UpdateManagedUserRequest(
    @Email @Size(max = MAX_EMAIL_LENGTH) String email, @NotNull Role role) {
  private static final int MAX_EMAIL_LENGTH = 254;
}
