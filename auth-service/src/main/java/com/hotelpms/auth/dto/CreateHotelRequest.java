package com.hotelpms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * First-stage tenant provisioning; owner must change the supplied initial password.
 *
 * @param name hotel display name
 * @param slug globally unique hotel slug
 * @param ownerUsername first owner username
 * @param ownerEmail first owner email
 * @param initialPassword temporary password
 */
public record CreateHotelRequest(
        @NotBlank @Size(max = NAME_MAX_LENGTH) String name,
        @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = SLUG_MAX_LENGTH) String slug,
        @NotBlank @Size(max = USERNAME_MAX_LENGTH) String ownerUsername,
        @NotBlank @Email @Size(max = 100) String ownerEmail,
        @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$") String initialPassword) {
    private static final int NAME_MAX_LENGTH = 160;
    private static final int SLUG_MAX_LENGTH = 80;
    private static final int USERNAME_MAX_LENGTH = 50;
}
