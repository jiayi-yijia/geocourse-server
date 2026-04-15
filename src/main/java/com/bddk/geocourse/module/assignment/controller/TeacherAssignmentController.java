package com.bddk.geocourse.module.assignment.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.assignment.model.TeacherExamRankingView;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordView;
import com.bddk.geocourse.module.assignment.model.TeacherPaperQuery;
import com.bddk.geocourse.module.assignment.model.TeacherPaperSaveRequest;
import com.bddk.geocourse.module.assignment.model.TeacherPaperView;
import com.bddk.geocourse.module.assignment.service.TeacherAssignmentService;
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

import java.util.List;

@Tag(name = "教师端试卷与考试管理")
@RestController
@RequestMapping("/admin-api/teacher/assignments")
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;

    public TeacherAssignmentController(TeacherAssignmentService teacherAssignmentService) {
        this.teacherAssignmentService = teacherAssignmentService;
    }

    @Operation(summary = "分页查询试卷")
    @GetMapping("/papers")
    public ApiResponse<PageResult<TeacherPaperView>> pagePapers(TeacherPaperQuery query) {
        return ApiResponse.success(teacherAssignmentService.pagePapers(query));
    }

    @Operation(summary = "查询试卷详情")
    @GetMapping("/papers/{paperId}")
    public ApiResponse<TeacherPaperView> getPaper(@PathVariable Long paperId) {
        return ApiResponse.success(teacherAssignmentService.getPaper(paperId));
    }

    @Operation(summary = "新增试卷")
    @PostMapping("/papers")
    public ApiResponse<TeacherPaperView> createPaper(@Valid @RequestBody TeacherPaperSaveRequest request) {
        return ApiResponse.success("新增试卷成功", teacherAssignmentService.createPaper(request));
    }

    @Operation(summary = "修改试卷")
    @PutMapping("/papers/{paperId}")
    public ApiResponse<TeacherPaperView> updatePaper(@PathVariable Long paperId,
                                                     @Valid @RequestBody TeacherPaperSaveRequest request) {
        return ApiResponse.success("修改试卷成功", teacherAssignmentService.updatePaper(paperId, request));
    }

    @Operation(summary = "更新试卷状态")
    @PostMapping("/papers/{paperId}/status")
    public ApiResponse<Void> updatePaperStatus(@PathVariable Long paperId, @RequestParam String status) {
        teacherAssignmentService.updatePaperStatus(paperId, status);
        return ApiResponse.success("试卷状态更新成功", null);
    }

    @Operation(summary = "删除试卷")
    @DeleteMapping("/papers/{paperId}")
    public ApiResponse<Void> deletePaper(@PathVariable Long paperId) {
        teacherAssignmentService.deletePaper(paperId);
        return ApiResponse.success("删除试卷成功", null);
    }

    @Operation(summary = "分页查询考试记录")
    @GetMapping("/exam-records")
    public ApiResponse<PageResult<TeacherExamRecordView>> pageExamRecords(TeacherExamRecordQuery query) {
        return ApiResponse.success(teacherAssignmentService.pageExamRecords(query));
    }

    @Operation(summary = "删除考试记录")
    @DeleteMapping("/exam-records/{recordId}")
    public ApiResponse<Void> deleteExamRecord(@PathVariable Long recordId) {
        teacherAssignmentService.deleteExamRecord(recordId);
        return ApiResponse.success("删除考试记录成功", null);
    }

    @Operation(summary = "查询考试排行")
    @GetMapping("/exam-records/ranking")
    public ApiResponse<List<TeacherExamRankingView>> listRanking(@RequestParam(required = false) Long paperId,
                                                                 @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(teacherAssignmentService.listRanking(paperId, limit));
    }
}
