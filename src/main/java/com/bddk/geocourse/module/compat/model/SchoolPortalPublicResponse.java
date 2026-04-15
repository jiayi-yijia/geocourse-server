package com.bddk.geocourse.module.compat.model;

import java.util.List;

public record SchoolPortalPublicResponse(
        String slug,
        String schoolName,
        String heroTitle,
        String heroSubtitle,
        String footerNote,
        String accentColor,
        boolean showOpenCourses,
        boolean showCourses,
        boolean showQuestionBank,
        boolean showQa,
        List<String> qaItems,
        List<SchoolPortalContentItemView> openCourses,
        List<SchoolPortalContentItemView> courses,
        List<SchoolPortalContentItemView> questionBanks,
        Integer teacherCount,
        Integer studentCount
) {
}
