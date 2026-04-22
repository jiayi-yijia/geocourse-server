package com.bddk.geocourse.module.compat.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.compat.service.FrontendAuthService;
import com.bddk.geocourse.module.compat.service.FrontendSchoolQuestionBankService;
import com.bddk.geocourse.module.questionbank.model.QuestionBankAttachmentImportRequest;
import com.bddk.geocourse.module.questionbank.model.QuestionBankAttachmentImportResult;
import com.bddk.geocourse.module.questionbank.model.QuestionCategorySaveRequest;
import com.bddk.geocourse.module.questionbank.model.QuestionCategoryView;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionQuery;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionSaveRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionView;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/school/question-bank")
public class FrontendSchoolQuestionBankController {

    private final FrontendAuthService frontendAuthService;
    private final FrontendSchoolQuestionBankService frontendSchoolQuestionBankService;

    public FrontendSchoolQuestionBankController(FrontendAuthService frontendAuthService,
                                                FrontendSchoolQuestionBankService frontendSchoolQuestionBankService) {
        this.frontendAuthService = frontendAuthService;
        this.frontendSchoolQuestionBankService = frontendSchoolQuestionBankService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<QuestionCategoryView>> listCategories() {
        frontendAuthService.requireCurrentSchoolAdmin();
        return ApiResponse.success(frontendSchoolQuestionBankService.listCategories());
    }

    @GetMapping("/categories/tree")
    public ApiResponse<List<QuestionCategoryView>> treeCategories() {
        frontendAuthService.requireCurrentSchoolAdmin();
        return ApiResponse.success(frontendSchoolQuestionBankService.treeCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<QuestionCategoryView> createCategory(@Valid @RequestBody QuestionCategorySaveRequest request) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return ApiResponse.success("Category created", frontendSchoolQuestionBankService.createCategory(request));
    }

    @PutMapping("/categories/{categoryId}")
    public ApiResponse<QuestionCategoryView> updateCategory(@PathVariable Long categoryId,
                                                            @Valid @RequestBody QuestionCategorySaveRequest request) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return ApiResponse.success("Category updated", frontendSchoolQuestionBankService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long categoryId) {
        frontendAuthService.requireCurrentSchoolAdmin();
        frontendSchoolQuestionBankService.deleteCategory(categoryId);
        return ApiResponse.success("Category deleted", null);
    }

    @GetMapping("/questions")
    public ApiResponse<PageResult<TeacherQuestionView>> pageQuestions(TeacherQuestionQuery query) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return ApiResponse.success(frontendSchoolQuestionBankService.pageQuestions(query));
    }

    @GetMapping("/questions/{questionId}")
    public ApiResponse<TeacherQuestionView> getQuestion(@PathVariable Long questionId) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return ApiResponse.success(frontendSchoolQuestionBankService.getQuestion(questionId));
    }

    @PostMapping("/questions")
    public ApiResponse<TeacherQuestionView> createQuestion(@Valid @RequestBody TeacherQuestionSaveRequest request) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return ApiResponse.success("Question created", frontendSchoolQuestionBankService.createQuestion(request));
    }

    @PutMapping("/questions/{questionId}")
    public ApiResponse<TeacherQuestionView> updateQuestion(@PathVariable Long questionId,
                                                           @Valid @RequestBody TeacherQuestionSaveRequest request) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return ApiResponse.success("Question updated", frontendSchoolQuestionBankService.updateQuestion(questionId, request));
    }

    @DeleteMapping("/questions/{questionId}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long questionId) {
        frontendAuthService.requireCurrentSchoolAdmin();
        frontendSchoolQuestionBankService.deleteQuestion(questionId);
        return ApiResponse.success("Question deleted", null);
    }

    @PostMapping(path = "/questions/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<QuestionBankAttachmentImportResult> importQuestions(
            @Valid @ModelAttribute QuestionBankAttachmentImportRequest request) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return ApiResponse.success("Questions imported", frontendSchoolQuestionBankService.importQuestions(request));
    }
}
