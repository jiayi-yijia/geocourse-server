package com.bddk.geocourse.module.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.assignment.entity.TeacherPaperQuestion;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperQuestionMapper;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import com.bddk.geocourse.module.questionbank.entity.Question;
import com.bddk.geocourse.module.questionbank.entity.QuestionAnswer;
import com.bddk.geocourse.module.questionbank.entity.QuestionCategory;
import com.bddk.geocourse.module.questionbank.entity.QuestionChoice;
import com.bddk.geocourse.module.questionbank.mapper.QuestionAnswerMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionCategoryMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionChoiceMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionMapper;
import com.bddk.geocourse.module.questionbank.model.QuestionCategorySaveRequest;
import com.bddk.geocourse.module.questionbank.model.QuestionCategoryView;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionAnswerRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionChoiceRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionQuery;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionSaveRequest;
import com.bddk.geocourse.module.questionbank.model.TeacherQuestionView;
import com.bddk.geocourse.module.questionbank.service.TeacherQuestionBankService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TeacherQuestionBankServiceImpl implements TeacherQuestionBankService {

    private static final int STATUS_ENABLED = 1;
    private static final Set<String> SUPPORTED_TYPES = Set.of("CHOICE", "JUDGE", "TEXT");
    private static final Set<String> SUPPORTED_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");

    private final TeacherPortalContextService teacherPortalContextService;
    private final QuestionCategoryMapper questionCategoryMapper;
    private final QuestionMapper questionMapper;
    private final QuestionChoiceMapper questionChoiceMapper;
    private final QuestionAnswerMapper questionAnswerMapper;
    private final TeacherPaperQuestionMapper teacherPaperQuestionMapper;

    public TeacherQuestionBankServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                          QuestionCategoryMapper questionCategoryMapper,
                                          QuestionMapper questionMapper,
                                          QuestionChoiceMapper questionChoiceMapper,
                                          QuestionAnswerMapper questionAnswerMapper,
                                          TeacherPaperQuestionMapper teacherPaperQuestionMapper) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.questionCategoryMapper = questionCategoryMapper;
        this.questionMapper = questionMapper;
        this.questionChoiceMapper = questionChoiceMapper;
        this.questionAnswerMapper = questionAnswerMapper;
        this.teacherPaperQuestionMapper = teacherPaperQuestionMapper;
    }

    @Override
    public List<QuestionCategoryView> listCategories() {
        return buildCategoryViews(teacherPortalContextService.currentTenantId());
    }

    @Override
    public List<QuestionCategoryView> treeCategories() {
        List<QuestionCategoryView> views = buildCategoryViews(teacherPortalContextService.currentTenantId());
        Map<Long, List<QuestionCategoryView>> childrenMap = views.stream()
                .collect(Collectors.groupingBy(view -> defaultParentId(view.getParentId()), LinkedHashMap::new, Collectors.toList()));
        views.forEach(view -> view.setChildren(childrenMap.getOrDefault(view.getId(), List.of()).stream()
                .sorted(Comparator.comparing(QuestionCategoryView::getSortNo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(QuestionCategoryView::getId))
                .toList()));
        return new ArrayList<>(childrenMap.getOrDefault(0L, List.of()));
    }

    @Override
    @Transactional
    public QuestionCategoryView createCategory(QuestionCategorySaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        ensureCategoryNameUnique(tenantId, request.getParentId(), request.getName(), null);
        validateParentCategory(tenantId, request.getParentId());

        QuestionCategory category = new QuestionCategory();
        category.setTenantId(tenantId);
        category.setParentId(defaultParentId(request.getParentId()));
        category.setName(trimToNull(request.getName()));
        category.setDescription(trimToNull(request.getDescription()));
        category.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        category.setStatus(STATUS_ENABLED);
        category.setCreateBy(teacherId);
        category.setUpdateBy(teacherId);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        questionCategoryMapper.insert(category);
        return toCategoryView(category, 0L);
    }

    @Override
    @Transactional
    public QuestionCategoryView updateCategory(Long categoryId, QuestionCategorySaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        QuestionCategory category = getCategoryOrThrow(tenantId, categoryId);
        validateParentCategory(tenantId, request.getParentId());
        if (Objects.equals(categoryId, defaultParentId(request.getParentId()))) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "分类父级不能指向自身");
        }
        ensureCategoryNameUnique(tenantId, request.getParentId(), request.getName(), categoryId);

        category.setParentId(defaultParentId(request.getParentId()));
        category.setName(trimToNull(request.getName()));
        category.setDescription(trimToNull(request.getDescription()));
        category.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        category.setUpdateBy(teacherId);
        category.setUpdateTime(LocalDateTime.now());
        questionCategoryMapper.updateById(category);
        long questionCount = questionMapper.selectCount(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .eq(Question::getCategoryId, categoryId));
        return toCategoryView(category, questionCount);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        getCategoryOrThrow(tenantId, categoryId);

        long childCount = questionCategoryMapper.selectCount(Wrappers.<QuestionCategory>lambdaQuery()
                .eq(QuestionCategory::getTenantId, tenantId)
                .eq(QuestionCategory::getParentId, categoryId));
        if (childCount > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "当前分类下仍有子分类，无法删除");
        }
        long questionCount = questionMapper.selectCount(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .eq(Question::getCategoryId, categoryId));
        if (questionCount > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "当前分类下仍有题目，无法删除");
        }
        questionCategoryMapper.deleteById(categoryId);
    }

    @Override
    public PageResult<TeacherQuestionView> pageQuestions(TeacherQuestionQuery query) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        Page<Question> page = questionMapper.selectPage(new Page<>(pageNo, pageSize), buildQuestionQuery(tenantId, query));
        return PageResult.of(fillQuestionViews(tenantId, page.getRecords()), page.getTotal(), pageNo, pageSize);
    }

    @Override
    public TeacherQuestionView getQuestion(Long questionId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Question question = getQuestionOrThrow(tenantId, questionId);
        return fillQuestionViews(tenantId, List.of(question)).stream().findFirst()
                .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND, "题目不存在"));
    }

    @Override
    @Transactional
    public TeacherQuestionView createQuestion(TeacherQuestionSaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
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
        question.setCreateBy(teacherId);
        question.setUpdateBy(teacherId);
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        questionMapper.insert(question);

        saveQuestionRelations(question.getId(), tenantId, teacherId, request);
        return getQuestion(question.getId());
    }

    @Override
    @Transactional
    public TeacherQuestionView updateQuestion(Long questionId, TeacherQuestionSaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        Question question = getQuestionOrThrow(tenantId, questionId);
        validateQuestionRequest(tenantId, request, questionId);

        question.setCategoryId(request.getCategoryId());
        question.setTitle(trimToNull(request.getTitle()));
        question.setType(normalizeType(request.getType()));
        question.setMultiSelect(Boolean.TRUE.equals(request.getMultiSelect()));
        question.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        question.setDefaultScore(request.getDefaultScore());
        question.setAnalysis(trimToNull(request.getAnalysis()));
        question.setUpdateBy(teacherId);
        question.setUpdateTime(LocalDateTime.now());
        questionMapper.updateById(question);

        questionChoiceMapper.delete(Wrappers.<QuestionChoice>lambdaQuery()
                .eq(QuestionChoice::getTenantId, tenantId)
                .eq(QuestionChoice::getQuestionId, questionId));
        questionAnswerMapper.delete(Wrappers.<QuestionAnswer>lambdaQuery()
                .eq(QuestionAnswer::getTenantId, tenantId)
                .eq(QuestionAnswer::getQuestionId, questionId));
        saveQuestionRelations(questionId, tenantId, teacherId, request);
        return getQuestion(questionId);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        getQuestionOrThrow(tenantId, questionId);

        long paperRefCount = teacherPaperQuestionMapper.selectCount(Wrappers.<TeacherPaperQuestion>lambdaQuery()
                .eq(TeacherPaperQuestion::getTenantId, tenantId)
                .eq(TeacherPaperQuestion::getQuestionId, questionId));
        if (paperRefCount > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "当前题目已被试卷引用，无法删除");
        }

        questionChoiceMapper.delete(Wrappers.<QuestionChoice>lambdaQuery()
                .eq(QuestionChoice::getTenantId, tenantId)
                .eq(QuestionChoice::getQuestionId, questionId));
        questionAnswerMapper.delete(Wrappers.<QuestionAnswer>lambdaQuery()
                .eq(QuestionAnswer::getTenantId, tenantId)
                .eq(QuestionAnswer::getQuestionId, questionId));
        questionMapper.deleteById(questionId);
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
            throw new ServiceException(ErrorCode.BAD_REQUEST, "同级分类下已存在相同名称");
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
            throw new ServiceException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    private Question getQuestionOrThrow(Long tenantId, Long questionId) {
        Question question = questionMapper.selectOne(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)
                .eq(Question::getId, questionId)
                .last("limit 1"));
        if (question == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "题目不存在");
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
            throw new ServiceException(ErrorCode.BAD_REQUEST, "当前分类下已存在相同题目");
        }

        if ("CHOICE".equals(type)) {
            if (CollectionUtils.isEmpty(request.getChoices()) || request.getChoices().size() < 2) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "选择题至少需要两个选项");
            }
            long correctCount = request.getChoices().stream()
                    .filter(choice -> Boolean.TRUE.equals(choice.getCorrect()))
                    .count();
            if (correctCount == 0) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "选择题至少需要一个正确选项");
            }
            if (!Boolean.TRUE.equals(request.getMultiSelect()) && correctCount > 1) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "单选题只能有一个正确选项");
            }
            return;
        }

        if (request.getAnswer() == null || !StringUtils.hasText(request.getAnswer().getAnswerText())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "当前题型必须填写参考答案");
        }
    }

    private void saveQuestionRelations(Long questionId,
                                       Long tenantId,
                                       Long teacherId,
                                       TeacherQuestionSaveRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if ("CHOICE".equals(normalizeType(request.getType()))) {
            List<String> answerKeys = new ArrayList<>();
            for (int i = 0; i < request.getChoices().size(); i++) {
                TeacherQuestionChoiceRequest item = request.getChoices().get(i);
                String choiceKey = StringUtils.hasText(item.getKey())
                        ? item.getKey().trim().toUpperCase()
                        : String.valueOf((char) ('A' + i));
                QuestionChoice choice = new QuestionChoice();
                choice.setTenantId(tenantId);
                choice.setQuestionId(questionId);
                choice.setChoiceKey(choiceKey);
                choice.setChoiceText(trimToNull(item.getText()));
                choice.setCorrect(Boolean.TRUE.equals(item.getCorrect()));
                choice.setSortNo(i);
                choice.setStatus(STATUS_ENABLED);
                choice.setCreateBy(teacherId);
                choice.setUpdateBy(teacherId);
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
            answer.setCreateBy(teacherId);
            answer.setUpdateBy(teacherId);
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
        answer.setCreateBy(teacherId);
        answer.setUpdateBy(teacherId);
        answer.setCreateTime(now);
        answer.setUpdateTime(now);
        questionAnswerMapper.insert(answer);
    }

    private String normalizeType(String type) {
        String normalized = normalizeTypeAllowBlank(type);
        if (!StringUtils.hasText(normalized) || !SUPPORTED_TYPES.contains(normalized)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "暂不支持的题目类型");
        }
        return normalized;
    }

    private String normalizeTypeAllowBlank(String type) {
        return StringUtils.hasText(type) ? type.trim().toUpperCase() : null;
    }

    private String normalizeDifficulty(String difficulty) {
        String normalized = normalizeDifficultyAllowBlank(difficulty);
        if (!StringUtils.hasText(normalized) || !SUPPORTED_DIFFICULTIES.contains(normalized)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "暂不支持的题目难度");
        }
        return normalized;
    }

    private String normalizeDifficultyAllowBlank(String difficulty) {
        return StringUtils.hasText(difficulty) ? difficulty.trim().toUpperCase() : null;
    }

    private long normalizePageNo(long pageNo) {
        return pageNo <= 0 ? 1 : pageNo;
    }

    private long normalizePageSize(long pageSize) {
        if (pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private Long defaultParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
