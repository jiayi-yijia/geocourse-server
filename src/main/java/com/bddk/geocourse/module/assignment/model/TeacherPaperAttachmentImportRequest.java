package com.bddk.geocourse.module.assignment.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TeacherPaperAttachmentImportRequest {

    private String name;

    private String description;

    @Min(value = 1, message = "考试时长必须大于0")
    private Integer duration = 60;

    @Min(value = 1, message = "默认分值必须大于0")
    private Integer defaultScore = 5;

    @NotNull(message = "请上传附件文件")
    private MultipartFile file;
}
