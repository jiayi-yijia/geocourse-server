package com.bddk.geocourse.module.questionbank.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionCategoryView {

    private Long id;
    private Long parentId;
    private String name;
    private String description;
    private Integer sortNo;
    private Long questionCount;
    private LocalDateTime createdAt;
    private List<QuestionCategoryView> children;
}
