package com.bddk.geocourse.module.identity.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginRequest;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.service.TeacherAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师工作台认证入口。
 */
@Tag(name = "教师认证")
@RestController
@RequestMapping("/admin-api/auth/teacher")
public class TeacherAuthController {

    private final TeacherAuthService teacherAuthService;

    public TeacherAuthController(TeacherAuthService teacherAuthService) {
        this.teacherAuthService = teacherAuthService;
    }

    @Operation(summary = "查看教师工作台登录设计")
    @GetMapping("/design")
    public ApiResponse<AdminAuthDesign> design() {
        return ApiResponse.success(teacherAuthService.getDesign());
    }

    @Operation(summary = "教师登录")
    @PostMapping("/login")
    public ApiResponse<AdminLoginResult> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success("登录成功", teacherAuthService.login(request));
    }

    @Operation(summary = "教师退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        teacherAuthService.logout();
        return ApiResponse.success("退出成功", null);
    }

    @Operation(summary = "获取当前教师信息")
    @GetMapping("/me")
    public ApiResponse<AdminOperatorProfile> me() {
        return ApiResponse.success(teacherAuthService.currentOperator());
    }

    @Operation(summary = "获取当前教师权限")
    @GetMapping("/permissions")
    public ApiResponse<AdminPermissionView> permissions() {
        return ApiResponse.success(teacherAuthService.currentPermissionView());
    }

}
