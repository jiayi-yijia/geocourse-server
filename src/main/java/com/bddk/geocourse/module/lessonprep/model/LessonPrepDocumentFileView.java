package com.bddk.geocourse.module.lessonprep.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonPrepDocumentFileView {

    private Long fileId;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private String contentType;
    private String accessUrl;
    private String storagePath;
    private String storageType;
    private String relationType;
    private Integer sortNo;
    private LocalDateTime linkedAt;
}
