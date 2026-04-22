package com.bddk.geocourse.module.lessonprep.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepFileUploadView;
import com.bddk.geocourse.module.lessonprep.service.LessonPrepFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "教师端备课附件")
@RestController
@RequestMapping("/admin-api/lesson-prep/files")
public class LessonPrepFileController {

    private final LessonPrepFileService lessonPrepFileService;

    public LessonPrepFileController(LessonPrepFileService lessonPrepFileService) {
        this.lessonPrepFileService = lessonPrepFileService;
    }

    @Operation(summary = "上传备课附件")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<LessonPrepFileUploadView> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("上传成功", lessonPrepFileService.upload(file));
    }
}
