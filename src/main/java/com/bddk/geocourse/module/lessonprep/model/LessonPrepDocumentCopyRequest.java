package com.bddk.geocourse.module.lessonprep.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonPrepDocumentCopyRequest {

    @NotBlank(message = "请输入复制后的文档标题")
    private String title;
}
