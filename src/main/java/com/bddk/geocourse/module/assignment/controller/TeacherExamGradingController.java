package com.bddk.geocourse.module.assignment.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.assignment.model.TeacherExamGradeRequest;
import com.bddk.geocourse.module.assignment.model.TeacherExamGradingDetailView;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordView;
import com.bddk.geocourse.module.assignment.service.TeacherExamGradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "教师端考试批改")
@RestController
@RequestMapping("/admin-api/teacher/grading")
public class TeacherExamGradingController {

    private final TeacherExamGradingService teacherExamGradingService;

    public TeacherExamGradingController(TeacherExamGradingService teacherExamGradingService) {
        this.teacherExamGradingService = teacherExamGradingService;
    }

    @Operation(summary = "分页查询待批改记录")
    @GetMapping("/records")
    public ApiResponse<PageResult<TeacherExamRecordView>> pageRecords(TeacherExamRecordQuery query) {
        return ApiResponse.success(teacherExamGradingService.pageRecordsForGrading(query));
    }

    @Operation(summary = "查询批改详情")
    @GetMapping("/records/{recordId}")
    public ApiResponse<TeacherExamGradingDetailView> getGradingDetail(@PathVariable Long recordId) {
        return ApiResponse.success(teacherExamGradingService.getGradingDetail(recordId));
    }

    @Operation(summary = "提交批改结果")
    @PostMapping("/records/{recordId}/grade")
    public ApiResponse<TeacherExamGradingDetailView> gradeRecord(@PathVariable Long recordId,
                                                                 @Valid @RequestBody TeacherExamGradeRequest request) {
        return ApiResponse.success("批改完成", teacherExamGradingService.gradeRecord(recordId, request));
    }
}
