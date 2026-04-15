package com.bddk.geocourse.module.identity.service;

/**
 * 学生认证服务。
 */
public interface StudentAuthService extends PortalAuthStrategy {

    String PORTAL_CODE = "STUDENT_PORTAL";
    String PORTAL_NAME = "学生端";
    String REQUIRED_ROLE_CODE = "student";
}
