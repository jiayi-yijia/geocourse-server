package com.bddk.geocourse.module.compat.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentAssignmentSummaryView {

    private Long publishId;
    private String title;
    private String description;
    private String paperName;
    private String teacherName;
    private String workflow;
    private String publishStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalScore;
    private BigDecimal passScore;
    private Integer questionCount;
    private Integer duration;
    private Long recordId;
    private String recordStatus;
    private BigDecimal score;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedTime;
}
