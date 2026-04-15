package com.bddk.geocourse.module.compat.model;

public record SchoolPortalContentItemView(
        Long id,
        String title,
        String module,
        String accessScope,
        String createdAt,
        String teacherName,
        String teacherAvatar,
        String coverImageUrl,
        boolean hasQuestionBank
) {
}
