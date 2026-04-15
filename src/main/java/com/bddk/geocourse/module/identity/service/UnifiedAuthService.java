package com.bddk.geocourse.module.identity.service;

import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.model.PortalLoginRequest;

import java.util.List;

/**
 * 统一登录入口服务。
 */
public interface UnifiedAuthService {

    List<AdminAuthDesign> listDesigns();

    AdminAuthDesign getDesign(String loginType);

    AdminLoginResult login(PortalLoginRequest request);

    void logout(String loginType);

    AdminOperatorProfile currentOperator(String loginType);

    AdminPermissionView currentPermissionView(String loginType);
}
