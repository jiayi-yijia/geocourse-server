package com.bddk.geocourse.module.compat.model;

public record FrontendAuthLoginResponse(
        String token,
        FrontendAuthUserView user
) {
}
