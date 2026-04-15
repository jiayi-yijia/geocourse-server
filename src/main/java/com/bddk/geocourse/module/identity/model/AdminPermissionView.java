package com.bddk.geocourse.module.identity.model;

import java.io.Serializable;
import java.util.List;

/**
 * 管理员后台权限视图。
 */
public record AdminPermissionView(
        String portalCode,
        String portalName,
        List<AdminMenuItem> menus,
        List<AdminPermissionGroup> permissionGroups,
        List<String> permissionCodes
) implements Serializable {
}
