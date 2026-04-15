package com.bddk.geocourse.module.questionbank.model;

import lombok.Data;

@Data
public class TeacherQuestionQuery {

    private String keyword;
    private String type;
    private String difficulty;
    private Long categoryId;
    private long pageNo = 1;
    private long pageSize = 10;
}
