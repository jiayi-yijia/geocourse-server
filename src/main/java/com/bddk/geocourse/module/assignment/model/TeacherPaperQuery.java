package com.bddk.geocourse.module.assignment.model;

import lombok.Data;

@Data
public class TeacherPaperQuery {

    private String name;
    private String status;
    private long pageNo = 1;
    private long pageSize = 10;
}
