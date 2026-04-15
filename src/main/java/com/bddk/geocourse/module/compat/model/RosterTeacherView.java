package com.bddk.geocourse.module.compat.model;

public record RosterTeacherView(
        Long id,
        String username,
        String initialPassword,
        String name,
        String mobile,
        String email,
        String status,
        String createdAt,
        String classProgress,
        String lessonProgress,
        String attendance
) {
}
