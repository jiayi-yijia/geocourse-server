package com.bddk.geocourse.module.assignment.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherExamGradeRequest {

    private String reviewComment;

    @Valid
    @NotEmpty(message = "批改明细不能为空")
    private List<TeacherExamGradeItemRequest> items = new ArrayList<>();
}
