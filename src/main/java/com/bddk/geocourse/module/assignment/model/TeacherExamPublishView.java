package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TeacherExamPublishView {

    private Long id;
    private Long paperId;
    private String paperName;
    private String title;
    private String description;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal passScore;
    private BigDecimal totalScore;
    private Integer questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
