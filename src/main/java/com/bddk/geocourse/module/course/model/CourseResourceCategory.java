package com.bddk.geocourse.module.course.model;

import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;

public enum CourseResourceCategory {
    TEACHER("teacher", "teacher"),
    STUDENT("student", "student"),
    INTERDISCIPLINARY("interdisciplinary", "interdisciplinary");

    private final String pathValue;
    private final String courseTypeValue;

    CourseResourceCategory(String pathValue, String courseTypeValue) {
        this.pathValue = pathValue;
        this.courseTypeValue = courseTypeValue;
    }

    public String pathValue() {
        return pathValue;
    }

    public String courseTypeValue() {
        return courseTypeValue;
    }

    public static CourseResourceCategory fromPath(String raw) {
        for (CourseResourceCategory value : values()) {
            if (value.pathValue.equalsIgnoreCase(raw)) {
                return value;
            }
        }
        throw new ServiceException(ErrorCode.BAD_REQUEST, "不支持的课程分类: " + raw);
    }
}
