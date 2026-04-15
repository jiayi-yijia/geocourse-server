package com.bddk.geocourse.module.identity.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginRequest;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.service.StudentAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端认证入口。
 */
@Tag(name = "学生认证")
@RestController
@RequestMapping("/admin-api/auth/student")
public class StudentAuthController {

    private final StudentAuthService studentAuthService;

    public StudentAuthController(StudentAuthService studentAuthService) {
        this.studentAuthService = studentAuthService;
    }

    @Operation(summary = "查看学生端登录设计")
    @GetMapping("/design")
    public ApiResponse<AdminAuthDesign> design() {
        return ApiResponse.success(studentAuthService.getDesign());
    }

    @Operation(summary = "学生登录")
    @PostMapping("/login")
    public ApiResponse<AdminLoginResult> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success("登录成功", studentAuthService.login(request));
    }

    @Operation(summary = "学生退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        studentAuthService.logout();
        return ApiResponse.success("退出成功", null);
    }

    @Operation(summary = "获取当前学生信息")
    @GetMapping("/me")
    public ApiResponse<AdminOperatorProfile> me() {
        return ApiResponse.success(studentAuthService.currentOperator());
    }

    @Operation(summary = "获取当前学生权限")
    @GetMapping("/permissions")
    public ApiResponse<AdminPermissionView> permissions() {
        return ApiResponse.success(studentAuthService.currentPermissionView());
    }

}
