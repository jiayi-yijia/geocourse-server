package com.bddk.geocourse.module.analytics.model;

import lombok.Data;

@Data
public class TeacherAnalyticsOverviewView {

    private Long categoryCount;
    private Long questionCount;
    private Long paperCount;
    private Long publishedPaperCount;
    private Long examCount;
    private Long todayExamCount;
    private Long teacherCount;
}
