package com.bddk.geocourse.module.questionbank.service;

import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.questionbank.model.QuestionCategorySaveRequest;
import com.bddk.geocourse.module.questionbank.model.QuestionCategoryView;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionQuery;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionSaveRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionView;

import java.util.List;

public interface TeacherQuestionBankService {

    List<QuestionCategoryView> listCategories();

    List<QuestionCategoryView> treeCategories();

    QuestionCategoryView createCategory(QuestionCategorySaveRequest request);

    QuestionCategoryView updateCategory(Long categoryId, QuestionCategorySaveRequest request);

    void deleteCategory(Long categoryId);

    PageResult<TeacherQuestionView> pageQuestions(TeacherQuestionQuery query);

    TeacherQuestionView getQuestion(Long questionId);

    TeacherQuestionView createQuestion(TeacherQuestionSaveRequest request);

    TeacherQuestionView updateQuestion(Long questionId, TeacherQuestionSaveRequest request);

    void deleteQuestion(Long questionId);
}
