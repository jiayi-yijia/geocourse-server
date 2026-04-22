package com.bddk.geocourse.module.lessonprep.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import com.bddk.geocourse.module.lessonprep.dal.dataobject.LessonPrepDocumentDO;
import com.bddk.geocourse.module.lessonprep.dal.dataobject.LessonPrepDocumentFileRelDO;
import com.bddk.geocourse.module.lessonprep.dal.dataobject.LessonPrepFileDO;
import com.bddk.geocourse.module.lessonprep.dal.mapper.LessonPrepDocumentFileRelMapper;
import com.bddk.geocourse.module.lessonprep.dal.mapper.LessonPrepDocumentMapper;
import com.bddk.geocourse.module.lessonprep.dal.mapper.LessonPrepFileMapper;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentDetailView;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentFileView;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentPageItemView;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentPageQuery;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentSaveRequest;
import com.bddk.geocourse.module.lessonprep.service.LessonPrepDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LessonPrepDocumentServiceImpl implements LessonPrepDocumentService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String RELATION_TYPE_ATTACHMENT = "ATTACHMENT";
    private static final Set<String> SUPPORTED_DOC_TYPES = Set.of("LESSON_PLAN", "COURSEWARE", "EXERCISE", "RESOURCE");
    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of("RICH_TEXT", "MARKDOWN", "PLAIN_TEXT");

    private final TeacherPortalContextService teacherPortalContextService;
    private final LessonPrepDocumentMapper lessonPrepDocumentMapper;
    private final LessonPrepFileMapper lessonPrepFileMapper;
    private final LessonPrepDocumentFileRelMapper lessonPrepDocumentFileRelMapper;

    public LessonPrepDocumentServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                         LessonPrepDocumentMapper lessonPrepDocumentMapper,
                                         LessonPrepFileMapper lessonPrepFileMapper,
                                         LessonPrepDocumentFileRelMapper lessonPrepDocumentFileRelMapper) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.lessonPrepDocumentMapper = lessonPrepDocumentMapper;
        this.lessonPrepFileMapper = lessonPrepFileMapper;
        this.lessonPrepDocumentFileRelMapper = lessonPrepDocumentFileRelMapper;
    }

    @Override
    public PageResult<LessonPrepDocumentPageItemView> pageDocuments(LessonPrepDocumentPageQuery query) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        String keyword = trimToNull(query.getKeyword());
        String courseName = trimToNull(query.getCourseName());
        String status = trimToNull(query.getStatus());

        Page<LessonPrepDocumentDO> page = lessonPrepDocumentMapper.selectPage(new Page<>(pageNo, pageSize),
                Wrappers.<LessonPrepDocumentDO>lambdaQuery()
                        .eq(LessonPrepDocumentDO::getTenantId, tenantId)
                        .eq(LessonPrepDocumentDO::getTeacherId, teacherId)
                        .like(StringUtils.hasText(keyword), LessonPrepDocumentDO::getTitle, keyword)
                        .eq(StringUtils.hasText(courseName), LessonPrepDocumentDO::getCourseName, courseName)
                        .eq(StringUtils.hasText(status), LessonPrepDocumentDO::getStatus, status)
                        .orderByDesc(LessonPrepDocumentDO::getUpdateTime)
                        .orderByDesc(LessonPrepDocumentDO::getId));

        Map<Long, Integer> attachmentCountMap = buildAttachmentCountMap(tenantId, page.getRecords().stream()
                .map(LessonPrepDocumentDO::getId)
                .toList());
        List<LessonPrepDocumentPageItemView> views = page.getRecords().stream()
                .map(item -> toPageItemView(item, attachmentCountMap.getOrDefault(item.getId(), 0)))
                .toList();
        return PageResult.of(views, page.getTotal(), pageNo, pageSize);
    }

    @Override
    public LessonPrepDocumentDetailView getDocument(Long documentId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        LessonPrepDocumentDO document = getOwnedDocumentOrThrow(tenantId, teacherId, documentId);
        return toDetailView(document, listAttachments(tenantId, document.getId()));
    }

    @Override
    @Transactional
    public LessonPrepDocumentDetailView createDocument(LessonPrepDocumentSaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        validateSaveRequest(tenantId, teacherId, request);

        LessonPrepDocumentDO document = new LessonPrepDocumentDO();
        document.setTenantId(tenantId);
        document.setTeacherId(teacherId);
        document.setCourseId(request.getCourseId());
        document.setCourseName(trimToNull(request.getCourseName()));
        document.setTitle(trimToNull(request.getTitle()));
        document.setDocType(normalizeUpper(request.getDocType()));
        document.setStatus(STATUS_DRAFT);
        document.setSummary(trimToNull(request.getSummary()));
        document.setContentType(normalizeUpper(request.getContentType()));
        document.setContentText(normalizeContentText(request.getContentText()));
        document.setLastEditedAt(LocalDateTime.now());
        document.setCreateBy(teacherId);
        document.setUpdateBy(teacherId);
        document.setCreateTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());
        lessonPrepDocumentMapper.insert(document);

        replaceAttachmentRelations(tenantId, teacherId, document.getId(), request.getAttachmentFileIds());
        return getDocument(document.getId());
    }

    @Override
    @Transactional
    public LessonPrepDocumentDetailView updateDocument(Long documentId, LessonPrepDocumentSaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        LessonPrepDocumentDO document = getOwnedDocumentOrThrow(tenantId, teacherId, documentId);
        validateSaveRequest(tenantId, teacherId, request);

        document.setCourseId(request.getCourseId());
        document.setCourseName(trimToNull(request.getCourseName()));
        document.setTitle(trimToNull(request.getTitle()));
        document.setDocType(normalizeUpper(request.getDocType()));
        document.setSummary(trimToNull(request.getSummary()));
        document.setContentType(normalizeUpper(request.getContentType()));
        document.setContentText(normalizeContentText(request.getContentText()));
        document.setLastEditedAt(LocalDateTime.now());
        document.setUpdateBy(teacherId);
        document.setUpdateTime(LocalDateTime.now());
        lessonPrepDocumentMapper.updateById(document);

        replaceAttachmentRelations(tenantId, teacherId, documentId, request.getAttachmentFileIds());
        return getDocument(documentId);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        getOwnedDocumentOrThrow(tenantId, teacherId, documentId);
        lessonPrepDocumentFileRelMapper.delete(Wrappers.<LessonPrepDocumentFileRelDO>lambdaQuery()
                .eq(LessonPrepDocumentFileRelDO::getTenantId, tenantId)
                .eq(LessonPrepDocumentFileRelDO::getDocumentId, documentId));
        lessonPrepDocumentMapper.deleteById(documentId);
    }

    @Override
    @Transactional
    public void batchDeleteDocuments(List<Long> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return;
        }
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        List<LessonPrepDocumentDO> ownedDocuments = lessonPrepDocumentMapper.selectList(Wrappers.<LessonPrepDocumentDO>lambdaQuery()
                .eq(LessonPrepDocumentDO::getTenantId, tenantId)
                .eq(LessonPrepDocumentDO::getTeacherId, teacherId)
                .in(LessonPrepDocumentDO::getId, documentIds));
        if (ownedDocuments.size() != documentIds.size()) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "存在无权删除或不存在的文档");
        }
        lessonPrepDocumentFileRelMapper.delete(Wrappers.<LessonPrepDocumentFileRelDO>lambdaQuery()
                .eq(LessonPrepDocumentFileRelDO::getTenantId, tenantId)
                .in(LessonPrepDocumentFileRelDO::getDocumentId, documentIds));
        lessonPrepDocumentMapper.deleteBatchIds(documentIds);
    }

    @Override
    @Transactional
    public LessonPrepDocumentDetailView copyDocument(Long documentId, String title) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        LessonPrepDocumentDO source = getOwnedDocumentOrThrow(tenantId, teacherId, documentId);
        List<LessonPrepDocumentFileView> attachments = listAttachments(tenantId, documentId);

        LessonPrepDocumentDO copy = new LessonPrepDocumentDO();
        copy.setTenantId(source.getTenantId());
        copy.setTeacherId(source.getTeacherId());
        copy.setCourseId(source.getCourseId());
        copy.setCourseName(source.getCourseName());
        copy.setTitle(trimToNull(title));
        copy.setDocType(source.getDocType());
        copy.setStatus(STATUS_DRAFT);
        copy.setSummary(source.getSummary());
        copy.setContentType(source.getContentType());
        copy.setContentText(source.getContentText());
        copy.setSourceType("COPY");
        copy.setSourceDocumentId(source.getId());
        copy.setLastEditedAt(LocalDateTime.now());
        copy.setCreateBy(teacherId);
        copy.setUpdateBy(teacherId);
        copy.setCreateTime(LocalDateTime.now());
        copy.setUpdateTime(LocalDateTime.now());
        lessonPrepDocumentMapper.insert(copy);

        replaceAttachmentRelations(tenantId, teacherId, copy.getId(), attachments.stream()
                .map(LessonPrepDocumentFileView::getFileId)
                .toList());
        return getDocument(copy.getId());
    }

    @Override
    @Transactional
    public LessonPrepDocumentDetailView publishDocument(Long documentId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        LessonPrepDocumentDO document = getOwnedDocumentOrThrow(tenantId, teacherId, documentId);
        document.setStatus(STATUS_PUBLISHED);
        if (document.getPublishedAt() == null) {
            document.setPublishedAt(LocalDateTime.now());
        }
        document.setUpdateBy(teacherId);
        document.setUpdateTime(LocalDateTime.now());
        lessonPrepDocumentMapper.updateById(document);
        return getDocument(documentId);
    }

    private LessonPrepDocumentDO getOwnedDocumentOrThrow(Long tenantId, Long teacherId, Long documentId) {
        LessonPrepDocumentDO document = lessonPrepDocumentMapper.selectOne(Wrappers.<LessonPrepDocumentDO>lambdaQuery()
                .eq(LessonPrepDocumentDO::getTenantId, tenantId)
                .eq(LessonPrepDocumentDO::getTeacherId, teacherId)
                .eq(LessonPrepDocumentDO::getId, documentId)
                .last("limit 1"));
        if (document == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "备课文档不存在");
        }
        return document;
    }

    private void validateSaveRequest(Long tenantId, Long teacherId, LessonPrepDocumentSaveRequest request) {
        String docType = normalizeUpper(request.getDocType());
        if (!SUPPORTED_DOC_TYPES.contains(docType)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "文档类型不支持");
        }
        String contentType = normalizeUpper(request.getContentType());
        if (!SUPPORTED_CONTENT_TYPES.contains(contentType)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "内容格式不支持");
        }
        validateAttachmentFileIds(tenantId, teacherId, request.getAttachmentFileIds());
    }

    private void validateAttachmentFileIds(Long tenantId, Long teacherId, List<Long> attachmentFileIds) {
        if (CollectionUtils.isEmpty(attachmentFileIds)) {
            return;
        }
        List<Long> distinctIds = attachmentFileIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        List<LessonPrepFileDO> files = lessonPrepFileMapper.selectList(Wrappers.<LessonPrepFileDO>lambdaQuery()
                .eq(LessonPrepFileDO::getTenantId, tenantId)
                .eq(LessonPrepFileDO::getUploaderId, teacherId)
                .in(LessonPrepFileDO::getId, distinctIds));
        if (files.size() != distinctIds.size()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "存在无效附件，请重新上传后再试");
        }
    }

    private void replaceAttachmentRelations(Long tenantId, Long teacherId, Long documentId, List<Long> attachmentFileIds) {
        lessonPrepDocumentFileRelMapper.delete(Wrappers.<LessonPrepDocumentFileRelDO>lambdaQuery()
                .eq(LessonPrepDocumentFileRelDO::getTenantId, tenantId)
                .eq(LessonPrepDocumentFileRelDO::getDocumentId, documentId));
        if (CollectionUtils.isEmpty(attachmentFileIds)) {
            return;
        }
        List<Long> orderedIds = attachmentFileIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        for (int i = 0; i < orderedIds.size(); i++) {
            LessonPrepDocumentFileRelDO relation = new LessonPrepDocumentFileRelDO();
            relation.setTenantId(tenantId);
            relation.setDocumentId(documentId);
            relation.setFileId(orderedIds.get(i));
            relation.setRelationType(RELATION_TYPE_ATTACHMENT);
            relation.setSortNo(i + 1);
            relation.setLinkedAt(LocalDateTime.now());
            relation.setCreateBy(teacherId);
            relation.setUpdateBy(teacherId);
            relation.setCreateTime(LocalDateTime.now());
            relation.setUpdateTime(LocalDateTime.now());
            lessonPrepDocumentFileRelMapper.insert(relation);
        }
    }

    private List<LessonPrepDocumentFileView> listAttachments(Long tenantId, Long documentId) {
        List<LessonPrepDocumentFileRelDO> relations = lessonPrepDocumentFileRelMapper.selectList(Wrappers.<LessonPrepDocumentFileRelDO>lambdaQuery()
                .eq(LessonPrepDocumentFileRelDO::getTenantId, tenantId)
                .eq(LessonPrepDocumentFileRelDO::getDocumentId, documentId)
                .orderByAsc(LessonPrepDocumentFileRelDO::getSortNo)
                .orderByAsc(LessonPrepDocumentFileRelDO::getId));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> fileIds = relations.stream().map(LessonPrepDocumentFileRelDO::getFileId).distinct().toList();
        Map<Long, LessonPrepFileDO> fileMap = lessonPrepFileMapper.selectBatchIds(fileIds).stream()
                .filter(item -> Objects.equals(item.getTenantId(), tenantId))
                .collect(Collectors.toMap(LessonPrepFileDO::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        List<LessonPrepDocumentFileView> attachments = new ArrayList<>();
        for (LessonPrepDocumentFileRelDO relation : relations) {
            LessonPrepFileDO file = fileMap.get(relation.getFileId());
            if (file == null) {
                continue;
            }
            LessonPrepDocumentFileView view = new LessonPrepDocumentFileView();
            view.setFileId(file.getId());
            view.setFileName(file.getFileName());
            view.setFileExt(file.getFileExt());
            view.setFileSize(file.getFileSize());
            view.setContentType(file.getContentType());
            view.setAccessUrl(file.getAccessUrl());
            view.setStoragePath(file.getStoragePath());
            view.setStorageType(file.getStorageType());
            view.setRelationType(relation.getRelationType());
            view.setSortNo(relation.getSortNo());
            view.setLinkedAt(relation.getLinkedAt());
            attachments.add(view);
        }
        return attachments;
    }

    private Map<Long, Integer> buildAttachmentCountMap(Long tenantId, List<Long> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return Map.of();
        }
        return lessonPrepDocumentFileRelMapper.selectList(Wrappers.<LessonPrepDocumentFileRelDO>lambdaQuery()
                        .eq(LessonPrepDocumentFileRelDO::getTenantId, tenantId)
                        .in(LessonPrepDocumentFileRelDO::getDocumentId, documentIds))
                .stream()
                .collect(Collectors.groupingBy(LessonPrepDocumentFileRelDO::getDocumentId,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private LessonPrepDocumentPageItemView toPageItemView(LessonPrepDocumentDO document, Integer attachmentCount) {
        LessonPrepDocumentPageItemView view = new LessonPrepDocumentPageItemView();
        view.setId(document.getId());
        view.setTitle(document.getTitle());
        view.setCourseName(document.getCourseName());
        view.setDocType(document.getDocType());
        view.setStatus(document.getStatus());
        view.setSummary(document.getSummary());
        view.setCreatedAt(document.getCreateTime());
        view.setUpdatedAt(document.getUpdateTime());
        view.setPublishedAt(document.getPublishedAt());
        view.setAttachmentCount(attachmentCount == null ? 0 : attachmentCount);
        return view;
    }

    private LessonPrepDocumentDetailView toDetailView(LessonPrepDocumentDO document, List<LessonPrepDocumentFileView> attachments) {
        LessonPrepDocumentDetailView view = new LessonPrepDocumentDetailView();
        view.setId(document.getId());
        view.setTitle(document.getTitle());
        view.setCourseId(document.getCourseId());
        view.setCourseName(document.getCourseName());
        view.setDocType(document.getDocType());
        view.setStatus(document.getStatus());
        view.setSummary(document.getSummary());
        view.setSourceType(document.getSourceType());
        view.setSourceDocumentId(document.getSourceDocumentId());
        view.setContentType(document.getContentType());
        view.setContentText(document.getContentText());
        view.setCreatedAt(document.getCreateTime());
        view.setUpdatedAt(document.getUpdateTime());
        view.setPublishedAt(document.getPublishedAt());
        view.setLastEditedAt(document.getLastEditedAt());
        view.setAttachmentCount(attachments.size());
        view.setAttachments(attachments.stream()
                .sorted(Comparator.comparing(LessonPrepDocumentFileView::getSortNo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(LessonPrepDocumentFileView::getFileId))
                .toList());
        return view;
    }

    private long normalizePageNo(Long pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    private long normalizePageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private String normalizeUpper(String value) {
        return trimToNull(value) == null ? "" : value.trim().toUpperCase();
    }

    private String normalizeContentText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n').strip();
        return normalized.isEmpty() ? null : normalized;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
