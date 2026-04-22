package com.bddk.geocourse.module.questionbank.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class QuestionBankAttachmentImportRequest {

    private Long categoryId;

    private String categoryName;

    @Min(value = 1, message = "Default score must be greater than 0")
    private Integer defaultScore = 5;

    @NotNull(message = "Please upload an attachment")
    private MultipartFile file;
}
