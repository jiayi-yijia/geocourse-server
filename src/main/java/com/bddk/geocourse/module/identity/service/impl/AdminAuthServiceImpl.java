package com.bddk.geocourse.module.identity.service.impl;

import cn.dev33.satoken.stp.StpLogic;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.service.AdminAuthService;
import com.bddk.geocourse.module.identity.service.IdentityPortalSupportService;
import com.bddk.geocourse.module.identity.stp.StpAdminUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 平台管理员认证策略实现。
 */
@Service
public class AdminAuthServiceImpl extends AbstractPortalAuthService implements AdminAuthService {

    public AdminAuthServiceImpl(IdentityPortalSupportService identityPortalSupportService) {
        super(identityPortalSupportService);
    }

    @Override
    public String getLoginType() {
        return StpAdminUtil.LOGIN_TYPE;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    protected String getPortalCode() {
        return PORTAL_CODE;
    }

    @Override
    protected String getPortalName() {
        return PORTAL_NAME;
    }

    @Override
    protected String getRequiredRoleCode() {
        return REQUIRED_ROLE_CODE;
    }

    @Override
    protected String getTargetRoleName() {
        return "平台管理员";
    }

    @Override
    protected String getForbiddenMessage() {
        return "当前账号不具备平台管理员权限";
    }

    @Override
    protected List<String> getResponsibilities() {
        return List.of("平台全局配置", "学校或机构入驻审核", "课程审核", "用户管理", "数据统计");
    }

    @Override
    protected List<AdminAuthDesign.ApiSpec> getApiSpecs() {
        return List.of(
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/admin/design", "查看管理员后台登录设计", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/admin/login", "管理员账号密码登录", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/admin/logout", "退出当前管理员登录态", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/admin/me", "获取当前管理员信息", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/admin/permissions", "获取当前管理员菜单与权限", true)
        );
    }

    @Override
    protected List<String> getSecurityControls() {
        return List.of(
                "管理员后台使用独立 loginType=admin，便于和其他端隔离",
                "角色、菜单、权限从数据库读取，角色菜单关系通过 sys_role_menu 维护",
                "当前仍兼容明文密码，建议后续迁移到 BCrypt"
        );
    }

    @Override
    protected List<String> getImplementationNotes() {
        return List.of(
                "当前仅允许 role_code=admin 的平台管理员进入后台",
                "后续新增平台运营、审核专员等角色时可继续复用同一套策略分发机制"
        );
    }

    @Override
    protected StpLogic stpLogic() {
        return StpAdminUtil.stpLogic;
    }
}
