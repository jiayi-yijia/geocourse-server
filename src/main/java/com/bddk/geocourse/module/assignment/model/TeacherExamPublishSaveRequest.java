package com.bddk.geocourse.module.assignment.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TeacherExamPublishSaveRequest {

    @NotNull(message = "试卷不能为空")
    private Long paperId;

    @NotBlank(message = "考试标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "开考时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "及格分不能为空")
    @DecimalMin(value = "0", message = "及格分不能小于0")
    private BigDecimal passScore;
}
