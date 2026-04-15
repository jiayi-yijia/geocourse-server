# geocourse-server 单体架构落地说明

## 1. 设计原则

- 采用“模块化单体 + 事件驱动”作为当前阶段的主架构。
- 借鉴 `ruoyi-vue-pro` 的做法，把“启动容器”和“业务模块”分离，但保留单应用部署。
- 业务强耦合部分先放在同一个 Spring Boot 工程里，AI、实时课堂、搜索、媒体存储按能力边界解耦。

## 2. 当前代码组织

```text
com.atguigu.geocourse
├─ GeocourseServerApplication
├─ framework
│  ├─ common
│  ├─ config
│  ├─ security
│  ├─ tenant
│  └─ web
└─ module
   ├─ architecture
   ├─ system
   ├─ identity
   ├─ organization
   ├─ course
   ├─ classroom
   ├─ assignment
   ├─ questionbank
   ├─ discussion
   ├─ ai
   ├─ analytics
   ├─ file
   └─ notification
```

## 3. 对齐设计文档后的模块职责

| 模块 | 职责 | 关键中间件 |
| --- | --- | --- |
| `system` | 系统配置、审计、运维接口、监控接入 | Prometheus、Grafana |
| `identity` | 登录认证、租户、Token、验证码、第三方认证 | Redis、Security |
| `organization` | 学校、班级、教师学生关系 | MySQL |
| `course` | 课程、章节、知识点、资源、学习进度 | MySQL、MinIO、ES |
| `classroom` | 课堂互动、签到、回放、课堂行为 | RabbitMQ、WebSocket |
| `assignment` | 作业、练习、判分、错题 | MySQL、RabbitMQ |
| `questionbank` | 题库、组卷、知识点挂接 | MySQL、ES |
| `discussion` | 讨论、协同研课、资源分享 | MySQL、RabbitMQ |
| `ai` | AI 学伴、教案、总结、评分、Prompt 管理 | RabbitMQ、PGVector、ES |
| `analytics` | 学习画像、课堂报告、教学看板 | MySQL、Redis、ES |
| `file` | 文件上传、对象存储、媒体管理 | MinIO |
| `notification` | 站内信、提醒、异步通知 | RabbitMQ、Redis |

## 4. 配置分层

- `application.yml`
  作用：统一端口、OpenAPI、MyBatis-Plus、基础 Spring 配置。
- `application-local.yml`
  作用：直接对接你当前服务器 `192.168.200.129` 上的中间件。
- `application-dev.yml`
  作用：开发环境通过环境变量覆盖。
- `application-prod.yml`
  作用：生产环境全部走环境变量。

## 5. 已接入的服务器资源

| 组件 | 配置位置 | 说明 |
| --- | --- | --- |
| MySQL | `application-local.yml` | 默认库名为 `geocourse`，需要手动创建 |
| Redis | `application-local.yml` | 用于缓存和验证码 |
| RabbitMQ | `application-local.yml` | AMQP 端口使用 `5672`，基于默认端口推断 |
| Elasticsearch | `application-local.yml` | 作为资源与题库搜索入口 |
| MinIO | `application-local.yml` | 对象存储主入口 |
| PGVector | `application-local.yml` | 作为 AI 知识检索存储 |
| Prometheus/Grafana | `application-local.yml` | 可观测预留 |

## 6. 预留接口

- `GET /admin-api/system/ping`
  用于基础连通性验证。
- `GET /admin-api/architecture/overview`
  用于查看当前后端架构总览。
- `GET /admin-api/architecture/modules`
  用于查看模块规划。
- `GET /admin-api/architecture/infrastructure`
  用于查看中间件接入信息。

## 7. 建议下一步

1. 先落第一期底座表：租户、用户、角色、班级、文件。
2. 以 `identity`、`organization`、`file` 三个模块优先开发。
3. 第二阶段再补 `course`、`classroom`、`assignment` 的业务实体与接口。
4. AI 模块先做异步任务编排，不直接耦合主链路。
