package com.bddk.geocourse.module.compat.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentAssignmentAnswerSubmitRequest {

    @NotNull
    private Long questionId;

    private String answer;
}
