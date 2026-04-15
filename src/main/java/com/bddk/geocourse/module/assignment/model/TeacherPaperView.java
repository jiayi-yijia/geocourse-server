package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherPaperView {

    private Long id;
    private String name;
    private String description;
    private String status;
    private BigDecimal totalScore;
    private Integer questionCount;
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TeacherPaperQuestionView> questions = new ArrayList<>();
}
