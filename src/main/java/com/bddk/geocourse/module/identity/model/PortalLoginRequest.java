package com.bddk.geocourse.module.identity.model;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 统一登录入口请求。
 */
public record PortalLoginRequest(
        @NotBlank(message = "登录类型不能为空")
        String loginType,
        @NotBlank(message = "用户名不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        String password
) implements Serializable {
}
