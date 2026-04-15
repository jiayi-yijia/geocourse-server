package com.bddk.geocourse.module.assignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.assignment.entity.TeacherExamRecord;
import com.bddk.geocourse.module.assignment.entity.TeacherPaper;
import com.bddk.geocourse.module.assignment.entity.TeacherPaperQuestion;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamRecordMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperQuestionMapper;
import com.bddk.geocourse.module.assignment.model.TeacherExamRankingView;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordView;
import com.bddk.geocourse.module.assignment.model.TeacherPaperQuery;
import com.bddk.geocourse.module.assignment.model.TeacherPaperQuestionScoreRequest;
import com.bddk.geocourse.module.assignment.model.TeacherPaperQuestionView;
import com.bddk.geocourse.module.assignment.model.TeacherPaperSaveRequest;
import com.bddk.geocourse.module.assignment.model.TeacherPaperView;
import com.bddk.geocourse.module.assignment.service.TeacherAssignmentService;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import com.bddk.geocourse.module.questionbank.entity.Question;
import com.bddk.geocourse.module.questionbank.entity.QuestionCategory;
import com.bddk.geocourse.module.questionbank.mapper.QuestionCategoryMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> PAPER_STATUSES = Set.of("DRAFT", "PUBLISHED", "STOPPED");
    private static final Set<String> IN_PROGRESS_STATUSES = Set.of("IN_PROGRESS", "进行中");

    private final TeacherPortalContextService teacherPortalContextService;
    private final TeacherPaperMapper teacherPaperMapper;
    private final TeacherPaperQuestionMapper teacherPaperQuestionMapper;
    private final TeacherExamRecordMapper teacherExamRecordMapper;
    private final QuestionMapper questionMapper;
    private final QuestionCategoryMapper questionCategoryMapper;

    public TeacherAssignmentServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                        TeacherPaperMapper teacherPaperMapper,
                                        TeacherPaperQuestionMapper teacherPaperQuestionMapper,
                                        TeacherExamRecordMapper teacherExamRecordMapper,
                                        QuestionMapper questionMapper,
                                        QuestionCategoryMapper questionCategoryMapper) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.teacherPaperMapper = teacherPaperMapper;
        this.teacherPaperQuestionMapper = teacherPaperQuestionMapper;
        this.teacherExamRecordMapper = teacherExamRecordMapper;
        this.questionMapper = questionMapper;
        this.questionCategoryMapper = questionCategoryMapper;
    }

    @Override
    public PageResult<TeacherPaperView> pagePapers(TeacherPaperQuery query) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        Page<TeacherPaper> page = teacherPaperMapper.selectPage(new Page<>(pageNo, pageSize),
                Wrappers.<TeacherPaper>lambdaQuery()
                        .eq(TeacherPaper::getTenantId, tenantId)
                        .like(StringUtils.hasText(query.getName()), TeacherPaper::getName, query.getName().trim())
                        .eq(StringUtils.hasText(query.getStatus()), TeacherPaper::getStatus, normalizePaperStatusAllowBlank(query.getStatus()))
                        .orderByDesc(TeacherPaper::getUpdateTime)
                        .orderByDesc(TeacherPaper::getId));
        List<TeacherPaperView> views = page.getRecords().stream().map(this::toPaperView).toList();
        return PageResult.of(views, page.getTotal(), pageNo, pageSize);
    }

    @Override
    public TeacherPaperView getPaper(Long paperId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        TeacherPaper paper = getPaperOrThrow(tenantId, paperId);
        TeacherPaperView view = toPaperView(paper);
        view.setQuestions(loadPaperQuestions(tenantId, paperId));
        return view;
    }

    @Override
    @Transactional
    public TeacherPaperView createPaper(TeacherPaperSaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        Map<Long, Question> questionMap = validatePaperRequest(tenantId, request, null);
        LocalDateTime now = LocalDateTime.now();

        TeacherPaper paper = new TeacherPaper();
        paper.setTenantId(tenantId);
        paper.setName(trimToNull(request.getName()));
        paper.setDescription(trimToNull(request.getDescription()));
        paper.setStatus("DRAFT");
        paper.setDuration(request.getDuration());
        paper.setQuestionCount(request.getQuestions().size());
        paper.setTotalScore(sumTotalScore(request.getQuestions()));
        paper.setCreateBy(teacherId);
        paper.setUpdateBy(teacherId);
        paper.setCreateTime(now);
        paper.setUpdateTime(now);
        teacherPaperMapper.insert(paper);

        savePaperQuestions(paper.getId(), tenantId, teacherId, request.getQuestions(), now);
        TeacherPaperView view = toPaperView(paper);
        view.setQuestions(toPaperQuestionViews(request.getQuestions(), questionMap));
        return view;
    }

    @Override
    @Transactional
    public TeacherPaperView updatePaper(Long paperId, TeacherPaperSaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        TeacherPaper paper = getPaperOrThrow(tenantId, paperId);
        ensureNoInProgressRecord(tenantId, paperId);
        Map<Long, Question> questionMap = validatePaperRequest(tenantId, request, paperId);
        LocalDateTime now = LocalDateTime.now();

        paper.setName(trimToNull(request.getName()));
        paper.setDescription(trimToNull(request.getDescription()));
        paper.setDuration(request.getDuration());
        paper.setQuestionCount(request.getQuestions().size());
        paper.setTotalScore(sumTotalScore(request.getQuestions()));
        paper.setStatus("DRAFT");
        paper.setUpdateBy(teacherId);
        paper.setUpdateTime(now);
        teacherPaperMapper.updateById(paper);

        teacherPaperQuestionMapper.delete(Wrappers.<TeacherPaperQuestion>lambdaQuery()
                .eq(TeacherPaperQuestion::getTenantId, tenantId)
                .eq(TeacherPaperQuestion::getPaperId, paperId));
        savePaperQuestions(paperId, tenantId, teacherId, request.getQuestions(), now);
        TeacherPaperView view = toPaperView(paper);
        view.setQuestions(toPaperQuestionViews(request.getQuestions(), questionMap));
        return view;
    }

    @Override
    @Transactional
    public void updatePaperStatus(Long paperId, String status) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        TeacherPaper paper = getPaperOrThrow(tenantId, paperId);
        ensureNoInProgressRecord(tenantId, paperId);
        paper.setStatus(normalizePaperStatus(status));
        paper.setUpdateBy(teacherPortalContextService.currentTeacherId());
        paper.setUpdateTime(LocalDateTime.now());
        teacherPaperMapper.updateById(paper);
    }

    @Override
    @Transactional
    public void deletePaper(Long paperId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        TeacherPaper paper = getPaperOrThrow(tenantId, paperId);
        if ("PUBLISHED".equalsIgnoreCase(paper.getStatus())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "已发布试卷不允许删除");
        }
        ensureNoInProgressRecord(tenantId, paperId);
        teacherPaperQuestionMapper.delete(Wrappers.<TeacherPaperQuestion>lambdaQuery()
                .eq(TeacherPaperQuestion::getTenantId, tenantId)
                .eq(TeacherPaperQuestion::getPaperId, paperId));
        teacherPaperMapper.deleteById(paperId);
    }

    @Override
    public PageResult<TeacherExamRecordView> pageExamRecords(TeacherExamRecordQuery query) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        Page<TeacherExamRecord> page = teacherExamRecordMapper.selectPage(new Page<>(pageNo, pageSize),
                Wrappers.<TeacherExamRecord>lambdaQuery()
                        .eq(TeacherExamRecord::getTenantId, tenantId)
                        .eq(query.getPaperId() != null, TeacherExamRecord::getPaperId, query.getPaperId())
                        .like(StringUtils.hasText(query.getStudentName()), TeacherExamRecord::getStudentName, query.getStudentName().trim())
                        .eq(StringUtils.hasText(query.getStatus()), TeacherExamRecord::getStatus, query.getStatus().trim())
                        .orderByDesc(TeacherExamRecord::getCreateTime)
                        .orderByDesc(TeacherExamRecord::getId));
        Map<Long, String> paperNameMap = teacherPaperMapper.selectList(Wrappers.<TeacherPaper>lambdaQuery()
                        .eq(TeacherPaper::getTenantId, tenantId))
                .stream()
                .collect(Collectors.toMap(TeacherPaper::getId, TeacherPaper::getName, (left, right) -> left));
        List<TeacherExamRecordView> views = page.getRecords().stream()
                .map(record -> toExamRecordView(record, paperNameMap.get(record.getPaperId())))
                .toList();
        return PageResult.of(views, page.getTotal(), pageNo, pageSize);
    }

    @Override
    @Transactional
    public void deleteExamRecord(Long recordId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        TeacherExamRecord record = getExamRecordOrThrow(tenantId, recordId);
        if (isInProgress(record.getStatus())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "进行中的考试记录不允许删除");
        }
        teacherExamRecordMapper.deleteById(recordId);
    }

    @Override
    public List<TeacherExamRankingView> listRanking(Long paperId, Integer limit) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        int actualLimit = limit == null || limit <= 0 ? 20 : Math.min(limit, 100);
        List<TeacherExamRecord> records = teacherExamRecordMapper.selectList(Wrappers.<TeacherExamRecord>lambdaQuery()
                .eq(TeacherExamRecord::getTenantId, tenantId)
                .eq(paperId != null, TeacherExamRecord::getPaperId, paperId)
                .notIn(TeacherExamRecord::getStatus, IN_PROGRESS_STATUSES)
                .orderByDesc(TeacherExamRecord::getScore)
                .orderByAsc(TeacherExamRecord::getEndTime)
                .orderByAsc(TeacherExamRecord::getId));
        if (records.size() > actualLimit) {
            records = records.subList(0, actualLimit);
        }
        Map<Long, String> paperNameMap = teacherPaperMapper.selectList(Wrappers.<TeacherPaper>lambdaQuery()
                        .eq(TeacherPaper::getTenantId, tenantId))
                .stream()
                .collect(Collectors.toMap(TeacherPaper::getId, TeacherPaper::getName, (left, right) -> left));
        List<TeacherExamRankingView> result = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            TeacherExamRecord record = records.get(i);
            TeacherExamRankingView view = new TeacherExamRankingView();
            view.setRankNo(i + 1);
            view.setPaperId(record.getPaperId());
            view.setPaperName(paperNameMap.get(record.getPaperId()));
            view.setStudentId(record.getStudentId());
            view.setStudentName(record.getStudentName());
            view.setScore(record.getScore());
            view.setEndTime(record.getEndTime());
            result.add(view);
        }
        return result;
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

    private TeacherExamRecord getExamRecordOrThrow(Long tenantId, Long recordId) {
        TeacherExamRecord record = teacherExamRecordMapper.selectOne(Wrappers.<TeacherExamRecord>lambdaQuery()
                .eq(TeacherExamRecord::getTenantId, tenantId)
                .eq(TeacherExamRecord::getId, recordId)
                .last("limit 1"));
        if (record == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "考试记录不存在");
        }
        return record;
    }

    private Map<Long, Question> validatePaperRequest(Long tenantId, TeacherPaperSaveRequest request, Long excludePaperId) {
        ensurePaperNameUnique(tenantId, request.getName(), excludePaperId);
        if (CollectionUtils.isEmpty(request.getQuestions())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "试卷至少需要一道题目");
        }
        LinkedHashSet<Long> questionIds = new LinkedHashSet<>();
        for (TeacherPaperQuestionScoreRequest item : request.getQuestions()) {
            if (item.getQuestionId() == null || item.getScore() == null || item.getScore().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "试卷题目配置不完整");
            }
            if (!questionIds.add(item.getQuestionId())) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "试卷中存在重复题目");
            }
        }
        List<Question> questions = questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .in(Question::getId, questionIds));
        if (questions.size() != questionIds.size()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "试卷中包含不存在的题目");
        }
        return questions.stream().collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private void ensurePaperNameUnique(Long tenantId, String name, Long excludePaperId) {
        LambdaQueryWrapper<TeacherPaper> query = Wrappers.<TeacherPaper>lambdaQuery()
                .eq(TeacherPaper::getTenantId, tenantId)
                .eq(TeacherPaper::getName, trimToNull(name));
        if (excludePaperId != null) {
            query.ne(TeacherPaper::getId, excludePaperId);
        }
        if (teacherPaperMapper.selectCount(query) > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "已存在同名试卷");
        }
    }

    private void savePaperQuestions(Long paperId,
                                    Long tenantId,
                                    Long teacherId,
                                    List<TeacherPaperQuestionScoreRequest> questions,
                                    LocalDateTime now) {
        for (int i = 0; i < questions.size(); i++) {
            TeacherPaperQuestionScoreRequest item = questions.get(i);
            TeacherPaperQuestion relation = new TeacherPaperQuestion();
            relation.setTenantId(tenantId);
            relation.setPaperId(paperId);
            relation.setQuestionId(item.getQuestionId());
            relation.setScore(item.getScore());
            relation.setSortNo(i);
            relation.setCreateBy(teacherId);
            relation.setUpdateBy(teacherId);
            relation.setCreateTime(now);
            relation.setUpdateTime(now);
            teacherPaperQuestionMapper.insert(relation);
        }
    }

    private void ensureNoInProgressRecord(Long tenantId, Long paperId) {
        long count = teacherExamRecordMapper.selectCount(Wrappers.<TeacherExamRecord>lambdaQuery()
                .eq(TeacherExamRecord::getTenantId, tenantId)
                .eq(TeacherExamRecord::getPaperId, paperId)
                .in(TeacherExamRecord::getStatus, IN_PROGRESS_STATUSES));
        if (count > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "当前试卷存在进行中的考试记录，不能执行该操作");
        }
    }

    private List<TeacherPaperQuestionView> loadPaperQuestions(Long tenantId, Long paperId) {
        List<TeacherPaperQuestion> relations = teacherPaperQuestionMapper.selectList(Wrappers.<TeacherPaperQuestion>lambdaQuery()
                .eq(TeacherPaperQuestion::getTenantId, tenantId)
                .eq(TeacherPaperQuestion::getPaperId, paperId)
                .orderByAsc(TeacherPaperQuestion::getSortNo)
                .orderByAsc(TeacherPaperQuestion::getId));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> questionIds = relations.stream().map(TeacherPaperQuestion::getQuestionId).toList();
        Map<Long, Question> questionMap = questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                        .eq(Question::getTenantId, tenantId)
                        .in(Question::getId, questionIds))
                .stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));
        Map<Long, QuestionCategory> categoryMap = questionCategoryMapper.selectList(Wrappers.<QuestionCategory>lambdaQuery()
                        .eq(QuestionCategory::getTenantId, tenantId))
                .stream()
                .collect(Collectors.toMap(QuestionCategory::getId, Function.identity(), (left, right) -> left));
        return relations.stream()
                .map(relation -> toPaperQuestionView(questionMap.get(relation.getQuestionId()), categoryMap, relation.getScore()))
                .filter(view -> view.getQuestionId() != null)
                .toList();
    }

    private List<TeacherPaperQuestionView> toPaperQuestionViews(List<TeacherPaperQuestionScoreRequest> relations,
                                                                Map<Long, Question> questionMap) {
        Map<Long, QuestionCategory> categoryMap = questionCategoryMapper.selectList(Wrappers.<QuestionCategory>lambdaQuery()
                        .eq(QuestionCategory::getTenantId, teacherPortalContextService.currentTenantId()))
                .stream()
                .collect(Collectors.toMap(QuestionCategory::getId, Function.identity(), (left, right) -> left));
        return relations.stream()
                .map(item -> toPaperQuestionView(questionMap.get(item.getQuestionId()), categoryMap, item.getScore()))
                .toList();
    }

    private TeacherPaperQuestionView toPaperQuestionView(Question question,
                                                         Map<Long, QuestionCategory> categoryMap,
                                                         BigDecimal paperScore) {
        TeacherPaperQuestionView view = new TeacherPaperQuestionView();
        if (question == null) {
            return view;
        }
        QuestionCategory category = categoryMap.get(question.getCategoryId());
        view.setQuestionId(question.getId());
        view.setCategoryId(question.getCategoryId());
        view.setCategoryName(category == null ? null : category.getName());
        view.setTitle(question.getTitle());
        view.setType(question.getType());
        view.setDifficulty(question.getDifficulty());
        view.setDefaultScore(question.getDefaultScore());
        view.setPaperScore(paperScore);
        return view;
    }

    private TeacherPaperView toPaperView(TeacherPaper paper) {
        TeacherPaperView view = new TeacherPaperView();
        view.setId(paper.getId());
        view.setName(paper.getName());
        view.setDescription(paper.getDescription());
        view.setStatus(paper.getStatus());
        view.setTotalScore(paper.getTotalScore());
        view.setQuestionCount(paper.getQuestionCount());
        view.setDuration(paper.getDuration());
        view.setCreatedAt(paper.getCreateTime());
        view.setUpdatedAt(paper.getUpdateTime());
        return view;
    }

    private TeacherExamRecordView toExamRecordView(TeacherExamRecord record, String paperName) {
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

    private BigDecimal sumTotalScore(List<TeacherPaperQuestionScoreRequest> questions) {
        return questions.stream()
                .map(TeacherPaperQuestionScoreRequest::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String normalizePaperStatus(String status) {
        String normalized = normalizePaperStatusAllowBlank(status);
        if (!StringUtils.hasText(normalized) || !PAPER_STATUSES.contains(normalized)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "不支持的试卷状态");
        }
        return normalized;
    }

    private String normalizePaperStatusAllowBlank(String status) {
        return StringUtils.hasText(status) ? status.trim().toUpperCase() : null;
    }

    private boolean isInProgress(String status) {
        return StringUtils.hasText(status) && IN_PROGRESS_STATUSES.contains(status.trim());
    }

    private long normalizePageNo(long pageNo) {
        return pageNo <= 0 ? 1 : pageNo;
    }

    private long normalizePageSize(long pageSize) {
        if (pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
