package com.hotelpms.auth.dto;
import com.hotelpms.auth.domain.Role;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record InvitationRequest(@NotBlank @Email String email, @NotNull Role role, UUID branchId) {}
