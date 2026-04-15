package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherExamGradingDetailView {

    private Long recordId;
    private Long publishId;
    private String publishTitle;
    private Long paperId;
    private String paperName;
    private Long studentId;
    private String studentName;
    private String status;
    private BigDecimal score;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private Integer windowSwitches;
    private String reviewComment;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime gradedTime;
    private List<TeacherExamAnswerView> answers = new ArrayList<>();
}
