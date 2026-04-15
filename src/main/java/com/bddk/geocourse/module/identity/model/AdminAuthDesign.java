package com.bddk.geocourse.module.identity.model;

import java.io.Serializable;
import java.util.List;

/**
 * 管理员后台认证模块的设计说明对象。
 */
public record AdminAuthDesign(
        String portalCode,
        String portalName,
        String loginType,
        String targetRoleCode,
        String targetRoleName,
        List<String> responsibilities,
        TokenStrategy tokenStrategy,
        List<ApiSpec> apis,
        List<String> securityControls,
        List<String> implementationNotes
) implements Serializable {

    /**
     * Token 策略说明。
     */
    public record TokenStrategy(
            String framework,
            String tokenName,
            String transport,
            Long timeoutSeconds
    ) implements Serializable {
    }

    /**
     * 接口契约说明。
     */
    public record ApiSpec(
            String method,
            String path,
            String description,
            Boolean authenticationRequired
    ) implements Serializable {
    }

}
