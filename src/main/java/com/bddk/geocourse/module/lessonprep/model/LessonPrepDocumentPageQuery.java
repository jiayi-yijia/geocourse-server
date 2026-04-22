package com.bddk.geocourse.module.lessonprep.model;

import lombok.Data;

@Data
public class LessonPrepDocumentPageQuery {

    private Long pageNo = 1L;

    private Long pageSize = 10L;

    private String keyword;

    private String courseName;

    private String status;
}
