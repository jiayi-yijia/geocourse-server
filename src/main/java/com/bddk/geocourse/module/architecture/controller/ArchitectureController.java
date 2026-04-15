package com.bddk.geocourse.module.architecture.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.architecture.model.ArchitectureOverview;
import com.bddk.geocourse.module.architecture.service.ArchitectureQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "架构总览")
@RestController
@RequestMapping("/admin-api/architecture")
public class ArchitectureController {

    private final ArchitectureQueryService architectureQueryService;

    public ArchitectureController(ArchitectureQueryService architectureQueryService) {
        this.architectureQueryService = architectureQueryService;
    }

    @Operation(summary = "获取项目后端架构总览")
    @GetMapping("/overview")
    public ApiResponse<ArchitectureOverview> overview() {
        return ApiResponse.success(architectureQueryService.getOverview());
    }

    @Operation(summary = "获取领域模块规划")
    @GetMapping("/modules")
    public ApiResponse<List<ArchitectureOverview.ArchitectureModule>> modules() {
        return ApiResponse.success(architectureQueryService.getModules());
    }

    @Operation(summary = "获取基础设施接入配置")
    @GetMapping("/infrastructure")
    public ApiResponse<List<ArchitectureOverview.InfrastructureNode>> infrastructure() {
        return ApiResponse.success(architectureQueryService.getInfrastructure());
    }

}

