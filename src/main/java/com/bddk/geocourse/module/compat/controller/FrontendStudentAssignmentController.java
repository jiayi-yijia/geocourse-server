package com.bddk.geocourse.module.compat.controller;

import com.bddk.geocourse.module.compat.model.StudentAssignmentDetailView;
import com.bddk.geocourse.module.compat.model.StudentAssignmentResultView;
import com.bddk.geocourse.module.compat.model.StudentAssignmentSubmitRequest;
import com.bddk.geocourse.module.compat.model.StudentAssignmentSummaryView;
import com.bddk.geocourse.module.compat.service.FrontendStudentAssignmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/assignments")
public class FrontendStudentAssignmentController {

    private final FrontendStudentAssignmentService frontendStudentAssignmentService;

    public FrontendStudentAssignmentController(FrontendStudentAssignmentService frontendStudentAssignmentService) {
        this.frontendStudentAssignmentService = frontendStudentAssignmentService;
    }

    @GetMapping
    public List<StudentAssignmentSummaryView> listAssignments() {
        return frontendStudentAssignmentService.listAssignments();
    }

    @GetMapping("/{publishId}")
    public StudentAssignmentDetailView getAssignmentDetail(@PathVariable Long publishId) {
        return frontendStudentAssignmentService.getAssignmentDetail(publishId);
    }

    @GetMapping("/{publishId}/result")
    public StudentAssignmentResultView getAssignmentResult(@PathVariable Long publishId) {
        return frontendStudentAssignmentService.getAssignmentResult(publishId);
    }

    @PostMapping("/{publishId}/submit")
    public StudentAssignmentResultView submitAssignment(@PathVariable Long publishId,
                                                        @Valid @RequestBody StudentAssignmentSubmitRequest request) {
        return frontendStudentAssignmentService.submitAssignment(publishId, request);
    }
}
