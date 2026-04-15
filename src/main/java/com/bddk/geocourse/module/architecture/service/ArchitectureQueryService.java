package com.bddk.geocourse.module.architecture.service;

import com.bddk.geocourse.framework.config.GeocourseInfoProperties;
import com.bddk.geocourse.framework.config.InfrastructureProperties;
import com.bddk.geocourse.module.architecture.model.ArchitectureOverview;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ArchitectureQueryService {

    private final GeocourseInfoProperties infoProperties;

    private final InfrastructureProperties infrastructureProperties;

    private final Environment environment;

    public ArchitectureQueryService(GeocourseInfoProperties infoProperties,
                                    InfrastructureProperties infrastructureProperties,
                                    Environment environment) {
        this.infoProperties = infoProperties;
        this.infrastructureProperties = infrastructureProperties;
        this.environment = environment;
    }

    public ArchitectureOverview getOverview() {
        return new ArchitectureOverview(
                infoProperties.getName(),
                infoProperties.getVersion(),
                "模块化单体",
                "Spring Boot 单体应用 + 领域模块分包 + 基础设施适配层",
                "共享数据库共享表 + tenant_id 隔离 + 预留跨租户能力",
                Arrays.asList(environment.getActiveProfiles()),
                buildModules(),
                buildInfrastructure(),
                buildRoadmap()
        );
    }

    public List<ArchitectureOverview.ArchitectureModule> getModules() {
        return buildModules();
    }

    public List<ArchitectureOverview.InfrastructureNode> getInfrastructure() {
        return buildInfrastructure();
    }

    private List<ArchitectureOverview.ArchitectureModule> buildModules() {
        return List.of(
                new ArchitectureOverview.ArchitectureModule("system", "平台底座", "系统配置、监控、审计日志和技术约定",
                        List.of("module.system.controller", "module.system.service", "module.system.dal"),
                        List.of("健康检查", "系统参数", "审计日志", "运维入口"),
                        List.of("Prometheus", "Grafana", "OpenAPI")),
                new ArchitectureOverview.ArchitectureModule("identity", "认证与租户", "登录认证、Token、验证码、租户隔离和第三方身份接入",
                        List.of("module.identity.controller", "module.identity.service", "module.identity.dal"),
                        List.of("账号密码登录", "租户识别", "角色权限", "登录风控"),
                        List.of("Redis", "Spring Security", "RabbitMQ")),
                new ArchitectureOverview.ArchitectureModule("organization", "用户与组织", "学校、班级、教师学生关系和组织架构",
                        List.of("module.organization.controller", "module.organization.service", "module.organization.dal"),
                        List.of("学校管理", "班级管理", "教师班级关系", "学生班级关系"),
                        List.of("MySQL")),
                new ArchitectureOverview.ArchitectureModule("course", "课程与资源", "课程、章节、知识点、课件、视频和学习进度",
                        List.of("module.course.controller", "module.course.service", "module.course.dal"),
                        List.of("课程目录", "资源标签", "学习进度", "发布管理"),
                        List.of("MySQL", "MinIO", "Elasticsearch")),
                new ArchitectureOverview.ArchitectureModule("classroom", "课堂互动", "开课、签到、互动答题、回放和课堂行为沉淀",
                        List.of("module.classroom.controller", "module.classroom.service", "module.classroom.dal"),
                        List.of("实时课堂", "签到互动", "回放记录", "课堂转写"),
                        List.of("RabbitMQ", "WebSocket", "MinIO")),
                new ArchitectureOverview.ArchitectureModule("assignment", "作业与练习", "作业布置、自测练习、判分和错题沉淀",
                        List.of("module.assignment.controller", "module.assignment.service", "module.assignment.dal"),
                        List.of("作业发布", "练习作答", "自动判分", "错题本"),
                        List.of("MySQL", "RabbitMQ")),
                new ArchitectureOverview.ArchitectureModule("questionbank", "题库", "题目分类、知识点映射、难度标签和组卷",
                        List.of("module.questionbank.controller", "module.questionbank.service", "module.questionbank.dal"),
                        List.of("题目管理", "知识点关联", "难度标签", "组卷"),
                        List.of("MySQL", "Elasticsearch")),
                new ArchitectureOverview.ArchitectureModule("discussion", "讨论与教研", "班级讨论、协同教研、教案共创和资源共享",
                        List.of("module.discussion.controller", "module.discussion.service", "module.discussion.dal"),
                        List.of("班级讨论", "教研协同", "评论点赞"),
                        List.of("MySQL", "RabbitMQ")),
                new ArchitectureOverview.ArchitectureModule("ai", "AI 编排", "AI 学伴、教案生成、课堂总结、智能评分和模型路由",
                        List.of("module.ai.controller", "module.ai.service", "module.ai.adapter"),
                        List.of("Prompt 管理", "AI 任务编排", "知识检索", "Token 统计"),
                        List.of("RabbitMQ", "PGVector", "Elasticsearch", "Redis")),
                new ArchitectureOverview.ArchitectureModule("analytics", "数据分析", "学习画像、课堂活跃度、知识点掌握度和教学报告",
                        List.of("module.analytics.controller", "module.analytics.service", "module.analytics.dal"),
                        List.of("课堂报告", "学习画像", "教学看板"),
                        List.of("MySQL", "Redis", "Elasticsearch")),
                new ArchitectureOverview.ArchitectureModule("file", "文件与媒体", "文件上传、对象存储、文档预览和视频资源管理",
                        List.of("module.file.controller", "module.file.service", "module.file.adapter"),
                        List.of("文件上传", "媒体存储", "预览管理"),
                        List.of("MinIO")),
                new ArchitectureOverview.ArchitectureModule("notification", "消息通知", "站内信、系统通知、作业提醒和 AI 任务回执",
                        List.of("module.notification.controller", "module.notification.service", "module.notification.dal"),
                        List.of("站内通知", "任务提醒", "课堂提醒"),
                        List.of("RabbitMQ", "Redis"))
        );
    }

    private List<ArchitectureOverview.InfrastructureNode> buildInfrastructure() {
        InfrastructureProperties.MysqlProperties mysql = infrastructureProperties.getMysql();
        InfrastructureProperties.RedisProperties redis = infrastructureProperties.getRedis();
        InfrastructureProperties.RabbitmqProperties rabbitmq = infrastructureProperties.getRabbitmq();
        InfrastructureProperties.ElasticsearchProperties elasticsearch = infrastructureProperties.getElasticsearch();
        InfrastructureProperties.MinioProperties minio = infrastructureProperties.getMinio();
        InfrastructureProperties.MonitoringProperties monitoring = infrastructureProperties.getMonitoring();
        InfrastructureProperties.PgvectorProperties pgvector = infrastructureProperties.getPgvector();

        return List.of(
                new ArchitectureOverview.InfrastructureNode("MySQL",
                        "jdbc:mysql://%s:%s/%s".formatted(mysql.getHost(), mysql.getPort(), mysql.getDatabase()),
                        "核心业务库，承载平台底座和教学主交易数据", "默认库名 geocourse，需要提前创建"),
                new ArchitectureOverview.InfrastructureNode("Redis",
                        "redis://%s:%s/%s".formatted(redis.getHost(), redis.getPort(), redis.getDatabase()),
                        "缓存、验证码、登录态和热点数据", "已按单体项目预留缓存层"),
                new ArchitectureOverview.InfrastructureNode("RabbitMQ",
                        "%s:%s".formatted(rabbitmq.getHost(), rabbitmq.getPort()),
                        "AI 任务、通知、课堂事件和报表生成异步化", "AMQP 端口通常为 5672"),
                new ArchitectureOverview.InfrastructureNode("RabbitMQ Console",
                        "http://%s:%s".formatted(rabbitmq.getHost(), rabbitmq.getManagementPort()),
                        "队列观测和消费排障", "使用管理端口"),
                new ArchitectureOverview.InfrastructureNode("Elasticsearch",
                        "%s://%s:%s".formatted(elasticsearch.getSchema(), elasticsearch.getHost(), elasticsearch.getPort()),
                        "课程资源、题库、讨论内容和知识点搜索", "后续可兼容混合检索"),
                new ArchitectureOverview.InfrastructureNode("MinIO", minio.getEndpoint(),
                        "课件、图片、录屏和文档对象存储", "默认桶 geocourse 已写入配置"),
                new ArchitectureOverview.InfrastructureNode("MinIO Console", minio.getConsoleEndpoint(),
                        "对象存储管理控制台", "便于运维和资源核对"),
                new ArchitectureOverview.InfrastructureNode("Prometheus", monitoring.getPrometheusUrl(),
                        "指标采集", "项目已暴露 prometheus 端点"),
                new ArchitectureOverview.InfrastructureNode("Grafana", monitoring.getGrafanaUrl(),
                        "监控可视化", "可直接接 Prometheus 做服务看板"),
                new ArchitectureOverview.InfrastructureNode("PGVector",
                        "postgresql://%s:%s/%s".formatted(pgvector.getHost(), pgvector.getPort(), pgvector.getDatabase()),
                        "AI 知识库向量检索和教学资料召回", "作为 AI 模块独立存储保留")
        );
    }

    private List<ArchitectureOverview.DeliveryPhase> buildRoadmap() {
        return List.of(
                new ArchitectureOverview.DeliveryPhase(1, "平台底座", List.of("identity", "organization", "file", "system")),
                new ArchitectureOverview.DeliveryPhase(2, "核心教学", List.of("course", "classroom", "assignment", "questionbank", "discussion")),
                new ArchitectureOverview.DeliveryPhase(3, "AI 能力", List.of("ai")),
                new ArchitectureOverview.DeliveryPhase(4, "数据智能", List.of("analytics", "notification"))
        );
    }

}

