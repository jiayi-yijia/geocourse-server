package com.bddk.geocourse.module.course.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CourseResourceCreateCommand {

    @NotBlank(message = "Course title must not be blank")
    private String title;

    @NotBlank(message = "Course module must not be blank")
    private String module;

    @NotBlank(message = "Course stage must not be blank")
    private String stage;

    @NotBlank(message = "Course difficulty must not be blank")
    private String difficulty;

    @NotNull(message = "Class hours must not be null")
    @Min(value = 1, message = "Class hours must be at least 1")
    @Max(value = 500, message = "Class hours must not exceed 500")
    private Integer classHours;

    private String description;
    private String phase;
    private String topicType;
    private Boolean encrypted;
    private String accessScope;
    private String downloadPermission;

    @NotNull(message = "Video file is required")
    private MultipartFile videoFile;

    private MultipartFile handoutFile;
    private MultipartFile materialFile;
    private MultipartFile exerciseFile;
    private MultipartFile audioFile;
    private MultipartFile imageFile;
    private MultipartFile questionBankFile;
    private MultipartFile answerAnalysisFile;
    private MultipartFile outlineFile;
    private MultipartFile teachingCaseFile;
    private MultipartFile implementationPlanFile;
    private MultipartFile expertGuideVideoFile;
}
