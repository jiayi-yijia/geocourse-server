package com.bddk.geocourse.module.assignment.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherPaperQuestionScoreRequest {

    @NotNull(message = "题目不能为空")
    private Long questionId;

    @NotNull(message = "题目分值不能为空")
    @DecimalMin(value = "0.1", message = "题目分值必须大于0")
    private BigDecimal score;
}
