package com.bddk.geocourse.module.compat.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class StudentAssignmentResultView {

    private Long publishId;
    private Long recordId;
    private String title;
    private String paperName;
    private String teacherName;
    private String workflow;
    private String recordStatus;
    private BigDecimal totalScore;
    private BigDecimal passScore;
    private BigDecimal score;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private String reviewComment;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedTime;
    private Boolean pendingManualReview;
    private List<StudentAssignmentResultQuestionView> answers = new ArrayList<>();
}
