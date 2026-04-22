package com.bddk.geocourse.module.lessonprep.model;

import lombok.Data;

@Data
public class LessonPrepFileUploadView {

    private Long fileId;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private String contentType;
    private String accessUrl;
    private String storagePath;
    private String storageType;
}
