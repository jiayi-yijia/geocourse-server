package com.bddk.geocourse.module.compat.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class StudentAssignmentQuestionView {

    private Long questionId;
    private String title;
    private String type;
    private Boolean multiSelect;
    private Integer sortNo;
    private BigDecimal score;
    private String userAnswer;
    private List<StudentAssignmentChoiceView> choices = new ArrayList<>();
}
