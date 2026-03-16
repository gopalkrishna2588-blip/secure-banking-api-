package com.banking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank @Size(min = 3, max = 50)
        private String username;
        @NotBlank @Size(min = 8)
        private String password;
        @NotBlank @Email
        private String email;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String tokenType = "Bearer";
        private String username;
    }
}