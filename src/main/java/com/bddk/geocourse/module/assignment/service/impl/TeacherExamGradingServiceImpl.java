package com.bddk.geocourse.module.assignment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.assignment.entity.TeacherExamAnswer;
import com.bddk.geocourse.module.assignment.entity.TeacherExamRecord;
import com.bddk.geocourse.module.assignment.entity.TeacherPaper;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamAnswerMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamRecordMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperMapper;
import com.bddk.geocourse.module.assignment.model.TeacherExamAnswerView;
import com.bddk.geocourse.module.assignment.model.TeacherExamGradeItemRequest;
import com.bddk.geocourse.module.assignment.model.TeacherExamGradeRequest;
import com.bddk.geocourse.module.assignment.model.TeacherExamGradingDetailView;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordView;
import com.bddk.geocourse.module.assignment.service.TeacherExamGradingService;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TeacherExamGradingServiceImpl implements TeacherExamGradingService {

    private final TeacherPortalContextService teacherPortalContextService;
    private final TeacherExamRecordMapper teacherExamRecordMapper;
    private final TeacherExamAnswerMapper teacherExamAnswerMapper;
    private final TeacherPaperMapper teacherPaperMapper;

    public TeacherExamGradingServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                         TeacherExamRecordMapper teacherExamRecordMapper,
                                         TeacherExamAnswerMapper teacherExamAnswerMapper,
                                         TeacherPaperMapper teacherPaperMapper) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.teacherExamRecordMapper = teacherExamRecordMapper;
        this.teacherExamAnswerMapper = teacherExamAnswerMapper;
        this.teacherPaperMapper = teacherPaperMapper;
    }

    @Override
    public PageResult<TeacherExamRecordView> pageRecordsForGrading(TeacherExamRecordQuery query) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        long pageNo = query.getPageNo() <= 0 ? 1 : query.getPageNo();
        long pageSize = query.getPageSize() <= 0 ? 10 : Math.min(query.getPageSize(), 100);
        Page<TeacherExamRecord> page = teacherExamRecordMapper.selectPage(new Page<>(pageNo, pageSize),
                Wrappers.<TeacherExamRecord>lambdaQuery()
                        .eq(TeacherExamRecord::getTenantId, tenantId)
                        .eq(query.getPaperId() != null, TeacherExamRecord::getPaperId, query.getPaperId())
                        .like(StringUtils.hasText(query.getStudentName()), TeacherExamRecord::getStudentName, query.getStudentName().trim())
                        .eq(StringUtils.hasText(query.getStatus()), TeacherExamRecord::getStatus, query.getStatus().trim())
                        .orderByDesc(TeacherExamRecord::getCreateTime)
                        .orderByDesc(TeacherExamRecord::getId));
        Map<Long, String> paperNames = teacherPaperMapper.selectList(Wrappers.<TeacherPaper>lambdaQuery()
                        .eq(TeacherPaper::getTenantId, tenantId))
                .stream()
                .collect(Collectors.toMap(TeacherPaper::getId, TeacherPaper::getName, (left, right) -> left));
        return PageResult.of(page.getRecords().stream().map(record -> toRecordView(record, paperNames.get(record.getPaperId()))).toList(),
                page.getTotal(), pageNo, pageSize);
    }

    @Override
    public TeacherExamGradingDetailView getGradingDetail(Long recordId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        TeacherExamRecord record = getRecordOrThrow(tenantId, recordId);
        TeacherPaper paper = getPaperOrThrow(tenantId, record.getPaperId());
        List<TeacherExamAnswer> answers = teacherExamAnswerMapper.selectList(Wrappers.<TeacherExamAnswer>lambdaQuery()
                .eq(TeacherExamAnswer::getTenantId, tenantId)
                .eq(TeacherExamAnswer::getExamRecordId, recordId)
                .orderByAsc(TeacherExamAnswer::getId));
        return toGradingDetail(record, paper.getName(), answers);
    }

    @Override
    @Transactional
    public TeacherExamGradingDetailView gradeRecord(Long recordId, TeacherExamGradeRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        TeacherExamRecord record = getRecordOrThrow(tenantId, recordId);
        List<TeacherExamAnswer> answers = teacherExamAnswerMapper.selectList(Wrappers.<TeacherExamAnswer>lambdaQuery()
                .eq(TeacherExamAnswer::getTenantId, tenantId)
                .eq(TeacherExamAnswer::getExamRecordId, recordId));
        Map<Long, TeacherExamAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(TeacherExamAnswer::getId, Function.identity(), (left, right) -> left));
        BigDecimal objectiveScore = BigDecimal.ZERO;
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (TeacherExamGradeItemRequest item : request.getItems()) {
            TeacherExamAnswer answer = answerMap.get(item.getAnswerId());
            if (answer == null) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "存在无效的答题明细");
            }
            if (item.getScore().compareTo(answer.getMaxScore()) > 0) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "批改分数不能超过题目满分");
            }
            answer.setScore(item.getScore());
            answer.setCorrectFlag(item.getCorrectFlag());
            answer.setTeacherComment(trimToNull(item.getTeacherComment()));
            answer.setReviewed(1);
            answer.setUpdateBy(teacherId);
            answer.setUpdateTime(now);
            teacherExamAnswerMapper.updateById(answer);
        }

        answers = teacherExamAnswerMapper.selectList(Wrappers.<TeacherExamAnswer>lambdaQuery()
                .eq(TeacherExamAnswer::getTenantId, tenantId)
                .eq(TeacherExamAnswer::getExamRecordId, recordId));
        for (TeacherExamAnswer answer : answers) {
            BigDecimal score = answer.getScore() == null ? BigDecimal.ZERO : answer.getScore();
            if ("TEXT".equalsIgnoreCase(answer.getQuestionType())) {
                subjectiveScore = subjectiveScore.add(score);
            } else {
                objectiveScore = objectiveScore.add(score);
            }
        }

        record.setObjectiveScore(objectiveScore);
        record.setSubjectiveScore(subjectiveScore);
        record.setScore(objectiveScore.add(subjectiveScore));
        record.setReviewComment(trimToNull(request.getReviewComment()));
        record.setGraderId(teacherId);
        record.setGradedTime(now);
        record.setStatus("REVIEWED");
        record.setUpdateBy(teacherId);
        record.setUpdateTime(now);
        if (record.getEndTime() == null) {
            record.setEndTime(now);
        }
        teacherExamRecordMapper.updateById(record);

        TeacherPaper paper = getPaperOrThrow(tenantId, record.getPaperId());
        return toGradingDetail(record, paper.getName(), answers);
    }

    private TeacherExamRecord getRecordOrThrow(Long tenantId, Long recordId) {
        TeacherExamRecord record = teacherExamRecordMapper.selectOne(Wrappers.<TeacherExamRecord>lambdaQuery()
                .eq(TeacherExamRecord::getTenantId, tenantId)
                .eq(TeacherExamRecord::getId, recordId)
                .last("limit 1"));
        if (record == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "考试记录不存在");
        }
        return record;
    }

    private TeacherPaper getPaperOrThrow(Long tenantId, Long paperId) {
        TeacherPaper paper = teacherPaperMapper.selectOne(Wrappers.<TeacherPaper>lambdaQuery()
                .eq(TeacherPaper::getTenantId, tenantId)
                .eq(TeacherPaper::getId, paperId)
                .last("limit 1"));
        if (paper == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "试卷不存在");
        }
        return paper;
    }

    private TeacherExamRecordView toRecordView(TeacherExamRecord record, String paperName) {
        TeacherExamRecordView view = new TeacherExamRecordView();
        view.setId(record.getId());
        view.setPublishId(record.getPublishId());
        view.setPublishTitle(record.getPublishTitle());
        view.setPaperId(record.getPaperId());
        view.setPaperName(paperName);
        view.setStudentId(record.getStudentId());
        view.setStudentName(record.getStudentName());
        view.setScore(record.getScore());
        view.setObjectiveScore(record.getObjectiveScore());
        view.setSubjectiveScore(record.getSubjectiveScore());
        view.setStatus(record.getStatus());
        view.setWindowSwitches(record.getWindowSwitches());
        view.setReviewComment(record.getReviewComment());
        view.setStartTime(record.getStartTime());
        view.setEndTime(record.getEndTime());
        view.setGradedTime(record.getGradedTime());
        view.setCreatedAt(record.getCreateTime());
        return view;
    }

    private TeacherExamGradingDetailView toGradingDetail(TeacherExamRecord record,
                                                         String paperName,
                                                         List<TeacherExamAnswer> answers) {
        TeacherExamGradingDetailView view = new TeacherExamGradingDetailView();
        view.setRecordId(record.getId());
        view.setPublishId(record.getPublishId());
        view.setPublishTitle(record.getPublishTitle());
        view.setPaperId(record.getPaperId());
        view.setPaperName(paperName);
        view.setStudentId(record.getStudentId());
        view.setStudentName(record.getStudentName());
        view.setStatus(record.getStatus());
        view.setScore(record.getScore());
        view.setObjectiveScore(record.getObjectiveScore());
        view.setSubjectiveScore(record.getSubjectiveScore());
        view.setWindowSwitches(record.getWindowSwitches());
        view.setReviewComment(record.getReviewComment());
        view.setStartTime(record.getStartTime());
        view.setEndTime(record.getEndTime());
        view.setGradedTime(record.getGradedTime());
        view.setAnswers(answers.stream().map(this::toAnswerView).toList());
        return view;
    }

    private TeacherExamAnswerView toAnswerView(TeacherExamAnswer answer) {
        TeacherExamAnswerView view = new TeacherExamAnswerView();
        view.setId(answer.getId());
        view.setQuestionId(answer.getQuestionId());
        view.setQuestionType(answer.getQuestionType());
        view.setQuestionTitle(answer.getQuestionTitle());
        view.setStandardAnswer(answer.getStandardAnswer());
        view.setUserAnswer(answer.getUserAnswer());
        view.setMaxScore(answer.getMaxScore());
        view.setScore(answer.getScore());
        view.setCorrectFlag(answer.getCorrectFlag());
        view.setAiComment(answer.getAiComment());
        view.setTeacherComment(answer.getTeacherComment());
        view.setReviewed(answer.getReviewed());
        return view;
    }

    private String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
