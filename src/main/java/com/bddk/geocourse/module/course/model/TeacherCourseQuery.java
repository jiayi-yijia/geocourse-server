package com.bddk.geocourse.module.course.model;

import lombok.Data;

@Data
public class TeacherCourseQuery {

    private String keyword;
    private String category;
    private String module;
    private String stage;
    private String difficulty;
    private String publishStatus;
    private Boolean mineOnly = Boolean.FALSE;
    private long pageNo = 1;
    private long pageSize = 10;
}
