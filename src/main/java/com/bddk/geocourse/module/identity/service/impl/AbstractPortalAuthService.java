package com.bddk.geocourse.module.identity.service.impl;

import cn.dev33.satoken.stp.StpLogic;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.framework.tenant.TenantContextHolder;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginRequest;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.service.IdentityPortalSupportService;
import com.bddk.geocourse.module.identity.service.PortalAuthStrategy;

import java.util.List;

/**
 * 门户认证抽象基类。
 */
public abstract class AbstractPortalAuthService implements PortalAuthStrategy {

    private final IdentityPortalSupportService identityPortalSupportService;

    protected AbstractPortalAuthService(IdentityPortalSupportService identityPortalSupportService) {
        this.identityPortalSupportService = identityPortalSupportService;
    }

    @Override
    public AdminAuthDesign getDesign() {
        return new AdminAuthDesign(
                getPortalCode(),
                getPortalName(),
                getLoginType(),
                getRequiredRoleCode(),
                getTargetRoleName(),
                getResponsibilities(),
                new AdminAuthDesign.TokenStrategy(
                        "Sa-Token",
                        stpLogic().getTokenName(),
                        "Header",
                        stpLogic().getConfigOrGlobal().getTimeout()
                ),
                getApiSpecs(),
                getSecurityControls(),
                getImplementationNotes()
        );
    }

    @Override
    public AdminLoginResult login(AdminLoginRequest request) {
        IdentityPortalSupportService.PortalUserContext context = identityPortalSupportService.authenticate(
                requiredTenantId(),
                request.username(),
                request.password(),
                getRequiredRoleCode(),
                getForbiddenMessage()
        );
        stpLogic().login(context.user().getId());
        return new AdminLoginResult(
                stpLogic().getTokenName(),
                stpLogic().getTokenValue(),
                getLoginType(),
                context.user().getId(),
                stpLogic().getTokenTimeout(),
                identityPortalSupportService.buildProfile(context.user(), context.roles(), getPortalCode(), getPortalName()),
                identityPortalSupportService.buildPermissionView(context.user().getTenantId(), context.roles(), getPortalCode(), getPortalName())
        );
    }

    @Override
    public void logout() {
        stpLogic().checkLogin();
        stpLogic().logout();
    }

    @Override
    public AdminOperatorProfile currentOperator() {
        IdentityPortalSupportService.PortalUserContext context = identityPortalSupportService.requirePortalUser(
                stpLogic().getLoginIdAsLong(),
                getRequiredRoleCode(),
                getForbiddenMessage()
        );
        return identityPortalSupportService.buildProfile(context.user(), context.roles(), getPortalCode(), getPortalName());
    }

    @Override
    public AdminPermissionView currentPermissionView() {
        IdentityPortalSupportService.PortalUserContext context = identityPortalSupportService.requirePortalUser(
                stpLogic().getLoginIdAsLong(),
                getRequiredRoleCode(),
                getForbiddenMessage()
        );
        return identityPortalSupportService.buildPermissionView(context.user().getTenantId(), context.roles(), getPortalCode(), getPortalName());
    }

    @Override
    public List<String> getRoleCodes(Object loginId) {
        return identityPortalSupportService.getRoleCodes(Long.valueOf(String.valueOf(loginId)));
    }

    @Override
    public List<String> getPermissionCodes(Object loginId) {
        return identityPortalSupportService.getPermissionCodes(
                Long.valueOf(String.valueOf(loginId)),
                getPortalCode(),
                getPortalName()
        );
    }

    protected Long requiredTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new ServiceException(ErrorCode.TENANT_REQUIRED);
        }
        return tenantId;
    }

    protected abstract String getPortalCode();

    protected abstract String getPortalName();

    protected abstract String getRequiredRoleCode();

    protected abstract String getTargetRoleName();

    protected abstract String getForbiddenMessage();

    protected abstract List<String> getResponsibilities();

    protected abstract List<AdminAuthDesign.ApiSpec> getApiSpecs();

    protected abstract List<String> getSecurityControls();

    protected abstract List<String> getImplementationNotes();

    protected abstract StpLogic stpLogic();
}
