package com.bddk.geocourse.module.identity.service;

/**
 * 普通用户认证服务。
 */
public interface ConsumerAuthService extends PortalAuthStrategy {

    String PORTAL_CODE = "CONSUMER_PORTAL";
    String PORTAL_NAME = "普通用户端";
    String REQUIRED_ROLE_CODE = "consumer";
}
