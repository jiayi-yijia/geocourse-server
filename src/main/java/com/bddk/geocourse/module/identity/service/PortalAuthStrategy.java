package com.bddk.geocourse.module.identity.service;

import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginRequest;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;

import java.util.List;

/**
 * 统一登录策略接口。
 */
public interface PortalAuthStrategy {

    String getLoginType();

    int getOrder();

    AdminAuthDesign getDesign();

    AdminLoginResult login(AdminLoginRequest request);

    void logout();

    AdminOperatorProfile currentOperator();

    AdminPermissionView currentPermissionView();

    List<String> getRoleCodes(Object loginId);

    List<String> getPermissionCodes(Object loginId);
}
