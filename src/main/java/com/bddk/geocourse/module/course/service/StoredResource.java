package com.bddk.geocourse.module.course.service;

public record StoredResource(
        String url,
        String provider,
        String objectKey
) {
}
