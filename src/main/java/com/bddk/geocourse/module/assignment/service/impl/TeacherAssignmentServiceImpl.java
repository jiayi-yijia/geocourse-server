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
import com.bddk.geocourse.module.assignment.model.TeacherPaperAttachmentImportRequest;
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
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionAnswerRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionChoiceRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionSaveRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionView;
import com.bddk.geocourse.module.questionbank.service.TeacherQuestionBankService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_IMPORTED_DURATION = 60;
    private static final int DEFAULT_IMPORTED_SCORE = 5;
    private static final int STATUS_ENABLED = 1;
    private static final Set<String> PAPER_STATUSES = Set.of("DRAFT", "PUBLISHED", "STOPPED");
    private static final Set<String> IN_PROGRESS_STATUSES = Set.of("IN_PROGRESS", "进行中");
    private static final Set<String> SUPPORTED_IMPORT_EXTENSIONS = Set.of("docx", "md", "txt", "pdf");
    private static final Set<String> JUDGE_TRUE_VALUES = Set.of("TRUE", "T", "YES", "Y", "对", "正确", "是");
    private static final Set<String> JUDGE_FALSE_VALUES = Set.of("FALSE", "F", "NO", "N", "错", "错误", "否");
    private static final DateTimeFormatter IMPORT_CATEGORY_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern QUESTION_START_PATTERN = Pattern.compile(
            "^(?:#{1,6}\\s*)?(?:(?:第\\s*[一二三四五六七八九十百零0-9]+\\s*题)|(?:[Qq](?:uestion)?\\s*\\d+)|(?:\\d+)|(?:[一二三四五六七八九十]+))[\\.、．\\):：\\s-]*(.*)$");
    private static final Pattern CHOICE_LINE_PATTERN = Pattern.compile("^(?:[-*]\\s*)?([A-H])[\\.、．\\)\\]:：\\s]+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INLINE_CHOICE_PATTERN = Pattern.compile("([A-H])[\\.、．\\)]\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern METADATA_PATTERN = Pattern.compile(
            "(?i)(答案|参考答案|标准答案|答|解析|分析|难度|分值|分数|类型|题型|answer|analysis|explanation|difficulty|score|type)\\s*[:：]");

    private final TeacherPortalContextService teacherPortalContextService;
    private final TeacherPaperMapper teacherPaperMapper;
    private final TeacherPaperQuestionMapper teacherPaperQuestionMapper;
    private final TeacherExamRecordMapper teacherExamRecordMapper;
    private final QuestionMapper questionMapper;
    private final QuestionCategoryMapper questionCategoryMapper;
    private final TeacherQuestionBankService teacherQuestionBankService;

    public TeacherAssignmentServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                        TeacherPaperMapper teacherPaperMapper,
                                        TeacherPaperQuestionMapper teacherPaperQuestionMapper,
                                        TeacherExamRecordMapper teacherExamRecordMapper,
                                        QuestionMapper questionMapper,
                                        QuestionCategoryMapper questionCategoryMapper,
                                        TeacherQuestionBankService teacherQuestionBankService) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.teacherPaperMapper = teacherPaperMapper;
        this.teacherPaperQuestionMapper = teacherPaperQuestionMapper;
        this.teacherExamRecordMapper = teacherExamRecordMapper;
        this.questionMapper = questionMapper;
        this.questionCategoryMapper = questionCategoryMapper;
        this.teacherQuestionBankService = teacherQuestionBankService;
    }

    @Override
    public PageResult<TeacherPaperView> pagePapers(TeacherPaperQuery query) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        String paperName = trimToNull(query.getName());
        String paperStatus = normalizePaperStatusAllowBlank(query.getStatus());
        Page<TeacherPaper> page = teacherPaperMapper.selectPage(new Page<>(pageNo, pageSize),
                Wrappers.<TeacherPaper>lambdaQuery()
                        .eq(TeacherPaper::getTenantId, tenantId)
                        .like(paperName != null, TeacherPaper::getName, paperName)
                        .eq(paperStatus != null, TeacherPaper::getStatus, paperStatus)
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
    public TeacherPaperView importPaperFromAttachment(TeacherPaperAttachmentImportRequest request) {
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Please upload a file first");
        }

        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        String extension = normalizeImportExtension(file);
        String text = normalizeImportText(readImportText(file, extension));
        if (!StringUtils.hasText(text)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "No readable text was found in the attachment");
        }

        int defaultScore = normalizeImportedDefaultScore(request.getDefaultScore());
        String paperName = resolveAvailablePaperName(tenantId, resolveImportedPaperName(request, file));
        String description = buildImportedPaperDescription(request.getDescription(), file.getOriginalFilename());
        List<ImportedQuestionDraft> drafts = deduplicateImportedTitles(parseImportedQuestions(text), defaultScore);
        if (drafts.isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST,
                    "No questions were recognized. Please format the file with question numbers, options, and answers");
        }

        Long categoryId = createImportCategory(tenantId, teacherId, paperName);
        List<TeacherPaperQuestionScoreRequest> relations = new ArrayList<>();
        for (ImportedQuestionDraft draft : drafts) {
            TeacherQuestionView question = teacherQuestionBankService.createQuestion(
                    toImportedQuestionRequest(draft, categoryId, defaultScore));
            TeacherPaperQuestionScoreRequest relation = new TeacherPaperQuestionScoreRequest();
            relation.setQuestionId(question.getId());
            relation.setScore(draft.getPaperScoreOrDefault(defaultScore));
            relations.add(relation);
        }

        TeacherPaperSaveRequest paperRequest = new TeacherPaperSaveRequest();
        paperRequest.setName(paperName);
        paperRequest.setDescription(description);
        paperRequest.setDuration(request.getDuration() == null ? DEFAULT_IMPORTED_DURATION : request.getDuration());
        paperRequest.setQuestions(relations);
        return createPaper(paperRequest);
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
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Published papers cannot be deleted");
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
        String studentName = trimToNull(query.getStudentName());
        String recordStatus = trimToNull(query.getStatus());
        Page<TeacherExamRecord> page = teacherExamRecordMapper.selectPage(new Page<>(pageNo, pageSize),
                Wrappers.<TeacherExamRecord>lambdaQuery()
                        .eq(TeacherExamRecord::getTenantId, tenantId)
                        .eq(query.getPaperId() != null, TeacherExamRecord::getPaperId, query.getPaperId())
                        .like(studentName != null, TeacherExamRecord::getStudentName, studentName)
                        .eq(recordStatus != null, TeacherExamRecord::getStatus, recordStatus)
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
            throw new ServiceException(ErrorCode.BAD_REQUEST, "In-progress exam records cannot be deleted");
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
            throw new ServiceException(ErrorCode.NOT_FOUND, "Paper not found");
        }
        return paper;
    }

    private TeacherExamRecord getExamRecordOrThrow(Long tenantId, Long recordId) {
        TeacherExamRecord record = teacherExamRecordMapper.selectOne(Wrappers.<TeacherExamRecord>lambdaQuery()
                .eq(TeacherExamRecord::getTenantId, tenantId)
                .eq(TeacherExamRecord::getId, recordId)
                .last("limit 1"));
        if (record == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "Exam record not found");
        }
        return record;
    }

    private Map<Long, Question> validatePaperRequest(Long tenantId, TeacherPaperSaveRequest request, Long excludePaperId) {
        ensurePaperNameUnique(tenantId, request.getName(), excludePaperId);
        if (CollectionUtils.isEmpty(request.getQuestions())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "A paper must contain at least one question");
        }
        LinkedHashSet<Long> questionIds = new LinkedHashSet<>();
        for (TeacherPaperQuestionScoreRequest item : request.getQuestions()) {
            if (item.getQuestionId() == null || item.getScore() == null || item.getScore().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Paper question configuration is incomplete");
            }
            if (!questionIds.add(item.getQuestionId())) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Duplicate questions are not allowed in the same paper");
            }
        }
        List<Question> questions = questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .in(Question::getId, questionIds));
        if (questions.size() != questionIds.size()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "The paper references missing questions");
        }
        return questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private void ensurePaperNameUnique(Long tenantId, String name, Long excludePaperId) {
        LambdaQueryWrapper<TeacherPaper> query = Wrappers.<TeacherPaper>lambdaQuery()
                .eq(TeacherPaper::getTenantId, tenantId)
                .eq(TeacherPaper::getName, trimToNull(name));
        if (excludePaperId != null) {
            query.ne(TeacherPaper::getId, excludePaperId);
        }
        if (teacherPaperMapper.selectCount(query) > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "A paper with the same name already exists");
        }
    }

    private String resolveAvailablePaperName(Long tenantId, String desiredName) {
        String baseName = trimToNull(desiredName);
        if (!StringUtils.hasText(baseName)) {
            baseName = "Imported Paper";
        }
        String candidate = baseName;
        int suffix = 2;
        while (paperNameExists(tenantId, candidate)) {
            candidate = baseName + " (" + suffix + ")";
            suffix++;
        }
        return candidate;
    }

    private boolean paperNameExists(Long tenantId, String paperName) {
        return teacherPaperMapper.selectCount(Wrappers.<TeacherPaper>lambdaQuery()
                .eq(TeacherPaper::getTenantId, tenantId)
                .eq(TeacherPaper::getName, trimToNull(paperName))) > 0;
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
            throw new ServiceException(ErrorCode.BAD_REQUEST,
                    "This paper has in-progress exam records and cannot be changed right now");
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

    private Long createImportCategory(Long tenantId, Long teacherId, String paperName) {
        QuestionCategory root = getOrCreateImportRootCategory(tenantId, teacherId);
        LocalDateTime now = LocalDateTime.now();
        QuestionCategory category = new QuestionCategory();
        category.setTenantId(tenantId);
        category.setParentId(root.getId());
        category.setName(paperName + " - " + IMPORT_CATEGORY_SUFFIX_FORMATTER.format(now));
        category.setDescription("Auto-created from attachment import");
        category.setSortNo(0);
        category.setStatus(STATUS_ENABLED);
        category.setCreateBy(teacherId);
        category.setUpdateBy(teacherId);
        category.setCreateTime(now);
        category.setUpdateTime(now);
        questionCategoryMapper.insert(category);
        return category.getId();
    }

    private QuestionCategory getOrCreateImportRootCategory(Long tenantId, Long teacherId) {
        QuestionCategory existing = questionCategoryMapper.selectOne(Wrappers.<QuestionCategory>lambdaQuery()
                .eq(QuestionCategory::getTenantId, tenantId)
                .eq(QuestionCategory::getParentId, 0L)
                .eq(QuestionCategory::getName, "Paper Imports")
                .last("limit 1"));
        if (existing != null) {
            return existing;
        }
        LocalDateTime now = LocalDateTime.now();
        QuestionCategory category = new QuestionCategory();
        category.setTenantId(tenantId);
        category.setParentId(0L);
        category.setName("Paper Imports");
        category.setDescription("Auto-created category root for imported papers");
        category.setSortNo(0);
        category.setStatus(STATUS_ENABLED);
        category.setCreateBy(teacherId);
        category.setUpdateBy(teacherId);
        category.setCreateTime(now);
        category.setUpdateTime(now);
        questionCategoryMapper.insert(category);
        return category;
    }

    private TeacherQuestionSaveRequest toImportedQuestionRequest(ImportedQuestionDraft draft, Long categoryId, int defaultScore) {
        TeacherQuestionSaveRequest request = new TeacherQuestionSaveRequest();
        request.setCategoryId(categoryId);
        request.setTitle(draft.title());
        request.setDifficulty(draft.difficulty());
        request.setAnalysis(trimToNull(draft.analysis()));

        String answerText = trimToNull(draft.answerText());
        List<ImportedChoiceDraft> choices = draft.choices();
        LinkedHashSet<String> answerKeys = normalizeChoiceAnswerKeys(answerText);
        String judgeAnswer = normalizeJudgeAnswer(answerText);

        if (choices.size() >= 2 && ("CHOICE".equals(draft.explicitType()) || !answerKeys.isEmpty()) && !answerKeys.isEmpty()) {
            request.setType("CHOICE");
            request.setMultiSelect(answerKeys.size() > 1);
            request.setDefaultScore(draft.defaultScoreOr(defaultScore));
            request.setChoices(toChoiceRequests(choices, answerKeys));
            TeacherQuestionAnswerRequest answer = new TeacherQuestionAnswerRequest();
            answer.setAnswerText(String.join(",", answerKeys));
            request.setAnswer(answer);
            return request;
        }

        if (judgeAnswer != null) {
            request.setType("JUDGE");
            request.setMultiSelect(Boolean.FALSE);
            request.setDefaultScore(draft.defaultScoreOr(defaultScore));
            TeacherQuestionAnswerRequest answer = new TeacherQuestionAnswerRequest();
            answer.setAnswerText(judgeAnswer);
            request.setAnswer(answer);
            return request;
        }

        request.setType("TEXT");
        request.setMultiSelect(Boolean.FALSE);
        request.setDefaultScore(draft.defaultScoreOr(defaultScore));
        request.setTitle(buildTextQuestionTitle(draft.title(), choices));
        TeacherQuestionAnswerRequest answer = new TeacherQuestionAnswerRequest();
        answer.setAnswerText(StringUtils.hasText(answerText) ? answerText : "Please grade this imported question manually");
        answer.setGradingRule(choices.isEmpty() ? null : "Options were preserved in the title because no objective answer was recognized");
        request.setAnswer(answer);
        return request;
    }

    private List<TeacherQuestionChoiceRequest> toChoiceRequests(List<ImportedChoiceDraft> choices, Set<String> answerKeys) {
        List<TeacherQuestionChoiceRequest> result = new ArrayList<>();
        for (ImportedChoiceDraft choice : choices) {
            TeacherQuestionChoiceRequest item = new TeacherQuestionChoiceRequest();
            item.setKey(choice.key());
            item.setText(choice.text());
            item.setCorrect(answerKeys.contains(choice.key()));
            result.add(item);
        }
        return result;
    }

    private String resolveImportedPaperName(TeacherPaperAttachmentImportRequest request, MultipartFile file) {
        String preferredName = trimToNull(request.getName());
        if (preferredName != null) {
            return preferredName;
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return "Imported Paper";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            return trimToNull(originalFilename.substring(0, dotIndex));
        }
        return trimToNull(originalFilename);
    }

    private String buildImportedPaperDescription(String description, String fileName) {
        String normalized = trimToNull(description);
        if (normalized != null) {
            return normalized;
        }
        if (!StringUtils.hasText(fileName)) {
            return "Created from attachment import";
        }
        return "Created from attachment: " + fileName.trim();
    }

    private int normalizeImportedDefaultScore(Integer defaultScore) {
        if (defaultScore == null || defaultScore <= 0) {
            return DEFAULT_IMPORTED_SCORE;
        }
        return defaultScore;
    }

    private String normalizeImportExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Unsupported file type. Please upload docx, md, txt, or pdf");
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_IMPORT_EXTENSIONS.contains(extension)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Only docx, md, txt, and pdf files are supported");
        }
        return extension;
    }

    private String readImportText(MultipartFile file, String extension) {
        try {
            byte[] bytes = file.getBytes();
            return switch (extension) {
                case "docx" -> extractDocxText(bytes);
                case "pdf" -> extractPdfText(bytes);
                case "md", "txt" -> new String(bytes, StandardCharsets.UTF_8);
                default -> "";
            };
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Failed to read the uploaded attachment");
        }
    }

    private String extractDocxText(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Failed to parse the DOCX attachment");
        }
    }

    private String extractPdfText(byte[] bytes) {
        try (PDDocument document = PDDocument.load(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Failed to parse the PDF attachment");
        }
    }

    private String normalizeImportText(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return "";
        }
        return rawText.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\f', '\n')
                .replace('\u3000', ' ')
                .trim();
    }

    private List<ImportedQuestionDraft> parseImportedQuestions(String text) {
        List<List<String>> blocks = splitQuestionBlocks(text);
        List<ImportedQuestionDraft> result = new ArrayList<>();
        for (List<String> block : blocks) {
            ImportedQuestionDraft draft = parseQuestionBlock(block);
            if (draft == null || !StringUtils.hasText(draft.title()) || isSectionHeader(draft.title())) {
                continue;
            }
            result.add(draft);
        }
        return result;
    }

    private List<List<String>> splitQuestionBlocks(String text) {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = null;
        for (String rawLine : text.split("\n")) {
            String line = normalizeImportLine(rawLine);
            if (!StringUtils.hasText(line)) {
                if (current != null) {
                    current.add("");
                }
                continue;
            }
            Matcher matcher = QUESTION_START_PATTERN.matcher(line);
            if (matcher.matches()) {
                if (current != null && current.stream().anyMatch(StringUtils::hasText)) {
                    blocks.add(current);
                }
                current = new ArrayList<>();
                String firstLine = trimToNull(matcher.group(1));
                if (firstLine != null) {
                    current.add(firstLine);
                }
                continue;
            }
            if (current != null) {
                current.add(line);
            }
        }
        if (current != null && current.stream().anyMatch(StringUtils::hasText)) {
            blocks.add(current);
        }
        return blocks;
    }

    private ImportedQuestionDraft parseQuestionBlock(List<String> block) {
        List<String> titleLines = new ArrayList<>();
        List<ImportedChoiceDraft> choices = new ArrayList<>();
        StringBuilder answerBuilder = new StringBuilder();
        StringBuilder analysisBuilder = new StringBuilder();
        String explicitType = null;
        String difficulty = null;
        BigDecimal score = null;
        ImportSection section = ImportSection.TITLE;

        for (String line : expandQuestionLines(block)) {
            if (!StringUtils.hasText(line)) {
                continue;
            }
            String normalized = normalizeImportLine(line);
            if (!StringUtils.hasText(normalized)) {
                continue;
            }

            String metadataValue = extractMetadataValue(normalized, "答案", "参考答案", "标准答案", "答", "answer");
            if (metadataValue != null) {
                section = ImportSection.ANSWER;
                appendLine(answerBuilder, metadataValue);
                continue;
            }

            metadataValue = extractMetadataValue(normalized, "解析", "分析", "analysis", "explanation");
            if (metadataValue != null) {
                section = ImportSection.ANALYSIS;
                appendLine(analysisBuilder, metadataValue);
                continue;
            }

            metadataValue = extractMetadataValue(normalized, "难度", "difficulty");
            if (metadataValue != null) {
                difficulty = normalizeImportedDifficulty(metadataValue);
                section = ImportSection.TITLE;
                continue;
            }

            metadataValue = extractMetadataValue(normalized, "分值", "分数", "score");
            if (metadataValue != null) {
                score = parseScore(metadataValue);
                section = ImportSection.TITLE;
                continue;
            }

            metadataValue = extractMetadataValue(normalized, "类型", "题型", "type");
            if (metadataValue != null) {
                explicitType = normalizeImportedType(metadataValue);
                section = ImportSection.TITLE;
                continue;
            }

            Matcher choiceMatcher = CHOICE_LINE_PATTERN.matcher(normalized);
            if (choiceMatcher.matches()) {
                section = ImportSection.CHOICE;
                choices.add(new ImportedChoiceDraft(choiceMatcher.group(1).toUpperCase(Locale.ROOT), trimToNull(choiceMatcher.group(2))));
                continue;
            }

            if (section == ImportSection.ANSWER) {
                appendLine(answerBuilder, normalized);
                continue;
            }
            if (section == ImportSection.ANALYSIS) {
                appendLine(analysisBuilder, normalized);
                continue;
            }
            if (section == ImportSection.CHOICE && !choices.isEmpty()) {
                ImportedChoiceDraft lastChoice = choices.get(choices.size() - 1);
                choices.set(choices.size() - 1, new ImportedChoiceDraft(lastChoice.key(), mergeText(lastChoice.text(), normalized)));
                continue;
            }
            titleLines.add(normalized);
        }

        String title = trimToNull(String.join("\n", titleLines));
        if (!StringUtils.hasText(title)) {
            return null;
        }

        String normalizedDifficulty = difficulty == null ? "MEDIUM" : difficulty;
        return new ImportedQuestionDraft(
                title,
                explicitType,
                normalizedDifficulty,
                trimToNull(analysisBuilder.toString()),
                trimToNull(answerBuilder.toString()),
                choices.stream().filter(choice -> StringUtils.hasText(choice.text())).toList(),
                score);
    }

    private List<String> expandQuestionLines(List<String> block) {
        List<String> expanded = new ArrayList<>();
        for (String line : block) {
            if (!StringUtils.hasText(line)) {
                expanded.add("");
                continue;
            }
            for (String metadataSegment : splitMetadataSegments(line)) {
                expanded.addAll(splitInlineChoiceSegments(metadataSegment));
            }
        }
        return expanded;
    }

    private List<String> splitMetadataSegments(String line) {
        Matcher matcher = METADATA_PATTERN.matcher(line);
        List<Integer> positions = new ArrayList<>();
        while (matcher.find()) {
            positions.add(matcher.start());
        }
        if (positions.size() < 2) {
            return List.of(line);
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i);
            int end = i + 1 < positions.size() ? positions.get(i + 1) : line.length();
            String segment = trimToNull(line.substring(start, end));
            if (segment != null) {
                result.add(segment);
            }
        }
        return result;
    }

    private List<String> splitInlineChoiceSegments(String line) {
        Matcher matcher = INLINE_CHOICE_PATTERN.matcher(line);
        List<Integer> positions = new ArrayList<>();
        while (matcher.find()) {
            positions.add(matcher.start());
        }
        if (positions.size() < 2) {
            return List.of(line);
        }
        List<String> result = new ArrayList<>();
        String prefix = trimToNull(line.substring(0, positions.get(0)));
        if (prefix != null) {
            result.add(prefix);
        }
        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i);
            int end = i + 1 < positions.size() ? positions.get(i + 1) : line.length();
            String segment = trimToNull(line.substring(start, end));
            if (segment != null) {
                result.add(segment);
            }
        }
        return result;
    }

    private String extractMetadataValue(String line, String... keys) {
        String normalizedLine = normalizeImportLine(line);
        for (String key : keys) {
            String lower = key.toLowerCase(Locale.ROOT);
            String candidate = normalizedLine.toLowerCase(Locale.ROOT);
            if (candidate.startsWith(lower + ":") || candidate.startsWith(lower + "：")) {
                return trimToNull(normalizedLine.substring(key.length() + 1));
            }
        }
        return null;
    }

    private LinkedHashSet<String> normalizeChoiceAnswerKeys(String answerText) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (!StringUtils.hasText(answerText)) {
            return result;
        }
        Matcher matcher = Pattern.compile("[A-H]", Pattern.CASE_INSENSITIVE).matcher(answerText.toUpperCase(Locale.ROOT));
        while (matcher.find()) {
            result.add(matcher.group().toUpperCase(Locale.ROOT));
        }
        return result;
    }

    private String normalizeJudgeAnswer(String answerText) {
        if (!StringUtils.hasText(answerText)) {
            return null;
        }
        String normalized = answerText.replaceAll("[\\s,;，；。.!！?？]", "").trim();
        if (JUDGE_TRUE_VALUES.contains(normalized.toUpperCase(Locale.ROOT)) || JUDGE_TRUE_VALUES.contains(normalized)) {
            return "TRUE";
        }
        if (JUDGE_FALSE_VALUES.contains(normalized.toUpperCase(Locale.ROOT)) || JUDGE_FALSE_VALUES.contains(normalized)) {
            return "FALSE";
        }
        return null;
    }

    private BigDecimal parseScore(String rawScore) {
        if (!StringUtils.hasText(rawScore)) {
            return null;
        }
        Matcher matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(rawScore);
        if (!matcher.find()) {
            return null;
        }
        BigDecimal score = new BigDecimal(matcher.group(1));
        return score.compareTo(BigDecimal.ZERO) > 0 ? score : null;
    }

    private String normalizeImportedType(String rawType) {
        if (!StringUtils.hasText(rawType)) {
            return null;
        }
        String normalized = rawType.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("CHOICE") || normalized.contains("选择") || normalized.contains("单选") || normalized.contains("多选")) {
            return "CHOICE";
        }
        if (normalized.contains("JUDGE") || normalized.contains("判断") || normalized.contains("是非")) {
            return "JUDGE";
        }
        if (normalized.contains("TEXT") || normalized.contains("简答") || normalized.contains("主观")
                || normalized.contains("论述") || normalized.contains("填空") || normalized.contains("问答")) {
            return "TEXT";
        }
        return null;
    }

    private String normalizeImportedDifficulty(String rawDifficulty) {
        if (!StringUtils.hasText(rawDifficulty)) {
            return null;
        }
        String normalized = rawDifficulty.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("EASY") || normalized.contains("简单") || normalized.contains("基础") || normalized.contains("易")) {
            return "EASY";
        }
        if (normalized.contains("HARD") || normalized.contains("困难") || normalized.contains("提高") || normalized.contains("难")) {
            return "HARD";
        }
        if (normalized.contains("MEDIUM") || normalized.contains("一般") || normalized.contains("中")) {
            return "MEDIUM";
        }
        return null;
    }

    private String buildTextQuestionTitle(String title, List<ImportedChoiceDraft> choices) {
        if (choices.isEmpty()) {
            return title;
        }
        StringBuilder builder = new StringBuilder(title);
        for (ImportedChoiceDraft choice : choices) {
            builder.append('\n').append(choice.key()).append(". ").append(choice.text());
        }
        return builder.toString();
    }

    private List<ImportedQuestionDraft> deduplicateImportedTitles(List<ImportedQuestionDraft> drafts, int defaultScore) {
        Map<String, Integer> titleCounter = new LinkedHashMap<>();
        List<ImportedQuestionDraft> result = new ArrayList<>();
        for (ImportedQuestionDraft draft : drafts) {
            String originalTitle = trimToNull(draft.title());
            if (!StringUtils.hasText(originalTitle)) {
                continue;
            }
            int count = titleCounter.getOrDefault(originalTitle, 0) + 1;
            titleCounter.put(originalTitle, count);
            String uniqueTitle = count == 1 ? originalTitle : originalTitle + " (" + count + ")";
            BigDecimal score = draft.paperScore() == null ? BigDecimal.valueOf(defaultScore) : draft.paperScore();
            result.add(new ImportedQuestionDraft(
                    uniqueTitle,
                    draft.explicitType(),
                    draft.difficulty(),
                    draft.analysis(),
                    draft.answerText(),
                    draft.choices(),
                    score));
        }
        return result;
    }

    private boolean isSectionHeader(String title) {
        String normalized = title.replaceAll("\\s+", "");
        if (normalized.length() > 24) {
            return false;
        }
        return normalized.contains("选择题")
                || normalized.contains("单选题")
                || normalized.contains("多选题")
                || normalized.contains("判断题")
                || normalized.contains("简答题")
                || normalized.contains("问答题")
                || normalized.contains("填空题")
                || normalized.contains("综合题")
                || normalized.contains("材料题");
    }

    private String normalizePaperStatus(String status) {
        String normalized = normalizePaperStatusAllowBlank(status);
        if (!StringUtils.hasText(normalized) || !PAPER_STATUSES.contains(normalized)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Unsupported paper status");
        }
        return normalized;
    }

    private String normalizePaperStatusAllowBlank(String status) {
        return StringUtils.hasText(status) ? status.trim().toUpperCase(Locale.ROOT) : null;
    }

    private boolean isInProgress(String status) {
        return StringUtils.hasText(status) && IN_PROGRESS_STATUSES.contains(status.trim().toUpperCase(Locale.ROOT));
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

    private String normalizeImportLine(String line) {
        if (line == null) {
            return "";
        }
        String normalized = line.trim();
        normalized = normalized.replaceFirst("^[-*]\\s+", "");
        return normalized;
    }

    private void appendLine(StringBuilder builder, String line) {
        String value = trimToNull(line);
        if (value == null) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append('\n');
        }
        builder.append(value);
    }

    private String mergeText(String left, String right) {
        String normalizedLeft = trimToNull(left);
        String normalizedRight = trimToNull(right);
        if (normalizedLeft == null) {
            return normalizedRight;
        }
        if (normalizedRight == null) {
            return normalizedLeft;
        }
        return normalizedLeft + " " + normalizedRight;
    }

    private String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private enum ImportSection {
        TITLE,
        CHOICE,
        ANSWER,
        ANALYSIS
    }

    private record ImportedChoiceDraft(String key, String text) {
    }

    private record ImportedQuestionDraft(String title,
                                         String explicitType,
                                         String difficulty,
                                         String analysis,
                                         String answerText,
                                         List<ImportedChoiceDraft> choices,
                                         BigDecimal paperScore) {

        private int defaultScoreOr(int defaultScore) {
            if (paperScore == null || paperScore.compareTo(BigDecimal.ZERO) <= 0) {
                return defaultScore;
            }
            return paperScore.setScale(0, RoundingMode.HALF_UP).intValue();
        }

        private BigDecimal getPaperScoreOrDefault(int defaultScore) {
            if (paperScore == null || paperScore.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.valueOf(defaultScore);
            }
            return paperScore;
        }
    }
}
