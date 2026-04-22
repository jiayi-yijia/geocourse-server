package com.bddk.geocourse.module.lessonprep.service;

import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.framework.config.InfrastructureProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class LessonPrepStorageService {

    private final MinioClient minioClient;
    private final InfrastructureProperties.MinioProperties properties;

    public LessonPrepStorageService(MinioClient minioClient, InfrastructureProperties infrastructureProperties) {
        this.minioClient = minioClient;
        this.properties = infrastructureProperties.getMinio();
    }

    public LessonPrepStoredObject store(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "上传文件不能为空");
        }
        try {
            ensureBucketExists();
            String objectKey = buildObjectKey(prefix, file.getOriginalFilename());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(resolveContentType(file))
                    .build());
            return new LessonPrepStoredObject(buildPublicUrl(objectKey), objectKey, "minio");
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "保存附件失败");
        }
    }

    public byte[] readBytes(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "附件存储路径为空");
        }
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(objectKey)
                .build());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "读取附件失败");
        }
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

    private String buildObjectKey(String prefix, String originalName) {
        LocalDate today = LocalDate.now();
        String folder = today.format(DateTimeFormatter.ofPattern("yyyy/MMdd"));
        String ext = "";
        if (StringUtils.hasText(originalName)) {
            int index = originalName.lastIndexOf('.');
            if (index >= 0) {
                ext = originalName.substring(index);
            }
        }
        return "lesson-prep/" + prefix + "/" + folder + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
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
