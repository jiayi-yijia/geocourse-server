package com.bddk.geocourse.module.questionbank.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionBankAttachmentImportResult {

    private String fileName;

    private Long categoryId;

    private String categoryName;

    private Integer importedCount;

    private List<Long> questionIds = new ArrayList<>();
}
