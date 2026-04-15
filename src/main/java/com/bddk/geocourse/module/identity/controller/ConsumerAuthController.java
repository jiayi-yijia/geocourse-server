package com.bddk.geocourse.module.identity.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.identity.model.AdminAuthDesign;
import com.bddk.geocourse.module.identity.model.AdminLoginRequest;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.model.AdminOperatorProfile;
import com.bddk.geocourse.module.identity.model.AdminPermissionView;
import com.bddk.geocourse.module.identity.service.ConsumerAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 普通用户端认证入口。
 */
@Tag(name = "普通用户认证")
@RestController
@RequestMapping("/admin-api/auth/consumer")
public class ConsumerAuthController {

    private final ConsumerAuthService consumerAuthService;

    public ConsumerAuthController(ConsumerAuthService consumerAuthService) {
        this.consumerAuthService = consumerAuthService;
    }

    @Operation(summary = "查看普通用户端登录设计")
    @GetMapping("/design")
    public ApiResponse<AdminAuthDesign> design() {
        return ApiResponse.success(consumerAuthService.getDesign());
    }

    @Operation(summary = "普通用户登录")
    @PostMapping("/login")
    public ApiResponse<AdminLoginResult> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success("登录成功", consumerAuthService.login(request));
    }

    @Operation(summary = "普通用户退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        consumerAuthService.logout();
        return ApiResponse.success("退出成功", null);
    }

    @Operation(summary = "获取当前普通用户信息")
    @GetMapping("/me")
    public ApiResponse<AdminOperatorProfile> me() {
        return ApiResponse.success(consumerAuthService.currentOperator());
    }

    @Operation(summary = "获取当前普通用户权限")
    @GetMapping("/permissions")
    public ApiResponse<AdminPermissionView> permissions() {
        return ApiResponse.success(consumerAuthService.currentPermissionView());
    }

}
