package com.smartwallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String token,
            String tokenType,
            Long userId,
            String fullName,
            String email,
            String role
    ) {
        public AuthResponse(String token, Long userId, String fullName, String email, String role) {
            this(token, "Bearer", userId, fullName, email, role);
        }
    }
}
