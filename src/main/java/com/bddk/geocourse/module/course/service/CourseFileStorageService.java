package com.bddk.geocourse.module.course.service;

import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.framework.config.InfrastructureProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class CourseFileStorageService {

    private static final Path LEGACY_UPLOAD_ROOT = Path.of(
            System.getProperty("java.io.tmpdir"),
            "geocourse-uploads"
    ).toAbsolutePath().normalize();

    private final MinioClient minioClient;
    private final InfrastructureProperties.MinioProperties properties;

    public CourseFileStorageService(MinioClient minioClient, InfrastructureProperties infrastructureProperties) {
        this.minioClient = minioClient;
        this.properties = infrastructureProperties.getMinio();
    }

    public StoredResource store(String category, String resourceType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            ensureBucketExists();
            String objectKey = buildObjectKey(category, resourceType, file.getOriginalFilename());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(resolveContentType(file))
                    .build());
            return new StoredResource(buildPublicUrl(objectKey), "minio", objectKey, null);
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "保存课程文件失败");
        }
    }

    public void deleteByObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ignored) {
            // Ignore delete failures so record cleanup can continue.
        }
    }

    public void deleteByUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        String objectKey = extractObjectKey(url);
        if (objectKey != null) {
            deleteByObjectKey(objectKey);
            return;
        }
        deleteLegacyLocalByUrl(url);
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(properties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
        }
    }

    private String buildObjectKey(String category, String resourceType, String originalName) {
        LocalDate today = LocalDate.now();
        String folder = today.format(DateTimeFormatter.ofPattern("yyyy/MMdd"));
        String ext = "";
        if (originalName != null) {
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                ext = originalName.substring(dotIndex);
            }
        }
        return "courses/" + category + "/" + folder + "/"
                + resourceType + "-" + UUID.randomUUID().toString().replace("-", "") + ext;
    }

    private String buildPublicUrl(String objectKey) {
        String baseUrl = StringUtils.hasText(properties.getPublicUrl())
                ? properties.getPublicUrl().trim()
                : properties.getEndpoint().trim() + "/" + properties.getBucket();
        return stripTrailingSlash(baseUrl) + "/" + objectKey;
    }

    private String resolveContentType(MultipartFile file) {
        if (StringUtils.hasText(file.getContentType())) {
            return file.getContentType();
        }
        return "application/octet-stream";
    }

    private String extractObjectKey(String url) {
        String[] candidatePrefixes = new String[] {
                stripTrailingSlash(properties.getPublicUrl()),
                stripTrailingSlash(properties.getEndpoint()) + "/" + properties.getBucket()
        };
        for (String prefix : candidatePrefixes) {
            if (StringUtils.hasText(prefix) && url.startsWith(prefix + "/")) {
                return url.substring((prefix + "/").length());
            }
        }
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            String bucketPrefix = "/" + properties.getBucket() + "/";
            if (path != null && path.startsWith(bucketPrefix)) {
                return path.substring(bucketPrefix.length());
            }
        } catch (IllegalArgumentException ignored) {
            // Ignore invalid URL and try legacy path cleanup below.
        }
        return null;
    }

    private void deleteLegacyLocalByUrl(String url) {
        if (!url.startsWith("/uploads/")) {
            return;
        }
        try {
            String relative = url.substring("/uploads/".length());
            Path target = LEGACY_UPLOAD_ROOT.resolve(relative).normalize();
            if (target.startsWith(LEGACY_UPLOAD_ROOT)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ignored) {
            // Ignore missing legacy files.
        }
    }

    private String stripTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
