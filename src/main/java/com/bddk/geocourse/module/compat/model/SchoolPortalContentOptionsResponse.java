package com.bddk.geocourse.module.compat.model;

import java.util.List;

public record SchoolPortalContentOptionsResponse(
        List<SchoolPortalContentItemView> openCourses,
        List<SchoolPortalContentItemView> courses,
        List<SchoolPortalContentItemView> questionBanks
) {
}
