package com.bddk.geocourse.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "geocourse.infrastructure")
public class InfrastructureProperties {

    private MysqlProperties mysql = new MysqlProperties();

    private RedisProperties redis = new RedisProperties();

    private MinioProperties minio = new MinioProperties();

    @Data
    public static class MysqlProperties {

        private String host;

        private Integer port;

        private String database;

        private String username;

        private String password;

    }

    @Data
    public static class RedisProperties {

        private String host;

        private Integer port;

        private String password;

        private Integer database;

    }

    @Data
    public static class MinioProperties {

        private String endpoint;

        private String consoleEndpoint;

        private String accessKey;

        private String secretKey;

        private String bucket;

        private String publicUrl;

    }

}

