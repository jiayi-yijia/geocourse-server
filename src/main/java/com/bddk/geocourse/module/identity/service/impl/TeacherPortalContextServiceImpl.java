package com.bddk.geocourse.module.identity.service.impl;

import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.framework.tenant.TenantContextHolder;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserDO;
import com.bddk.geocourse.module.identity.service.IdentityPortalSupportService;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import com.bddk.geocourse.module.identity.stp.StpTeacherUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TeacherPortalContextServiceImpl implements TeacherPortalContextService {

    private static final String ROLE_CODE = "teacher";
    private static final String FORBIDDEN_MESSAGE = "当前账号不具备教师工作台访问权限";

    private final IdentityPortalSupportService identityPortalSupportService;

    public TeacherPortalContextServiceImpl(IdentityPortalSupportService identityPortalSupportService) {
        this.identityPortalSupportService = identityPortalSupportService;
    }

    @Override
    public SysUserDO currentTeacher() {
        StpTeacherUtil.stpLogic.checkLogin();
        Long teacherId = StpTeacherUtil.stpLogic.getLoginIdAsLong();
        IdentityPortalSupportService.PortalUserContext context = identityPortalSupportService.requirePortalUser(
                teacherId,
                ROLE_CODE,
                FORBIDDEN_MESSAGE
        );
        Long requestTenantId = TenantContextHolder.getTenantId();
        if (requestTenantId != null && !Objects.equals(requestTenantId, context.user().getTenantId())) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "教师租户上下文不匹配");
        }
        return context.user();
    }

    @Override
    public Long currentTeacherId() {
        return currentTeacher().getId();
    }

    @Override
    public Long currentTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            return tenantId;
        }
        SysUserDO teacher = currentTeacher();
        if (teacher.getTenantId() == null) {
            throw new ServiceException(ErrorCode.TENANT_REQUIRED);
        }
        return teacher.getTenantId();
    }
}
