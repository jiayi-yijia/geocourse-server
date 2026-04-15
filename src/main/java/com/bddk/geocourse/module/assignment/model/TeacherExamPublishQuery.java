package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

@Data
public class TeacherExamPublishQuery {

    private String title;
    private String status;
    private Long paperId;
    private long pageNo = 1;
    private long pageSize = 10;
}
