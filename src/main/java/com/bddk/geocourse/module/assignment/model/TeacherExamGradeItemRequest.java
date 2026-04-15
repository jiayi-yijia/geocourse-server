package com.bddk.geocourse.module.assignment.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherExamGradeItemRequest {

    @NotNull(message = "答题明细不能为空")
    private Long answerId;

    @NotNull(message = "分数不能为空")
    @DecimalMin(value = "0", message = "分数不能小于0")
    private BigDecimal score;

    private Integer correctFlag;

    private String teacherComment;
}
