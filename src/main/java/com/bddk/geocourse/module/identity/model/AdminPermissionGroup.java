package com.bddk.geocourse.module.identity.model;

import java.io.Serializable;
import java.util.List;

/**
 * 权限分组视图，用于把后台职责和权限码一起返回给前端。
 */
public record AdminPermissionGroup(
        String moduleCode,
        String moduleName,
        List<String> responsibilities,
        List<String> permissionCodes
) implements Serializable {
}
