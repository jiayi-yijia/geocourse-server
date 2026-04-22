package com.bddk.geocourse.module.course.model;

import lombok.Data;

@Data
public class CourseResourceStorageMetadata {

    private String provider;

    private String objectKey;

    private String coverUrl;

    private String coverObjectKey;

    private Long fileSize;

    private String contentType;

    private String resourceTitle;

    private String originalFileName;

    private Long uploaderId;
}
