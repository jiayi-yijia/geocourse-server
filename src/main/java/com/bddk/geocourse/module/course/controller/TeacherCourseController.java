package com.bddk.geocourse.module.course.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.course.model.CourseResourceView;
import com.bddk.geocourse.module.course.model.TeacherCourseQuery;
import com.bddk.geocourse.module.course.service.TeacherCourseQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Teacher Course Query")
@RestController
@RequestMapping("/admin-api/teacher/courses")
public class TeacherCourseController {

    private final TeacherCourseQueryService teacherCourseQueryService;

    public TeacherCourseController(TeacherCourseQueryService teacherCourseQueryService) {
        this.teacherCourseQueryService = teacherCourseQueryService;
    }

    @Operation(summary = "Page teacher courses")
    @GetMapping
    public ApiResponse<PageResult<CourseResourceView>> pageCourses(TeacherCourseQuery query) {
        return ApiResponse.success(teacherCourseQueryService.pageCourses(query));
    }

    @Operation(summary = "Get teacher course detail")
    @GetMapping("/{courseId}")
    public ApiResponse<CourseResourceView> getCourse(@PathVariable Long courseId) {
        return ApiResponse.success(teacherCourseQueryService.getCourse(courseId));
    }
}
