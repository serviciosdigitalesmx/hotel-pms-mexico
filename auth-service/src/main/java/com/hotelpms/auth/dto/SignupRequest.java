package com.hotelpms.auth.dto;
import jakarta.validation.constraints.*;
public record SignupRequest(
    @NotBlank @Size(max=160) String ownerName,
    @NotBlank @Email @Size(max=100) String email,
    @Size(max=100) String username,
    @NotBlank @Pattern(regexp="^(?=.*[A-Z]).{6,}$", message="La contraseña debe tener al menos 6 caracteres y una mayúscula.") String password,
    @NotBlank @Size(max=160) String businessName,
    @Size(max=80) String slug,
    @NotBlank @Size(max=120) String branchName) {}
