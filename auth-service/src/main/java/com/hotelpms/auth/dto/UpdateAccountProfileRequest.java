package com.hotelpms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Self-service changes to the authenticated account's visible name and contact email. */
public record UpdateAccountProfileRequest(
    @NotBlank @Size(max = 160) String fullName,
    @NotBlank @Email @Size(max = 100) String email) { }
