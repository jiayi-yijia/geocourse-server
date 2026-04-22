package com.bddk.geocourse.module.course.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CourseResourcePreviewView {

    private Long resourceId;

    private String title;

    private String fileName;

    private String contentType;

    private String previewType;

    private String contentUrl;

    private String originalUrl;

    private Integer pageCount;

    private List<CourseResourcePreviewPageView> pages = new ArrayList<>();
}
