package com.bddk.geocourse.module.questionbank.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionCategorySaveRequest {

    @Min(value = 0, message = "父级分类不能为空")
    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    private Integer sortNo = 0;
}
