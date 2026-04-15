package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

@Data
public class TeacherExamRecordQuery {

    private Long paperId;
    private String studentName;
    private String status;
    private long pageNo = 1;
    private long pageSize = 10;
}
