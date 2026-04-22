package com.bddk.geocourse.module.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperQuestionMapper;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import com.bddk.geocourse.module.questionbank.entity.Question;
import com.bddk.geocourse.module.questionbank.mapper.QuestionAnswerMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionCategoryMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionChoiceMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionMapper;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionQuery;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionBankQueryNullSafetyTest {

    @Test
    void pageQuestions_allowsMissingOptionalFilters() {
        TeacherPortalContextService contextService = mock(TeacherPortalContextService.class);
        QuestionCategoryMapper categoryMapper = mock(QuestionCategoryMapper.class);
        QuestionMapper questionMapper = mock(QuestionMapper.class);
        QuestionChoiceMapper choiceMapper = mock(QuestionChoiceMapper.class);
        QuestionAnswerMapper answerMapper = mock(QuestionAnswerMapper.class);
        TeacherPaperQuestionMapper paperQuestionMapper = mock(TeacherPaperQuestionMapper.class);

        when(contextService.currentTenantId()).thenReturn(1L);
        when(questionMapper.selectPage(any(Page.class), any())).thenReturn(new Page<Question>(1, 10));
        when(categoryMapper.selectList(any())).thenReturn(List.of());
        when(choiceMapper.selectList(any())).thenReturn(List.of());
        when(answerMapper.selectList(any())).thenReturn(List.of());

        TeacherQuestionBankServiceImpl service = new TeacherQuestionBankServiceImpl(
                contextService,
                categoryMapper,
                questionMapper,
                choiceMapper,
                answerMapper,
                paperQuestionMapper
        );

        PageResult<TeacherQuestionView> result = service.pageQuestions(new TeacherQuestionQuery());

        assertNotNull(result);
        assertEquals(0, result.list().size());
    }
}
