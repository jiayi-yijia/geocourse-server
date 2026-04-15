package com.bddk.geocourse.module.identity.service.impl;

import cn.dev33.satoken.stp.StpLogic;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.service.ConsumerAuthService;
import com.bddk.geocourse.module.identity.service.IdentityPortalSupportService;
import com.bddk.geocourse.module.identity.stp.StpConsumerUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 普通用户认证策略实现。
 */
@Service
public class ConsumerAuthServiceImpl extends AbstractPortalAuthService implements ConsumerAuthService {

    public ConsumerAuthServiceImpl(IdentityPortalSupportService identityPortalSupportService) {
        super(identityPortalSupportService);
    }

    @Override
    public String getLoginType() {
        return StpConsumerUtil.LOGIN_TYPE;
    }

    @Override
    public int getOrder() {
        return 50;
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
        return "普通用户";
    }

    @Override
    protected String getForbiddenMessage() {
        return "当前账号不具备普通用户端访问权限";
    }

    @Override
    protected List<String> getResponsibilities() {
        return List.of("浏览学校课程广场", "购买跨校课程", "进入已购课程学习", "管理订单", "查看个人学习记录");
    }

    @Override
    protected List<AdminAuthDesign.ApiSpec> getApiSpecs() {
        return List.of(
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/consumer/design", "查看普通用户端登录设计", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/consumer/login", "普通用户账号密码登录", false),
                new AdminAuthDesign.ApiSpec("POST", "/admin-api/auth/consumer/logout", "退出当前普通用户登录态", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/consumer/me", "获取当前普通用户信息", true),
                new AdminAuthDesign.ApiSpec("GET", "/admin-api/auth/consumer/permissions", "获取当前普通用户菜单与权限", true)
        );
    }

    @Override
    protected List<String> getSecurityControls() {
        return List.of(
                "普通用户端使用独立 loginType=consumer，与后台、教师端、学生端会话隔离",
                "普通用户允许跨学校浏览课程，并通过订单能力购买后进入学习中心",
                "学生端仅保留本校免费课程学习，不承担跨校付费购课流程"
        );
    }

    @Override
    protected List<String> getImplementationNotes() {
        return List.of(
                "当前仅允许 role_code=consumer 的账号进入普通用户端",
                "后续可以继续扩展购物车、优惠券、支付流水、收藏等消费侧模块"
        );
    }

    @Override
    protected StpLogic stpLogic() {
        return StpConsumerUtil.stpLogic;
    }
}
