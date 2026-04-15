package com.bddk.geocourse.module.assignment.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishSaveRequest;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishView;
import com.bddk.geocourse.module.assignment.service.TeacherExamPublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "教师端考试发布")
@RestController
@RequestMapping("/admin-api/teacher/exam-publishes")
public class TeacherExamPublishController {

    private final TeacherExamPublishService teacherExamPublishService;

    public TeacherExamPublishController(TeacherExamPublishService teacherExamPublishService) {
        this.teacherExamPublishService = teacherExamPublishService;
    }

    @Operation(summary = "分页查询考试发布")
    @GetMapping
    public ApiResponse<PageResult<TeacherExamPublishView>> pagePublishes(TeacherExamPublishQuery query) {
        return ApiResponse.success(teacherExamPublishService.pagePublishes(query));
    }

    @Operation(summary = "查询考试发布详情")
    @GetMapping("/{publishId}")
    public ApiResponse<TeacherExamPublishView> getPublish(@PathVariable Long publishId) {
        return ApiResponse.success(teacherExamPublishService.getPublish(publishId));
    }

    @Operation(summary = "新增考试发布")
    @PostMapping
    public ApiResponse<TeacherExamPublishView> createPublish(@Valid @RequestBody TeacherExamPublishSaveRequest request) {
        return ApiResponse.success("考试发布成功", teacherExamPublishService.createPublish(request));
    }

    @Operation(summary = "修改考试发布")
    @PutMapping("/{publishId}")
    public ApiResponse<TeacherExamPublishView> updatePublish(@PathVariable Long publishId,
                                                             @Valid @RequestBody TeacherExamPublishSaveRequest request) {
        return ApiResponse.success("考试发布更新成功", teacherExamPublishService.updatePublish(publishId, request));
    }

    @Operation(summary = "更新考试发布状态")
    @PostMapping("/{publishId}/status")
    public ApiResponse<Void> updatePublishStatus(@PathVariable Long publishId, @RequestParam String status) {
        teacherExamPublishService.updatePublishStatus(publishId, status);
        return ApiResponse.success("考试发布状态更新成功", null);
    }

    @Operation(summary = "删除考试发布")
    @DeleteMapping("/{publishId}")
    public ApiResponse<Void> deletePublish(@PathVariable Long publishId) {
        teacherExamPublishService.deletePublish(publishId);
        return ApiResponse.success("删除考试发布成功", null);
    }
}
