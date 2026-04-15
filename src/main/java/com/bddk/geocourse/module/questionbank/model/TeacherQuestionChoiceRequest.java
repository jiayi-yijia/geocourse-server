package com.bddk.geocourse.module.questionbank.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherQuestionChoiceRequest {

    private String key;

    @NotBlank(message = "选项内容不能为空")
    private String text;

    private Boolean correct = Boolean.FALSE;
}
