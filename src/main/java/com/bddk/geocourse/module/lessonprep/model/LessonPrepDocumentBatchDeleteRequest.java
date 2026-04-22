package com.bddk.geocourse.module.lessonprep.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class LessonPrepDocumentBatchDeleteRequest {

    @NotEmpty(message = "请选择要删除的文档")
    private List<Long> ids;
}
