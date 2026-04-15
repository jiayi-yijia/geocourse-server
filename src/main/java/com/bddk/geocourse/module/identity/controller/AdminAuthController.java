package com.bddk.geocourse.module.identity.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginRequest;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员后台认证入口。
 * 当前只负责后台管理员这一条登录链路，后续学校管理员、教师、学生端建议拆分独立控制器。
 */
@Tag(name = "管理员认证")
@RestController
@RequestMapping("/admin-api/auth/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * 返回管理员后台登录模块的后端设计说明，方便前后端先对齐接口契约。
     */
    @Operation(summary = "查看管理员后台登录设计")
    @GetMapping("/design")
    public ApiResponse<AdminAuthDesign> design() {
        return ApiResponse.success(adminAuthService.getDesign());
    }

    /**
     * 管理员账号密码登录。
     * 登录成功后由 Sa-Token 生成 token，并返回当前管理员的基础信息和权限视图。
     */
    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public ApiResponse<AdminLoginResult> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success("登录成功", adminAuthService.login(request));
    }

    /**
     * 退出当前管理员登录态。
     */
    @Operation(summary = "管理员退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        adminAuthService.logout();
        return ApiResponse.success("退出成功", null);
    }

    /**
     * 获取当前登录管理员的基础资料。
     */
    @Operation(summary = "获取当前管理员信息")
    @GetMapping("/me")
    public ApiResponse<AdminOperatorProfile> me() {
        return ApiResponse.success(adminAuthService.currentOperator());
    }

    /**
     * 获取当前登录管理员的菜单树与权限码。
     * 前端可以据此完成菜单渲染、路由守卫和按钮权限控制。
     */
    @Operation(summary = "获取当前管理员权限")
    @GetMapping("/permissions")
    public ApiResponse<AdminPermissionView> permissions() {
        return ApiResponse.success(adminAuthService.currentPermissionView());
    }

}
