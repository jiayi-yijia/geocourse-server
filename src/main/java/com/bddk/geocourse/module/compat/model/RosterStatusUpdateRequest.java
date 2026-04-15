package com.bddk.geocourse.module.compat.model;

import jakarta.validation.constraints.NotBlank;

public record RosterStatusUpdateRequest(
        @NotBlank(message = "Status is required")
        String status
) {
}
