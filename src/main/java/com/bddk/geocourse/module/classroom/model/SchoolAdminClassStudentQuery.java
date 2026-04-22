package com.bddk.geocourse.module.classroom.model;

import lombok.Data;

@Data
public class SchoolAdminClassStudentQuery {

    private String keyword;
    private String joinStatus;
    private long pageNo = 1;
    private long pageSize = 10;
}
