package com.bddk.geocourse.module.identity.service;

/**
 * 平台管理员认证服务。
 */
public interface AdminAuthService extends PortalAuthStrategy {

    String PORTAL_CODE = "ADMIN_PORTAL";
    String PORTAL_NAME = "独立后台管理系统";
    String REQUIRED_ROLE_CODE = "admin";
}
