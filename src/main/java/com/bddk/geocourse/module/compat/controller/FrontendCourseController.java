package com.bddk.geocourse.module.compat.controller;

import com.bddk.geocourse.module.course.model.CourseResourceCreateCommand;
import com.bddk.geocourse.module.course.model.CourseResourceUpdateRequest;
import com.bddk.geocourse.module.course.model.CourseResourceView;
import com.bddk.geocourse.module.course.service.CourseResourceService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class FrontendCourseController {

    private final CourseResourceService courseResourceService;

    public FrontendCourseController(CourseResourceService courseResourceService) {
        this.courseResourceService = courseResourceService;
    }

    @GetMapping("/{category}")
    public List<CourseResourceView> list(@PathVariable String category) {
        return courseResourceService.listByCategory(category);
    }

    @PostMapping(path = "/{category}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CourseResourceView create(@PathVariable String category,
                                     @Valid @ModelAttribute CourseResourceCreateCommand command) {
        return courseResourceService.create(category, command);
    }

    @PutMapping("/{category}/{courseId}")
    public CourseResourceView update(@PathVariable String category,
                                     @PathVariable Long courseId,
                                     @Valid @RequestBody CourseResourceUpdateRequest request) {
        return courseResourceService.update(category, courseId, request);
    }

    @DeleteMapping("/{category}/{courseId}")
    public void delete(@PathVariable String category, @PathVariable Long courseId) {
        courseResourceService.delete(category, courseId);
    }
}
