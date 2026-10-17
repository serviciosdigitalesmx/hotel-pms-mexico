package com.hotelpms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Email address used to request account recovery or verification.
 *
 * @param email account email address
 */
public record ForgotPasswordRequest(@NotBlank @Email String email) {
}
