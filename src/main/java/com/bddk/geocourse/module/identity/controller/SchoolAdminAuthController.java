package com.bddk.geocourse.module.identity.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginRequest;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.service.SchoolAdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学校管理员后台认证入口。
 */
@Tag(name = "学校管理员认证")
@RestController
@RequestMapping("/admin-api/auth/school-admin")
public class SchoolAdminAuthController {

    private final SchoolAdminAuthService schoolAdminAuthService;

    public SchoolAdminAuthController(SchoolAdminAuthService schoolAdminAuthService) {
        this.schoolAdminAuthService = schoolAdminAuthService;
    }

    @Operation(summary = "查看学校管理员后台登录设计")
    @GetMapping("/design")
    public ApiResponse<AdminAuthDesign> design() {
        return ApiResponse.success(schoolAdminAuthService.getDesign());
    }

    @Operation(summary = "学校管理员登录")
    @PostMapping("/login")
    public ApiResponse<AdminLoginResult> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success("登录成功", schoolAdminAuthService.login(request));
    }

    @Operation(summary = "学校管理员退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        schoolAdminAuthService.logout();
        return ApiResponse.success("退出成功", null);
    }

    @Operation(summary = "获取当前学校管理员信息")
    @GetMapping("/me")
    public ApiResponse<AdminOperatorProfile> me() {
        return ApiResponse.success(schoolAdminAuthService.currentOperator());
    }

    @Operation(summary = "获取当前学校管理员权限")
    @GetMapping("/permissions")
    public ApiResponse<AdminPermissionView> permissions() {
        return ApiResponse.success(schoolAdminAuthService.currentPermissionView());
    }

}
