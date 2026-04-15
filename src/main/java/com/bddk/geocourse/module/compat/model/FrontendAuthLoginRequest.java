package com.bddk.geocourse.module.compat.model;

import jakarta.validation.constraints.NotBlank;

public record FrontendAuthLoginRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password,
        String school
) {
}
