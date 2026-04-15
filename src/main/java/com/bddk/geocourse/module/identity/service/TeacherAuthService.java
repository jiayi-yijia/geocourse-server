package com.bddk.geocourse.module.identity.service;

/**
 * 教师认证服务。
 */
public interface TeacherAuthService extends PortalAuthStrategy {

    String PORTAL_CODE = "TEACHER_WORKBENCH";
    String PORTAL_NAME = "教师工作台";
    String REQUIRED_ROLE_CODE = "teacher";
}
