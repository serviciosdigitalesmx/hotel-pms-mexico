package com.hotelpms.auth.dto;
import jakarta.validation.constraints.*;
public record AcceptInvitationRequest(
    @NotBlank String token,
    @NotBlank @Size(max=160) String fullName,
    @NotBlank @Size(max=50) String username,
    @NotBlank @Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{12,}$") String password) {}
