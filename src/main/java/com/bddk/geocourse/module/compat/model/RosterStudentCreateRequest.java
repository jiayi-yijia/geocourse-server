package com.bddk.geocourse.module.compat.model;

import jakarta.validation.constraints.NotBlank;

public record RosterStudentCreateRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Initial password is required")
        String initialPassword,
        String name,
        @NotBlank(message = "Mobile is required")
        String mobile,
        String email,
        String studentNo,
        String gradeClass,
        String status
) {
}
