package com.bddk.geocourse.module.lessonprep.controller;

import com.bddk.geocourse.framework.common.api.ApiResponse;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepAttachmentImportRequest;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepAttachmentImportView;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentBatchDeleteRequest;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentCopyRequest;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentDetailView;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentPageItemView;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentPageQuery;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentSaveRequest;
import com.bddk.geocourse.module.lessonprep.service.LessonPrepDocumentService;
import com.bddk.geocourse.module.lessonprep.service.LessonPrepImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "教师端备课文档")
@RestController
@RequestMapping("/admin-api/lesson-prep/documents")
public class LessonPrepDocumentController {

    private final LessonPrepDocumentService lessonPrepDocumentService;
    private final LessonPrepImportService lessonPrepImportService;

    public LessonPrepDocumentController(LessonPrepDocumentService lessonPrepDocumentService,
                                        LessonPrepImportService lessonPrepImportService) {
        this.lessonPrepDocumentService = lessonPrepDocumentService;
        this.lessonPrepImportService = lessonPrepImportService;
    }

    @Operation(summary = "分页查询备课文档")
    @GetMapping("/page")
    public ApiResponse<PageResult<LessonPrepDocumentPageItemView>> page(LessonPrepDocumentPageQuery query) {
        return ApiResponse.success(lessonPrepDocumentService.pageDocuments(query));
    }

    @Operation(summary = "查询备课文档详情")
    @GetMapping("/{documentId}")
    public ApiResponse<LessonPrepDocumentDetailView> detail(@PathVariable Long documentId) {
        return ApiResponse.success(lessonPrepDocumentService.getDocument(documentId));
    }

    @Operation(summary = "新增备课文档")
    @PostMapping
    public ApiResponse<LessonPrepDocumentDetailView> create(@Valid @RequestBody LessonPrepDocumentSaveRequest request) {
        return ApiResponse.success("创建成功", lessonPrepDocumentService.createDocument(request));
    }

    @Operation(summary = "修改备课文档")
    @PutMapping("/{documentId}")
    public ApiResponse<LessonPrepDocumentDetailView> update(@PathVariable Long documentId,
                                                            @Valid @RequestBody LessonPrepDocumentSaveRequest request) {
        return ApiResponse.success("保存成功", lessonPrepDocumentService.updateDocument(documentId, request));
    }

    @Operation(summary = "删除备课文档")
    @DeleteMapping("/{documentId}")
    public ApiResponse<Void> delete(@PathVariable Long documentId) {
        lessonPrepDocumentService.deleteDocument(documentId);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "批量删除备课文档")
    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody LessonPrepDocumentBatchDeleteRequest request) {
        lessonPrepDocumentService.batchDeleteDocuments(request.getIds());
        return ApiResponse.success("批量删除成功", null);
    }

    @Operation(summary = "复制备课文档")
    @PostMapping("/{documentId}/copy")
    public ApiResponse<LessonPrepDocumentDetailView> copy(@PathVariable Long documentId,
                                                          @Valid @RequestBody LessonPrepDocumentCopyRequest request) {
        return ApiResponse.success("复制成功", lessonPrepDocumentService.copyDocument(documentId, request.getTitle()));
    }

    @Operation(summary = "发布备课文档")
    @PostMapping("/{documentId}/publish")
    public ApiResponse<LessonPrepDocumentDetailView> publish(@PathVariable Long documentId) {
        return ApiResponse.success("发布成功", lessonPrepDocumentService.publishDocument(documentId));
    }

    @Operation(summary = "从附件导入教案正文")
    @PostMapping("/import/attachment")
    public ApiResponse<LessonPrepAttachmentImportView> importAttachment(
            @Valid @RequestBody LessonPrepAttachmentImportRequest request) {
        return ApiResponse.success("导入成功", lessonPrepImportService.importFromAttachment(request));
    }
}
