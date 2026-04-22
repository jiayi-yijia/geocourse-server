package com.bddk.geocourse.module.lessonprep.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class LessonPrepDocumentSaveRequest {

    private Long courseId;

    @NotBlank(message = "请输入文档标题")
    private String title;

    @NotBlank(message = "请输入课程名称")
    private String courseName;

    @NotBlank(message = "请选择文档类型")
    private String docType;

    private String summary;

    @NotBlank(message = "请选择内容格式")
    private String contentType;

    private String contentText;

    private List<Long> attachmentFileIds;
}
