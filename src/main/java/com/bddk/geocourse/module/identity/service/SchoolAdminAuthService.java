package com.bddk.geocourse.module.identity.service;

/**
 * 学校管理员认证服务。
 */
public interface SchoolAdminAuthService extends PortalAuthStrategy {

    String PORTAL_CODE = "SCHOOL_ADMIN_PORTAL";
    String PORTAL_NAME = "学校独立管理后台";
    String REQUIRED_ROLE_CODE = "school_admin";
}
