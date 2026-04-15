package com.bddk.geocourse.module.course.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseResourceUpdateRequest {

    @NotBlank(message = "课程名称不能为空")
    private String title;

    @NotBlank(message = "课程模块不能为空")
    private String module;

    @NotBlank(message = "学段不能为空")
    private String stage;

    @NotBlank(message = "难度不能为空")
    private String difficulty;

    @NotNull(message = "课时不能为空")
    @Min(value = 1, message = "课时至少为 1")
    @Max(value = 500, message = "课时不能超过 500")
    private Integer classHours;

    private String description;
}
