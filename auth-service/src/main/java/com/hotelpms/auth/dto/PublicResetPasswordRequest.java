package com.hotelpms.auth.dto;
import jakarta.validation.constraints.*;
public record PublicResetPasswordRequest(
    @NotBlank String token,
    @NotBlank @Pattern(regexp="^(?=.*[A-Z]).{6,}$", message="La contraseña debe tener al menos 6 caracteres y una mayúscula.") String password) {}
