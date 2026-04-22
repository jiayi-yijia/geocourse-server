package com.bddk.geocourse.module.compat.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentAssignmentResultQuestionView {

    private Long questionId;
    private String questionTitle;
    private String questionType;
    private String standardAnswer;
    private String userAnswer;
    private BigDecimal maxScore;
    private BigDecimal score;
    private Integer correctFlag;
    private String aiComment;
    private String teacherComment;
    private Boolean reviewed;
}
