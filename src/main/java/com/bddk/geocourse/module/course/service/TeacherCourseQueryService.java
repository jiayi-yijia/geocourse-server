package com.bddk.geocourse.module.course.service;

import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.course.model.CourseResourceView;
import com.bddk.geocourse.module.course.model.TeacherCourseQuery;

public interface TeacherCourseQueryService {

    PageResult<CourseResourceView> pageCourses(TeacherCourseQuery query);

    CourseResourceView getCourse(Long courseId);
}
