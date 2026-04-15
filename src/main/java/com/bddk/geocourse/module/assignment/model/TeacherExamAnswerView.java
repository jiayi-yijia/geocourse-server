package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherExamAnswerView {

    private Long id;
    private Long questionId;
    private String questionType;
    private String questionTitle;
    private String standardAnswer;
    private String userAnswer;
    private BigDecimal maxScore;
    private BigDecimal score;
    private Integer correctFlag;
    private String aiComment;
    private String teacherComment;
    private Integer reviewed;
}
