package com.bddk.geocourse.module.lessonprep.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonPrepAttachmentImportRequest {

    @NotNull(message = "请选择附件")
    private Long fileId;

    private String docType;
}
