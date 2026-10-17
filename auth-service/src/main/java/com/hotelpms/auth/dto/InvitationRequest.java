package com.hotelpms.auth.dto;

import com.hotelpms.auth.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Tenant-scoped invitation details.
 *
 * @param email recipient email address
 * @param role non-privileged role to assign
 * @param branchId optional branch membership
 */
public record InvitationRequest(@NotBlank @Email String email, @NotNull Role role, UUID branchId) {
}
