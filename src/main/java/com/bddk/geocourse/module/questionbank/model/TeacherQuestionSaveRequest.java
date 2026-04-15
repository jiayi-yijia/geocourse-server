package com.bddk.geocourse.module.questionbank.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherQuestionSaveRequest {

    @NotNull(message = "题目分类不能为空")
    private Long categoryId;

    @NotBlank(message = "题目内容不能为空")
    private String title;

    @NotBlank(message = "题目类型不能为空")
    private String type;

    private Boolean multiSelect = Boolean.FALSE;

    @NotBlank(message = "难度不能为空")
    private String difficulty;

    @NotNull(message = "默认分值不能为空")
    @Min(value = 1, message = "默认分值必须大于0")
    private Integer defaultScore;

    private String analysis;

    @Valid
    private List<TeacherQuestionChoiceRequest> choices = new ArrayList<>();

    @Valid
    private TeacherQuestionAnswerRequest answer;
}
