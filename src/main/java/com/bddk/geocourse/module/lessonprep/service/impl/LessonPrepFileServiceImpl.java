package com.bddk.geocourse.module.lessonprep.service.impl;

import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import com.bddk.geocourse.module.lessonprep.dal.dataobject.LessonPrepFileDO;
import com.bddk.geocourse.module.lessonprep.dal.mapper.LessonPrepFileMapper;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepFileUploadView;
import com.bddk.geocourse.module.lessonprep.service.LessonPrepFileService;
import com.bddk.geocourse.module.lessonprep.service.LessonPrepStorageService;
import com.bddk.geocourse.module.lessonprep.service.LessonPrepStoredObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
public class LessonPrepFileServiceImpl implements LessonPrepFileService {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "doc", "docx", "md", "txt", "pdf", "ppt", "pptx", "xls", "xlsx", "zip", "rar",
            "jpg", "jpeg", "png", "gif"
    );

    private final TeacherPortalContextService teacherPortalContextService;
    private final LessonPrepFileMapper lessonPrepFileMapper;
    private final LessonPrepStorageService lessonPrepStorageService;

    public LessonPrepFileServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                     LessonPrepFileMapper lessonPrepFileMapper,
                                     LessonPrepStorageService lessonPrepStorageService) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.lessonPrepFileMapper = lessonPrepFileMapper;
        this.lessonPrepStorageService = lessonPrepStorageService;
    }

    @Override
    @Transactional
    public LessonPrepFileUploadView upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "请选择要上传的附件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "附件大小不能超过 50MB");
        }
        String originalName = trimToNull(file.getOriginalFilename());
        if (!StringUtils.hasText(originalName)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "附件文件名不能为空");
        }
        String extension = extractExtension(originalName);
        if (!StringUtils.hasText(extension) || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "当前附件格式不支持上传");
        }

        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        LessonPrepStoredObject storedObject = lessonPrepStorageService.store(file, "attachments");

        LessonPrepFileDO dataObject = new LessonPrepFileDO();
        dataObject.setTenantId(tenantId);
        dataObject.setUploaderId(teacherId);
        dataObject.setFileName(originalName);
        dataObject.setFileExt(extension);
        dataObject.setFileSize(file.getSize());
        dataObject.setContentType(trimToNull(file.getContentType()));
        dataObject.setAccessUrl(storedObject.accessUrl());
        dataObject.setStoragePath(storedObject.storagePath());
        dataObject.setStorageType(storedObject.storageType());
        dataObject.setStatus(1);
        dataObject.setCreateBy(teacherId);
        dataObject.setUpdateBy(teacherId);
        dataObject.setCreateTime(LocalDateTime.now());
        dataObject.setUpdateTime(LocalDateTime.now());
        lessonPrepFileMapper.insert(dataObject);

        LessonPrepFileUploadView view = new LessonPrepFileUploadView();
        view.setFileId(dataObject.getId());
        view.setFileName(dataObject.getFileName());
        view.setFileExt(dataObject.getFileExt());
        view.setFileSize(dataObject.getFileSize());
        view.setContentType(dataObject.getContentType());
        view.setAccessUrl(dataObject.getAccessUrl());
        view.setStoragePath(dataObject.getStoragePath());
        view.setStorageType(dataObject.getStorageType());
        return view;
    }

    private String extractExtension(String originalName) {
        int index = originalName.lastIndexOf('.');
        if (index < 0 || index == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
