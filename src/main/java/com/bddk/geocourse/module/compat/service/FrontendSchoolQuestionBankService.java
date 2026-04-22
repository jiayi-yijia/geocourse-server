package com.bddk.geocourse.module.compat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.assignment.entity.TeacherPaperQuestion;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperQuestionMapper;
import com.bddk.geocourse.module.questionbank.entity.Question;
import com.bddk.geocourse.module.questionbank.entity.QuestionAnswer;
import com.bddk.geocourse.module.questionbank.entity.QuestionCategory;
import com.bddk.geocourse.module.questionbank.entity.QuestionChoice;
import com.bddk.geocourse.module.questionbank.mapper.QuestionAnswerMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionCategoryMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionChoiceMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionMapper;
import com.bddk.geocourse.module.questionbank.model.QuestionBankAttachmentImportRequest;
import com.bddk.geocourse.module.questionbank.model.QuestionBankAttachmentImportResult;
import com.bddk.geocourse.module.questionbank.model.QuestionCategorySaveRequest;
import com.bddk.geocourse.module.questionbank.model.QuestionCategoryView;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionAnswerRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionChoiceRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionQuery;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionSaveRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionView;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FrontendSchoolQuestionBankService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_IMPORTED_SCORE = 5;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SUPPORTED_TYPES = Set.of("CHOICE", "JUDGE", "TEXT");
    private static final Set<String> SUPPORTED_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");
    private static final Set<String> SUPPORTED_IMPORT_EXTENSIONS = Set.of("docx", "md", "txt", "pdf");
    private static final Set<String> JUDGE_TRUE_VALUES = Set.of("TRUE", "T", "YES", "Y", "对", "正确", "是");
    private static final Set<String> JUDGE_FALSE_VALUES = Set.of("FALSE", "F", "NO", "N", "错", "错误", "否");
    private static final DateTimeFormatter IMPORT_CATEGORY_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern QUESTION_START_PATTERN = Pattern.compile(
            "^(?:#{1,6}\\s*)?(?:(?:第\\s*[一二三四五六七八九十百零0-9]+\\s*题?|(?:[Qq](?:uestion)?\\s*\\d+)|(?:\\d+)|(?:[一二三四五六七八九十]+))[\\.、．\\):：\\s-]*)(.*)$");
    private static final Pattern CHOICE_LINE_PATTERN = Pattern.compile("^(?:[-*]\\s*)?([A-H])[\\.、．\\)\\]:：\\s]+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INLINE_CHOICE_PATTERN = Pattern.compile("([A-H])[\\.、．\\)]\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern METADATA_PATTERN = Pattern.compile(
            "(?i)(答案|参考答案|标准答案|答|解析|分析|难度|分值|分数|类型|题型|answer|analysis|explanation|difficulty|score|type)\\s*[:：]");
    private static final Pattern CHOICE_ANSWER_PATTERN = Pattern.compile("[A-H]", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    private final FrontendAuthService frontendAuthService;
    private final QuestionCategoryMapper questionCategoryMapper;
    private final QuestionMapper questionMapper;
    private final QuestionChoiceMapper questionChoiceMapper;
    private final QuestionAnswerMapper questionAnswerMapper;
    private final TeacherPaperQuestionMapper teacherPaperQuestionMapper;

    public FrontendSchoolQuestionBankService(FrontendAuthService frontendAuthService,
                                             QuestionCategoryMapper questionCategoryMapper,
                                             QuestionMapper questionMapper,
                                             QuestionChoiceMapper questionChoiceMapper,
                                             QuestionAnswerMapper questionAnswerMapper,
                                             TeacherPaperQuestionMapper teacherPaperQuestionMapper) {
        this.frontendAuthService = frontendAuthService;
        this.questionCategoryMapper = questionCategoryMapper;
        this.questionMapper = questionMapper;
        this.questionChoiceMapper = questionChoiceMapper;
        this.questionAnswerMapper = questionAnswerMapper;
        this.teacherPaperQuestionMapper = teacherPaperQuestionMapper;
    }

    public List<QuestionCategoryView> listCategories() {
        return buildCategoryViews(currentTenantId());
    }

    public List<QuestionCategoryView> treeCategories() {
        List<QuestionCategoryView> views = buildCategoryViews(currentTenantId());
        Map<Long, List<QuestionCategoryView>> childrenMap = views.stream()
                .collect(Collectors.groupingBy(view -> defaultParentId(view.getParentId()), LinkedHashMap::new, Collectors.toList()));
        views.forEach(view -> view.setChildren(childrenMap.getOrDefault(view.getId(), List.of()).stream()
                .sorted(Comparator.comparing(QuestionCategoryView::getSortNo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(QuestionCategoryView::getId))
                .toList()));
        return new ArrayList<>(childrenMap.getOrDefault(0L, List.of()));
    }

    @Transactional
    public QuestionCategoryView createCategory(QuestionCategorySaveRequest request) {
        Long tenantId = currentTenantId();
        Long operatorId = currentOperatorId();
        ensureCategoryNameUnique(tenantId, request.getParentId(), request.getName(), null);
        validateParentCategory(tenantId, request.getParentId());

        QuestionCategory category = new QuestionCategory();
        category.setTenantId(tenantId);
        category.setParentId(defaultParentId(request.getParentId()));
        category.setName(trimToNull(request.getName()));
        category.setDescription(trimToNull(request.getDescription()));
        category.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        category.setStatus(STATUS_ENABLED);
        category.setCreateBy(operatorId);
        category.setUpdateBy(operatorId);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        questionCategoryMapper.insert(category);
        return toCategoryView(category, 0L);
    }

    @Transactional
    public QuestionCategoryView updateCategory(Long categoryId, QuestionCategorySaveRequest request) {
        Long tenantId = currentTenantId();
        Long operatorId = currentOperatorId();
        QuestionCategory category = getCategoryOrThrow(tenantId, categoryId);
        validateParentCategory(tenantId, request.getParentId());
        if (Objects.equals(categoryId, defaultParentId(request.getParentId()))) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Category parent cannot point to itself");
        }
        ensureCategoryNameUnique(tenantId, request.getParentId(), request.getName(), categoryId);

        category.setParentId(defaultParentId(request.getParentId()));
        category.setName(trimToNull(request.getName()));
        category.setDescription(trimToNull(request.getDescription()));
        category.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        category.setUpdateBy(operatorId);
        category.setUpdateTime(LocalDateTime.now());
        questionCategoryMapper.updateById(category);
        long questionCount = questionMapper.selectCount(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .eq(Question::getCategoryId, categoryId));
        return toCategoryView(category, questionCount);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Long tenantId = currentTenantId();
        getCategoryOrThrow(tenantId, categoryId);

        long childCount = questionCategoryMapper.selectCount(Wrappers.<QuestionCategory>lambdaQuery()
                .eq(QuestionCategory::getTenantId, tenantId)
                .eq(QuestionCategory::getParentId, categoryId));
        if (childCount > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "The category still has child categories");
        }
        long questionCount = questionMapper.selectCount(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .eq(Question::getCategoryId, categoryId));
        if (questionCount > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "The category still contains questions");
        }
        questionCategoryMapper.deleteById(categoryId);
    }

    public PageResult<TeacherQuestionView> pageQuestions(TeacherQuestionQuery query) {
        Long tenantId = currentTenantId();
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        Page<Question> page = questionMapper.selectPage(new Page<>(pageNo, pageSize), buildQuestionQuery(tenantId, query));
        return PageResult.of(fillQuestionViews(tenantId, page.getRecords()), page.getTotal(), pageNo, pageSize);
    }

    public TeacherQuestionView getQuestion(Long questionId) {
        Long tenantId = currentTenantId();
        Question question = getQuestionOrThrow(tenantId, questionId);
        return fillQuestionViews(tenantId, List.of(question)).stream().findFirst()
                .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND, "Question not found"));
    }

    @Transactional
    public TeacherQuestionView createQuestion(TeacherQuestionSaveRequest request) {
        Long tenantId = currentTenantId();
        Long operatorId = currentOperatorId();
        validateQuestionRequest(tenantId, request, null);

        Question question = new Question();
        question.setTenantId(tenantId);
        question.setCategoryId(request.getCategoryId());
        question.setTitle(trimToNull(request.getTitle()));
        question.setType(normalizeType(request.getType()));
        question.setMultiSelect(Boolean.TRUE.equals(request.getMultiSelect()));
        question.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        question.setDefaultScore(request.getDefaultScore());
        question.setAnalysis(trimToNull(request.getAnalysis()));
        question.setStatus(STATUS_ENABLED);
        question.setCreateBy(operatorId);
        question.setUpdateBy(operatorId);
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        questionMapper.insert(question);

        saveQuestionRelations(question.getId(), tenantId, operatorId, request);
        return getQuestion(question.getId());
    }

    @Transactional
    public TeacherQuestionView updateQuestion(Long questionId, TeacherQuestionSaveRequest request) {
        Long tenantId = currentTenantId();
        Long operatorId = currentOperatorId();
        Question question = getQuestionOrThrow(tenantId, questionId);
        validateQuestionRequest(tenantId, request, questionId);

        question.setCategoryId(request.getCategoryId());
        question.setTitle(trimToNull(request.getTitle()));
        question.setType(normalizeType(request.getType()));
        question.setMultiSelect(Boolean.TRUE.equals(request.getMultiSelect()));
        question.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        question.setDefaultScore(request.getDefaultScore());
        question.setAnalysis(trimToNull(request.getAnalysis()));
        question.setUpdateBy(operatorId);
        question.setUpdateTime(LocalDateTime.now());
        questionMapper.updateById(question);

        questionChoiceMapper.delete(Wrappers.<QuestionChoice>lambdaQuery()
                .eq(QuestionChoice::getTenantId, tenantId)
                .eq(QuestionChoice::getQuestionId, questionId));
        questionAnswerMapper.delete(Wrappers.<QuestionAnswer>lambdaQuery()
                .eq(QuestionAnswer::getTenantId, tenantId)
                .eq(QuestionAnswer::getQuestionId, questionId));
        saveQuestionRelations(questionId, tenantId, operatorId, request);
        return getQuestion(questionId);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Long tenantId = currentTenantId();
        getQuestionOrThrow(tenantId, questionId);

        long paperRefCount = teacherPaperQuestionMapper.selectCount(Wrappers.<TeacherPaperQuestion>lambdaQuery()
                .eq(TeacherPaperQuestion::getTenantId, tenantId)
                .eq(TeacherPaperQuestion::getQuestionId, questionId));
        if (paperRefCount > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "The question is already referenced by papers");
        }

        questionChoiceMapper.delete(Wrappers.<QuestionChoice>lambdaQuery()
                .eq(QuestionChoice::getTenantId, tenantId)
                .eq(QuestionChoice::getQuestionId, questionId));
        questionAnswerMapper.delete(Wrappers.<QuestionAnswer>lambdaQuery()
                .eq(QuestionAnswer::getTenantId, tenantId)
                .eq(QuestionAnswer::getQuestionId, questionId));
        questionMapper.deleteById(questionId);
    }

    @Transactional
    public QuestionBankAttachmentImportResult importQuestions(QuestionBankAttachmentImportRequest request) {
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Please upload a file first");
        }

        Long tenantId = currentTenantId();
        Long operatorId = currentOperatorId();
        String extension = normalizeImportExtension(file);
        String text = normalizeImportText(readImportText(file, extension));
        if (!StringUtils.hasText(text)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "No readable text was found in the attachment");
        }

        int defaultScore = normalizeImportedDefaultScore(request.getDefaultScore());
        Long categoryId = resolveImportCategoryId(tenantId, operatorId, request, file);
        QuestionCategory category = getCategoryOrThrow(tenantId, categoryId);
        List<ImportedQuestionDraft> drafts = deduplicateImportedTitles(parseImportedQuestions(text), defaultScore);
        if (drafts.isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST,
                    "No questions were recognized. Please format the file with question numbers, options, and answers");
        }

        List<Long> questionIds = new ArrayList<>();
        for (ImportedQuestionDraft draft : drafts) {
            TeacherQuestionSaveRequest saveRequest = toImportedQuestionRequest(
                    draft,
                    categoryId,
                    resolveAvailableQuestionTitle(tenantId, categoryId, draft.title()),
                    defaultScore);
            TeacherQuestionView created = createQuestion(saveRequest);
            questionIds.add(created.getId());
        }

        QuestionBankAttachmentImportResult result = new QuestionBankAttachmentImportResult();
        result.setFileName(file.getOriginalFilename());
        result.setCategoryId(category.getId());
        result.setCategoryName(category.getName());
        result.setImportedCount(questionIds.size());
        result.setQuestionIds(questionIds);
        return result;
    }

    private Long currentTenantId() {
        frontendAuthService.requireCurrentSchoolAdmin();
        return frontendAuthService.currentTenantId();
    }

    private Long currentOperatorId() {
        return frontendAuthService.requireCurrentSchoolAdmin().id();
    }

    private List<QuestionCategoryView> buildCategoryViews(Long tenantId) {
        List<QuestionCategory> categories = questionCategoryMapper.selectList(Wrappers.<QuestionCategory>lambdaQuery()
                .eq(QuestionCategory::getTenantId, tenantId)
                .orderByAsc(QuestionCategory::getSortNo)
                .orderByAsc(QuestionCategory::getId));
        Map<Long, Long> questionCountMap = questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                        .eq(Question::getTenantId, tenantId))
                .stream()
                .collect(Collectors.groupingBy(Question::getCategoryId, Collectors.counting()));
        return categories.stream()
                .map(category -> toCategoryView(category, questionCountMap.getOrDefault(category.getId(), 0L)))
                .toList();
    }

    private QuestionCategoryView toCategoryView(QuestionCategory category, long questionCount) {
        QuestionCategoryView view = new QuestionCategoryView();
        view.setId(category.getId());
        view.setParentId(category.getParentId());
        view.setName(category.getName());
        view.setDescription(category.getDescription());
        view.setSortNo(category.getSortNo());
        view.setQuestionCount(questionCount);
        view.setCreatedAt(category.getCreateTime());
        view.setChildren(List.of());
        return view;
    }

    private void ensureCategoryNameUnique(Long tenantId, Long parentId, String name, Long excludeId) {
        LambdaQueryWrapper<QuestionCategory> query = Wrappers.<QuestionCategory>lambdaQuery()
                .eq(QuestionCategory::getTenantId, tenantId)
                .eq(QuestionCategory::getParentId, defaultParentId(parentId))
                .eq(QuestionCategory::getName, trimToNull(name));
        if (excludeId != null) {
            query.ne(QuestionCategory::getId, excludeId);
        }
        if (questionCategoryMapper.selectCount(query) > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "A category with the same name already exists under the parent");
        }
    }

    private void validateParentCategory(Long tenantId, Long parentId) {
        Long normalizedParentId = defaultParentId(parentId);
        if (normalizedParentId != 0L) {
            getCategoryOrThrow(tenantId, normalizedParentId);
        }
    }

    private QuestionCategory getCategoryOrThrow(Long tenantId, Long categoryId) {
        QuestionCategory category = questionCategoryMapper.selectOne(Wrappers.<QuestionCategory>lambdaQuery()
                .eq(QuestionCategory::getTenantId, tenantId)
                .eq(QuestionCategory::getId, categoryId)
                .last("limit 1"));
        if (category == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "Category not found");
        }
        return category;
    }

    private Question getQuestionOrThrow(Long tenantId, Long questionId) {
        Question question = questionMapper.selectOne(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .eq(Question::getId, questionId)
                .last("limit 1"));
        if (question == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "Question not found");
        }
        return question;
    }

    private LambdaQueryWrapper<Question> buildQuestionQuery(Long tenantId, TeacherQuestionQuery query) {
        String keyword = trimToNull(query.getKeyword());
        String type = normalizeTypeAllowBlank(query.getType());
        String difficulty = normalizeDifficultyAllowBlank(query.getDifficulty());
        return Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .like(keyword != null, Question::getTitle, keyword)
                .eq(type != null, Question::getType, type)
                .eq(difficulty != null, Question::getDifficulty, difficulty)
                .eq(query.getCategoryId() != null, Question::getCategoryId, query.getCategoryId())
                .orderByDesc(Question::getUpdateTime)
                .orderByDesc(Question::getId);
    }

    private List<TeacherQuestionView> fillQuestionViews(Long tenantId, List<Question> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return List.of();
        }
        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        Map<Long, QuestionCategory> categoryMap = questionCategoryMapper.selectList(Wrappers.<QuestionCategory>lambdaQuery()
                        .eq(QuestionCategory::getTenantId, tenantId))
                .stream()
                .collect(Collectors.toMap(QuestionCategory::getId, Function.identity(), (left, right) -> left));
        Map<Long, List<QuestionChoice>> choiceMap = questionChoiceMapper.selectList(Wrappers.<QuestionChoice>lambdaQuery()
                        .eq(QuestionChoice::getTenantId, tenantId)
                        .in(QuestionChoice::getQuestionId, questionIds)
                        .orderByAsc(QuestionChoice::getSortNo)
                        .orderByAsc(QuestionChoice::getId))
                .stream()
                .collect(Collectors.groupingBy(QuestionChoice::getQuestionId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, QuestionAnswer> answerMap = questionAnswerMapper.selectList(Wrappers.<QuestionAnswer>lambdaQuery()
                        .eq(QuestionAnswer::getTenantId, tenantId)
                        .in(QuestionAnswer::getQuestionId, questionIds))
                .stream()
                .collect(Collectors.toMap(QuestionAnswer::getQuestionId, Function.identity(), (left, right) -> left));
        return questions.stream().map(question -> toQuestionView(question,
                categoryMap.get(question.getCategoryId()),
                choiceMap.getOrDefault(question.getId(), List.of()),
                answerMap.get(question.getId()))).toList();
    }

    private TeacherQuestionView toQuestionView(Question question,
                                               QuestionCategory category,
                                               List<QuestionChoice> choices,
                                               QuestionAnswer answer) {
        TeacherQuestionView view = new TeacherQuestionView();
        view.setId(question.getId());
        view.setCategoryId(question.getCategoryId());
        view.setCategoryName(category == null ? null : category.getName());
        view.setTitle(question.getTitle());
        view.setType(question.getType());
        view.setMultiSelect(Boolean.TRUE.equals(question.getMultiSelect()));
        view.setDifficulty(question.getDifficulty());
        view.setDefaultScore(question.getDefaultScore());
        view.setAnalysis(question.getAnalysis());
        view.setChoices(choices.stream().map(item -> {
            TeacherQuestionChoiceRequest choice = new TeacherQuestionChoiceRequest();
            choice.setKey(item.getChoiceKey());
            choice.setText(item.getChoiceText());
            choice.setCorrect(Boolean.TRUE.equals(item.getCorrect()));
            return choice;
        }).toList());
        if (answer != null) {
            TeacherQuestionAnswerRequest answerView = new TeacherQuestionAnswerRequest();
            answerView.setAnswerText(answer.getAnswerText());
            answerView.setGradingRule(answer.getGradingRule());
            view.setAnswer(answerView);
        }
        view.setCreatedAt(question.getCreateTime());
        view.setUpdatedAt(question.getUpdateTime());
        return view;
    }

    private void validateQuestionRequest(Long tenantId, TeacherQuestionSaveRequest request, Long excludeId) {
        getCategoryOrThrow(tenantId, request.getCategoryId());
        String type = normalizeType(request.getType());
        normalizeDifficulty(request.getDifficulty());
        LambdaQueryWrapper<Question> duplicateQuery = Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .eq(Question::getCategoryId, request.getCategoryId())
                .eq(Question::getTitle, trimToNull(request.getTitle()));
        if (excludeId != null) {
            duplicateQuery.ne(Question::getId, excludeId);
        }
        if (questionMapper.selectCount(duplicateQuery) > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "A question with the same title already exists in the category");
        }

        if ("CHOICE".equals(type)) {
            if (CollectionUtils.isEmpty(request.getChoices()) || request.getChoices().size() < 2) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Choice questions require at least two options");
            }
            long correctCount = request.getChoices().stream()
                    .filter(choice -> Boolean.TRUE.equals(choice.getCorrect()))
                    .count();
            if (correctCount == 0) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Choice questions require at least one correct option");
            }
            if (!Boolean.TRUE.equals(request.getMultiSelect()) && correctCount > 1) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Single choice questions can only have one correct option");
            }
            return;
        }

        if (request.getAnswer() == null || !StringUtils.hasText(request.getAnswer().getAnswerText())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "The answer field is required for the current question type");
        }
    }

    private void saveQuestionRelations(Long questionId,
                                       Long tenantId,
                                       Long operatorId,
                                       TeacherQuestionSaveRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if ("CHOICE".equals(normalizeType(request.getType()))) {
            List<String> answerKeys = new ArrayList<>();
            for (int i = 0; i < request.getChoices().size(); i++) {
                TeacherQuestionChoiceRequest item = request.getChoices().get(i);
                String choiceKey = StringUtils.hasText(item.getKey())
                        ? item.getKey().trim().toUpperCase(Locale.ROOT)
                        : String.valueOf((char) ('A' + i));
                QuestionChoice choice = new QuestionChoice();
                choice.setTenantId(tenantId);
                choice.setQuestionId(questionId);
                choice.setChoiceKey(choiceKey);
                choice.setChoiceText(trimToNull(item.getText()));
                choice.setCorrect(Boolean.TRUE.equals(item.getCorrect()));
                choice.setSortNo(i);
                choice.setStatus(STATUS_ENABLED);
                choice.setCreateBy(operatorId);
                choice.setUpdateBy(operatorId);
                choice.setCreateTime(now);
                choice.setUpdateTime(now);
                questionChoiceMapper.insert(choice);
                if (Boolean.TRUE.equals(item.getCorrect())) {
                    answerKeys.add(choiceKey);
                }
            }
            QuestionAnswer answer = new QuestionAnswer();
            answer.setTenantId(tenantId);
            answer.setQuestionId(questionId);
            answer.setAnswerText(String.join(",", answerKeys));
            answer.setGradingRule(request.getAnswer() == null ? null : trimToNull(request.getAnswer().getGradingRule()));
            answer.setStatus(STATUS_ENABLED);
            answer.setCreateBy(operatorId);
            answer.setUpdateBy(operatorId);
            answer.setCreateTime(now);
            answer.setUpdateTime(now);
            questionAnswerMapper.insert(answer);
            return;
        }

        QuestionAnswer answer = new QuestionAnswer();
        answer.setTenantId(tenantId);
        answer.setQuestionId(questionId);
        answer.setAnswerText(trimToNull(request.getAnswer().getAnswerText()));
        answer.setGradingRule(trimToNull(request.getAnswer().getGradingRule()));
        answer.setStatus(STATUS_ENABLED);
        answer.setCreateBy(operatorId);
        answer.setUpdateBy(operatorId);
        answer.setCreateTime(now);
        answer.setUpdateTime(now);
        questionAnswerMapper.insert(answer);
    }

    private Long resolveImportCategoryId(Long tenantId,
                                         Long operatorId,
                                         QuestionBankAttachmentImportRequest request,
                                         MultipartFile file) {
        if (request.getCategoryId() != null) {
            return getCategoryOrThrow(tenantId, request.getCategoryId()).getId();
        }

        String preferredCategoryName = trimToNull(request.getCategoryName());
        if (preferredCategoryName != null) {
            QuestionCategory existing = questionCategoryMapper.selectOne(Wrappers.<QuestionCategory>lambdaQuery()
                    .eq(QuestionCategory::getTenantId, tenantId)
                    .eq(QuestionCategory::getParentId, 0L)
                    .eq(QuestionCategory::getName, preferredCategoryName)
                    .last("limit 1"));
            if (existing != null) {
                return existing.getId();
            }

            LocalDateTime now = LocalDateTime.now();
            QuestionCategory created = new QuestionCategory();
            created.setTenantId(tenantId);
            created.setParentId(0L);
            created.setName(preferredCategoryName);
            created.setDescription("Created from attachment import");
            created.setSortNo(0);
            created.setStatus(STATUS_ENABLED);
            created.setCreateBy(operatorId);
            created.setUpdateBy(operatorId);
            created.setCreateTime(now);
            created.setUpdateTime(now);
            questionCategoryMapper.insert(created);
            return created.getId();
        }

        QuestionCategory importRoot = getOrCreateImportRootCategory(tenantId, operatorId);
        String baseName = resolveImportedCategoryName(file);
        String categoryName = baseName + " - " + IMPORT_CATEGORY_SUFFIX_FORMATTER.format(LocalDateTime.now());

        QuestionCategory category = new QuestionCategory();
        category.setTenantId(tenantId);
        category.setParentId(importRoot.getId());
        category.setName(categoryName);
        category.setDescription("Auto-created from attachment import");
        category.setSortNo(0);
        category.setStatus(STATUS_ENABLED);
        category.setCreateBy(operatorId);
        category.setUpdateBy(operatorId);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        questionCategoryMapper.insert(category);
        return category.getId();
    }

    private QuestionCategory getOrCreateImportRootCategory(Long tenantId, Long operatorId) {
        QuestionCategory existing = questionCategoryMapper.selectOne(Wrappers.<QuestionCategory>lambdaQuery()
                .eq(QuestionCategory::getTenantId, tenantId)
                .eq(QuestionCategory::getParentId, 0L)
                .eq(QuestionCategory::getName, "题库导入")
                .last("limit 1"));
        if (existing != null) {
            return existing;
        }

        LocalDateTime now = LocalDateTime.now();
        QuestionCategory created = new QuestionCategory();
        created.setTenantId(tenantId);
        created.setParentId(0L);
        created.setName("题库导入");
        created.setDescription("Auto-created root category for imported question banks");
        created.setSortNo(0);
        created.setStatus(STATUS_ENABLED);
        created.setCreateBy(operatorId);
        created.setUpdateBy(operatorId);
        created.setCreateTime(now);
        created.setUpdateTime(now);
        questionCategoryMapper.insert(created);
        return created;
    }

    private String resolveImportedCategoryName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return "Imported Question Bank";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            return trimToNull(originalFilename.substring(0, dotIndex));
        }
        return trimToNull(originalFilename);
    }

    private String resolveAvailableQuestionTitle(Long tenantId, Long categoryId, String originalTitle) {
        String baseTitle = trimToNull(originalTitle);
        if (!StringUtils.hasText(baseTitle)) {
            return "Imported Question";
        }
        String candidate = baseTitle;
        int suffix = 2;
        while (true) {
            String checkingTitle = candidate;
            long count = questionMapper.selectCount(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .eq(Question::getCategoryId, categoryId)
                .eq(Question::getTitle, checkingTitle));
            if (count <= 0) {
                return candidate;
            }
            candidate = baseTitle + " (" + suffix + ")";
            suffix++;
        }
    }

    private TeacherQuestionSaveRequest toImportedQuestionRequest(ImportedQuestionDraft draft,
                                                                 Long categoryId,
                                                                 String resolvedTitle,
                                                                 int defaultScore) {
        TeacherQuestionSaveRequest request = new TeacherQuestionSaveRequest();
        request.setCategoryId(categoryId);
        request.setTitle(resolvedTitle);
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
        request.setTitle(buildTextQuestionTitle(resolvedTitle, choices));
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
        Matcher matcher = CHOICE_ANSWER_PATTERN.matcher(answerText.toUpperCase(Locale.ROOT));
        while (matcher.find()) {
            result.add(matcher.group().toUpperCase(Locale.ROOT));
        }
        return result;
    }

    private String normalizeJudgeAnswer(String answerText) {
        if (!StringUtils.hasText(answerText)) {
            return null;
        }
        String normalized = answerText.replaceAll("[\\s,;，；。?!？！]", "").trim();
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
        Matcher matcher = SCORE_PATTERN.matcher(rawScore);
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
        if (normalized.contains("EASY") || normalized.contains("简单") || normalized.contains("基础")) {
            return "EASY";
        }
        if (normalized.contains("HARD") || normalized.contains("困难") || normalized.contains("提高")) {
            return "HARD";
        }
        if (normalized.contains("MEDIUM") || normalized.contains("一般") || normalized.contains("中等")) {
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

    private String normalizeType(String type) {
        String normalized = normalizeTypeAllowBlank(type);
        if (!StringUtils.hasText(normalized) || !SUPPORTED_TYPES.contains(normalized)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Unsupported question type");
        }
        return normalized;
    }

    private String normalizeTypeAllowBlank(String type) {
        return StringUtils.hasText(type) ? type.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String normalizeDifficulty(String difficulty) {
        String normalized = normalizeDifficultyAllowBlank(difficulty);
        if (!StringUtils.hasText(normalized) || !SUPPORTED_DIFFICULTIES.contains(normalized)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Unsupported difficulty");
        }
        return normalized;
    }

    private String normalizeDifficultyAllowBlank(String difficulty) {
        return StringUtils.hasText(difficulty) ? difficulty.trim().toUpperCase(Locale.ROOT) : null;
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

    private Long defaultParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private String normalizeImportLine(String line) {
        if (line == null) {
            return "";
        }
        return line.trim().replaceFirst("^[-*]\\s+", "");
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
    }
}
