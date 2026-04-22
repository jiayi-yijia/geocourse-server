package com.bddk.geocourse.module.lessonprep.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonPrepDocumentPageItemView {

    private Long id;
    private String title;
    private String courseName;
    private String docType;
    private String status;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private Integer attachmentCount;
}
