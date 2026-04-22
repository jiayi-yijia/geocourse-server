package com.bddk.geocourse.module.compat.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudentAssignmentSubmitRequest {

    @Valid
    @NotEmpty
    private List<StudentAssignmentAnswerSubmitRequest> answers = new ArrayList<>();
}
