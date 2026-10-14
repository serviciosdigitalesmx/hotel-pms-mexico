package com.hotelpms.auth.dto;

import com.hotelpms.auth.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Editable non-privileged team profile fields. */
public record UpdateManagedUserRequest(
    @Email @Size(max = 254) String email,
    @NotNull Role role) { }
