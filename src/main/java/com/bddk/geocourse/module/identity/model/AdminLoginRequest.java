package com.bddk.geocourse.module.identity.model;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 管理员登录请求。
 */
public record AdminLoginRequest(
        @NotBlank(message = "用户名不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        String password
) implements Serializable {
}
