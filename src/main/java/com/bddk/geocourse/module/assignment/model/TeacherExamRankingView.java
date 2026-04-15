package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TeacherExamRankingView {

    private Integer rankNo;
    private Long paperId;
    private String paperName;
    private Long studentId;
    private String studentName;
    private BigDecimal score;
    private LocalDateTime endTime;
}
