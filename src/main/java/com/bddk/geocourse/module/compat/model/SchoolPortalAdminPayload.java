package com.bddk.geocourse.module.compat.model;

import java.util.List;

public record SchoolPortalAdminPayload(
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
        List<Long> selectedOpenCourseIds,
        List<Long> selectedCourseIds,
        List<Long> selectedQuestionBankCourseIds,
        List<String> qaItems
) {
}
