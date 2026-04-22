package com.bddk.geocourse.module.lessonprep.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class LessonPrepDocumentDetailView {

    private Long id;
    private String title;
    private Long courseId;
    private String courseName;
    private String docType;
    private String status;
    private String summary;
    private String sourceType;
    private Long sourceDocumentId;
    private String contentType;
    private String contentText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime lastEditedAt;
    private Integer attachmentCount;
    private List<LessonPrepDocumentFileView> attachments = new ArrayList<>();
}
