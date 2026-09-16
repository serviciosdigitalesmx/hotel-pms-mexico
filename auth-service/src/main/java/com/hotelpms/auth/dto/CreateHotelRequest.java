package com.hotelpms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** First-stage tenant provisioning; owner must change the supplied initial password. */
public record CreateHotelRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 80) String slug,
        @NotBlank @Size(max = 50) String ownerUsername,
        @NotBlank @Email @Size(max = 100) String ownerEmail,
        @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$") String initialPassword) { }
