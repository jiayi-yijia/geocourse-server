package com.bddk.geocourse.module.identity.model;

import java.io.Serializable;
import java.util.List;

/**
 * 当前登录管理员资料。
 */
public record AdminOperatorProfile(
        Long userId,
        String username,
        String displayName,
        String status,
        String portalCode,
        String portalName,
        List<String> roleCodes,
        List<String> roleNames
) implements Serializable {
}
