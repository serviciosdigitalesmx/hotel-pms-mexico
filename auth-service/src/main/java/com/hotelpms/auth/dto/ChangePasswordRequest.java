package com.hotelpms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for password change requests.
 *
 * <p>The caller must supply the current password to prevent an attacker with a
 * stolen access token from silently replacing the victim's credentials.</p>
 *
 * @param currentPassword the user's existing password for identity re-verification
 * @param newPassword     the replacement password (at least 8 chars, 1 uppercase, 1 digit)
 */
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
                message = "PASSWORD_TOO_WEAK") String newPassword) {
}
