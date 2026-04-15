package com.bddk.geocourse.module.identity.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.model.PortalLoginRequest;
import com.bddk.geocourse.module.identity.service.UnifiedAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统一认证入口。
 */
@Tag(name = "统一认证")
@RestController
@RequestMapping("/admin-api/auth")
public class UnifiedAuthController {

    private final UnifiedAuthService unifiedAuthService;

    public UnifiedAuthController(UnifiedAuthService unifiedAuthService) {
        this.unifiedAuthService = unifiedAuthService;
    }

    @Operation(summary = "查看所有登录门户设计")
    @GetMapping("/portals")
    public ApiResponse<List<AdminAuthDesign>> portals() {
        return ApiResponse.success(unifiedAuthService.listDesigns());
    }

    @Operation(summary = "查看指定登录门户设计")
    @GetMapping("/portals/{loginType}/design")
    public ApiResponse<AdminAuthDesign> design(@PathVariable String loginType) {
        return ApiResponse.success(unifiedAuthService.getDesign(loginType));
    }

    @Operation(summary = "统一登录")
    @PostMapping("/login")
    public ApiResponse<AdminLoginResult> login(@Valid @RequestBody PortalLoginRequest request) {
        return ApiResponse.success("登录成功", unifiedAuthService.login(request));
    }

    @Operation(summary = "统一退出登录")
    @PostMapping("/portals/{loginType}/logout")
    public ApiResponse<Void> logout(@PathVariable String loginType) {
        unifiedAuthService.logout(loginType);
        return ApiResponse.success("退出成功", null);
    }

    @Operation(summary = "查看当前登录人信息")
    @GetMapping("/portals/{loginType}/me")
    public ApiResponse<AdminOperatorProfile> me(@PathVariable String loginType) {
        return ApiResponse.success(unifiedAuthService.currentOperator(loginType));
    }

    @Operation(summary = "查看当前登录人权限")
    @GetMapping("/portals/{loginType}/permissions")
    public ApiResponse<AdminPermissionView> permissions(@PathVariable String loginType) {
        return ApiResponse.success(unifiedAuthService.currentPermissionView(loginType));
    }
}
