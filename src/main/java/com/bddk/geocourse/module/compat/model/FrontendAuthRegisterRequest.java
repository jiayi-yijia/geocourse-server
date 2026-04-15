package com.bddk.geocourse.module.compat.model;

import jakarta.validation.constraints.NotBlank;

public record FrontendAuthRegisterRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password,
        @NotBlank(message = "Role is required")
        String role,
        String school
) {
}
