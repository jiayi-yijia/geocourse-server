package com.bddk.geocourse.module.compat.controller;

import com.bddk.geocourse.module.course.model.CourseResourceCreateCommand;
import com.bddk.geocourse.module.course.model.CourseResourcePreviewView;
import com.bddk.geocourse.module.course.model.CourseResourceUpdateRequest;
import com.bddk.geocourse.module.course.model.CourseResourceView;
import com.bddk.geocourse.module.course.service.CourseResourceService;
import com.bddk.geocourse.module.course.service.CourseResourcePreviewService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/courses")
public class FrontendCourseController {

    private final CourseResourceService courseResourceService;
    private final CourseResourcePreviewService courseResourcePreviewService;

    public FrontendCourseController(CourseResourceService courseResourceService,
                                    CourseResourcePreviewService courseResourcePreviewService) {
        this.courseResourceService = courseResourceService;
        this.courseResourcePreviewService = courseResourcePreviewService;
    }

    @GetMapping("/detail/{courseId}")
    public CourseResourceView detail(@PathVariable Long courseId) {
        return courseResourceService.getCourseDetail(courseId);
    }

    @GetMapping("/resources/{resourceId}/preview")
    public CourseResourcePreviewView resourcePreview(@PathVariable Long resourceId) {
        return courseResourcePreviewService.getPreview(resourceId);
    }

    @GetMapping("/resources/{resourceId}/content")
    public ResponseEntity<Resource> resourceContent(@PathVariable Long resourceId) {
        CourseResourcePreviewView preview = courseResourcePreviewService.getPreview(resourceId);
        Resource resource = courseResourcePreviewService.getInlineContent(resourceId);
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(preview.getFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(preview.getContentType()))
                .body(resource);
    }

    @GetMapping(value = "/resources/{resourceId}/preview/slides/{pageNo}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> resourceSlide(@PathVariable Long resourceId,
                                                  @PathVariable Integer pageNo) {
        return ResponseEntity.ok(courseResourcePreviewService.getSlideImage(resourceId, pageNo));
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
