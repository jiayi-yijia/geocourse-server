package com.bddk.geocourse.module.questionbank.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherQuestionView {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String type;
    private Boolean multiSelect;
    private String difficulty;
    private Integer defaultScore;
    private String analysis;
    private TeacherQuestionAnswerRequest answer;
    private List<TeacherQuestionChoiceRequest> choices = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
