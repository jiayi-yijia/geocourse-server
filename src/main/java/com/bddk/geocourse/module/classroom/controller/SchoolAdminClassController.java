package com.bddk.geocourse.module.classroom.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.classroom.model.SchoolAdminClassQuery;
import com.bddk.geocourse.module.classroom.model.SchoolAdminClassStudentQuery;
import com.bddk.geocourse.module.classroom.model.SchoolAdminClassViews;
import com.bddk.geocourse.module.classroom.service.SchoolAdminClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Tag(name = "School Admin Class")
@RestController
@RequestMapping("/admin-api/school-admin/classes")
public class SchoolAdminClassController {

    private static final String TEMPLATE_FILE_NAME = "class-student-import-template.xlsx";

    private final SchoolAdminClassService schoolAdminClassService;

    public SchoolAdminClassController(SchoolAdminClassService schoolAdminClassService) {
        this.schoolAdminClassService = schoolAdminClassService;
    }

    @Operation(summary = "Page classes for school admin")
    @GetMapping
    public ApiResponse<PageResult<SchoolAdminClassViews.Item>> pageClasses(SchoolAdminClassQuery query) {
        return ApiResponse.success(schoolAdminClassService.pageClasses(query));
    }

    @Operation(summary = "Get class detail for school admin")
    @GetMapping("/{classId}")
    public ApiResponse<SchoolAdminClassViews.Detail> getClassDetail(@PathVariable Long classId) {
        return ApiResponse.success(schoolAdminClassService.getClassDetail(classId));
    }

    @Operation(summary = "Page students in a class for school admin")
    @GetMapping("/{classId}/students")
    public ApiResponse<PageResult<SchoolAdminClassViews.Student>> pageStudents(@PathVariable Long classId,
                                                                               SchoolAdminClassStudentQuery query) {
        return ApiResponse.success(schoolAdminClassService.pageStudents(classId, query));
    }

    @Operation(summary = "Import students into a class")
    @PostMapping(value = "/{classId}/students/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SchoolAdminClassViews.StudentImportResult> importStudents(@PathVariable Long classId,
                                                                                 @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(schoolAdminClassService.importStudents(classId, file));
    }

    @Operation(summary = "Download student import template")
    @GetMapping("/students/import/template")
    public ResponseEntity<byte[]> downloadStudentImportTemplate() {
        byte[] content = schoolAdminClassService.downloadStudentImportTemplate();
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(TEMPLATE_FILE_NAME, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
}
