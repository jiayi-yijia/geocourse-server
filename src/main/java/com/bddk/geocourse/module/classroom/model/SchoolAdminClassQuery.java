package com.bddk.geocourse.module.classroom.model;

import lombok.Data;

@Data
public class SchoolAdminClassQuery {

    private String keyword;
    private String status;
    private long pageNo = 1;
    private long pageSize = 10;
}
