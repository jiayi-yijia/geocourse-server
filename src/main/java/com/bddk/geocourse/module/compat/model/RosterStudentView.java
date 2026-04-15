package com.bddk.geocourse.module.compat.model;

public record RosterStudentView(
        Long id,
        String username,
        String initialPassword,
        String name,
        String mobile,
        String email,
        String studentNo,
        String gradeClass,
        String status,
        String createdAt
) {
}
