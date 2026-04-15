package com.bddk.geocourse.module.identity.service.impl;

import cn.dev33.satoken.stp.StpLogic;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.service.IdentityPortalSupportService;
import com.bddk.geocourse.module.identity.service.SchoolAdminAuthService;
import com.bddk.geocourse.module.identity.stp.StpSchoolAdminUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学校管理员认证策略实现。
 */
@Service
public class SchoolAdminAuthServiceImpl extends AbstractPortalAuthService implements SchoolAdminAuthService {

    public SchoolAdminAuthServiceImpl(IdentityPortalSupportService identityPortalSupportService) {
        super(identityPortalSupportService);
    }

    @Override
    public String getLoginType() {
        return StpSchoolAdminUtil.LOGIN_TYPE;
    }

    @Override
    public int getOrder() {
        return 20;
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
        return "学校管理员";
    }

    @Override
    protected String getForbiddenMessage() {
        return "当前账号不具备学校管理员权限";
    }

    @Override
    protected List<String> getResponsibilities() {
        return List.of("管理本校教师", "管理课程", "管理学生", "管理班级", "教学安排");
    }

    @Override
    protected List<AdminAuthDesign.ApiSpec> getApiSpecs() {
        return List.of(
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/school-admin/design", "查看学校管理员后台登录设计", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/school-admin/login", "学校管理员账号密码登录", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/school-admin/logout", "退出当前学校管理员登录态", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/school-admin/me", "获取当前学校管理员信息", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/school-admin/permissions", "获取当前学校管理员菜单与权限", true)
        );
    }

    @Override
    protected List<String> getSecurityControls() {
        return List.of(
                "学校后台使用独立 loginType=school-admin，与平台后台会话隔离",
                "账号主体仍复用 sys_user，授权来源为 sys_user_role、sys_role_menu 与 sys_menu",
                "只返回当前租户内、当前角色可见的学校管理菜单"
        );
    }

    @Override
    protected List<String> getImplementationNotes() {
        return List.of(
                "当前仅允许 role_code=school_admin 的账号进入学校后台",
                "后续可在该策略下继续扩展教师管理、班级管理、教学安排等真实业务模块"
        );
    }

    @Override
    protected StpLogic stpLogic() {
        return StpSchoolAdminUtil.stpLogic;
    }
}
