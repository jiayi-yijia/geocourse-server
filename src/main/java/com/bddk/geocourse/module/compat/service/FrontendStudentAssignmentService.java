package com.bddk.geocourse.module.compat.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.assignment.entity.TeacherExamAnswer;
import com.bddk.geocourse.module.assignment.entity.TeacherExamPublish;
import com.bddk.geocourse.module.assignment.entity.TeacherExamRecord;
import com.bddk.geocourse.module.assignment.entity.TeacherPaper;
import com.bddk.geocourse.module.assignment.entity.TeacherPaperQuestion;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamAnswerMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamPublishMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamRecordMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperQuestionMapper;
import com.bddk.geocourse.module.compat.model.FrontendAuthUserView;
import com.bddk.geocourse.module.compat.model.StudentAssignmentAnswerSubmitRequest;
import com.bddk.geocourse.module.compat.model.StudentAssignmentChoiceView;
import com.bddk.geocourse.module.compat.model.StudentAssignmentDetailView;
import com.bddk.geocourse.module.compat.model.StudentAssignmentQuestionView;
import com.bddk.geocourse.module.compat.model.StudentAssignmentResultQuestionView;
import com.bddk.geocourse.module.compat.model.StudentAssignmentResultView;
import com.bddk.geocourse.module.compat.model.StudentAssignmentSubmitRequest;
import com.bddk.geocourse.module.compat.model.StudentAssignmentSummaryView;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserDO;
import com.bddk.geocourse.module.identity.dal.mapper.SysUserMapper;
import com.bddk.geocourse.module.questionbank.entity.Question;
import com.bddk.geocourse.module.questionbank.entity.QuestionAnswer;
import com.bddk.geocourse.module.questionbank.entity.QuestionChoice;
import com.bddk.geocourse.module.questionbank.mapper.QuestionAnswerMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionChoiceMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FrontendStudentAssignmentService {

    private static final Set<String> VISIBLE_PUBLISH_STATUSES = Set.of("PUBLISHED", "CLOSED");
    private static final Set<String> SUBMITTED_RECORD_STATUSES = Set.of("SUBMITTED", "REVIEWED", "IN_PROGRESS");

    private final FrontendAuthService frontendAuthService;
    private final TeacherExamPublishMapper teacherExamPublishMapper;
    private final TeacherPaperMapper teacherPaperMapper;
    private final TeacherPaperQuestionMapper teacherPaperQuestionMapper;
    private final TeacherExamRecordMapper teacherExamRecordMapper;
    private final TeacherExamAnswerMapper teacherExamAnswerMapper;
    private final QuestionMapper questionMapper;
    private final QuestionChoiceMapper questionChoiceMapper;
    private final QuestionAnswerMapper questionAnswerMapper;
    private final SysUserMapper sysUserMapper;

    public FrontendStudentAssignmentService(FrontendAuthService frontendAuthService,
                                            TeacherExamPublishMapper teacherExamPublishMapper,
                                            TeacherPaperMapper teacherPaperMapper,
                                            TeacherPaperQuestionMapper teacherPaperQuestionMapper,
                                            TeacherExamRecordMapper teacherExamRecordMapper,
                                            TeacherExamAnswerMapper teacherExamAnswerMapper,
                                            QuestionMapper questionMapper,
                                            QuestionChoiceMapper questionChoiceMapper,
                                            QuestionAnswerMapper questionAnswerMapper,
                                            SysUserMapper sysUserMapper) {
        this.frontendAuthService = frontendAuthService;
        this.teacherExamPublishMapper = teacherExamPublishMapper;
        this.teacherPaperMapper = teacherPaperMapper;
        this.teacherPaperQuestionMapper = teacherPaperQuestionMapper;
        this.teacherExamRecordMapper = teacherExamRecordMapper;
        this.teacherExamAnswerMapper = teacherExamAnswerMapper;
        this.questionMapper = questionMapper;
        this.questionChoiceMapper = questionChoiceMapper;
        this.questionAnswerMapper = questionAnswerMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public List<StudentAssignmentSummaryView> listAssignments() {
        StudentContext student = requireCurrentStudent();
        Long tenantId = frontendAuthService.currentTenantId();
        LocalDateTime now = LocalDateTime.now();

        List<TeacherExamPublish> publishes = teacherExamPublishMapper.selectList(Wrappers.<TeacherExamPublish>lambdaQuery()
                .eq(TeacherExamPublish::getTenantId, tenantId)
                .in(TeacherExamPublish::getStatus, VISIBLE_PUBLISH_STATUSES)
                .orderByAsc(TeacherExamPublish::getEndTime)
                .orderByDesc(TeacherExamPublish::getId));
        if (publishes.isEmpty()) {
            return List.of();
        }

        Map<Long, TeacherPaper> paperMap = loadPaperMap(tenantId, publishes.stream().map(TeacherExamPublish::getPaperId).toList());
        Map<Long, TeacherExamRecord> recordMap = loadLatestRecordMap(tenantId, student.studentId(), publishes.stream().map(TeacherExamPublish::getId).toList());
        Map<Long, String> teacherNameMap = loadUserDisplayNameMap(publishes.stream().map(TeacherExamPublish::getCreateBy).toList());

        return publishes.stream()
                .map(publish -> toSummaryView(
                        publish,
                        paperMap.get(publish.getPaperId()),
                        recordMap.get(publish.getId()),
                        teacherNameMap.get(publish.getCreateBy()),
                        now))
                .toList();
    }

    public StudentAssignmentDetailView getAssignmentDetail(Long publishId) {
        StudentContext student = requireCurrentStudent();
        Long tenantId = frontendAuthService.currentTenantId();
        LocalDateTime now = LocalDateTime.now();

        TeacherExamPublish publish = getVisiblePublishOrThrow(tenantId, publishId);
        TeacherPaper paper = getPaperOrThrow(tenantId, publish.getPaperId());
        TeacherExamRecord record = loadLatestRecord(tenantId, student.studentId(), publishId);
        Map<Long, TeacherExamAnswer> answerMap = record == null ? Map.of() : loadAnswerMap(tenantId, record.getId());

        StudentAssignmentDetailView view = new StudentAssignmentDetailView();
        view.setPublishId(publish.getId());
        view.setTitle(publish.getTitle());
        view.setDescription(publish.getDescription());
        view.setPaperName(paper.getName());
        view.setTeacherName(loadUserDisplayNameMap(List.of(publish.getCreateBy())).get(publish.getCreateBy()));
        view.setWorkflow(resolveWorkflow(publish, record, now));
        view.setPublishStatus(normalizeStatus(publish.getStatus()));
        view.setStartTime(publish.getStartTime());
        view.setEndTime(publish.getEndTime());
        view.setTotalScore(publish.getTotalScore());
        view.setPassScore(publish.getPassScore());
        view.setQuestionCount(publish.getQuestionCount());
        view.setDuration(paper.getDuration());
        if (record != null) {
            view.setRecordId(record.getId());
            view.setRecordStatus(normalizeStatus(record.getStatus()));
            view.setScore(record.getScore());
            view.setObjectiveScore(record.getObjectiveScore());
            view.setSubjectiveScore(record.getSubjectiveScore());
            view.setReviewComment(record.getReviewComment());
        }
        view.setAvailableForSubmit(isAvailableForSubmit(publish, record, now));
        view.setQuestions(loadStudentQuestions(tenantId, paper.getId(), answerMap));
        return view;
    }

    public StudentAssignmentResultView getAssignmentResult(Long publishId) {
        StudentContext student = requireCurrentStudent();
        Long tenantId = frontendAuthService.currentTenantId();

        TeacherExamPublish publish = getVisiblePublishOrThrow(tenantId, publishId);
        TeacherPaper paper = getPaperOrThrow(tenantId, publish.getPaperId());
        TeacherExamRecord record = loadLatestRecord(tenantId, student.studentId(), publishId);
        if (record == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "Assignment result not found");
        }

        String teacherName = loadUserDisplayNameMap(List.of(publish.getCreateBy())).get(publish.getCreateBy());
        List<TeacherExamAnswer> answers = teacherExamAnswerMapper.selectList(Wrappers.<TeacherExamAnswer>lambdaQuery()
                .eq(TeacherExamAnswer::getTenantId, tenantId)
                .eq(TeacherExamAnswer::getExamRecordId, record.getId())
                .orderByAsc(TeacherExamAnswer::getId));
        return toResultView(publish, paper, record, answers, teacherName);
    }

    @Transactional
    public StudentAssignmentResultView submitAssignment(Long publishId, StudentAssignmentSubmitRequest request) {
        StudentContext student = requireCurrentStudent();
        Long tenantId = frontendAuthService.currentTenantId();
        LocalDateTime now = LocalDateTime.now();

        TeacherExamPublish publish = getVisiblePublishOrThrow(tenantId, publishId);
        TeacherExamRecord existingRecord = loadLatestRecord(tenantId, student.studentId(), publishId);
        if (!isAvailableForSubmit(publish, existingRecord, now)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "This assignment can no longer be submitted");
        }

        TeacherPaper paper = getPaperOrThrow(tenantId, publish.getPaperId());
        List<TeacherPaperQuestion> paperQuestions = teacherPaperQuestionMapper.selectList(Wrappers.<TeacherPaperQuestion>lambdaQuery()
                .eq(TeacherPaperQuestion::getTenantId, tenantId)
                .eq(TeacherPaperQuestion::getPaperId, paper.getId())
                .orderByAsc(TeacherPaperQuestion::getSortNo)
                .orderByAsc(TeacherPaperQuestion::getId));
        if (paperQuestions.isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "No questions are configured for this assignment");
        }

        Map<Long, Question> questionMap = loadQuestionMap(tenantId, paperQuestions.stream().map(TeacherPaperQuestion::getQuestionId).toList());
        Map<Long, QuestionAnswer> standardAnswerMap = loadStandardAnswerMap(tenantId, questionMap.keySet());
        Map<Long, String> submittedAnswerMap = toSubmittedAnswerMap(request.getAnswers());

        BigDecimal objectiveScore = BigDecimal.ZERO;
        boolean pendingManualReview = false;
        List<TeacherExamAnswer> answers = new ArrayList<>();

        for (TeacherPaperQuestion relation : paperQuestions) {
            Question question = questionMap.get(relation.getQuestionId());
            if (question == null) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Question is missing from this assignment");
            }
            String userAnswer = normalizeText(submittedAnswerMap.get(question.getId()));
            QuestionAnswer standard = standardAnswerMap.get(question.getId());

            TeacherExamAnswer answer = new TeacherExamAnswer();
            answer.setTenantId(tenantId);
            answer.setQuestionId(question.getId());
            answer.setQuestionType(normalizeStatus(question.getType()));
            answer.setQuestionTitle(question.getTitle());
            answer.setStandardAnswer(standard == null ? null : standard.getAnswerText());
            answer.setUserAnswer(userAnswer);
            answer.setMaxScore(relation.getScore());
            answer.setCreateBy(student.studentId());
            answer.setCreateTime(now);
            answer.setUpdateBy(student.studentId());
            answer.setUpdateTime(now);

            if (isManualReviewQuestion(question)) {
                pendingManualReview = true;
                answer.setReviewed(0);
                answer.setAiComment("Pending teacher review");
            } else {
                boolean correct = isObjectiveAnswerCorrect(question, standard, userAnswer);
                BigDecimal score = correct ? relation.getScore() : BigDecimal.ZERO;
                objectiveScore = objectiveScore.add(score);
                answer.setScore(score);
                answer.setCorrectFlag(correct ? 1 : 0);
                answer.setReviewed(1);
                answer.setAiComment(correct ? "Auto graded as correct" : "Auto graded as incorrect");
            }
            answers.add(answer);
        }

        TeacherExamRecord record = new TeacherExamRecord();
        record.setTenantId(tenantId);
        record.setPublishId(publish.getId());
        record.setPublishTitle(publish.getTitle());
        record.setPaperId(paper.getId());
        record.setStudentId(student.studentId());
        record.setStudentName(student.displayName());
        record.setScore(objectiveScore);
        record.setObjectiveScore(objectiveScore);
        record.setSubjectiveScore(pendingManualReview ? null : BigDecimal.ZERO);
        record.setAnswers("Submitted from student portal");
        record.setStartTime(now);
        record.setEndTime(now);
        record.setStatus(pendingManualReview ? "SUBMITTED" : "REVIEWED");
        record.setWindowSwitches(0);
        record.setGradedTime(pendingManualReview ? null : now);
        record.setReviewComment(pendingManualReview ? "Pending teacher review" : "Auto graded");
        record.setCreateBy(student.studentId());
        record.setCreateTime(now);
        record.setUpdateBy(student.studentId());
        record.setUpdateTime(now);
        teacherExamRecordMapper.insert(record);

        for (TeacherExamAnswer answer : answers) {
            answer.setExamRecordId(record.getId());
            teacherExamAnswerMapper.insert(answer);
        }

        String teacherName = loadUserDisplayNameMap(List.of(publish.getCreateBy())).get(publish.getCreateBy());
        return toResultView(publish, paper, record, answers, teacherName);
    }

    private StudentContext requireCurrentStudent() {
        FrontendAuthUserView currentUser = frontendAuthService.currentUser();
        if (currentUser == null || currentUser.id() == null || !"STUDENT".equalsIgnoreCase(currentUser.role())) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "Student login is required");
        }
        String displayName = StringUtils.hasText(currentUser.displayName()) ? currentUser.displayName() : currentUser.username();
        return new StudentContext(currentUser.id(), displayName);
    }

    private TeacherExamPublish getVisiblePublishOrThrow(Long tenantId, Long publishId) {
        TeacherExamPublish publish = teacherExamPublishMapper.selectOne(Wrappers.<TeacherExamPublish>lambdaQuery()
                .eq(TeacherExamPublish::getTenantId, tenantId)
                .eq(TeacherExamPublish::getId, publishId)
                .last("limit 1"));
        if (publish == null || !VISIBLE_PUBLISH_STATUSES.contains(normalizeStatus(publish.getStatus()))) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "Assignment not found");
        }
        return publish;
    }

    private TeacherPaper getPaperOrThrow(Long tenantId, Long paperId) {
        TeacherPaper paper = teacherPaperMapper.selectOne(Wrappers.<TeacherPaper>lambdaQuery()
                .eq(TeacherPaper::getTenantId, tenantId)
                .eq(TeacherPaper::getId, paperId)
                .last("limit 1"));
        if (paper == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "Assignment paper not found");
        }
        return paper;
    }

    private Map<Long, TeacherPaper> loadPaperMap(Long tenantId, Collection<Long> paperIds) {
        if (paperIds.isEmpty()) {
            return Map.of();
        }
        return teacherPaperMapper.selectList(Wrappers.<TeacherPaper>lambdaQuery()
                        .eq(TeacherPaper::getTenantId, tenantId)
                        .in(TeacherPaper::getId, new LinkedHashSet<>(paperIds)))
                .stream()
                .collect(Collectors.toMap(TeacherPaper::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Question> loadQuestionMap(Long tenantId, Collection<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionMapper.selectList(Wrappers.<Question>lambdaQuery()
                        .eq(Question::getTenantId, tenantId)
                        .in(Question::getId, new LinkedHashSet<>(questionIds)))
                .stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, QuestionAnswer> loadStandardAnswerMap(Long tenantId, Collection<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionAnswerMapper.selectList(Wrappers.<QuestionAnswer>lambdaQuery()
                        .eq(QuestionAnswer::getTenantId, tenantId)
                        .in(QuestionAnswer::getQuestionId, new LinkedHashSet<>(questionIds))
                        .orderByAsc(QuestionAnswer::getId))
                .stream()
                .collect(Collectors.toMap(QuestionAnswer::getQuestionId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, TeacherExamRecord> loadLatestRecordMap(Long tenantId, Long studentId, Collection<Long> publishIds) {
        if (publishIds.isEmpty()) {
            return Map.of();
        }
        List<TeacherExamRecord> records = teacherExamRecordMapper.selectList(Wrappers.<TeacherExamRecord>lambdaQuery()
                .eq(TeacherExamRecord::getTenantId, tenantId)
                .eq(TeacherExamRecord::getStudentId, studentId)
                .in(TeacherExamRecord::getPublishId, new LinkedHashSet<>(publishIds))
                .orderByDesc(TeacherExamRecord::getCreateTime)
                .orderByDesc(TeacherExamRecord::getId));
        Map<Long, TeacherExamRecord> result = new LinkedHashMap<>();
        for (TeacherExamRecord record : records) {
            result.putIfAbsent(record.getPublishId(), record);
        }
        return result;
    }

    private TeacherExamRecord loadLatestRecord(Long tenantId, Long studentId, Long publishId) {
        return teacherExamRecordMapper.selectOne(Wrappers.<TeacherExamRecord>lambdaQuery()
                .eq(TeacherExamRecord::getTenantId, tenantId)
                .eq(TeacherExamRecord::getStudentId, studentId)
                .eq(TeacherExamRecord::getPublishId, publishId)
                .orderByDesc(TeacherExamRecord::getCreateTime)
                .orderByDesc(TeacherExamRecord::getId)
                .last("limit 1"));
    }

    private Map<Long, TeacherExamAnswer> loadAnswerMap(Long tenantId, Long recordId) {
        return teacherExamAnswerMapper.selectList(Wrappers.<TeacherExamAnswer>lambdaQuery()
                        .eq(TeacherExamAnswer::getTenantId, tenantId)
                        .eq(TeacherExamAnswer::getExamRecordId, recordId))
                .stream()
                .collect(Collectors.toMap(TeacherExamAnswer::getQuestionId, Function.identity(), (left, right) -> left));
    }

    private List<StudentAssignmentQuestionView> loadStudentQuestions(Long tenantId,
                                                                     Long paperId,
                                                                     Map<Long, TeacherExamAnswer> answerMap) {
        List<TeacherPaperQuestion> relations = teacherPaperQuestionMapper.selectList(Wrappers.<TeacherPaperQuestion>lambdaQuery()
                .eq(TeacherPaperQuestion::getTenantId, tenantId)
                .eq(TeacherPaperQuestion::getPaperId, paperId)
                .orderByAsc(TeacherPaperQuestion::getSortNo)
                .orderByAsc(TeacherPaperQuestion::getId));
        if (relations.isEmpty()) {
            return List.of();
        }

        Map<Long, Question> questionMap = loadQuestionMap(tenantId, relations.stream().map(TeacherPaperQuestion::getQuestionId).toList());
        Map<Long, List<QuestionChoice>> choiceMap = loadChoiceMap(tenantId, relations.stream().map(TeacherPaperQuestion::getQuestionId).toList());
        List<StudentAssignmentQuestionView> result = new ArrayList<>();
        for (TeacherPaperQuestion relation : relations) {
            Question question = questionMap.get(relation.getQuestionId());
            if (question == null) {
                continue;
            }
            TeacherExamAnswer answer = answerMap.get(question.getId());
            StudentAssignmentQuestionView view = new StudentAssignmentQuestionView();
            view.setQuestionId(question.getId());
            view.setTitle(question.getTitle());
            view.setType(normalizeStatus(question.getType()));
            view.setMultiSelect(Boolean.TRUE.equals(question.getMultiSelect()));
            view.setSortNo(relation.getSortNo());
            view.setScore(relation.getScore());
            view.setUserAnswer(answer == null ? null : answer.getUserAnswer());
            view.setChoices(toChoiceViews(question, choiceMap.get(question.getId())));
            result.add(view);
        }
        return result;
    }

    private Map<Long, List<QuestionChoice>> loadChoiceMap(Long tenantId, Collection<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionChoiceMapper.selectList(Wrappers.<QuestionChoice>lambdaQuery()
                        .eq(QuestionChoice::getTenantId, tenantId)
                        .in(QuestionChoice::getQuestionId, new LinkedHashSet<>(questionIds))
                        .orderByAsc(QuestionChoice::getSortNo)
                        .orderByAsc(QuestionChoice::getId))
                .stream()
                .collect(Collectors.groupingBy(QuestionChoice::getQuestionId, TreeMap::new, Collectors.toList()));
    }

    private List<StudentAssignmentChoiceView> toChoiceViews(Question question, List<QuestionChoice> choices) {
        if (choices != null && !choices.isEmpty()) {
            return choices.stream().map(choice -> {
                StudentAssignmentChoiceView view = new StudentAssignmentChoiceView();
                view.setKey(choice.getChoiceKey());
                view.setText(choice.getChoiceText());
                return view;
            }).toList();
        }
        if ("JUDGE".equalsIgnoreCase(question.getType())) {
            StudentAssignmentChoiceView trueChoice = new StudentAssignmentChoiceView();
            trueChoice.setKey("TRUE");
            trueChoice.setText("True");
            StudentAssignmentChoiceView falseChoice = new StudentAssignmentChoiceView();
            falseChoice.setKey("FALSE");
            falseChoice.setText("False");
            return List.of(trueChoice, falseChoice);
        }
        return List.of();
    }

    private Map<Long, String> loadUserDisplayNameMap(Collection<Long> userIds) {
        List<Long> ids = userIds.stream().filter(id -> id != null && id > 0).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectList(Wrappers.<SysUserDO>lambdaQuery()
                        .in(SysUserDO::getId, ids))
                .stream()
                .collect(Collectors.toMap(SysUserDO::getId, this::resolveUserDisplayName, (left, right) -> left));
    }

    private StudentAssignmentSummaryView toSummaryView(TeacherExamPublish publish,
                                                       TeacherPaper paper,
                                                       TeacherExamRecord record,
                                                       String teacherName,
                                                       LocalDateTime now) {
        StudentAssignmentSummaryView view = new StudentAssignmentSummaryView();
        view.setPublishId(publish.getId());
        view.setTitle(publish.getTitle());
        view.setDescription(publish.getDescription());
        view.setPaperName(paper == null ? null : paper.getName());
        view.setTeacherName(teacherName);
        view.setWorkflow(resolveWorkflow(publish, record, now));
        view.setPublishStatus(normalizeStatus(publish.getStatus()));
        view.setStartTime(publish.getStartTime());
        view.setEndTime(publish.getEndTime());
        view.setTotalScore(publish.getTotalScore());
        view.setPassScore(publish.getPassScore());
        view.setQuestionCount(publish.getQuestionCount());
        view.setDuration(paper == null ? null : paper.getDuration());
        if (record != null) {
            view.setRecordId(record.getId());
            view.setRecordStatus(normalizeStatus(record.getStatus()));
            view.setScore(record.getScore());
            view.setObjectiveScore(record.getObjectiveScore());
            view.setSubjectiveScore(record.getSubjectiveScore());
            view.setSubmittedAt(record.getEndTime() == null ? record.getCreateTime() : record.getEndTime());
            view.setGradedTime(record.getGradedTime());
        }
        return view;
    }

    private StudentAssignmentResultView toResultView(TeacherExamPublish publish,
                                                     TeacherPaper paper,
                                                     TeacherExamRecord record,
                                                     List<TeacherExamAnswer> answers,
                                                     String teacherName) {
        StudentAssignmentResultView view = new StudentAssignmentResultView();
        view.setPublishId(publish.getId());
        view.setRecordId(record.getId());
        view.setTitle(publish.getTitle());
        view.setPaperName(paper.getName());
        view.setTeacherName(teacherName);
        view.setWorkflow("REVIEWED".equalsIgnoreCase(record.getStatus()) ? "GRADED" : "SUBMITTED");
        view.setRecordStatus(normalizeStatus(record.getStatus()));
        view.setTotalScore(publish.getTotalScore());
        view.setPassScore(publish.getPassScore());
        view.setScore(record.getScore());
        view.setObjectiveScore(record.getObjectiveScore());
        view.setSubjectiveScore(record.getSubjectiveScore());
        view.setReviewComment(record.getReviewComment());
        view.setSubmittedAt(record.getEndTime() == null ? record.getCreateTime() : record.getEndTime());
        view.setGradedTime(record.getGradedTime());
        view.setPendingManualReview(!"REVIEWED".equalsIgnoreCase(record.getStatus()));
        view.setAnswers(answers.stream().map(this::toResultQuestionView).toList());
        return view;
    }

    private StudentAssignmentResultQuestionView toResultQuestionView(TeacherExamAnswer answer) {
        StudentAssignmentResultQuestionView view = new StudentAssignmentResultQuestionView();
        view.setQuestionId(answer.getQuestionId());
        view.setQuestionTitle(answer.getQuestionTitle());
        view.setQuestionType(normalizeStatus(answer.getQuestionType()));
        view.setStandardAnswer(answer.getStandardAnswer());
        view.setUserAnswer(answer.getUserAnswer());
        view.setMaxScore(answer.getMaxScore());
        view.setScore(answer.getScore());
        view.setCorrectFlag(answer.getCorrectFlag());
        view.setAiComment(answer.getAiComment());
        view.setTeacherComment(answer.getTeacherComment());
        view.setReviewed(answer.getReviewed() != null && answer.getReviewed() == 1);
        return view;
    }

    private Map<Long, String> toSubmittedAnswerMap(List<StudentAssignmentAnswerSubmitRequest> answers) {
        Map<Long, String> result = new HashMap<>();
        for (StudentAssignmentAnswerSubmitRequest answer : answers) {
            if (answer.getQuestionId() == null) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Question id is required");
            }
            if (result.putIfAbsent(answer.getQuestionId(), answer.getAnswer()) != null) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Duplicate question answers are not allowed");
            }
        }
        return result;
    }

    private boolean isManualReviewQuestion(Question question) {
        return "TEXT".equalsIgnoreCase(question.getType());
    }

    private boolean isObjectiveAnswerCorrect(Question question, QuestionAnswer standardAnswer, String userAnswer) {
        if (!StringUtils.hasText(userAnswer) || standardAnswer == null || !StringUtils.hasText(standardAnswer.getAnswerText())) {
            return false;
        }
        if (Boolean.TRUE.equals(question.getMultiSelect())) {
            return normalizeMultiValue(standardAnswer.getAnswerText()).equals(normalizeMultiValue(userAnswer));
        }
        return normalizeStatus(userAnswer).equals(normalizeStatus(standardAnswer.getAnswerText()));
    }

    private String resolveWorkflow(TeacherExamPublish publish, TeacherExamRecord record, LocalDateTime now) {
        if (record != null) {
            String status = normalizeStatus(record.getStatus());
            if ("REVIEWED".equals(status)) {
                return "GRADED";
            }
            if ("SUBMITTED".equals(status)) {
                return "SUBMITTED";
            }
        }
        if (isExpired(publish, now)) {
            return "OVERDUE";
        }
        return "TODO";
    }

    private boolean isAvailableForSubmit(TeacherExamPublish publish, TeacherExamRecord record, LocalDateTime now) {
        if (!"PUBLISHED".equalsIgnoreCase(publish.getStatus())) {
            return false;
        }
        if (publish.getStartTime() != null && now.isBefore(publish.getStartTime())) {
            return false;
        }
        if (publish.getEndTime() != null && !now.isBefore(publish.getEndTime())) {
            return false;
        }
        return record == null || !SUBMITTED_RECORD_STATUSES.contains(normalizeStatus(record.getStatus()));
    }

    private boolean isExpired(TeacherExamPublish publish, LocalDateTime now) {
        if ("CLOSED".equalsIgnoreCase(publish.getStatus())) {
            return true;
        }
        return publish.getEndTime() != null && !now.isBefore(publish.getEndTime());
    }

    private String resolveUserDisplayName(SysUserDO user) {
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName();
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        return user.getUsername();
    }

    private String normalizeStatus(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Set<String> normalizeMultiValue(String value) {
        return List.of(value.split("[,;\\s]+"))
                .stream()
                .map(this::normalizeStatus)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private record StudentContext(Long studentId, String displayName) {
    }
}
