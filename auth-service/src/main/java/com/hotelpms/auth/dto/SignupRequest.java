package com.hotelpms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Validated payload for creating the initial Fixi tenant owner.
 *
 * @param ownerName owner display name
 * @param email account email and username
 * @param username optional legacy username field
 * @param password initial password
 * @param businessName tenant display name
 * @param slug optional tenant slug
 * @param branchName name of the initial branch
 */
public record SignupRequest(
    @NotBlank @Size(max = MAX_OWNER_NAME_LENGTH) String ownerName,
    @NotBlank @Email @Size(max = MAX_EMAIL_LENGTH) String email,
    @Size(max = MAX_USERNAME_LENGTH) String username,
    @NotBlank
        @Pattern(
            regexp = "^(?=.*[A-Z]).{6,}$",
            message = "La contraseña debe tener al menos 6 caracteres y una mayúscula.")
        String password,
    @NotBlank @Size(max = MAX_BUSINESS_NAME_LENGTH) String businessName,
    @Size(max = MAX_SLUG_LENGTH) String slug,
    @NotBlank @Size(max = MAX_BRANCH_NAME_LENGTH) String branchName) {
  private static final int MAX_OWNER_NAME_LENGTH = 160;
  private static final int MAX_EMAIL_LENGTH = 100;
  private static final int MAX_USERNAME_LENGTH = 100;
  private static final int MAX_BUSINESS_NAME_LENGTH = 160;
  private static final int MAX_SLUG_LENGTH = 80;
  private static final int MAX_BRANCH_NAME_LENGTH = 120;
}
