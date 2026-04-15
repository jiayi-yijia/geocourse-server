package com.bddk.geocourse.module.questionbank.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.questionbank.model.QuestionCategorySaveRequest;
import com.bddk.geocourse.module.questionbank.model.QuestionCategoryView;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionQuery;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionSaveRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionView;
import com.bddk.geocourse.module.questionbank.service.TeacherQuestionBankService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "教师端题库管理")
@RestController
@RequestMapping("/admin-api/teacher/question-bank")
public class TeacherQuestionBankController {

    private final TeacherQuestionBankService teacherQuestionBankService;

    public TeacherQuestionBankController(TeacherQuestionBankService teacherQuestionBankService) {
        this.teacherQuestionBankService = teacherQuestionBankService;
    }

    @Operation(summary = "查询分类列表")
    @GetMapping("/categories")
    public ApiResponse<List<QuestionCategoryView>> listCategories() {
        return ApiResponse.success(teacherQuestionBankService.listCategories());
    }

    @Operation(summary = "查询分类树")
    @GetMapping("/categories/tree")
    public ApiResponse<List<QuestionCategoryView>> treeCategories() {
        return ApiResponse.success(teacherQuestionBankService.treeCategories());
    }

    @Operation(summary = "新增分类")
    @PostMapping("/categories")
    public ApiResponse<QuestionCategoryView> createCategory(@Valid @RequestBody QuestionCategorySaveRequest request) {
        return ApiResponse.success("新增分类成功", teacherQuestionBankService.createCategory(request));
    }

    @Operation(summary = "修改分类")
    @PutMapping("/categories/{categoryId}")
    public ApiResponse<QuestionCategoryView> updateCategory(@PathVariable Long categoryId,
                                                            @Valid @RequestBody QuestionCategorySaveRequest request) {
        return ApiResponse.success("修改分类成功", teacherQuestionBankService.updateCategory(categoryId, request));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long categoryId) {
        teacherQuestionBankService.deleteCategory(categoryId);
        return ApiResponse.success("删除分类成功", null);
    }

    @Operation(summary = "分页查询题目")
    @GetMapping("/questions")
    public ApiResponse<PageResult<TeacherQuestionView>> pageQuestions(TeacherQuestionQuery query) {
        return ApiResponse.success(teacherQuestionBankService.pageQuestions(query));
    }

    @Operation(summary = "查询题目详情")
    @GetMapping("/questions/{questionId}")
    public ApiResponse<TeacherQuestionView> getQuestion(@PathVariable Long questionId) {
        return ApiResponse.success(teacherQuestionBankService.getQuestion(questionId));
    }

    @Operation(summary = "新增题目")
    @PostMapping("/questions")
    public ApiResponse<TeacherQuestionView> createQuestion(@Valid @RequestBody TeacherQuestionSaveRequest request) {
        return ApiResponse.success("新增题目成功", teacherQuestionBankService.createQuestion(request));
    }

    @Operation(summary = "修改题目")
    @PutMapping("/questions/{questionId}")
    public ApiResponse<TeacherQuestionView> updateQuestion(@PathVariable Long questionId,
                                                           @Valid @RequestBody TeacherQuestionSaveRequest request) {
        return ApiResponse.success("修改题目成功", teacherQuestionBankService.updateQuestion(questionId, request));
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/questions/{questionId}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long questionId) {
        teacherQuestionBankService.deleteQuestion(questionId);
        return ApiResponse.success("删除题目成功", null);
    }
}
