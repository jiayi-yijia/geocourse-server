package com.bddk.geocourse.module.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.framework.tenant.TenantContextHolder;
import com.bddk.geocourse.module.course.dal.dataobject.CourseResourceDO;
import com.bddk.geocourse.module.course.dal.mapper.CourseResourceMapper;
import com.bddk.geocourse.module.course.model.CourseResourcePreviewPageView;
import com.bddk.geocourse.module.course.model.CourseResourcePreviewView;
import com.bddk.geocourse.module.course.model.CourseResourceStorageMetadata;
import com.bddk.geocourse.module.course.service.CourseFileStorageService;
import com.bddk.geocourse.module.course.service.CourseResourcePreviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CourseResourcePreviewServiceImpl implements CourseResourcePreviewService {

    private static final Path PREVIEW_ROOT = Path.of(
            System.getProperty("java.io.tmpdir"),
            "geocourse-course-preview"
    ).toAbsolutePath().normalize();

    private final CourseResourceMapper courseResourceMapper;
    private final CourseFileStorageService courseFileStorageService;
    private final ObjectMapper objectMapper;

    public CourseResourcePreviewServiceImpl(CourseResourceMapper courseResourceMapper,
                                            CourseFileStorageService courseFileStorageService,
                                            ObjectMapper objectMapper) {
        this.courseResourceMapper = courseResourceMapper;
        this.courseFileStorageService = courseFileStorageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public CourseResourcePreviewView getPreview(Long resourceId) {
        CourseResourceDO resource = getResourceOrThrow(resourceId);
        CourseResourceStorageMetadata metadata = parseMetadata(resource.getRemark());
        String extension = resolveExtension(resource, metadata);
        String contentType = resolveContentType(resource, metadata, extension);

        CourseResourcePreviewView view = new CourseResourcePreviewView();
        view.setResourceId(resource.getId());
        view.setTitle(resolveTitle(resource, metadata));
        view.setFileName(resolveFileName(resource, metadata));
        view.setContentType(contentType);
        view.setContentUrl("/courses/resources/" + resource.getId() + "/content");
        view.setOriginalUrl(resource.getResourceUrl());

        if (isPdf(extension, contentType)) {
            view.setPreviewType("pdf");
            view.setPageCount(1);
            return view;
        }
        if (isImage(extension, contentType)) {
            view.setPreviewType("image");
            view.setPageCount(1);
            return view;
        }
        if (isSlide(extension, contentType)) {
            List<Path> renderedSlides = ensureSlidesRendered(resource, metadata, extension);
            List<CourseResourcePreviewPageView> pages = new ArrayList<>();
            for (int index = 0; index < renderedSlides.size(); index++) {
                CourseResourcePreviewPageView pageView = new CourseResourcePreviewPageView();
                pageView.setPageNo(index + 1);
                pageView.setImageUrl("/courses/resources/" + resource.getId() + "/preview/slides/" + (index + 1));
                pages.add(pageView);
            }
            view.setPreviewType("slides");
            view.setPageCount(pages.size());
            view.setPages(pages);
            return view;
        }

        view.setPreviewType("external");
        view.setPageCount(0);
        return view;
    }

    @Override
    public Resource getInlineContent(Long resourceId) {
        CourseResourceDO resource = getResourceOrThrow(resourceId);
        CourseResourceStorageMetadata metadata = parseMetadata(resource.getRemark());
        byte[] bytes = courseFileStorageService.readBytes(resolveObjectKey(resource, metadata), resource.getResourceUrl());
        return new ByteArrayResource(bytes);
    }

    @Override
    public Resource getSlideImage(Long resourceId, Integer pageNo) {
        if (pageNo == null || pageNo <= 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "页码必须大于 0");
        }
        CourseResourceDO resource = getResourceOrThrow(resourceId);
        CourseResourceStorageMetadata metadata = parseMetadata(resource.getRemark());
        String extension = resolveExtension(resource, metadata);
        if (!isSlide(extension, resolveContentType(resource, metadata, extension))) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "当前资源不支持幻灯片预览");
        }

        List<Path> renderedSlides = ensureSlidesRendered(resource, metadata, extension);
        if (pageNo > renderedSlides.size()) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "预览页不存在");
        }
        Path target = renderedSlides.get(pageNo - 1);
        return new FileSystemResource(target);
    }

    private List<Path> ensureSlidesRendered(CourseResourceDO resource,
                                            CourseResourceStorageMetadata metadata,
                                            String extension) {
        try {
            Path renderDir = resolveRenderDir(resource);
            Path completedMarker = renderDir.resolve(".complete");
            if (!Files.exists(completedMarker)) {
                Files.createDirectories(renderDir);
                clearRenderDir(renderDir);
                byte[] bytes = courseFileStorageService.readBytes(resolveObjectKey(resource, metadata), resource.getResourceUrl());
                if ("ppt".equals(extension)) {
                    renderPptSlides(bytes, renderDir);
                } else {
                    renderPptxSlides(bytes, renderDir);
                }
                Files.writeString(completedMarker, "ok");
            }

            List<Path> pages = new ArrayList<>();
            int index = 1;
            while (true) {
                Path candidate = renderDir.resolve("slide-" + index + ".png");
                if (!Files.exists(candidate)) {
                    break;
                }
                pages.add(candidate);
                index += 1;
            }
            if (pages.isEmpty()) {
                throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "课件预览生成失败");
            }
            return pages;
        } catch (IOException ex) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "课件预览生成失败");
        }
    }

    private void renderPptxSlides(byte[] bytes, Path renderDir) throws IOException {
        try (XMLSlideShow slideShow = new XMLSlideShow(new ByteArrayInputStream(bytes))) {
            renderSlides(slideShow.getSlides(), slideShow.getPageSize(), renderDir);
        }
    }

    private void renderPptSlides(byte[] bytes, Path renderDir) throws IOException {
        try (HSLFSlideShow slideShow = new HSLFSlideShow(new ByteArrayInputStream(bytes))) {
            renderSlides(slideShow.getSlides(), slideShow.getPageSize(), renderDir);
        }
    }

    private void renderSlides(List<? extends Slide<?, ?>> slides, Dimension pageSize, Path renderDir) throws IOException {
        double scale = 1.8d;
        int width = Math.max((int) Math.round(pageSize.getWidth() * scale), 960);
        int height = Math.max((int) Math.round(pageSize.getHeight() * scale), 540);
        int pageNo = 1;
        for (Slide<?, ?> slide : slides) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, width, height);
                graphics.setTransform(AffineTransform.getScaleInstance(scale, scale));
                slide.draw(graphics);
            } finally {
                graphics.dispose();
            }
            ImageIO.write(image, "png", renderDir.resolve("slide-" + pageNo + ".png").toFile());
            pageNo += 1;
        }
    }

    private void clearRenderDir(Path renderDir) throws IOException {
        if (!Files.exists(renderDir)) {
            return;
        }
        try (var stream = Files.list(renderDir)) {
            stream.forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // ignore stale preview cleanup failure
                }
            });
        }
    }

    private Path resolveRenderDir(CourseResourceDO resource) {
        long version = resolveVersion(resource.getUpdateTime(), resource.getCreateTime());
        Path renderDir = PREVIEW_ROOT.resolve(resource.getId() + "-" + version).normalize();
        if (!renderDir.startsWith(PREVIEW_ROOT)) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "预览目录非法");
        }
        return renderDir;
    }

    private long resolveVersion(LocalDateTime updateTime, LocalDateTime createTime) {
        LocalDateTime reference = updateTime != null ? updateTime : createTime;
        if (reference == null) {
            return 0L;
        }
        return reference.toEpochSecond(ZoneOffset.UTC);
    }

    private CourseResourceDO getResourceOrThrow(Long resourceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new ServiceException(ErrorCode.TENANT_REQUIRED);
        }
        CourseResourceDO resource = courseResourceMapper.selectOne(new LambdaQueryWrapper<CourseResourceDO>()
                .eq(CourseResourceDO::getTenantId, tenantId)
                .eq(CourseResourceDO::getId, resourceId)
                .eq(CourseResourceDO::getStatus, 1)
                .last("limit 1"));
        if (resource == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return resource;
    }

    private CourseResourceStorageMetadata parseMetadata(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, CourseResourceStorageMetadata.class);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String resolveTitle(CourseResourceDO resource, CourseResourceStorageMetadata metadata) {
        if (metadata != null && StringUtils.hasText(metadata.getResourceTitle())) {
            return metadata.getResourceTitle().trim();
        }
        String fileName = resolveFileName(resource, metadata);
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private String resolveFileName(CourseResourceDO resource, CourseResourceStorageMetadata metadata) {
        if (metadata != null && StringUtils.hasText(metadata.getOriginalFileName())) {
            return metadata.getOriginalFileName().trim();
        }
        if (StringUtils.hasText(resource.getResourceName())) {
            return resource.getResourceName().trim();
        }
        String objectKey = resolveObjectKey(resource, metadata);
        if (StringUtils.hasText(objectKey) && objectKey.contains("/")) {
            return objectKey.substring(objectKey.lastIndexOf('/') + 1);
        }
        return "resource-" + resource.getId();
    }

    private String resolveObjectKey(CourseResourceDO resource, CourseResourceStorageMetadata metadata) {
        if (metadata != null && StringUtils.hasText(metadata.getObjectKey())) {
            return metadata.getObjectKey().trim();
        }
        return courseFileStorageService.extractObjectKey(resource.getResourceUrl());
    }

    private String resolveExtension(CourseResourceDO resource, CourseResourceStorageMetadata metadata) {
        String fileName = resolveFileName(resource, metadata);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String resolveContentType(CourseResourceDO resource, CourseResourceStorageMetadata metadata, String extension) {
        if (metadata != null && StringUtils.hasText(metadata.getContentType())) {
            return metadata.getContentType().trim();
        }
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "svg" -> "image/svg+xml";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/octet-stream";
        };
    }

    private boolean isPdf(String extension, String contentType) {
        return "pdf".equals(extension) || (contentType != null && contentType.contains("pdf"));
    }

    private boolean isImage(String extension, String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return true;
        }
        return switch (extension) {
            case "png", "jpg", "jpeg", "webp", "gif", "bmp", "svg" -> true;
            default -> false;
        };
    }

    private boolean isSlide(String extension, String contentType) {
        if ("ppt".equals(extension) || "pptx".equals(extension)) {
            return true;
        }
        return contentType != null
                && (contentType.contains("presentationml.presentation") || contentType.contains("ms-powerpoint"));
    }
}
