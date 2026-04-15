package com.bddk.geocourse.module.identity.service.impl;

import cn.dev33.satoken.stp.StpLogic;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.service.IdentityPortalSupportService;
import com.bddk.geocourse.module.identity.service.StudentAuthService;
import com.bddk.geocourse.module.identity.stp.StpStudentUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生认证策略实现。
 */
@Service
public class StudentAuthServiceImpl extends AbstractPortalAuthService implements StudentAuthService {

    public StudentAuthServiceImpl(IdentityPortalSupportService identityPortalSupportService) {
        super(identityPortalSupportService);
    }

    @Override
    public String getLoginType() {
        return StpStudentUtil.LOGIN_TYPE;
    }

    @Override
    public int getOrder() {
        return 40;
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
        return "学生";
    }

    @Override
    protected String getForbiddenMessage() {
        return "当前账号不具备学生端访问权限";
    }

    @Override
    protected List<String> getResponsibilities() {
        return List.of("选课", "参与上课", "提交作业", "查询成绩", "查看公告");
    }

    @Override
    protected List<AdminAuthDesign.ApiSpec> getApiSpecs() {
        return List.of(
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/student/design", "查看学生端登录设计", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/student/login", "学生账号密码登录", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/student/logout", "退出当前学生登录态", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/student/me", "获取当前学生信息", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/student/permissions", "获取当前学生菜单与权限", true)
        );
    }

    @Override
    protected List<String> getSecurityControls() {
        return List.of(
                "学生端使用独立 loginType=student，与后台和教师端会话隔离",
                "学生端同样基于 sys_user、sys_user_role、sys_role、sys_role_menu、sys_menu 进行授权装配",
                "学生端仅面向本校学生开放本校免费课程学习，不承担跨校付费购课流程"
        );
    }

    @Override
    protected List<String> getImplementationNotes() {
        return List.of(
                "当前仅允许 role_code=student 的账号进入学生端",
                "后续可以继续扩展考试、问答、学习轨迹等学生侧业务模块"
        );
    }

    @Override
    protected StpLogic stpLogic() {
        return StpStudentUtil.stpLogic;
    }
}
