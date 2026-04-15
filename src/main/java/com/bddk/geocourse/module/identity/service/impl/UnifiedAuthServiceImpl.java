package com.bddk.geocourse.module.identity.service.impl;

import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginRequest;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.model.PortalLoginRequest;
import com.bddk.geocourse.module.identity.service.PortalAuthStrategy;
import com.bddk.geocourse.module.identity.service.PortalAuthStrategyFactory;
import com.bddk.geocourse.module.identity.service.UnifiedAuthService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 统一登录入口服务实现。
 */
@Service
public class UnifiedAuthServiceImpl implements UnifiedAuthService {

    private final PortalAuthStrategyFactory portalAuthStrategyFactory;

    public UnifiedAuthServiceImpl(PortalAuthStrategyFactory portalAuthStrategyFactory) {
        this.portalAuthStrategyFactory = portalAuthStrategyFactory;
    }

    @Override
    public List<AdminAuthDesign> listDesigns() {
        return portalAuthStrategyFactory.listDesigns();
    }

    @Override
    public AdminAuthDesign getDesign(String loginType) {
        return portalAuthStrategyFactory.getStrategy(loginType).getDesign();
    }

    @Override
    public AdminLoginResult login(PortalLoginRequest request) {
        PortalAuthStrategy strategy = portalAuthStrategyFactory.getStrategy(request.loginType());
        return strategy.login(new AdminLoginRequest(request.username(), request.password()));
    }

    @Override
    public void logout(String loginType) {
        portalAuthStrategyFactory.getStrategy(loginType).logout();
    }

    @Override
    public AdminOperatorProfile currentOperator(String loginType) {
        return portalAuthStrategyFactory.getStrategy(loginType).currentOperator();
    }

    @Override
    public AdminPermissionView currentPermissionView(String loginType) {
        return portalAuthStrategyFactory.getStrategy(loginType).currentPermissionView();
    }
}
