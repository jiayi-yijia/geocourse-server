package com.bddk.geocourse.module.compat.model;

public record FrontendAuthUserView(
        Long id,
        String username,
        String school,
        String displayName,
        String avatarUrl,
        String role
) {
}
