package com.bddk.geocourse.module.identity.service;

import com.bddk.geocourse.module.identity.dal.dataobject.SysUserDO;

public interface TeacherPortalContextService {

    SysUserDO currentTeacher();

    Long currentTeacherId();

    Long currentTenantId();
}
