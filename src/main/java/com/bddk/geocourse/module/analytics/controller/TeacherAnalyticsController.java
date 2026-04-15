package com.bddk.geocourse.module.analytics.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.analytics.model.TeacherAnalyticsOverviewView;
import com.bddk.geocourse.module.analytics.service.TeacherAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "教师端统计概览")
@RestController
@RequestMapping("/admin-api/teacher/analytics")
public class TeacherAnalyticsController {

    private final TeacherAnalyticsService teacherAnalyticsService;

    public TeacherAnalyticsController(TeacherAnalyticsService teacherAnalyticsService) {
        this.teacherAnalyticsService = teacherAnalyticsService;
    }

    @Operation(summary = "查询教师端概览统计")
    @GetMapping("/overview")
    public ApiResponse<TeacherAnalyticsOverviewView> overview() {
        return ApiResponse.success(teacherAnalyticsService.getOverview());
    }
}
