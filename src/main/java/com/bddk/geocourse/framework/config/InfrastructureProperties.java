package com.bddk.geocourse.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "geocourse.infrastructure")
public class InfrastructureProperties {

    private MysqlProperties mysql = new MysqlProperties();

    private RedisProperties redis = new RedisProperties();

    private RabbitmqProperties rabbitmq = new RabbitmqProperties();

    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();

    private MinioProperties minio = new MinioProperties();

    private VodProperties vod = new VodProperties();

    private MonitoringProperties monitoring = new MonitoringProperties();

    private PgvectorProperties pgvector = new PgvectorProperties();

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
    public static class RabbitmqProperties {

        private String host;

        private Integer port;

        private String username;

        private String password;

        private String virtualHost;

        private Integer managementPort;

    }

    @Data
    public static class ElasticsearchProperties {

        private String schema;

        private String host;

        private Integer port;

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

    @Data
    public static class VodProperties {

        private String secretId;

        private String secretKey;

        private String region;

        private Long subAppId;

        private String procedure;

    }

    @Data
    public static class MonitoringProperties {

        private String prometheusUrl;

        private String grafanaUrl;

        private String grafanaUsername;

    }

    @Data
    public static class PgvectorProperties {

        private String host;

        private Integer port;

        private String database;

        private String username;

        private String password;

    }

}

