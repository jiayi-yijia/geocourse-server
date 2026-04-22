package com.bddk.geocourse.module.course.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourseChapterView {

    private Long chapterId;

    private String title;

    private Integer sortNo;

    private Integer resourceCount;

    private Integer videoCount;

    private String primaryVideoUrl;

    private String primaryVideoCoverUrl;

    private List<CourseChapterResourceView> resources = new ArrayList<>();
}
