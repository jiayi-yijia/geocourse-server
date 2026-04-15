package com.bddk.geocourse.module.identity.service;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限回调实现。
 */
@Component
public class AdminStpInterface implements StpInterface {

    private final PortalAuthStrategyFactory portalAuthStrategyFactory;

    public AdminStpInterface(PortalAuthStrategyFactory portalAuthStrategyFactory) {
        this.portalAuthStrategyFactory = portalAuthStrategyFactory;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        try {
            return portalAuthStrategyFactory.getStrategy(loginType).getPermissionCodes(loginId);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            return portalAuthStrategyFactory.getStrategy(loginType).getRoleCodes(loginId);
        } catch (Exception ignored) {
            return List.of();
        }
    }
}
