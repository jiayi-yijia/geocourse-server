package com.bddk.geocourse.module.identity.model;

import java.io.Serializable;
import java.util.List;

/**
 * 管理员后台菜单节点。
 */
public record AdminMenuItem(
        String code,
        String name,
        String path,
        String component,
        List<AdminMenuItem> children
) implements Serializable {
}
