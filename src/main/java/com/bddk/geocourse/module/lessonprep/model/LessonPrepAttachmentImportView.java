package com.bddk.geocourse.module.lessonprep.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LessonPrepAttachmentImportView {

    private Long fileId;
    private String fileName;
    private String detectedFileType;
    private String title;
    private String courseName;
    private String docType;
    private String summary;
    private String contentType;
    private String contentText;
    private List<String> warnings = new ArrayList<>();
}
