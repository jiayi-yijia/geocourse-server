package com.bddk.geocourse.module.system.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.framework.config.GeocourseInfoProperties;
import com.bddk.geocourse.framework.tenant.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Tag(name = "系统基础接口")
@RestController
@RequestMapping("/admin-api/system")
public class SystemPingController {

    private final GeocourseInfoProperties infoProperties;

    private final Environment environment;

    public SystemPingController(GeocourseInfoProperties infoProperties, Environment environment) {
        this.infoProperties = infoProperties;
        this.environment = environment;
    }

    @Operation(summary = "基础连通性检查")
    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("application", infoProperties.getName());
        payload.put("version", infoProperties.getVersion());
        payload.put("profiles", profiles);
        payload.put("serverTime", LocalDateTime.now());
        payload.put("tenantId", TenantContextHolder.getTenantId());
        return ApiResponse.success(payload);
    }

}

