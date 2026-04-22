package com.bddk.geocourse.module.course.service;

import com.bddk.geocourse.module.course.model.CourseResourceCreateCommand;
import com.bddk.geocourse.module.course.model.CourseResourceUpdateRequest;
import com.bddk.geocourse.module.course.model.CourseResourceView;

import java.util.List;

public interface CourseResourceService {
    List<CourseResourceView> listByCategory(String category);

    CourseResourceView getCourseDetail(Long courseId);

    CourseResourceView create(String category, CourseResourceCreateCommand command);

    CourseResourceView update(String category, Long courseId, CourseResourceUpdateRequest request);

    void delete(String category, Long courseId);
}
