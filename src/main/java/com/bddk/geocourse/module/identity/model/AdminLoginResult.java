package com.bddk.geocourse.module.identity.model;

import java.io.Serializable;

/**
 * 管理员登录成功后的返回对象。
 */
public record AdminLoginResult(
        String tokenName,
        String tokenValue,
        String loginType,
        Long loginId,
        Long tokenTimeout,
        AdminOperatorProfile operator,
        AdminPermissionView permissionView
) implements Serializable {
}
