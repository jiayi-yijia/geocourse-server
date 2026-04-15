package com.bddk.geocourse.module.course.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.course.model.CourseResourceCreateCommand;
import com.bddk.geocourse.module.course.model.CourseResourceUpdateRequest;
import com.bddk.geocourse.module.course.model.CourseResourceView;
import com.bddk.geocourse.module.course.service.CourseResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "课程资源")
@RestController
@RequestMapping("/admin-api/courses")
public class CourseResourceController {

    private final CourseResourceService courseResourceService;

    public CourseResourceController(CourseResourceService courseResourceService) {
        this.courseResourceService = courseResourceService;
    }

    @Operation(summary = "查询课程资源列表")
    @GetMapping("/{category}")
    public ApiResponse<List<CourseResourceView>> list(@PathVariable String category) {
        return ApiResponse.success(courseResourceService.listByCategory(category));
    }

    @Operation(summary = "上传课程资源")
    @PostMapping(path = "/{category}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CourseResourceView> create(@PathVariable String category,
                                                  @Valid @ModelAttribute CourseResourceCreateCommand command) {
        return ApiResponse.success("上传成功", courseResourceService.create(category, command));
    }

    @Operation(summary = "修改课程基础信息")
    @PutMapping("/{category}/{courseId}")
    public ApiResponse<CourseResourceView> update(@PathVariable String category,
                                                  @PathVariable Long courseId,
                                                  @Valid @RequestBody CourseResourceUpdateRequest request) {
        return ApiResponse.success("修改成功", courseResourceService.update(category, courseId, request));
    }

    @Operation(summary = "删除课程资源")
    @DeleteMapping("/{category}/{courseId}")
    public ApiResponse<Void> delete(@PathVariable String category, @PathVariable Long courseId) {
        courseResourceService.delete(category, courseId);
        return ApiResponse.success("删除成功", null);
    }
}
