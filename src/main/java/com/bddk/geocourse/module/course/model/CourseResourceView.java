package com.bddk.geocourse.module.course.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseResourceView {
    private Long id;
    private String category;
    private String title;
    private String module;
    private String stage;
    private String difficulty;
    private Integer classHours;
    private String videoUrl;
    private String handoutUrl;
    private String materialUrl;
    private String exerciseUrl;
    private String audioUrl;
    private String imageUrl;
    private String questionBankUrl;
    private String answerAnalysisUrl;
    private String outlineUrl;
    private String teachingCaseUrl;
    private String implementationPlanUrl;
    private String expertGuideVideoUrl;
    private String phase;
    private String topicType;
    private Boolean encrypted;
    private String accessScope;
    private String downloadPermission;
    private String description;
    private String publishStatus;
    private Long teacherId;
    private Integer resourceCount;
    private Integer chapterCount;
    private List<CourseChapterView> chapters;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
