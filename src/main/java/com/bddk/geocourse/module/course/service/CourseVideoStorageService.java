package com.bddk.geocourse.module.course.service;

import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.framework.config.InfrastructureProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CourseVideoStorageService {

    private static final String VOD_HOST = "vod.tencentcloudapi.com";
    private static final String VOD_VERSION = "2018-07-17";
    private static final String VOD_SERVICE = "vod";
    private static final String DELETE_MEDIA_ACTION = "DeleteMedia";
    private static final String CONTENT_TYPE = "application/json; charset=utf-8";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final InfrastructureProperties.VodProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public CourseVideoStorageService(InfrastructureProperties infrastructureProperties, ObjectMapper objectMapper) {
        this.properties = infrastructureProperties.getVod();
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public StoredResource store(String resourceType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        validateConfigured();
        Path tempFile = null;
        try {
            tempFile = createTempFile(resourceType, file.getOriginalFilename());
            file.transferTo(tempFile);

            VodUploadClient client = new VodUploadClient(properties.getSecretId(), properties.getSecretKey());
            VodUploadRequest request = new VodUploadRequest();
            request.setMediaFilePath(tempFile.toString());
            if (StringUtils.hasText(file.getOriginalFilename())) {
                request.setMediaName(file.getOriginalFilename());
            }
            if (properties.getSubAppId() != null) {
                request.setSubAppId(properties.getSubAppId());
            }
            if (StringUtils.hasText(properties.getProcedure())) {
                request.setProcedure(properties.getProcedure());
            }

            VodUploadResponse response = client.upload(properties.getRegion(), request);
            return new StoredResource(response.getMediaUrl(), "vod", null, response.getFileId());
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "上传视频到云点播失败");
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Ignore temp file cleanup failures.
                }
            }
        }
    }

    public void deleteByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return;
        }
        validateConfigured();
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("FileId", fileId);
            if (properties.getSubAppId() != null) {
                payload.put("SubAppId", properties.getSubAppId());
            }
            String body = objectMapper.writeValueAsString(payload);
            long timestamp = Instant.now().getEpochSecond();
            String authorization = buildAuthorization(DELETE_MEDIA_ACTION, body, timestamp);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create("https://" + VOD_HOST))
                    .header("Authorization", authorization)
                    .header("Content-Type", CONTENT_TYPE)
                    .header("Host", VOD_HOST)
                    .header("X-TC-Action", DELETE_MEDIA_ACTION)
                    .header("X-TC-Timestamp", String.valueOf(timestamp))
                    .header("X-TC-Version", VOD_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            if (StringUtils.hasText(properties.getRegion())) {
                requestBuilder.header("X-TC-Region", properties.getRegion());
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "删除云点播视频失败");
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode error = root.path("Response").path("Error");
            if (!error.isMissingNode() && !error.isEmpty()) {
                throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "删除云点播视频失败");
            }
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ignored) {
            // Ignore remote delete failures to keep course cleanup moving.
        }
    }

    private void validateConfigured() {
        if (!StringUtils.hasText(properties.getSecretId())
                || !StringUtils.hasText(properties.getSecretKey())
                || !StringUtils.hasText(properties.getRegion())) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "云点播配置不完整");
        }
    }

    private Path createTempFile(String resourceType, String originalFilename) throws IOException {
        String ext = "";
        if (StringUtils.hasText(originalFilename)) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                ext = originalFilename.substring(dotIndex);
            }
        }
        return Files.createTempFile("vod-" + resourceType + "-" + UUID.randomUUID(), ext);
    }

    private String buildAuthorization(String action, String body, long timestamp) throws Exception {
        String canonicalHeaders = "content-type:" + CONTENT_TYPE + "\n" + "host:" + VOD_HOST + "\n";
        String signedHeaders = "content-type;host";
        String hashedRequestPayload = sha256Hex(body);
        String canonicalRequest = "POST\n/\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;

        String date = DATE_FORMATTER.format(LocalDate.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC));
        String credentialScope = date + "/" + VOD_SERVICE + "/tc3_request";
        String stringToSign = "TC3-HMAC-SHA256\n"
                + timestamp + "\n"
                + credentialScope + "\n"
                + sha256Hex(canonicalRequest);

        byte[] secretDate = hmacSha256(("TC3" + properties.getSecretKey()).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, VOD_SERVICE);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));

        return "TC3-HMAC-SHA256 Credential=" + properties.getSecretId() + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders
                + ", Signature=" + signature;
    }

    private byte[] hmacSha256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, mac.getAlgorithm()));
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    private String sha256Hex(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return bytesToHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte current : bytes) {
            builder.append(Character.forDigit((current >> 4) & 0xf, 16));
            builder.append(Character.forDigit(current & 0xf, 16));
        }
        return builder.toString();
    }
}
