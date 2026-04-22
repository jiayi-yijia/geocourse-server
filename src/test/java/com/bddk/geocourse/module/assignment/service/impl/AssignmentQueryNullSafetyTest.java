package com.bddk.geocourse.module.assignment.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.assignment.entity.TeacherExamPublish;
import com.bddk.geocourse.module.assignment.entity.TeacherExamRecord;
import com.bddk.geocourse.module.assignment.entity.TeacherPaper;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamAnswerMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamPublishMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamRecordMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperQuestionMapper;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishView;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordView;
import com.bddk.geocourse.module.assignment.model.TeacherPaperQuery;
import com.bddk.geocourse.module.assignment.model.TeacherPaperView;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import com.bddk.geocourse.module.questionbank.mapper.QuestionCategoryMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionMapper;
import com.bddk.geocourse.module.questionbank.service.TeacherQuestionBankService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssignmentQueryNullSafetyTest {

    @Test
    void pagePapers_allowsMissingOptionalFilters() {
        TeacherPortalContextService contextService = mock(TeacherPortalContextService.class);
        TeacherPaperMapper paperMapper = mock(TeacherPaperMapper.class);
        TeacherPaperQuestionMapper paperQuestionMapper = mock(TeacherPaperQuestionMapper.class);
        TeacherExamRecordMapper examRecordMapper = mock(TeacherExamRecordMapper.class);
        QuestionMapper questionMapper = mock(QuestionMapper.class);
        QuestionCategoryMapper questionCategoryMapper = mock(QuestionCategoryMapper.class);
        TeacherQuestionBankService teacherQuestionBankService = mock(TeacherQuestionBankService.class);
        when(contextService.currentTenantId()).thenReturn(1L);
        when(paperMapper.selectPage(any(Page.class), any())).thenReturn(new Page<TeacherPaper>(1, 10));

        TeacherAssignmentServiceImpl service = new TeacherAssignmentServiceImpl(
                contextService,
                paperMapper,
                paperQuestionMapper,
                examRecordMapper,
                questionMapper,
                questionCategoryMapper,
                teacherQuestionBankService
        );

        PageResult<TeacherPaperView> result = service.pagePapers(new TeacherPaperQuery());

        assertNotNull(result);
        assertEquals(0, result.list().size());
    }

    @Test
    void pageExamRecords_allowsMissingOptionalFilters() {
        TeacherPortalContextService contextService = mock(TeacherPortalContextService.class);
        TeacherPaperMapper paperMapper = mock(TeacherPaperMapper.class);
        TeacherPaperQuestionMapper paperQuestionMapper = mock(TeacherPaperQuestionMapper.class);
        TeacherExamRecordMapper examRecordMapper = mock(TeacherExamRecordMapper.class);
        QuestionMapper questionMapper = mock(QuestionMapper.class);
        QuestionCategoryMapper questionCategoryMapper = mock(QuestionCategoryMapper.class);
        TeacherQuestionBankService teacherQuestionBankService = mock(TeacherQuestionBankService.class);
        when(contextService.currentTenantId()).thenReturn(1L);
        when(examRecordMapper.selectPage(any(Page.class), any())).thenReturn(new Page<TeacherExamRecord>(1, 10));
        when(paperMapper.selectList(any())).thenReturn(List.of());

        TeacherAssignmentServiceImpl service = new TeacherAssignmentServiceImpl(
                contextService,
                paperMapper,
                paperQuestionMapper,
                examRecordMapper,
                questionMapper,
                questionCategoryMapper,
                teacherQuestionBankService
        );

        PageResult<TeacherExamRecordView> result = service.pageExamRecords(new TeacherExamRecordQuery());

        assertNotNull(result);
        assertEquals(0, result.list().size());
    }

    @Test
    void pagePublishes_allowsMissingOptionalFilters() {
        TeacherPortalContextService contextService = mock(TeacherPortalContextService.class);
        TeacherExamPublishMapper publishMapper = mock(TeacherExamPublishMapper.class);
        TeacherPaperMapper paperMapper = mock(TeacherPaperMapper.class);
        when(contextService.currentTenantId()).thenReturn(1L);
        when(publishMapper.selectPage(any(Page.class), any())).thenReturn(new Page<TeacherExamPublish>(1, 10));
        when(paperMapper.selectList(any())).thenReturn(List.of());

        TeacherExamPublishServiceImpl service = new TeacherExamPublishServiceImpl(
                contextService,
                publishMapper,
                paperMapper
        );

        PageResult<TeacherExamPublishView> result = service.pagePublishes(new TeacherExamPublishQuery());

        assertNotNull(result);
        assertEquals(0, result.list().size());
    }

    @Test
    void pageRecordsForGrading_allowsMissingOptionalFilters() {
        TeacherPortalContextService contextService = mock(TeacherPortalContextService.class);
        TeacherExamRecordMapper examRecordMapper = mock(TeacherExamRecordMapper.class);
        TeacherExamAnswerMapper examAnswerMapper = mock(TeacherExamAnswerMapper.class);
        TeacherPaperMapper paperMapper = mock(TeacherPaperMapper.class);
        when(contextService.currentTenantId()).thenReturn(1L);
        when(examRecordMapper.selectPage(any(Page.class), any())).thenReturn(new Page<TeacherExamRecord>(1, 10));
        when(paperMapper.selectList(any())).thenReturn(List.of());

        TeacherExamGradingServiceImpl service = new TeacherExamGradingServiceImpl(
                contextService,
                examRecordMapper,
                examAnswerMapper,
                paperMapper
        );

        PageResult<TeacherExamRecordView> result = service.pageRecordsForGrading(new TeacherExamRecordQuery());

        assertNotNull(result);
        assertEquals(0, result.list().size());
    }
}
