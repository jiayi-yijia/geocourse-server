package com.bddk.geocourse.module.course.model;

import lombok.Data;

@Data
public class CourseMetadata {
    private String category;
    private String module;
    private String stage;
    private String difficulty;
    private Integer classHours;
    private String phase;
    private String topicType;
    private Boolean encrypted;
    private String accessScope;
    private String downloadPermission;
}
