package com.bddk.geocourse.module.lessonprep.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import com.bddk.geocourse.module.lessonprep.dal.dataobject.LessonPrepFileDO;
import com.bddk.geocourse.module.lessonprep.dal.mapper.LessonPrepFileMapper;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepAttachmentImportRequest;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepAttachmentImportView;
import com.bddk.geocourse.module.lessonprep.service.LessonPrepImportService;
import com.bddk.geocourse.module.lessonprep.service.LessonPrepStorageService;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class LessonPrepImportServiceImpl implements LessonPrepImportService {

    private static final List<String> SUPPORTED_IMPORT_EXTENSIONS = List.of("docx", "md", "txt");

    private final TeacherPortalContextService teacherPortalContextService;
    private final LessonPrepFileMapper lessonPrepFileMapper;
    private final LessonPrepStorageService lessonPrepStorageService;

    public LessonPrepImportServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                       LessonPrepFileMapper lessonPrepFileMapper,
                                       LessonPrepStorageService lessonPrepStorageService) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.lessonPrepFileMapper = lessonPrepFileMapper;
        this.lessonPrepStorageService = lessonPrepStorageService;
    }

    @Override
    public LessonPrepAttachmentImportView importFromAttachment(LessonPrepAttachmentImportRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        LessonPrepFileDO file = lessonPrepFileMapper.selectOne(Wrappers.<LessonPrepFileDO>lambdaQuery()
                .eq(LessonPrepFileDO::getTenantId, tenantId)
                .eq(LessonPrepFileDO::getUploaderId, teacherId)
                .eq(LessonPrepFileDO::getId, request.getFileId())
                .last("limit 1"));
        if (file == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "附件不存在");
        }

        String extension = normalizeExtension(file.getFileExt(), file.getFileName());
        if (!SUPPORTED_IMPORT_EXTENSIONS.contains(extension)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "当前仅支持 docx、md、txt 附件导入");
        }

        byte[] bytes = lessonPrepStorageService.readBytes(file.getStoragePath());
        String rawText = switch (extension) {
            case "docx" -> extractDocxText(bytes);
            case "md", "txt" -> new String(bytes, StandardCharsets.UTF_8);
            default -> "";
        };
        String normalizedText = normalizeText(rawText);
        if (!StringUtils.hasText(normalizedText)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "附件中未识别到可导入文本");
        }

        String guessedTitle = guessTitle(normalizedText, file.getFileName());
        String markdownContent = toMarkdown(normalizedText, guessedTitle, "md".equals(extension));
        LessonPrepAttachmentImportView view = new LessonPrepAttachmentImportView();
        view.setFileId(file.getId());
        view.setFileName(file.getFileName());
        view.setDetectedFileType(extension.toUpperCase(Locale.ROOT));
        view.setTitle(guessedTitle);
        view.setCourseName(guessCourseName(file.getFileName()));
        view.setDocType(StringUtils.hasText(request.getDocType()) ? request.getDocType().trim() : "LESSON_PLAN");
        view.setSummary(buildSummary(normalizedText, guessedTitle));
        view.setContentType("MARKDOWN");
        view.setContentText(markdownContent);
        view.setWarnings(buildWarnings(extension));
        return view;
    }

    private String extractDocxText(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "DOCX 附件解析失败");
        }
    }

    private String normalizeExtension(String fileExt, String fileName) {
        if (StringUtils.hasText(fileExt)) {
            return fileExt.trim().toLowerCase(Locale.ROOT);
        }
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return "";
        }
        return rawText.replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }

    private String guessTitle(String text, String fileName) {
        for (String line : text.split("\n")) {
            String cleaned = stripMarkdownHeading(line);
            if (StringUtils.hasText(cleaned)) {
                return cleaned;
            }
        }
        if (!StringUtils.hasText(fileName)) {
            return "导入教案";
        }
        int index = fileName.lastIndexOf('.');
        return index > 0 ? fileName.substring(0, index) : fileName;
    }

    private String guessCourseName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        String baseName = fileName;
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        String[] segments = baseName.split("[-_\\s]+");
        if (segments.length >= 2 && StringUtils.hasText(segments[0])) {
            return segments[0].trim();
        }
        return "";
    }

    private String toMarkdown(String rawText, String title, boolean sourceMarkdown) {
        if (sourceMarkdown) {
            String normalized = rawText.trim();
            if (normalized.startsWith("#")) {
                return normalized;
            }
        }
        List<String> lines = Arrays.stream(rawText.split("\n"))
                .map(String::trim)
                .toList();
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(title)) {
            builder.append("# ").append(title.trim()).append("\n\n");
        }
        boolean skippedTitleLine = false;
        boolean previousBlank = true;
        for (String line : lines) {
            String cleaned = stripMarkdownHeading(line);
            if (!skippedTitleLine && StringUtils.hasText(title) && cleaned.equals(title.trim())) {
                skippedTitleLine = true;
                continue;
            }
            if (!StringUtils.hasText(line)) {
                if (!previousBlank) {
                    builder.append('\n');
                }
                previousBlank = true;
                continue;
            }
            builder.append(line).append("\n\n");
            previousBlank = false;
        }
        return builder.toString().trim();
    }

    private String buildSummary(String text, String title) {
        String plain = text.replace("\n", " ").replaceAll("\\s+", " ").trim();
        if (StringUtils.hasText(title) && plain.startsWith(title)) {
            plain = plain.substring(title.length()).trim();
        }
        if (!StringUtils.hasText(plain)) {
            return "";
        }
        return plain.length() > 120 ? plain.substring(0, 120) + "..." : plain;
    }

    private List<String> buildWarnings(String extension) {
        List<String> warnings = new ArrayList<>();
        if ("docx".equals(extension)) {
            warnings.add("DOCX 导入优先抽取文本内容，图片与复杂表格不会完整还原");
        }
        if ("txt".equals(extension)) {
            warnings.add("TXT 仅保留纯文本内容，原始层级结构需要人工调整");
        }
        return warnings;
    }

    private String stripMarkdownHeading(String line) {
        if (!StringUtils.hasText(line)) {
            return "";
        }
        return line.replaceFirst("^#+\\s*", "").trim();
    }
}
