package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherPaperQuestionView {

    private Long questionId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String type;
    private String difficulty;
    private Integer defaultScore;
    private BigDecimal paperScore;
}
