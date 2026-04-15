package com.bddk.geocourse.module.identity.service;

import com.bddk.geocourse.module.identity.dal.dataobject.SysRoleDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserDO;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;

import java.util.List;

/**
 * 门户认证公共支撑服务。
 */
public interface IdentityPortalSupportService {

    PortalUserContext authenticate(Long tenantId,
                                   String username,
                                   String rawPassword,
                                   String requiredRoleCode,
                                   String forbiddenMessage);

    PortalUserContext requirePortalUser(Long userId,
                                        String requiredRoleCode,
                                        String forbiddenMessage);

    List<String> getRoleCodes(Long userId);

    List<String> getPermissionCodes(Long userId, String portalCode, String portalName);

    AdminOperatorProfile buildProfile(SysUserDO user, List<SysRoleDO> roles, String portalCode, String portalName);

    AdminPermissionView buildPermissionView(Long tenantId,
                                            List<SysRoleDO> roles,
                                            String portalCode,
                                            String portalName);

    record PortalUserContext(SysUserDO user, List<SysRoleDO> roles) {
    }
}
