package com.bddk.geocourse.module.identity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.identity.dal.dataobject.SysMenuDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysRoleDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysRoleMenuDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserRoleDO;
import com.bddk.geocourse.module.identity.dal.mapper.SysMenuMapper;
import com.bddk.geocourse.module.identity.dal.mapper.SysRoleMapper;
import com.bddk.geocourse.module.identity.dal.mapper.SysRoleMenuMapper;
import com.bddk.geocourse.module.identity.dal.mapper.SysUserMapper;
import com.bddk.geocourse.module.identity.dal.mapper.SysUserRoleMapper;
import com.bddk.geocourse.module.identity.model.AdminMenuItem;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionGroup;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.service.IdentityPortalSupportService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 门户认证公共能力实现。
 */
@Service
public class IdentityPortalSupportServiceImpl implements IdentityPortalSupportService {

    private static final int STATUS_ENABLED = 1;
    private static final String MENU_TYPE_BUTTON = "BUTTON";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public IdentityPortalSupportServiceImpl(SysUserMapper sysUserMapper,
                                            SysRoleMapper sysRoleMapper,
                                            SysMenuMapper sysMenuMapper,
                                            SysUserRoleMapper sysUserRoleMapper,
                                            SysRoleMenuMapper sysRoleMenuMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
    }

    @Override
    public PortalUserContext authenticate(Long tenantId,
                                          String username,
                                          String rawPassword,
                                          String requiredRoleCode,
                                          String forbiddenMessage) {
        SysUserDO user = findEnabledUserByUsername(tenantId, username);
        if (user == null || !matchesPassword(rawPassword, user.getPasswordHash())) {
            throw new ServiceException(ErrorCode.AUTH_LOGIN_FAILED);
        }
        return requirePortalUser(user, requiredRoleCode, forbiddenMessage);
    }

    @Override
    public PortalUserContext requirePortalUser(Long userId,
                                               String requiredRoleCode,
                                               String forbiddenMessage) {
        SysUserDO user = findEnabledUserById(userId);
        return requirePortalUser(user, requiredRoleCode, forbiddenMessage);
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        SysUserDO user = findEnabledUserById(userId);
        return listUserRoles(user.getTenantId(), user.getId()).stream()
                .map(SysRoleDO::getRoleCode)
                .distinct()
                .toList();
    }

    @Override
    public List<String> getPermissionCodes(Long userId, String portalCode, String portalName) {
        SysUserDO user = findEnabledUserById(userId);
        return buildPermissionView(user.getTenantId(), listUserRoles(user.getTenantId(), user.getId()), portalCode, portalName)
                .permissionCodes();
    }

    @Override
    public AdminOperatorProfile buildProfile(SysUserDO user, List<SysRoleDO> roles, String portalCode, String portalName) {
        List<String> roleCodes = roles.stream().map(SysRoleDO::getRoleCode).distinct().toList();
        List<String> roleNames = roles.stream().map(SysRoleDO::getRoleName).distinct().toList();
        String displayName = StringUtils.hasText(user.getRealName()) ? user.getRealName()
                : StringUtils.hasText(user.getNickname()) ? user.getNickname()
                : user.getUsername();
        return new AdminOperatorProfile(
                user.getId(),
                user.getUsername(),
                displayName,
                Objects.equals(user.getStatus(), STATUS_ENABLED) ? "ACTIVE" : "DISABLED",
                portalCode,
                portalName,
                roleCodes,
                roleNames
        );
    }

    @Override
    public AdminPermissionView buildPermissionView(Long tenantId,
                                                   List<SysRoleDO> roles,
                                                   String portalCode,
                                                   String portalName) {
        List<Long> roleIds = roles.stream().map(SysRoleDO::getId).distinct().toList();
        if (roleIds.isEmpty()) {
            return new AdminPermissionView(portalCode, portalName, List.of(), List.of(), List.of());
        }

        List<Long> menuIds = sysRoleMenuMapper.selectList(Wrappers.<SysRoleMenuDO>lambdaQuery()
                        .eq(SysRoleMenuDO::getTenantId, tenantId)
                        .in(SysRoleMenuDO::getRoleId, roleIds)
                        .eq(SysRoleMenuDO::getStatus, STATUS_ENABLED))
                .stream()
                .map(SysRoleMenuDO::getMenuId)
                .distinct()
                .toList();
        if (menuIds.isEmpty()) {
            return new AdminPermissionView(portalCode, portalName, List.of(), List.of(), List.of());
        }

        List<SysMenuDO> menus = sysMenuMapper.selectList(Wrappers.<SysMenuDO>lambdaQuery()
                        .eq(SysMenuDO::getTenantId, tenantId)
                        .in(SysMenuDO::getId, menuIds)
                        .eq(SysMenuDO::getStatus, STATUS_ENABLED)
                        .orderByAsc(SysMenuDO::getSortNo)
                        .orderByAsc(SysMenuDO::getId))
                .stream()
                .toList();

        Map<Long, SysMenuDO> menuById = menus.stream().collect(Collectors.toMap(SysMenuDO::getId, menu -> menu));
        List<AdminMenuItem> menuTree = buildMenuTree(0L, menus);
        List<String> permissionCodes = menus.stream()
                .map(SysMenuDO::getPermissionCode)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        List<AdminPermissionGroup> permissionGroups = buildPermissionGroups(menus, menuById);

        return new AdminPermissionView(portalCode, portalName, menuTree, permissionGroups, permissionCodes);
    }

    private PortalUserContext requirePortalUser(SysUserDO user,
                                                String requiredRoleCode,
                                                String forbiddenMessage) {
        List<SysRoleDO> roles = listUserRoles(user.getTenantId(), user.getId());
        boolean matched = roles.stream().anyMatch(role -> requiredRoleCode.equalsIgnoreCase(role.getRoleCode()));
        if (!matched) {
            throw new ServiceException(ErrorCode.FORBIDDEN, forbiddenMessage);
        }
        return new PortalUserContext(user, roles);
    }

    private SysUserDO findEnabledUserByUsername(Long tenantId, String username) {
        return sysUserMapper.selectOne(Wrappers.<SysUserDO>lambdaQuery()
                .eq(SysUserDO::getTenantId, tenantId)
                .eq(SysUserDO::getUsername, username)
                .eq(SysUserDO::getStatus, STATUS_ENABLED)
                .last("limit 1"));
    }

    private SysUserDO findEnabledUserById(Long userId) {
        SysUserDO user = sysUserMapper.selectOne(Wrappers.<SysUserDO>lambdaQuery()
                .eq(SysUserDO::getId, userId)
                .eq(SysUserDO::getStatus, STATUS_ENABLED)
                .last("limit 1"));
        if (user == null) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    private List<SysRoleDO> listUserRoles(Long tenantId, Long userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(Wrappers.<SysUserRoleDO>lambdaQuery()
                        .eq(SysUserRoleDO::getTenantId, tenantId)
                        .eq(SysUserRoleDO::getUserId, userId)
                        .eq(SysUserRoleDO::getStatus, STATUS_ENABLED))
                .stream()
                .map(SysUserRoleDO::getRoleId)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleMapper.selectList(Wrappers.<SysRoleDO>lambdaQuery()
                        .eq(SysRoleDO::getTenantId, tenantId)
                        .in(SysRoleDO::getId, roleIds)
                        .eq(SysRoleDO::getStatus, STATUS_ENABLED))
                .stream()
                .sorted(Comparator.comparing(SysRoleDO::getId))
                .toList();
    }

    private boolean matchesPassword(String rawPassword, String passwordHash) {
        if (!StringUtils.hasText(passwordHash)) {
            return false;
        }
        if (passwordHash.startsWith("$2a$") || passwordHash.startsWith("$2b$") || passwordHash.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, passwordHash);
        }
        return Objects.equals(rawPassword, passwordHash);
    }

    private List<AdminMenuItem> buildMenuTree(Long parentId, List<SysMenuDO> menus) {
        return menus.stream()
                .filter(menu -> Objects.equals(menu.getParentId(), parentId))
                .filter(menu -> !MENU_TYPE_BUTTON.equalsIgnoreCase(menu.getMenuType()))
                .filter(menu -> Objects.equals(menu.getVisible(), STATUS_ENABLED))
                .map(menu -> new AdminMenuItem(
                        buildMenuCode(menu),
                        menu.getMenuName(),
                        menu.getRoutePath(),
                        menu.getComponentPath(),
                        buildMenuTree(menu.getId(), menus)
                ))
                .toList();
    }

    private List<AdminPermissionGroup> buildPermissionGroups(List<SysMenuDO> menus, Map<Long, SysMenuDO> menuById) {
        Map<Long, List<SysMenuDO>> groupMenus = new LinkedHashMap<>();
        for (SysMenuDO menu : menus) {
            if (!StringUtils.hasText(menu.getPermissionCode())) {
                continue;
            }
            Long rootId = findRootMenuId(menu, menuById);
            groupMenus.computeIfAbsent(rootId, key -> new ArrayList<>()).add(menu);
        }

        List<AdminPermissionGroup> groups = new ArrayList<>();
        for (Map.Entry<Long, List<SysMenuDO>> entry : groupMenus.entrySet()) {
            SysMenuDO root = menuById.get(entry.getKey());
            List<String> codes = entry.getValue().stream()
                    .map(SysMenuDO::getPermissionCode)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .toList();
            if (root == null || codes.isEmpty()) {
                continue;
            }
            groups.add(new AdminPermissionGroup(
                    buildMenuCode(root),
                    root.getMenuName(),
                    List.of("访问" + root.getMenuName() + "相关功能"),
                    codes
            ));
        }
        return groups;
    }

    private Long findRootMenuId(SysMenuDO menu, Map<Long, SysMenuDO> menuById) {
        SysMenuDO current = menu;
        while (current != null && current.getParentId() != null && current.getParentId() != 0L) {
            current = menuById.get(current.getParentId());
        }
        return current != null ? current.getId() : menu.getId();
    }

    private String buildMenuCode(SysMenuDO menu) {
        if (StringUtils.hasText(menu.getPermissionCode())) {
            return menu.getPermissionCode();
        }
        if (StringUtils.hasText(menu.getRoutePath())) {
            return menu.getRoutePath();
        }
        return "menu:" + menu.getId();
    }
}
