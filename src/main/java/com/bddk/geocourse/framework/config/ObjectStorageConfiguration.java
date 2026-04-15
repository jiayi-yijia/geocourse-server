package com.bddk.geocourse.framework.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ObjectStorageConfiguration {

    @Bean
    public MinioClient minioClient(InfrastructureProperties infrastructureProperties) {
        InfrastructureProperties.MinioProperties properties = infrastructureProperties.getMinio();
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
