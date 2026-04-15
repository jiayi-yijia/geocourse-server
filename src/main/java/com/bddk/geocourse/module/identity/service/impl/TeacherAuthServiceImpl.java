package com.bddk.geocourse.module.identity.service.impl;

import cn.dev33.satoken.stp.StpLogic;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.service.IdentityPortalSupportService;
import com.bddk.geocourse.module.identity.service.TeacherAuthService;
import com.bddk.geocourse.module.identity.stp.StpTeacherUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教师认证策略实现。
 */
@Service
public class TeacherAuthServiceImpl extends AbstractPortalAuthService implements TeacherAuthService {

    public TeacherAuthServiceImpl(IdentityPortalSupportService identityPortalSupportService) {
        super(identityPortalSupportService);
    }

    @Override
    public String getLoginType() {
        return StpTeacherUtil.LOGIN_TYPE;
    }

    @Override
    public int getOrder() {
        return 30;
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
        return "教师";
    }

    @Override
    protected String getForbiddenMessage() {
        return "当前账号不具备教师工作台访问权限";
    }

    @Override
    protected List<String> getResponsibilities() {
        return List.of("管理已审核通过的课程", "管理所授课学生", "录入成绩", "发布教学资源");
    }

    @Override
    protected List<AdminAuthDesign.ApiSpec> getApiSpecs() {
        return List.of(
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/teacher/design", "查看教师工作台登录设计", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/teacher/login", "教师账号密码登录", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/teacher/logout", "退出当前教师登录态", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/teacher/me", "获取当前教师信息", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/teacher/permissions", "获取当前教师菜单与权限", true)
        );
    }

    @Override
    protected List<String> getSecurityControls() {
        return List.of(
                "教师工作台使用独立 loginType=teacher，与平台后台、学校后台会话隔离",
                "教师端同样基于 sys_user、sys_user_role、sys_role、sys_role_menu、sys_menu 进行授权装配",
                "当前优先覆盖课程、学生、成绩、资源四类核心教学能力"
        );
    }

    @Override
    protected List<String> getImplementationNotes() {
        return List.of(
                "当前仅允许 role_code=teacher 的账号进入教师工作台",
                "后续可以继续扩展作业、题库、课堂互动等业务模块"
        );
    }

    @Override
    protected StpLogic stpLogic() {
        return StpTeacherUtil.stpLogic;
    }
}
