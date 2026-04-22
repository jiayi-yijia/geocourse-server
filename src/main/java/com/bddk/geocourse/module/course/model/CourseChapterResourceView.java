package com.bddk.geocourse.module.course.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseChapterResourceView {

    private Long resourceId;

    private Long chapterId;

    private String resourceType;

    private String title;

    private String fileName;

    private String url;

    private String coverUrl;

    private Long fileSize;

    private String contentType;

    private Integer sortNo;

    private LocalDateTime createdAt;
}
