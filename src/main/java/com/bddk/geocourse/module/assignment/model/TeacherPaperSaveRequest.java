package com.bddk.geocourse.module.assignment.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherPaperSaveRequest {

    @NotBlank(message = "试卷名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "考试时长不能为空")
    @Min(value = 1, message = "考试时长必须大于0")
    private Integer duration;

    @Valid
    @NotEmpty(message = "试卷至少需要一道题目")
    private List<TeacherPaperQuestionScoreRequest> questions = new ArrayList<>();
}
