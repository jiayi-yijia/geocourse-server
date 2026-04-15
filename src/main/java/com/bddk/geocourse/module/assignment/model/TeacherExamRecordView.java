package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TeacherExamRecordView {

    private Long id;
    private Long publishId;
    private String publishTitle;
    private Long paperId;
    private String paperName;
    private Long studentId;
    private String studentName;
    private BigDecimal score;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private String status;
    private Integer windowSwitches;
    private String reviewComment;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime gradedTime;
    private LocalDateTime createdAt;
}
