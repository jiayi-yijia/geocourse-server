/*
 Navicat Premium Dump SQL

 Source Server         : Geo
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : 192.168.200.129:3306
 Source Schema         : geocourse

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 22/04/2026 09:45:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_call_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_call_log`;
CREATE TABLE `ai_call_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NULL DEFAULT NULL,
  `scene` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `request_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `tenant_id` bigint NOT NULL,
  `operator_id` bigint NULL DEFAULT NULL,
  `success` tinyint NOT NULL DEFAULT 0,
  `latency_ms` bigint NULL DEFAULT NULL,
  `input_tokens` int NULL DEFAULT NULL,
  `output_tokens` int NULL DEFAULT NULL,
  `error_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_call_log_task`(`task_id` ASC) USING BTREE,
  INDEX `idx_ai_call_log_tenant_scene`(`tenant_id` ASC, `scene` ASC) USING BTREE,
  INDEX `idx_ai_call_log_created_at`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_call_log
-- ----------------------------

-- ----------------------------
-- Table structure for ai_task
-- ----------------------------
DROP TABLE IF EXISTS `ai_task`;
CREATE TABLE `ai_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `operator_id` bigint NOT NULL,
  `task_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `scene` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `request_payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `result_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `error_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `input_tokens` int NULL DEFAULT NULL,
  `output_tokens` int NULL DEFAULT NULL,
  `latency_ms` bigint NULL DEFAULT NULL,
  `retry_count` int NOT NULL DEFAULT 0,
  `max_retry_count` int NOT NULL DEFAULT 1,
  `queued_at` datetime NULL DEFAULT NULL,
  `started_at` datetime NULL DEFAULT NULL,
  `finished_at` datetime NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_task_tenant_operator`(`tenant_id` ASC, `operator_id` ASC) USING BTREE,
  INDEX `idx_ai_task_tenant_status`(`tenant_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_ai_task_tenant_scene`(`tenant_id` ASC, `scene` ASC) USING BTREE,
  INDEX `idx_ai_task_tenant_type`(`tenant_id` ASC, `task_type` ASC) USING BTREE,
  INDEX `idx_ai_task_queued_at`(`queued_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_task
-- ----------------------------
INSERT INTO `ai_task` VALUES (1, 1001, 3, 'TEXT_GENERATION', 'TEACHER_WORKBENCH_ASSISTANT', 'default', 'qwen-plus', 'SUCCESS', '{\"model\": \"qwen-plus\", \"scene\": \"TEACHER_WORKBENCH_ASSISTANT\", \"provider\": \"default\", \"userPrompt\": \"请为七年级地理《地球自转》设计一个 5 分钟课堂导入活动。\", \"systemPrompt\": \"你是地理教学系统中的教师 AI 教学助手。请使用中文回答，优先给出结构化、可直接落地的教学建议。\"}', '一、导入目标：用生活情境激活学生对昼夜更替的已有经验。二、导入活动：播放清晨与夜晚校园延时视频，让学生判断变化原因。三、教师追问：如果地球停止自转会怎样？四、过渡语：带着这个问题进入本节课的实验演示。', NULL, NULL, NULL, NULL, NULL, 0, 1, '2026-04-17 15:05:29', '2026-04-20 14:37:36', '2026-04-20 14:37:36', 3, '2026-04-17 15:05:29', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `ai_task` VALUES (2, 1001, 3, 'TEXT_GENERATION', 'TEACHER_WORKBENCH_ASSISTANT', 'default', 'qwen-plus', 'QUEUED', '{\"model\": \"qwen-plus\", \"scene\": \"TEACHER_WORKBENCH_ASSISTANT\", \"provider\": \"default\", \"userPrompt\": \"请生成一份八年级地理《季风气候与农业》分层作业。\", \"systemPrompt\": \"你是地理教学系统中的教师 AI 教学助手。请使用中文回答，优先给出结构化、可直接落地的教学建议。\"}', NULL, NULL, NULL, NULL, NULL, NULL, 0, 1, '2026-04-17 15:18:44', NULL, NULL, 3, '2026-04-17 15:18:44', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `ai_task` VALUES (3, 1001, 3, 'TEXT_GENERATION', 'TEACHER_WORKBENCH_ASSISTANT', 'default', 'qwen-plus', 'SUCCESS', '{\"model\": \"qwen-plus\", \"scene\": \"TEACHER_WORKBENCH_ASSISTANT\", \"provider\": \"default\", \"userPrompt\": \"请给出高中地理《自然灾害》主题研讨课的活动安排。\", \"systemPrompt\": \"你是地理教学系统中的教师 AI 教学助手。请使用中文回答，优先给出结构化、可直接落地的教学建议。\"}', '建议按“灾害识别、成因分析、避险方案、班级汇报”四个环节组织研讨。每组聚焦一种灾害，准备一张风险地图和一份校园避险清单，最后由教师归纳共性减灾策略。', NULL, NULL, NULL, NULL, NULL, 0, 1, '2026-04-17 15:20:55', '2026-04-20 14:37:36', '2026-04-20 14:37:36', 3, '2026-04-17 15:20:55', 3, '2026-04-20 14:37:36', 0);

-- ----------------------------
-- Table structure for assignment_exam_answer
-- ----------------------------
DROP TABLE IF EXISTS `assignment_exam_answer`;
CREATE TABLE `assignment_exam_answer`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `exam_record_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `sort_no` int NOT NULL DEFAULT 0,
  `question_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `question_score_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `answer_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `question_title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `standard_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `max_score` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `auto_score` decimal(10, 2) NULL DEFAULT NULL,
  `final_score` decimal(10, 2) NULL DEFAULT NULL,
  `score` decimal(10, 2) NULL DEFAULT NULL,
  `auto_correct_flag` tinyint NOT NULL DEFAULT 0,
  `correct_flag` tinyint NULL DEFAULT NULL,
  `ai_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `teacher_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `review_round` int NOT NULL DEFAULT 1,
  `reviewed_by` bigint NULL DEFAULT NULL,
  `reviewed_at` datetime NULL DEFAULT NULL,
  `time_spent_seconds` int NOT NULL DEFAULT 0,
  `snapshot_question_json` json NULL,
  `snapshot_standard_answer_json` json NULL,
  `reviewed` tinyint NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_exam_answer_tenant_record`(`tenant_id` ASC, `exam_record_id` ASC) USING BTREE,
  INDEX `idx_assignment_exam_answer_tenant_question`(`tenant_id` ASC, `question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_exam_answer
-- ----------------------------
INSERT INTO `assignment_exam_answer` VALUES (1, 1001, 1, 1, 0, 'CHOICE', NULL, NULL, '【测试】我国面积最大的省级行政区是下列哪一个？', 'B', 'B', 10.00, NULL, NULL, 10.00, 0, 1, '客观题自动判分正确。', NULL, 1, NULL, NULL, 0, NULL, NULL, 1, 4, '2026-04-15 10:01:48', 4, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (2, 1001, 1, 2, 0, 'JUDGE', NULL, NULL, '【测试】黄河最终注入渤海。', 'TRUE', 'TRUE', 5.00, NULL, NULL, 5.00, 0, 1, '客观题自动判分正确。', NULL, 1, NULL, NULL, 0, NULL, NULL, 1, 4, '2026-04-15 10:01:48', 4, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (3, 1001, 1, 3, 0, 'TEXT', NULL, NULL, '【测试】简述季风气候对我国农业生产的影响。', '季风气候使我国大部分地区雨热同期，有利于农作物生长；但降水年际变化大，易造成旱涝灾害，需要水利调节。', '季风带来充足热量和降水，利于种植业发展，但雨量不稳定也容易造成旱涝。', 15.00, NULL, NULL, NULL, 0, NULL, '主观题待教师批改。', NULL, 1, NULL, NULL, 0, NULL, NULL, 0, 4, '2026-04-15 10:01:48', 4, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (4, 1001, 2, 1, 0, 'CHOICE', NULL, NULL, '【测试】我国面积最大的省级行政区是下列哪一个？', 'B', 'A', 10.00, NULL, NULL, 0.00, 0, 0, '客观题自动判分错误。', '基础概念掌握不准确。', 1, NULL, NULL, 0, NULL, NULL, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (5, 1001, 2, 2, 0, 'JUDGE', NULL, NULL, '【测试】黄河最终注入渤海。', 'TRUE', 'TRUE', 5.00, NULL, NULL, 5.00, 0, 1, '客观题自动判分正确。', NULL, 1, NULL, NULL, 0, NULL, NULL, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (6, 1001, 2, 3, 0, 'TEXT', NULL, NULL, '【测试】简述季风气候对我国农业生产的影响。', '季风气候使我国大部分地区雨热同期，有利于农作物生长；但降水年际变化大，易造成旱涝灾害，需要水利调节。', '雨热同期利于农业，但夏季风不稳定会产生旱涝，对农业有双重影响。', 15.00, NULL, NULL, 12.00, 0, 2, '主观题已给出较完整要点。', '回答到了雨热同期和旱涝风险，得分较高。', 1, NULL, NULL, 0, NULL, NULL, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (7, 1001, 3, 6, 0, 'CHOICE', 'OBJECTIVE', 'AUTO_GRADED', '【演示考试】中国面积最大的省级行政区是哪个？', 'B', 'B', 10.00, 10.00, 10.00, 10.00, 1, 1, '自动判分正确', NULL, 1, NULL, NULL, 110, NULL, NULL, 1, 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (8, 1001, 3, 7, 1, 'JUDGE', 'OBJECTIVE', 'UNANSWERED', '【演示考试】黄河最终注入渤海。', 'TRUE', NULL, 5.00, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1, NULL, NULL, 15, NULL, NULL, 0, 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (9, 1001, 3, 8, 2, 'TEXT', 'SUBJECTIVE', 'ANSWERED', '【演示考试】简述季风气候对中国农业的一个影响。', '雨热同期有利于作物生长，但降水不稳定也会带来旱涝风险。', '季风气候为农业提供了较好的热量和水分条件，但也可能带来旱涝风险。', 15.00, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1, NULL, NULL, 360, NULL, NULL, 0, 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (10, 1001, 4, 6, 0, 'CHOICE', 'OBJECTIVE', 'AUTO_GRADED', '【演示考试】中国面积最大的省级行政区是哪个？', 'B', 'B', 10.00, 10.00, 10.00, 10.00, 1, 1, '自动判分正确', NULL, 1, NULL, NULL, 120, NULL, NULL, 1, 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (11, 1001, 4, 9, 1, 'CHOICE', 'OBJECTIVE', 'AUTO_GRADED', '【演示考试】尼罗河位于哪个洲？', 'A', 'B', 10.00, 0.00, 0.00, 0.00, 0, 0, '自动判分错误', NULL, 1, NULL, NULL, 95, NULL, NULL, 1, 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (12, 1001, 4, 8, 2, 'TEXT', 'SUBJECTIVE', 'ANSWERED', '【演示考试】简述季风气候对中国农业的一个影响。', '雨热同期有利于作物生长，但降水不稳定也会带来旱涝风险。', '季风气候有利于农业生产，但也会带来旱涝风险。', 10.00, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1, NULL, NULL, 420, NULL, NULL, 0, 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (13, 1001, 4, 10, 3, 'TEXT', 'SUBJECTIVE', 'ANSWERED', '【演示考试】请说明西欧海洋性气候形成的一个原因。', '盛行西风和北大西洋暖流把海洋湿润空气带向内陆。', '盛行西风把海洋湿润气流带入内陆。', 10.00, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1, NULL, NULL, 360, NULL, NULL, 0, 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (14, 1001, 5, 6, 0, 'CHOICE', 'OBJECTIVE', 'AUTO_GRADED', '【演示考试】中国面积最大的省级行政区是哪个？', 'B', 'B', 10.00, 10.00, 10.00, 10.00, 1, 1, '自动判分正确', NULL, 1, NULL, NULL, 120, NULL, NULL, 1, 2044339999999990001, '2026-04-16 13:11:25', 2044339999999990001, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (15, 1001, 5, 9, 1, 'CHOICE', 'OBJECTIVE', 'AUTO_GRADED', '【演示考试】尼罗河位于哪个洲？', 'A', 'A', 10.00, 10.00, 10.00, 10.00, 1, 1, '自动判分正确', NULL, 1, NULL, NULL, 100, NULL, NULL, 1, 2044339999999990001, '2026-04-16 13:11:25', 2044339999999990001, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (16, 1001, 5, 8, 2, 'TEXT', 'SUBJECTIVE', 'ANSWERED', '【演示考试】简述季风气候对中国农业的一个影响。', '雨热同期有利于作物生长，但降水不稳定也会带来旱涝风险。', '季风气候为农业提供了较好的热量和水分条件，但也可能带来灾害。', 10.00, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1, NULL, NULL, 380, NULL, NULL, 0, 2044339999999990001, '2026-04-16 13:11:25', 2044339999999990001, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (17, 1001, 5, 10, 3, 'TEXT', 'SUBJECTIVE', 'ANSWERED', '【演示考试】请说明西欧海洋性气候形成的一个原因。', '盛行西风和北大西洋暖流把海洋湿润空气带向内陆。', '北大西洋暖流和盛行西风共同影响，使海洋气流深入内陆。', 10.00, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1, NULL, NULL, 350, NULL, NULL, 0, 2044339999999990001, '2026-04-16 13:11:25', 2044339999999990001, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (18, 1001, 6, 6, 0, 'CHOICE', 'OBJECTIVE', 'REVIEWED', '【演示考试】中国面积最大的省级行政区是哪个？', 'B', 'B', 10.00, 10.00, 10.00, 10.00, 1, 1, '自动判分正确', '回答正确', 1, 3, '2026-04-13 13:11:25', 100, NULL, NULL, 1, 4, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (19, 1001, 6, 9, 1, 'CHOICE', 'OBJECTIVE', 'REVIEWED', '【演示考试】尼罗河位于哪个洲？', 'A', 'A', 10.00, 10.00, 10.00, 10.00, 1, 1, '自动判分正确', '回答正确', 1, 3, '2026-04-13 13:11:25', 90, NULL, NULL, 1, 4, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (20, 1001, 6, 8, 2, 'TEXT', 'SUBJECTIVE', 'REVIEWED', '【演示考试】简述季风气候对中国农业的一个影响。', '雨热同期有利于作物生长，但降水不稳定也会带来旱涝风险。', '季风气候有利于作物生长，但变化性也会带来旱涝风险。', 10.00, NULL, 6.00, 6.00, 0, 1, NULL, '要点不错，但展开略少。', 1, 3, '2026-04-13 13:11:25', 300, NULL, NULL, 1, 4, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (21, 1001, 6, 10, 3, 'TEXT', 'SUBJECTIVE', 'REVIEWED', '【演示考试】请说明西欧海洋性气候形成的一个原因。', '盛行西风和北大西洋暖流把海洋湿润空气带向内陆。', '北大西洋暖流和盛行西风共同影响，使海洋气流深入内陆。', 10.00, NULL, 8.00, 8.00, 0, 1, NULL, '表述清楚，原因正确。', 1, 3, '2026-04-13 13:11:25', 280, NULL, NULL, 1, 4, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (22, 1001, 7, 6, 0, 'CHOICE', 'OBJECTIVE', 'UNANSWERED', '【演示考试】中国面积最大的省级行政区是哪个？', 'B', NULL, 10.00, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1, NULL, NULL, 0, '{\"id\": 6, \"type\": \"CHOICE\", \"title\": \"[DEMO_EXAM] Which province has the largest area in China?\", \"choices\": [{\"key\": \"A\", \"text\": \"Tibet\", \"sortNo\": 0}, {\"key\": \"B\", \"text\": \"Xinjiang\", \"sortNo\": 1}, {\"key\": \"C\", \"text\": \"Qinghai\", \"sortNo\": 2}, {\"key\": \"D\", \"text\": \"Sichuan\", \"sortNo\": 3}], \"analysis\": \"Xinjiang is the largest provincial administrative region by area.\", \"multiSelect\": false, \"defaultScore\": 10}', '{\"answerText\": \"B\", \"gradingRule\": \"Single choice question; full score for exact match.\"}', 0, 4, '2026-04-16 13:56:07', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (23, 1001, 7, 7, 1, 'JUDGE', 'OBJECTIVE', 'UNANSWERED', '【演示考试】黄河最终注入渤海。', 'TRUE', NULL, 5.00, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1, NULL, NULL, 0, '{\"id\": 7, \"type\": \"JUDGE\", \"title\": \"[DEMO_EXAM] The Yellow River flows into the Bohai Sea.\", \"choices\": [], \"analysis\": \"It enters the Bohai Sea in Shandong.\", \"multiSelect\": false, \"defaultScore\": 5}', '{\"answerText\": \"TRUE\", \"gradingRule\": \"Judge question; TRUE/FALSE accepted.\"}', 0, 4, '2026-04-16 13:56:07', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (24, 1001, 7, 8, 2, 'TEXT', 'SUBJECTIVE', 'UNANSWERED', '【演示考试】简述季风气候对中国农业的一个影响。', '雨热同期有利于作物生长，但降水不稳定也会带来旱涝风险。', NULL, 15.00, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1, NULL, NULL, 0, '{\"id\": 8, \"type\": \"TEXT\", \"title\": \"[DEMO_EXAM] Briefly explain one impact of monsoon climate on agriculture in China.\", \"choices\": [], \"analysis\": \"Key points: rain-heat synchronization and drought/flood variability.\", \"multiSelect\": false, \"defaultScore\": 15}', '{\"answerText\": \"Rain-heat synchronization helps crops, but unstable rainfall can cause drought/flood risk.\", \"gradingRule\": \"Subjective question; evaluate key points and clarity.\"}', 0, 4, '2026-04-16 13:56:07', 4, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (25, 1001, 8, 6, 0, 'CHOICE', NULL, NULL, '【演示考试】中国面积最大的省级行政区是哪个？', 'B', 'A', 10.00, NULL, NULL, 0.00, 0, 0, 'Auto graded as incorrect', NULL, 1, NULL, NULL, 0, NULL, NULL, 1, 2044375386258702342, '2026-04-21 12:46:53', 3, '2026-04-21 13:01:15', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (26, 1001, 8, 9, 0, 'CHOICE', NULL, NULL, '【演示考试】尼罗河位于哪个洲？', 'A', 'D', 10.00, NULL, NULL, 0.00, 0, 0, 'Auto graded as incorrect', NULL, 1, NULL, NULL, 0, NULL, NULL, 1, 2044375386258702342, '2026-04-21 12:46:53', 3, '2026-04-21 13:01:15', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (27, 1001, 8, 8, 0, 'TEXT', NULL, NULL, '【演示考试】简述季风气候对中国农业的一个影响。', '雨热同期有利于作物生长，但降水不稳定也会带来旱涝风险。', '不知道', 10.00, NULL, NULL, 3.50, 0, 0, 'Pending teacher review', NULL, 1, NULL, NULL, 0, NULL, NULL, 1, 2044375386258702342, '2026-04-21 12:46:53', 3, '2026-04-21 13:01:15', 0, NULL);
INSERT INTO `assignment_exam_answer` VALUES (28, 1001, 8, 10, 0, 'TEXT', NULL, NULL, '【演示考试】请说明西欧海洋性气候形成的一个原因。', '盛行西风和北大西洋暖流把海洋湿润空气带向内陆。', '海洋', 10.00, NULL, NULL, 0.00, 0, NULL, 'Pending teacher review', NULL, 1, NULL, NULL, 0, NULL, NULL, 1, 2044375386258702342, '2026-04-21 12:46:53', 3, '2026-04-21 13:01:15', 0, NULL);

-- ----------------------------
-- Table structure for assignment_exam_event
-- ----------------------------
DROP TABLE IF EXISTS `assignment_exam_event`;
CREATE TABLE `assignment_exam_event`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `publish_id` bigint NULL DEFAULT NULL,
  `record_id` bigint NOT NULL,
  `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_time` datetime NOT NULL,
  `event_data_json` json NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_exam_event_tenant_record`(`tenant_id` ASC, `record_id` ASC) USING BTREE,
  INDEX `idx_assignment_exam_event_tenant_publish`(`tenant_id` ASC, `publish_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_exam_event
-- ----------------------------
INSERT INTO `assignment_exam_event` VALUES (1, 1001, 4, 3, 'ENTER', '2026-04-16 12:32:25', '{\"pageCode\": \"exam-enter\"}', 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, '学生');
INSERT INTO `assignment_exam_event` VALUES (2, 1001, 4, 3, 'HEARTBEAT', '2026-04-16 13:09:25', '{\"windowSwitches\": 2}', 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, '学生');
INSERT INTO `assignment_exam_event` VALUES (3, 1001, 5, 4, 'SUBMIT', '2026-04-16 11:11:25', '{\"submitType\": \"MANUAL\"}', 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, '学生');
INSERT INTO `assignment_exam_event` VALUES (4, 1001, 5, 5, 'REGRADE', '2026-04-16 12:46:25', '{\"reason\": \"教师认领后复核\"}', 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, '教师');
INSERT INTO `assignment_exam_event` VALUES (5, 1001, 6, 6, 'SUBMIT', '2026-04-12 13:11:25', '{\"submitType\": \"MANUAL\"}', 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, '学生');

-- ----------------------------
-- Table structure for assignment_exam_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `assignment_exam_operation_log`;
CREATE TABLE `assignment_exam_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `business_id` bigint NOT NULL,
  `operation_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `operator_id` bigint NULL DEFAULT NULL,
  `operation_time` datetime NOT NULL,
  `before_json` json NULL,
  `after_json` json NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_exam_operation_log_tenant_business`(`tenant_id` ASC, `business_type` ASC, `business_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_exam_operation_log
-- ----------------------------
INSERT INTO `assignment_exam_operation_log` VALUES (1, 1001, 'EXAM_PUBLISH', 6, 'PUBLISH_RESULTS', 3, '2026-04-16 11:11:25', '{\"status\": \"FINISHED\", \"resultPublishedAt\": null}', '{\"status\": \"FINISHED\", \"resultPublishedAt\": \"2026-04-16 11:11:25.000000\"}', 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, '{\"reason\": \"演示数据初始化\", \"operatorRole\": \"教师\"}');
INSERT INTO `assignment_exam_operation_log` VALUES (2, 1001, 'EXAM_RECORD', 5, 'REQUEST_REGRADE', 3, '2026-04-16 12:51:25', '{\"status\": \"PENDING_REVIEW\"}', '{\"status\": \"REVIEWING\"}', 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, '{\"reason\": \"演示数据初始化\", \"operatorRole\": \"教师\"}');
INSERT INTO `assignment_exam_operation_log` VALUES (3, 1001, 'EXAM_RECORD', 3, 'HEARTBEAT', 4, '2026-04-16 13:09:25', '{\"windowSwitches\": 1}', '{\"windowSwitches\": 2}', 4, '2026-04-16 13:11:25', 4, '2026-04-16 14:11:44', 0, '{\"reason\": \"演示数据初始化\", \"operatorRole\": \"学生\"}');

-- ----------------------------
-- Table structure for assignment_exam_publish
-- ----------------------------
DROP TABLE IF EXISTS `assignment_exam_publish`;
CREATE TABLE `assignment_exam_publish`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `paper_id` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `duration_minutes` int NOT NULL DEFAULT 0,
  `pass_score` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `total_score` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `question_count` int NOT NULL DEFAULT 0,
  `late_entry_allowed` tinyint NOT NULL DEFAULT 0,
  `late_entry_minutes` int NOT NULL DEFAULT 0,
  `reentry_allowed` tinyint NOT NULL DEFAULT 0,
  `max_reentry_count` int NOT NULL DEFAULT 0,
  `auto_submit_enabled` tinyint NOT NULL DEFAULT 1,
  `score_visible_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AFTER_REVIEW',
  `answer_visible_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AFTER_REVIEW',
  `anti_cheat_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `max_window_switches` int NOT NULL DEFAULT 0,
  `access_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `publish_scope_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ALL',
  `grading_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MIXED',
  `result_published_at` datetime NULL DEFAULT NULL,
  `archived_at` datetime NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_exam_publish_tenant_status`(`tenant_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_assignment_exam_publish_tenant_paper`(`tenant_id` ASC, `paper_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_exam_publish
-- ----------------------------
INSERT INTO `assignment_exam_publish` VALUES (1, 1001, 1, '【测试】中国地理周测', '已发布的中国地理考试。', 'SCHEDULED', '2026-04-14 10:01:48', '2026-04-22 10:01:48', 0, 18.00, 30.00, 3, 0, 0, 0, 0, 1, 'AFTER_REVIEW', 'AFTER_REVIEW', NULL, 0, NULL, 'ALL', 'MIXED', NULL, NULL, 3, '2026-04-15 10:01:48', 3, '2026-04-16 11:14:49', 0, NULL);
INSERT INTO `assignment_exam_publish` VALUES (2, 1001, 2, '【测试】世界地理阶段练习', '草稿状态的世界地理考试。', 'DRAFT', '2026-04-16 10:01:48', '2026-04-25 10:01:48', 0, 18.00, 30.00, 2, 0, 0, 0, 0, 1, 'AFTER_REVIEW', 'AFTER_REVIEW', NULL, 0, NULL, 'ALL', 'MIXED', NULL, NULL, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_exam_publish` VALUES (3, 1001, 3, '【演示考试】进行中-可开始', '学生A当前没有作答记录，可直接开考', 'RUNNING', '2026-04-16 12:11:25', '2026-04-19 13:11:25', 45, 18.00, 30.00, 3, 1, 10, 1, 2, 1, 'AFTER_REVIEW', 'AFTER_REVIEW', 'MEDIUM', 3, NULL, 'ALL', 'MIXED', NULL, NULL, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_publish` VALUES (4, 1001, 3, '【演示考试】进行中-可续考', '学生A已有一条进行中的答题记录', 'RUNNING', '2026-04-16 11:11:25', '2026-04-19 13:11:25', 45, 18.00, 30.00, 3, 1, 10, 1, 2, 1, 'AFTER_REVIEW', 'AFTER_REVIEW', 'MEDIUM', 3, NULL, 'ALL', 'MIXED', NULL, NULL, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_exam_publish` VALUES (5, 1001, 4, '【演示考试】批改队列', '包含待批改和批改中的记录', 'GRADING', '2026-04-14 13:11:25', '2026-04-17 13:11:25', 60, 24.00, 40.00, 4, 1, 15, 1, 2, 1, 'AFTER_REVIEW', 'AFTER_REVIEW', 'HIGH', 5, NULL, 'ALL', 'MIXED', NULL, NULL, 3, '2026-04-16 13:11:25', 3, '2026-04-21 12:31:41', 1, NULL);
INSERT INTO `assignment_exam_publish` VALUES (6, 1001, 4, '【演示考试】已结束-可查看结果', '用于学生端结果页展示已批改结果', 'PUBLISHED', '2026-04-11 13:11:25', '2026-04-14 13:11:25', 60, 24.00, 40.00, 4, 1, 15, 1, 2, 1, 'MANUAL_PUBLISH', 'MANUAL_PUBLISH', 'MEDIUM', 5, NULL, 'ALL', 'MIXED', '2026-04-16 11:11:25', NULL, 3, '2026-04-16 13:11:25', 3, '2026-04-21 12:31:36', 1, NULL);
INSERT INTO `assignment_exam_publish` VALUES (7, 1001, 4, '【演示考试】教师批改队列卷 - 考试发布', NULL, 'PUBLISHED', '2026-04-21 12:45:00', '2026-04-24 13:45:00', 0, 24.00, 40.00, 4, 0, 0, 0, 0, 1, 'AFTER_REVIEW', 'AFTER_REVIEW', NULL, 0, NULL, 'ALL', 'MIXED', NULL, NULL, 3, '2026-04-21 12:45:30', 3, '2026-04-21 13:01:29', 0, NULL);

-- ----------------------------
-- Table structure for assignment_exam_publish_target
-- ----------------------------
DROP TABLE IF EXISTS `assignment_exam_publish_target`;
CREATE TABLE `assignment_exam_publish_target`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `publish_id` bigint NOT NULL,
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_exam_publish_target_tenant_publish`(`tenant_id` ASC, `publish_id` ASC) USING BTREE,
  INDEX `idx_assignment_exam_publish_target_tenant_target`(`tenant_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_exam_publish_target
-- ----------------------------
INSERT INTO `assignment_exam_publish_target` VALUES (1, 1001, 1, 'CLASS', 11001, 3, '2026-04-20 14:37:36', 3, '2026-04-20 14:37:36', 0, '面向七年级一班发布');
INSERT INTO `assignment_exam_publish_target` VALUES (2, 1001, 2, 'CLASS', 11002, 3, '2026-04-20 14:37:36', 3, '2026-04-20 14:37:36', 0, '面向八年级二班发布');
INSERT INTO `assignment_exam_publish_target` VALUES (3, 1001, 3, 'CLASS', 11001, 3, '2026-04-20 14:37:36', 3, '2026-04-20 14:37:36', 0, '开考演示班级');
INSERT INTO `assignment_exam_publish_target` VALUES (4, 1001, 4, 'CLASS', 11001, 3, '2026-04-20 14:37:36', 3, '2026-04-20 14:37:36', 0, '续考演示班级');
INSERT INTO `assignment_exam_publish_target` VALUES (5, 1001, 5, 'CLASS', 11002, 3, '2026-04-20 14:37:36', 3, '2026-04-20 14:37:36', 0, '批改队列演示班级');
INSERT INTO `assignment_exam_publish_target` VALUES (6, 1001, 6, 'CLASS', 11002, 3, '2026-04-20 14:37:36', 3, '2026-04-20 14:37:36', 0, '结果展示演示班级');

-- ----------------------------
-- Table structure for assignment_exam_record
-- ----------------------------
DROP TABLE IF EXISTS `assignment_exam_record`;
CREATE TABLE `assignment_exam_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `publish_id` bigint NULL DEFAULT NULL,
  `publish_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `paper_id` bigint NOT NULL,
  `student_id` bigint NULL DEFAULT NULL,
  `student_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `attempt_no` int NOT NULL DEFAULT 1,
  `score` decimal(10, 2) NULL DEFAULT NULL,
  `objective_score` decimal(10, 2) NULL DEFAULT NULL,
  `subjective_score` decimal(10, 2) NULL DEFAULT NULL,
  `answers` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IN_PROGRESS',
  `submit_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `absent_flag` tinyint NOT NULL DEFAULT 0,
  `objective_reviewed` tinyint NOT NULL DEFAULT 0,
  `subjective_reviewed` tinyint NOT NULL DEFAULT 0,
  `review_progress` int NOT NULL DEFAULT 0,
  `entered_at` datetime NULL DEFAULT NULL,
  `submitted_at` datetime NULL DEFAULT NULL,
  `last_active_at` datetime NULL DEFAULT NULL,
  `ip_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `device_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `risk_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `violation_count` int NOT NULL DEFAULT 0,
  `pass_flag` tinyint NOT NULL DEFAULT 0,
  `result_visible` tinyint NOT NULL DEFAULT 0,
  `window_switches` int NOT NULL DEFAULT 0,
  `grader_id` bigint NULL DEFAULT NULL,
  `graded_time` datetime NULL DEFAULT NULL,
  `review_lock_teacher_id` bigint NULL DEFAULT NULL,
  `review_lock_time` datetime NULL DEFAULT NULL,
  `review_round` int NOT NULL DEFAULT 1,
  `review_version` int NOT NULL DEFAULT 1,
  `review_comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_exam_record_tenant_paper`(`tenant_id` ASC, `paper_id` ASC) USING BTREE,
  INDEX `idx_assignment_exam_record_tenant_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_exam_record
-- ----------------------------
INSERT INTO `assignment_exam_record` VALUES (1, 1001, 1, '【测试】中国地理周测', 1, 4, '李晓雨', 1, 15.00, 15.00, NULL, '待教师批改主观题。', '2026-04-15 08:01:48', '2026-04-15 08:31:48', 'PENDING_REVIEW', NULL, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 1, NULL, NULL, NULL, NULL, 1, 1, NULL, 4, '2026-04-15 10:01:48', 3, '2026-04-20 14:37:36', 0, NULL);
INSERT INTO `assignment_exam_record` VALUES (2, 1001, 1, '【测试】中国地理周测', 1, 2044339999999990001, '周子航', 1, 17.00, 5.00, 12.00, '已完成教师批改。', '2026-04-15 06:01:48', '2026-04-15 07:01:48', 'REVIEWED', NULL, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0, 3, '2026-04-15 08:01:48', NULL, NULL, 1, 1, '主观题要点较完整，表达还可更准确。', 3, '2026-04-15 10:01:48', 3, '2026-04-20 14:37:36', 0, NULL);
INSERT INTO `assignment_exam_record` VALUES (3, 1001, 4, '【演示考试】进行中-可续考', 3, 2044375386258702338, '王若彤', 1, NULL, NULL, NULL, NULL, '2026-04-16 12:31:25', NULL, 'IN_PROGRESS', NULL, 0, 0, 0, 10, '2026-04-16 12:31:25', NULL, '2026-04-16 13:09:25', '127.0.0.1', 'Exam Playground Browser', 'MEDIUM', 0, 0, 0, 2, NULL, NULL, NULL, NULL, 1, 1, NULL, 4, '2026-04-16 13:11:25', 3, '2026-04-20 14:37:36', 0, '续考场景');
INSERT INTO `assignment_exam_record` VALUES (4, 1001, 5, '【演示考试】批改队列', 4, 2044375386258702340, '孙浩然', 1, 10.00, 10.00, NULL, NULL, '2026-04-16 10:11:25', '2026-04-16 11:11:25', 'PENDING_REVIEW', 'MANUAL', 0, 1, 0, 50, '2026-04-16 10:11:25', '2026-04-16 11:11:25', '2026-04-16 11:21:25', '127.0.0.1', 'Exam Playground Browser', 'HIGH', 1, 0, 0, 5, NULL, NULL, NULL, NULL, 1, 1, '等待教师手动批改', 4, '2026-04-16 13:11:25', 3, '2026-04-20 14:37:36', 0, '待批改场景');
INSERT INTO `assignment_exam_record` VALUES (5, 1001, 5, '【演示考试】批改队列', 4, 2044375386258702341, '陈思齐', 1, 20.00, 20.00, NULL, NULL, '2026-04-16 07:11:25', '2026-04-16 08:11:25', 'REVIEWING', 'MANUAL', 0, 1, 0, 50, '2026-04-16 07:11:25', '2026-04-16 08:11:25', '2026-04-16 12:41:25', '127.0.0.1', 'Exam Playground Browser', 'MEDIUM', 0, 0, 0, 2, NULL, NULL, 3, '2026-04-16 13:06:25', 1, 1, '教师已认领，正在批改', 2044339999999990001, '2026-04-16 13:11:25', 3, '2026-04-20 14:37:36', 0, '批改中场景');
INSERT INTO `assignment_exam_record` VALUES (6, 1001, 6, '【演示考试】已结束-可查看结果', 4, 2044375386258702342, '赵子涵', 1, 34.00, 20.00, 14.00, NULL, '2026-04-12 13:11:25', '2026-04-12 14:11:25', 'REVIEWED', 'MANUAL', 0, 1, 1, 100, '2026-04-12 13:11:25', '2026-04-12 14:11:25', '2026-04-12 14:11:25', '127.0.0.1', 'Exam Playground Browser', 'LOW', 0, 1, 1, 1, 3, '2026-04-13 13:11:25', NULL, NULL, 1, 1, '结构清晰，关键要点覆盖到位。', 4, '2026-04-16 13:11:25', 3, '2026-04-20 14:37:36', 0, '已批改场景');
INSERT INTO `assignment_exam_record` VALUES (7, 1001, 3, '【演示考试】进行中-可开始', 3, 2044375386258702339, '林书瑶', 1, NULL, NULL, NULL, NULL, '2026-04-16 13:56:07', NULL, 'IN_PROGRESS', NULL, 0, 0, 0, 0, '2026-04-16 13:56:07', NULL, '2026-04-16 13:56:07', NULL, NULL, NULL, 0, 0, 0, 0, NULL, NULL, NULL, NULL, 1, 1, NULL, 4, '2026-04-16 13:56:07', 3, '2026-04-20 14:37:36', 0, NULL);
INSERT INTO `assignment_exam_record` VALUES (8, 1001, 7, '【演示考试】教师批改队列卷 - 考试发布', 4, 2044375386258702342, '赵子涵', 1, 3.50, 0.00, 3.50, 'Submitted from student portal', '2026-04-21 12:46:53', '2026-04-21 12:46:53', 'REVIEWED', NULL, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0, 3, '2026-04-21 13:01:15', NULL, NULL, 1, 1, 'Pending teacher review', 2044375386258702342, '2026-04-21 12:46:53', 3, '2026-04-21 13:01:15', 0, NULL);

-- ----------------------------
-- Table structure for assignment_paper
-- ----------------------------
DROP TABLE IF EXISTS `assignment_paper`;
CREATE TABLE `assignment_paper`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT',
  `total_score` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `question_count` int NOT NULL DEFAULT 0,
  `duration` int NOT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_paper_tenant_status`(`tenant_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_paper
-- ----------------------------
INSERT INTO `assignment_paper` VALUES (1, 1001, '【测试】中国地理单元测验', '用于教师端组卷、发布和批改联调。', 'PUBLISHED', 30.00, 3, 45, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_paper` VALUES (2, 1001, '【测试】世界地理拓展测验', '用于教师端第二套试卷展示。', 'PUBLISHED', 30.00, 2, 40, 3, '2026-04-15 10:01:48', 3, '2026-04-16 11:34:57', 0, NULL);
INSERT INTO `assignment_paper` VALUES (3, 1001, '【演示考试】学生开考与续考卷', '用于展示开考、续考与学生结果页面', 'PUBLISHED', 30.00, 3, 45, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `assignment_paper` VALUES (4, 1001, '【演示考试】教师批改队列卷', '用于展示批改队列与考试生命周期操作', 'STOPPED', 40.00, 4, 60, 3, '2026-04-16 13:11:25', 3, '2026-04-21 12:45:45', 0, NULL);

-- ----------------------------
-- Table structure for assignment_paper_question
-- ----------------------------
DROP TABLE IF EXISTS `assignment_paper_question`;
CREATE TABLE `assignment_paper_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `paper_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `score` decimal(10, 2) NOT NULL,
  `sort_no` int NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_pq_tenant_paper`(`tenant_id` ASC, `paper_id` ASC) USING BTREE,
  INDEX `idx_assignment_pq_tenant_question`(`tenant_id` ASC, `question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_paper_question
-- ----------------------------
INSERT INTO `assignment_paper_question` VALUES (1, 1001, 1, 1, 10.00, 0, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (2, 1001, 1, 2, 5.00, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (3, 1001, 1, 3, 15.00, 2, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (4, 1001, 2, 4, 10.00, 0, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (5, 1001, 2, 5, 20.00, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (6, 1001, 3, 6, 10.00, 0, 3, '2026-04-16 13:11:25', 3, '2026-04-16 13:11:25', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (7, 1001, 3, 7, 5.00, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 13:11:25', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (8, 1001, 3, 8, 15.00, 2, 3, '2026-04-16 13:11:25', 3, '2026-04-16 13:11:25', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (9, 1001, 4, 6, 10.00, 0, 3, '2026-04-16 13:11:25', 3, '2026-04-16 13:11:25', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (10, 1001, 4, 9, 10.00, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 13:11:25', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (11, 1001, 4, 8, 10.00, 2, 3, '2026-04-16 13:11:25', 3, '2026-04-16 13:11:25', 0, NULL);
INSERT INTO `assignment_paper_question` VALUES (12, 1001, 4, 10, 10.00, 3, 3, '2026-04-16 13:11:25', 3, '2026-04-16 13:11:25', 0, NULL);

-- ----------------------------
-- Table structure for course_info
-- ----------------------------
DROP TABLE IF EXISTS `course_info`;
CREATE TABLE `course_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `course_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `course_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `course_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `subject_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `intro_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `teacher_id` bigint NULL DEFAULT NULL,
  `sale_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price_amount` decimal(10, 2) NULL DEFAULT 0.00,
  `publish_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `learn_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_info_tenant_code`(`tenant_id` ASC, `course_code` ASC) USING BTREE,
  INDEX `idx_course_info_teacher`(`tenant_id` ASC, `teacher_id` ASC, `status` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 709 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_info
-- ----------------------------
INSERT INTO `course_info` VALUES (701, 1001, 'TCH-202604-701', '七年级地理：地球自转与昼夜更替', 'teacher', 'geoscience', 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/701/cover-earth-rotation.jpg', '围绕昼夜更替、太阳视运动和地球自转建立七年级课堂资源包，适合教师用于微课、讲义和课堂练习联调。', 3, 'free', 0.00, 'published', 'self', 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"phase\": \"春季同步课\", \"stage\": \"初中\", \"module\": \"地球科学\", \"category\": \"teacher\", \"encrypted\": false, \"topicType\": \"微专题\", \"classHours\": 8, \"difficulty\": \"基础\", \"accessScope\": \"校内可见\", \"downloadPermission\": \"教师可下载\"}');
INSERT INTO `course_info` VALUES (702, 1001, 'TCH-202604-702', '八年级地理：季风气候与农业生产', 'teacher', 'atmosphere-ocean', 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/702/cover-monsoon.jpg', '用于展示视频、资料包、大纲与解析类资源的中文测试课程，覆盖季风气候、农业分布和区域差异。', 3, 'free', 0.00, 'published', 'self', 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"phase\": \"单元提升课\", \"stage\": \"初中\", \"module\": \"大气与海洋\", \"category\": \"teacher\", \"encrypted\": false, \"topicType\": \"综合专题\", \"classHours\": 10, \"difficulty\": \"进阶\", \"accessScope\": \"校内可见\", \"downloadPermission\": \"教师可下载\"}');
INSERT INTO `course_info` VALUES (703, 1001, 'TCH-202604-703', '九年级地理：城市化与区域发展', 'teacher', 'environment', 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/703/cover-urbanization.jpg', '用于展示教学案例、实施方案和专家指导视频等资源类型，适合测试课程资源元数据展示。', 3, 'free', 0.00, 'published', 'self', 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"phase\": \"中考专题课\", \"stage\": \"初中\", \"module\": \"环境与区域\", \"category\": \"teacher\", \"encrypted\": false, \"topicType\": \"案例研修\", \"classHours\": 12, \"difficulty\": \"进阶\", \"accessScope\": \"教研组可见\", \"downloadPermission\": \"教师可下载\"}');
INSERT INTO `course_info` VALUES (704, 1001, 'TCH-202604-704', '高中地理：板块构造与火山地震', 'teacher', 'tectonics', 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/704/cover-tectonics.jpg', '用于展示高中课程资源看板，包含视频、讲义、资料包和练习包，可用于教师工作台课程运营区联调。', 3, 'free', 0.00, 'published', 'self', 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"phase\": \"竞赛拓展课\", \"stage\": \"高中\", \"module\": \"构造地质\", \"category\": \"teacher\", \"encrypted\": false, \"topicType\": \"重难点突破\", \"classHours\": 16, \"difficulty\": \"拔高\", \"accessScope\": \"校内可见\", \"downloadPermission\": \"教师可下载\"}');
INSERT INTO `course_info` VALUES (705, 1001, 'TCH-202604-705', '天文地理拓展：太阳系与行星地貌', 'teacher', 'astronomy-space', 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/705/cover-solar-system.jpg', '用于展示行星地貌和太阳系主题下的视频资源、课程大纲与专家指导视频，方便测试视频类资源展示。', 3, 'free', 0.00, 'published', 'self', 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"phase\": \"社团选修课\", \"stage\": \"高中\", \"module\": \"天文与空间\", \"category\": \"teacher\", \"encrypted\": false, \"topicType\": \"探究课\", \"classHours\": 9, \"difficulty\": \"进阶\", \"accessScope\": \"校内可见\", \"downloadPermission\": \"教师可下载\"}');
INSERT INTO `course_info` VALUES (706, 1001, 'TCH-202604-706', '遥感实践：影像判读入门', 'teacher', 'remote-sensing', 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/706/cover-remote-sensing.jpg', '用于展示资料包、题库和答案解析等配套课件资源，适合测试资源列表和删除接口。', 3, 'free', 0.00, 'published', 'self', 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"phase\": \"实践活动课\", \"stage\": \"高中\", \"module\": \"遥感技术\", \"category\": \"teacher\", \"encrypted\": false, \"topicType\": \"技能训练\", \"classHours\": 6, \"difficulty\": \"基础\", \"accessScope\": \"教研组可见\", \"downloadPermission\": \"教师可下载\"}');
INSERT INTO `course_info` VALUES (707, 1001, 'COURSE-TEACHER-20260417141016103', '人工智能地理：遥感判读入门', 'teacher', 'remote-sensing-ai', NULL, '围绕遥感影像判读、地物识别和智能辅助分析设计的中文测试课程，适合演示教师端课程详情、资源分发和课堂任务配置。', 3, 'free', 0.00, 'published', 'self', 1, NULL, '2026-04-17 14:10:17', 3, '2026-04-20 14:37:36', 0, '中文测试课程：遥感专题');
INSERT INTO `course_info` VALUES (708, 1001, 'COURSE-TEACHER-20260417162137538', '区域地理专题：地貌演化与防灾减灾', 'teacher', 'geomorphology', NULL, '聚焦地貌演化、防灾减灾和区域案例分析的中文演示课程，用于联调课程资源管理、专题讲解和综合练习展示。', 3, 'free', 0.00, 'published', 'self', 1, NULL, '2026-04-17 16:21:39', 3, '2026-04-20 14:37:36', 0, '中文测试课程：区域地理专题');

-- ----------------------------
-- Table structure for course_resource
-- ----------------------------
DROP TABLE IF EXISTS `course_resource`;
CREATE TABLE `course_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `chapter_id` bigint NULL DEFAULT NULL,
  `resource_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `resource_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_id` bigint NULL DEFAULT NULL,
  `resource_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT 999,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_resource_course`(`tenant_id` ASC, `course_id` ASC, `status` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_course_resource_type`(`tenant_id` ASC, `resource_type` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9034 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_resource
-- ----------------------------
INSERT INTO `course_resource` VALUES (9001, 1001, 701, 101, 'video', '七年级地理-地球自转微课.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/701/video-earth-rotation.mp4', 10, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"coverUrl\": \"http://192.168.200.129:9000/geocourse/courses/teacher/demo/701/video-earth-rotation-cover.jpg\", \"fileSize\": 157286400, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/701/video-earth-rotation.mp4\", \"uploaderId\": 3, \"contentType\": \"video/mp4\", \"resourceTitle\": \"地球自转微课\", \"coverObjectKey\": \"courses/teacher/demo/701/video-earth-rotation-cover.jpg\", \"originalFileName\": \"七年级地理-地球自转微课.mp4\"}');
INSERT INTO `course_resource` VALUES (9002, 1001, 701, 101, 'handout', '地球自转课堂讲义.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/701/handout-earth-rotation.pdf', 20, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 1289456, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/701/handout-earth-rotation.pdf\", \"uploaderId\": 3, \"contentType\": \"application/pdf\", \"resourceTitle\": \"地球自转课堂讲义\", \"originalFileName\": \"地球自转课堂讲义.pdf\"}');
INSERT INTO `course_resource` VALUES (9003, 1001, 701, 102, 'exercise', '昼夜更替课堂练习.zip', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/701/exercise-day-night.zip', 40, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 348921, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/701/exercise-day-night.zip\", \"uploaderId\": 3, \"contentType\": \"application/zip\", \"resourceTitle\": \"昼夜更替课堂练习\", \"originalFileName\": \"昼夜更替课堂练习.zip\"}');
INSERT INTO `course_resource` VALUES (9004, 1001, 701, 102, 'questionBank', '地球自转题库资源.zip', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/701/question-bank-earth-rotation.zip', 70, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 542113, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/701/question-bank-earth-rotation.zip\", \"uploaderId\": 3, \"contentType\": \"application/zip\", \"resourceTitle\": \"地球自转题库资源\", \"originalFileName\": \"地球自转题库资源.zip\"}');
INSERT INTO `course_resource` VALUES (9005, 1001, 702, 201, 'video', '季风气候与农业生产.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/702/video-monsoon.mp4', 10, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"coverUrl\": \"http://192.168.200.129:9000/geocourse/courses/teacher/demo/702/video-monsoon-cover.jpg\", \"fileSize\": 186532800, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/702/video-monsoon.mp4\", \"uploaderId\": 3, \"contentType\": \"video/mp4\", \"resourceTitle\": \"季风气候教学视频\", \"coverObjectKey\": \"courses/teacher/demo/702/video-monsoon-cover.jpg\", \"originalFileName\": \"季风气候与农业生产.mp4\"}');
INSERT INTO `course_resource` VALUES (9006, 1001, 702, 201, 'material', '季风气候资料包.zip', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/702/material-monsoon.zip', 30, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 891245, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/702/material-monsoon.zip\", \"uploaderId\": 3, \"contentType\": \"application/zip\", \"resourceTitle\": \"季风气候资料包\", \"originalFileName\": \"季风气候资料包.zip\"}');
INSERT INTO `course_resource` VALUES (9007, 1001, 702, 202, 'outline', '农业生产课程大纲.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/702/outline-agriculture.pdf', 90, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 725881, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/702/outline-agriculture.pdf\", \"uploaderId\": 3, \"contentType\": \"application/pdf\", \"resourceTitle\": \"农业生产课程大纲\", \"originalFileName\": \"农业生产课程大纲.pdf\"}');
INSERT INTO `course_resource` VALUES (9008, 1001, 702, 202, 'answerAnalysis', '季风气候答案解析.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/702/analysis-monsoon.pdf', 80, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 612544, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/702/analysis-monsoon.pdf\", \"uploaderId\": 3, \"contentType\": \"application/pdf\", \"resourceTitle\": \"季风气候答案解析\", \"originalFileName\": \"季风气候答案解析.pdf\"}');
INSERT INTO `course_resource` VALUES (9009, 1001, 703, 301, 'video', '城市化与区域发展课堂视频.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/703/video-urbanization.mp4', 10, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"coverUrl\": \"http://192.168.200.129:9000/geocourse/courses/teacher/demo/703/video-urbanization-cover.jpg\", \"fileSize\": 209715200, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/703/video-urbanization.mp4\", \"uploaderId\": 3, \"contentType\": \"video/mp4\", \"resourceTitle\": \"城市化与区域发展课堂视频\", \"coverObjectKey\": \"courses/teacher/demo/703/video-urbanization-cover.jpg\", \"originalFileName\": \"城市化与区域发展课堂视频.mp4\"}');
INSERT INTO `course_resource` VALUES (9010, 1001, 703, 301, 'teachingCase', '城市化教学案例.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/703/case-urbanization.pdf', 100, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 984771, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/703/case-urbanization.pdf\", \"uploaderId\": 3, \"contentType\": \"application/pdf\", \"resourceTitle\": \"城市化教学案例\", \"originalFileName\": \"城市化教学案例.pdf\"}');
INSERT INTO `course_resource` VALUES (9011, 1001, 703, 302, 'implementationPlan', '区域发展实施方案.docx', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/703/implementation-regional-development.docx', 110, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 463882, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/703/implementation-regional-development.docx\", \"uploaderId\": 3, \"contentType\": \"application/vnd.openxmlformats-officedocument.wordprocessingml.document\", \"resourceTitle\": \"区域发展实施方案\", \"originalFileName\": \"区域发展实施方案.docx\"}');
INSERT INTO `course_resource` VALUES (9012, 1001, 703, 302, 'expertGuideVideo', '城市化专题专家指导.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/703/expert-urbanization.mp4', 120, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"coverUrl\": \"http://192.168.200.129:9000/geocourse/courses/teacher/demo/703/expert-urbanization-cover.jpg\", \"fileSize\": 133169152, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/703/expert-urbanization.mp4\", \"uploaderId\": 3, \"contentType\": \"video/mp4\", \"resourceTitle\": \"城市化专题专家指导\", \"coverObjectKey\": \"courses/teacher/demo/703/expert-urbanization-cover.jpg\", \"originalFileName\": \"城市化专题专家指导.mp4\"}');
INSERT INTO `course_resource` VALUES (9013, 1001, 704, 401, 'video', '板块构造与火山地震.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/704/video-tectonics.mp4', 10, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"coverUrl\": \"http://192.168.200.129:9000/geocourse/courses/teacher/demo/704/video-tectonics-cover.jpg\", \"fileSize\": 241172480, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/704/video-tectonics.mp4\", \"uploaderId\": 3, \"contentType\": \"video/mp4\", \"resourceTitle\": \"板块构造与火山地震视频\", \"coverObjectKey\": \"courses/teacher/demo/704/video-tectonics-cover.jpg\", \"originalFileName\": \"板块构造与火山地震.mp4\"}');
INSERT INTO `course_resource` VALUES (9014, 1001, 704, 401, 'handout', '板块构造核心讲义.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/704/handout-tectonics.pdf', 20, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 1428612, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/704/handout-tectonics.pdf\", \"uploaderId\": 3, \"contentType\": \"application/pdf\", \"resourceTitle\": \"板块构造核心讲义\", \"originalFileName\": \"板块构造核心讲义.pdf\"}');
INSERT INTO `course_resource` VALUES (9015, 1001, 704, 402, 'material', '火山地震资料包.zip', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/704/material-volcano-earthquake.zip', 30, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 1386211, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/704/material-volcano-earthquake.zip\", \"uploaderId\": 3, \"contentType\": \"application/zip\", \"resourceTitle\": \"火山地震资料包\", \"originalFileName\": \"火山地震资料包.zip\"}');
INSERT INTO `course_resource` VALUES (9016, 1001, 704, 402, 'exercise', '板块边界判读训练.zip', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/704/exercise-plate-boundary.zip', 40, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 522001, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/704/exercise-plate-boundary.zip\", \"uploaderId\": 3, \"contentType\": \"application/zip\", \"resourceTitle\": \"板块边界判读训练\", \"originalFileName\": \"板块边界判读训练.zip\"}');
INSERT INTO `course_resource` VALUES (9017, 1001, 705, 501, 'video', '太阳系与行星地貌导学视频.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/705/video-solar-system.mp4', 10, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"coverUrl\": \"http://192.168.200.129:9000/geocourse/courses/teacher/demo/705/video-solar-system-cover.jpg\", \"fileSize\": 176160768, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/705/video-solar-system.mp4\", \"uploaderId\": 3, \"contentType\": \"video/mp4\", \"resourceTitle\": \"太阳系与行星地貌导学视频\", \"coverObjectKey\": \"courses/teacher/demo/705/video-solar-system-cover.jpg\", \"originalFileName\": \"太阳系与行星地貌导学视频.mp4\"}');
INSERT INTO `course_resource` VALUES (9018, 1001, 705, 501, 'outline', '太阳系课程大纲.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/705/outline-solar-system.pdf', 90, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 482112, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/705/outline-solar-system.pdf\", \"uploaderId\": 3, \"contentType\": \"application/pdf\", \"resourceTitle\": \"太阳系课程大纲\", \"originalFileName\": \"太阳系课程大纲.pdf\"}');
INSERT INTO `course_resource` VALUES (9019, 1001, 705, 502, 'expertGuideVideo', '行星地貌专家讲解.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/705/expert-planetary-geomorphology.mp4', 120, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"coverUrl\": \"http://192.168.200.129:9000/geocourse/courses/teacher/demo/705/expert-planetary-geomorphology-cover.jpg\", \"fileSize\": 115343360, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/705/expert-planetary-geomorphology.mp4\", \"uploaderId\": 3, \"contentType\": \"video/mp4\", \"resourceTitle\": \"行星地貌专家讲解\", \"coverObjectKey\": \"courses/teacher/demo/705/expert-planetary-geomorphology-cover.jpg\", \"originalFileName\": \"行星地貌专家讲解.mp4\"}');
INSERT INTO `course_resource` VALUES (9020, 1001, 706, 601, 'video', '遥感影像判读入门.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/706/video-remote-sensing.mp4', 10, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"coverUrl\": \"http://192.168.200.129:9000/geocourse/courses/teacher/demo/706/video-remote-sensing-cover.jpg\", \"fileSize\": 164626432, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/706/video-remote-sensing.mp4\", \"uploaderId\": 3, \"contentType\": \"video/mp4\", \"resourceTitle\": \"遥感影像判读入门\", \"coverObjectKey\": \"courses/teacher/demo/706/video-remote-sensing-cover.jpg\", \"originalFileName\": \"遥感影像判读入门.mp4\"}');
INSERT INTO `course_resource` VALUES (9021, 1001, 706, 601, 'material', '遥感实训资料包.zip', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/706/material-remote-sensing.zip', 30, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 1015523, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/706/material-remote-sensing.zip\", \"uploaderId\": 3, \"contentType\": \"application/zip\", \"resourceTitle\": \"遥感实训资料包\", \"originalFileName\": \"遥感实训资料包.zip\"}');
INSERT INTO `course_resource` VALUES (9022, 1001, 706, 602, 'questionBank', '影像判读题库.zip', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/706/question-bank-remote-sensing.zip', 70, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 604118, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/706/question-bank-remote-sensing.zip\", \"uploaderId\": 3, \"contentType\": \"application/zip\", \"resourceTitle\": \"影像判读题库\", \"originalFileName\": \"影像判读题库.zip\"}');
INSERT INTO `course_resource` VALUES (9023, 1001, 706, 602, 'answerAnalysis', '遥感判读答案解析.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/demo/706/analysis-remote-sensing.pdf', 80, 1, 3, '2026-04-17 13:23:37', 3, '2026-04-17 13:23:37', 0, '{\"fileSize\": 355883, \"provider\": \"minio\", \"objectKey\": \"courses/teacher/demo/706/analysis-remote-sensing.pdf\", \"uploaderId\": 3, \"contentType\": \"application/pdf\", \"resourceTitle\": \"遥感判读答案解析\", \"originalFileName\": \"遥感判读答案解析.pdf\"}');
INSERT INTO `course_resource` VALUES (9024, 1001, 707, NULL, 'video', '遥感判读导学视频.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/video-039cbe7a5f104da6b27f8c924c14193a.mp4', 10, 1, NULL, '2026-04-17 14:10:20', 3, '2026-04-20 14:37:36', 0, '中文测试资源：遥感专题视频');
INSERT INTO `course_resource` VALUES (9025, 1001, 707, NULL, 'handout', '遥感判读课堂讲义.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/handout-46f1d12f05b4420abd11e5b9ee686de9.pdf', 20, 1, NULL, '2026-04-17 14:10:20', 3, '2026-04-20 14:37:36', 0, '中文测试资源：遥感讲义');
INSERT INTO `course_resource` VALUES (9026, 1001, 707, NULL, 'material', '遥感影像样例资料包.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/material-43d0d5c797eb4a82a4e9ae374e110d39.pdf', 30, 1, NULL, '2026-04-17 14:10:20', 3, '2026-04-20 14:37:36', 0, '中文测试资源：遥感资料包');
INSERT INTO `course_resource` VALUES (9027, 1001, 707, NULL, 'exercise', '遥感判读分层练习.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/exercise-74a7f4c3607a4288ac4dc306156b1ae7.pdf', 40, 1, NULL, '2026-04-17 14:10:20', 3, '2026-04-20 14:37:36', 0, '中文测试资源：遥感练习');
INSERT INTO `course_resource` VALUES (9028, 1001, 701, NULL, 'handout', '课堂观察记录说明.txt', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/handout-c7da69841c1b4368b403edd1b2303a3b.txt', 20, 1, 3, '2026-04-17 16:07:09', 3, '2026-04-20 14:37:36', 0, '中文测试资源：课堂说明附件');
INSERT INTO `course_resource` VALUES (9029, 1001, 701, NULL, 'video', '地球自转实验演示.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/video-807ebf6dadbd4892945a0cb5d89d29b4.mp4', 10, 1, 3, '2026-04-17 16:07:50', 3, '2026-04-20 14:37:36', 0, '中文测试资源：实验演示视频');
INSERT INTO `course_resource` VALUES (9030, 1001, 708, NULL, 'video', '地貌演化导学视频.mp4', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/video-92f4be31b247416eb69f6d4784fd8d75.mp4', 10, 1, NULL, '2026-04-17 16:21:41', 3, '2026-04-20 14:37:36', 0, '中文测试资源：地貌视频');
INSERT INTO `course_resource` VALUES (9031, 1001, 708, NULL, 'handout', '地貌演化课堂课件.pptx', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/handout-6d255b65949e44ae88eb0dbfa2e8745a.pptx', 20, 1, NULL, '2026-04-17 16:21:41', 3, '2026-04-20 14:37:36', 0, '中文测试资源：专题课件');
INSERT INTO `course_resource` VALUES (9032, 1001, 708, NULL, 'material', '区域地理资料包.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/material-167b55cadc1046e19c50c492b5329d1f.pdf', 30, 1, NULL, '2026-04-17 16:21:41', 3, '2026-04-20 14:37:36', 0, '中文测试资源：区域案例资料');
INSERT INTO `course_resource` VALUES (9033, 1001, 708, NULL, 'exercise', '防灾减灾综合练习.pdf', NULL, 'http://192.168.200.129:9000/geocourse/courses/teacher/2026/0417/exercise-6b77180dbb0947edb5c532d5979f3cec.pdf', 40, 1, NULL, '2026-04-17 16:21:41', 3, '2026-04-20 14:37:36', 0, '中文测试资源：综合练习');

-- ----------------------------
-- Table structure for lesson_prep_document
-- ----------------------------
DROP TABLE IF EXISTS `lesson_prep_document`;
CREATE TABLE `lesson_prep_document`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `school_id` bigint NULL DEFAULT NULL,
  `teacher_id` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_id` bigint NULL DEFAULT NULL,
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `doc_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `content_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `content_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MANUAL',
  `source_document_id` bigint NULL DEFAULT NULL,
  `published_at` datetime NULL DEFAULT NULL,
  `last_edited_at` datetime NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lesson_prep_document_tenant_teacher`(`tenant_id` ASC, `teacher_id` ASC) USING BTREE,
  INDEX `idx_lesson_prep_document_tenant_status`(`tenant_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_lesson_prep_document_tenant_course`(`tenant_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_lesson_prep_document_tenant_update_time`(`tenant_id` ASC, `update_time` ASC) USING BTREE,
  INDEX `idx_lesson_prep_document_source_document`(`source_document_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lesson_prep_document
-- ----------------------------
INSERT INTO `lesson_prep_document` VALUES (1, 1001, NULL, 3, '七年级地理《地球自转》教学设计', 701, '七年级地理', 'LESSON_PLAN', 'PUBLISHED', '围绕昼夜更替和地球自转设计的中文教案，用于演示教案详情和发布状态。', 'PLAIN_TEXT', NULL, 'MANUAL', NULL, '2026-04-16 16:11:31', '2026-04-20 14:37:36', 3, '2026-04-16 16:11:31', 3, '2026-04-21 11:00:48', 1, NULL);
INSERT INTO `lesson_prep_document` VALUES (2, 1001, NULL, 3, '七年级地理《地图三要素》备课资料', 701, '七年级地理', 'RESOURCE', 'DRAFT', '整理地图三要素、比例尺和图例符号的备课资料，适合测试资源型文档。', 'PLAIN_TEXT', NULL, 'MANUAL', NULL, NULL, '2026-04-20 14:37:36', 3, '2026-04-16 16:12:10', 3, '2026-04-21 11:00:48', 1, NULL);
INSERT INTO `lesson_prep_document` VALUES (3, 1001, NULL, 3, '八年级地理《季风气候》课堂素材汇编', 702, '八年级地理', 'RESOURCE', 'DRAFT', '汇总季风气候判读案例、图表素材和课堂引导语。', 'PLAIN_TEXT', NULL, 'MANUAL', NULL, NULL, '2026-04-20 14:37:36', 3, '2026-04-16 16:12:44', 3, '2026-04-21 11:00:48', 1, NULL);
INSERT INTO `lesson_prep_document` VALUES (4, 1001, NULL, 3, '八年级地理《人口分布》教案初稿', 702, '八年级地理', 'LESSON_PLAN', 'DRAFT', '用于演示教案编辑、复制和教研协同的中文初稿。', 'PLAIN_TEXT', NULL, 'MANUAL', NULL, NULL, '2026-04-20 14:37:36', 3, '2026-04-16 16:13:44', 3, '2026-04-21 11:00:48', 1, NULL);
INSERT INTO `lesson_prep_document` VALUES (5, 1001, NULL, 3, '八年级地理《人口分布》教案复制稿', 702, '八年级地理', 'LESSON_PLAN', 'PUBLISHED', '从初稿复制出的发布版本，用于演示复制关系。', 'PLAIN_TEXT', NULL, 'COPY', 4, '2026-04-20 14:37:36', '2026-04-20 14:37:36', 3, '2026-04-16 16:13:44', 3, '2026-04-21 11:00:48', 1, NULL);
INSERT INTO `lesson_prep_document` VALUES (6, 1001, NULL, 3, '七年级地理《地球自转》分层教学方案', 701, '七年级地理', 'LESSON_PLAN', 'DRAFT', '突出分层提问、实验观察和课堂达标检测的教学方案。', 'RICH_TEXT', '<p><strong>七年级地理《地球自转》分层教学方案</strong></p><p>基础层关注方向与周期，提升层增加太阳视运动解释，拓展层结合生活中的时差现象。</p><p>结尾安排一题一图一解释，便于快速验收。</p>', 'MANUAL', NULL, NULL, '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (7, 1001, NULL, 3, '七年级地理《经纬网》课件大纲', 701, '七年级地理', 'COURSEWARE', 'PUBLISHED', '经纬网定位与读图能力训练的课件脚本。', 'RICH_TEXT', '<p><strong>七年级地理《经纬网》课件大纲</strong></p><p>包含经线纬线定义、经纬度判读和地图定位练习。</p><p>建议在第 3 页插入经纬网小游戏。</p>', 'MANUAL', NULL, '2026-04-16 16:24:12', '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (8, 1001, NULL, 3, '七年级地理《地图符号》课堂练习', 701, '七年级地理', 'EXERCISE', 'DRAFT', '地图符号识别和图例应用的课堂练习。', 'PLAIN_TEXT', '七年级地理《地图符号》课堂练习\n1. 判断常见图例含义。\n2. 根据比例尺估算两地距离。\n3. 选择题与简答题结合，便于课堂即时反馈。', 'MANUAL', NULL, NULL, '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (9, 1001, NULL, 3, '八年级地理《东亚季风》备课笔记', 702, '八年级地理', 'RESOURCE', 'PUBLISHED', '东亚季风成因、分布和农业影响的备课笔记。', 'MARKDOWN', '# 八年级地理《东亚季风》备课笔记\n\n重点提示：从海陆热力差异、风向转换和降水时空分布三个方面讲解。\n\n- 课堂提问：冬季风与夏季风各自来自哪里？\n- 板书建议：成因、特征、影响三栏展开。', 'MANUAL', NULL, '2026-04-16 16:24:12', '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (10, 1001, NULL, 3, '八年级地理《人口分布》教学设计', 702, '八年级地理', 'LESSON_PLAN', 'DRAFT', '人口分布差异与自然环境联系的教学设计。', 'RICH_TEXT', '<p><strong>八年级地理《人口分布》教学设计</strong></p><p>从中国人口东密西疏现象切入，联系地形、气候与交通区位分析原因。</p><p>设计一项地图判读活动，帮助学生形成空间概念。</p>', 'MANUAL', NULL, NULL, '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (11, 1001, NULL, 3, '八年级地理《河流流域》课件脚本', 702, '八年级地理', 'COURSEWARE', 'PUBLISHED', '河流流域结构、上下游联系与区域治理的课件脚本。', 'RICH_TEXT', '<p><strong>八年级地理《河流流域》课件脚本</strong></p><p>聚焦流域结构、上下游关系及综合治理案例。</p><p>建议结合黄河与长江流域图片进行对比讲解。</p>', 'MANUAL', NULL, '2026-04-16 16:24:12', '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (12, 1001, NULL, 3, '九年级地理《城市化》练习单', 703, '九年级地理', 'EXERCISE', 'DRAFT', '围绕城市化概念、问题与治理设计的练习单。', 'PLAIN_TEXT', '九年级地理《城市化》练习单\n1. 识记城市化的基本表现。\n2. 分析城市病形成原因。\n3. 结合本地案例提出治理建议。', 'MANUAL', NULL, NULL, '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (13, 1001, NULL, 3, '九年级地理《区域发展》案例册', 703, '九年级地理', 'RESOURCE', 'PUBLISHED', '用于比较不同区域发展路径的案例材料。', 'MARKDOWN', '# 九年级地理《区域发展》案例册\n\n选取沿海制造业城市、资源型城市和山地旅游城市三个案例。\n\n- 比较产业结构差异。\n- 归纳区域协同发展的关键条件。', 'MANUAL', NULL, '2026-04-16 16:24:12', '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (14, 1001, NULL, 3, '高中地理《大气环流》教学设计', 704, '高中地理', 'LESSON_PLAN', 'DRAFT', '大气环流、风带气压带和气候联系的教学设计。', 'RICH_TEXT', '<p><strong>高中地理《大气环流》教学设计</strong></p><p>重点讲解气压带、风带与全球热量差异之间的关系。</p><p>建议结合三圈环流图和季风形成示意图完成课堂串联。</p>', 'MANUAL', NULL, NULL, '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (15, 1001, NULL, 3, '高中地理《自然灾害》资源包', 704, '高中地理', 'RESOURCE', 'PUBLISHED', '自然灾害类型、成因与减灾措施的资源包。', 'MARKDOWN', '# 高中地理《自然灾害》资源包\n\n内容包括地震、火山、洪涝和台风案例材料。\n\n- 配套地图：灾害高发区分布图。\n- 配套任务：制定校园防灾避险清单。', 'MANUAL', NULL, '2026-04-16 16:24:12', '2026-04-20 14:37:36', 3, '2026-04-16 16:24:12', 3, '2026-04-21 11:00:48', 0, NULL);
INSERT INTO `lesson_prep_document` VALUES (21, 1001, NULL, 3, '高中地理《大气环流》教学设计（复制）', 704, '高中地理', 'LESSON_PLAN', 'PUBLISHED', '复制版教案，用于演示复制后继续发布的效果。', 'PLAIN_TEXT', NULL, 'COPY', 14, '2026-04-16 16:37:17', '2026-04-20 14:37:36', 3, '2026-04-16 16:37:12', 3, '2026-04-21 11:00:48', 1, NULL);
INSERT INTO `lesson_prep_document` VALUES (22, 1001, NULL, 3, '高中地理《自然灾害》资源包（复制）', 704, '高中地理', 'RESOURCE', 'DRAFT', '自然灾害类型、成因与减灾措施的资源包。', 'MARKDOWN', '# 高中地理《自然灾害》资源包\n\n内容包括地震、火山、洪涝和台风案例材料。\n\n- 配套地图：灾害高发区分布图。\n- 配套任务：制定校园防灾避险清单。', 'COPY', 15, NULL, '2026-04-21 12:50:58', 3, '2026-04-21 12:50:58', 3, '2026-04-21 12:50:58', 0, NULL);

-- ----------------------------
-- Table structure for lesson_prep_document_content
-- ----------------------------
DROP TABLE IF EXISTS `lesson_prep_document_content`;
CREATE TABLE `lesson_prep_document_content`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `document_id` bigint NOT NULL,
  `content_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RICH_TEXT',
  `content_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `ext_json` json NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_lesson_prep_document_content_document_id`(`document_id` ASC) USING BTREE,
  INDEX `idx_lesson_prep_document_content_tenant_document`(`tenant_id` ASC, `document_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lesson_prep_document_content
-- ----------------------------
INSERT INTO `lesson_prep_document_content` VALUES (1, 1001, 1, 'RICH_TEXT', '<p><strong>七年级地理《地球自转》教学设计</strong></p><p>教学目标：理解地球自转方向、周期及其带来的昼夜更替现象。</p><p>教学流程：情境导入、实验演示、现象解释、课堂练习、反思总结。</p>', NULL, 3, '2026-04-16 16:11:31', 3, '2026-04-20 14:37:36', 1);
INSERT INTO `lesson_prep_document_content` VALUES (2, 1001, 2, 'MARKDOWN', '## 七年级地理《地图三要素》备课资料\n\n1. 导入素材：校园平面图与地铁线路图。\n2. 核心概念：方向、比例尺、图例与注记。\n3. 课堂建议：先读图后动手标注，再进行小组互评。', NULL, 3, '2026-04-16 16:12:10', 3, '2026-04-20 14:37:36', 1);
INSERT INTO `lesson_prep_document_content` VALUES (3, 1001, 3, 'PLAIN_TEXT', '季风气候课堂素材汇编\n一、东亚季风示意图。\n二、降水柱状图对比材料。\n三、农业生产案例：水稻种植与旱涝风险。', NULL, 3, '2026-04-16 16:12:44', 3, '2026-04-20 14:37:36', 1);
INSERT INTO `lesson_prep_document_content` VALUES (4, 1001, 4, 'PLAIN_TEXT', '人口分布教案初稿\n1. 引导学生观察胡焕庸线。\n2. 讨论自然环境与经济条件对人口分布的影响。\n3. 形成课堂结论并布置拓展任务。', NULL, 3, '2026-04-16 16:13:44', 3, '2026-04-20 14:37:36', 1);
INSERT INTO `lesson_prep_document_content` VALUES (5, 1001, 5, 'PLAIN_TEXT', '人口分布教案复制稿\n一、沿用初稿结构。\n二、增加区域对比案例。\n三、补充课堂即时评价语。', NULL, 3, '2026-04-16 16:13:44', 3, '2026-04-20 14:37:36', 1);
INSERT INTO `lesson_prep_document_content` VALUES (6, 1001, 6, 'RICH_TEXT', '<p><strong>七年级地理《地球自转》分层教学方案</strong></p><p>基础层关注方向与周期，提升层增加太阳视运动解释，拓展层结合生活中的时差现象。</p><p>结尾安排一题一图一解释，便于快速验收。</p>', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (7, 1001, 7, 'RICH_TEXT', '<p><strong>七年级地理《经纬网》课件大纲</strong></p><p>包含经线纬线定义、经纬度判读和地图定位练习。</p><p>建议在第 3 页插入经纬网小游戏。</p>', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (8, 1001, 8, 'PLAIN_TEXT', '七年级地理《地图符号》课堂练习\n1. 判断常见图例含义。\n2. 根据比例尺估算两地距离。\n3. 选择题与简答题结合，便于课堂即时反馈。', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (9, 1001, 9, 'MARKDOWN', '# 八年级地理《东亚季风》备课笔记\n\n重点提示：从海陆热力差异、风向转换和降水时空分布三个方面讲解。\n\n- 课堂提问：冬季风与夏季风各自来自哪里？\n- 板书建议：成因、特征、影响三栏展开。', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (10, 1001, 10, 'RICH_TEXT', '<p><strong>八年级地理《人口分布》教学设计</strong></p><p>从中国人口东密西疏现象切入，联系地形、气候与交通区位分析原因。</p><p>设计一项地图判读活动，帮助学生形成空间概念。</p>', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (11, 1001, 11, 'RICH_TEXT', '<p><strong>八年级地理《河流流域》课件脚本</strong></p><p>聚焦流域结构、上下游关系及综合治理案例。</p><p>建议结合黄河与长江流域图片进行对比讲解。</p>', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (12, 1001, 12, 'PLAIN_TEXT', '九年级地理《城市化》练习单\n1. 识记城市化的基本表现。\n2. 分析城市病形成原因。\n3. 结合本地案例提出治理建议。', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (13, 1001, 13, 'MARKDOWN', '# 九年级地理《区域发展》案例册\n\n选取沿海制造业城市、资源型城市和山地旅游城市三个案例。\n\n- 比较产业结构差异。\n- 归纳区域协同发展的关键条件。', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (14, 1001, 14, 'RICH_TEXT', '<p><strong>高中地理《大气环流》教学设计</strong></p><p>重点讲解气压带、风带与全球热量差异之间的关系。</p><p>建议结合三圈环流图和季风形成示意图完成课堂串联。</p>', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (15, 1001, 15, 'MARKDOWN', '# 高中地理《自然灾害》资源包\n\n内容包括地震、火山、洪涝和台风案例材料。\n\n- 配套地图：灾害高发区分布图。\n- 配套任务：制定校园防灾避险清单。', NULL, 3, '2026-04-16 16:24:12', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_document_content` VALUES (21, 1001, 21, 'RICH_TEXT', '<p><strong>高中地理《大气环流》教学设计（复制）</strong></p><p>在原教案基础上新增课堂追问与随堂练习，适合公开课联调展示。</p><p>建议配合风带演示动画使用。</p>', NULL, 3, '2026-04-16 16:37:12', 3, '2026-04-20 14:37:36', 1);

-- ----------------------------
-- Table structure for lesson_prep_document_file
-- ----------------------------
DROP TABLE IF EXISTS `lesson_prep_document_file`;
CREATE TABLE `lesson_prep_document_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `document_id` bigint NOT NULL,
  `file_id` bigint NOT NULL,
  `relation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ATTACHMENT',
  `sort_no` int NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_lesson_prep_document_file`(`document_id` ASC, `file_id` ASC) USING BTREE,
  INDEX `idx_lesson_prep_document_file_tenant_document`(`tenant_id` ASC, `document_id` ASC) USING BTREE,
  INDEX `idx_lesson_prep_document_file_tenant_file`(`tenant_id` ASC, `file_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lesson_prep_document_file
-- ----------------------------
INSERT INTO `lesson_prep_document_file` VALUES (1, 1001, 1, 1, 'ATTACHMENT', 0, 3, '2026-04-16 16:12:05', 3, '2026-04-16 16:12:06', 1);
INSERT INTO `lesson_prep_document_file` VALUES (2, 1001, 3, 2, 'ATTACHMENT', 0, 3, '2026-04-16 16:13:19', 3, '2026-04-16 16:13:19', 1);
INSERT INTO `lesson_prep_document_file` VALUES (3, 1001, 15, 3, 'ATTACHMENT', 0, 3, '2026-04-16 16:37:18', 3, '2026-04-16 16:37:18', 0);

-- ----------------------------
-- Table structure for lesson_prep_document_file_rel
-- ----------------------------
DROP TABLE IF EXISTS `lesson_prep_document_file_rel`;
CREATE TABLE `lesson_prep_document_file_rel`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `document_id` bigint NOT NULL,
  `file_id` bigint NOT NULL,
  `relation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ATTACHMENT',
  `sort_no` int NULL DEFAULT 0,
  `linked_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lp_rel_doc`(`tenant_id` ASC, `document_id` ASC) USING BTREE,
  INDEX `idx_lp_rel_file`(`tenant_id` ASC, `file_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lesson_prep_document_file_rel
-- ----------------------------
INSERT INTO `lesson_prep_document_file_rel` VALUES (1, 1001, 1, 1, 'ATTACHMENT', 0, '2026-04-16 16:12:05', 3, '2026-04-16 16:12:05', 3, '2026-04-16 16:12:06', 1);
INSERT INTO `lesson_prep_document_file_rel` VALUES (2, 1001, 3, 2, 'ATTACHMENT', 0, '2026-04-16 16:13:19', 3, '2026-04-16 16:13:19', 3, '2026-04-16 16:13:19', 1);
INSERT INTO `lesson_prep_document_file_rel` VALUES (3, 1001, 15, 3, 'ATTACHMENT', 0, '2026-04-16 16:37:18', 3, '2026-04-16 16:37:18', 3, '2026-04-16 16:37:18', 0);
INSERT INTO `lesson_prep_document_file_rel` VALUES (4, 1001, 22, 3, 'ATTACHMENT', 1, '2026-04-21 12:50:58', 3, '2026-04-21 12:50:58', 3, '2026-04-21 12:50:58', 0);

-- ----------------------------
-- Table structure for lesson_prep_file
-- ----------------------------
DROP TABLE IF EXISTS `lesson_prep_file`;
CREATE TABLE `lesson_prep_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `uploader_id` bigint NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_ext` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `content_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `access_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `storage_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'minio',
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lp_file_tenant_uploader`(`tenant_id` ASC, `uploader_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lesson_prep_file
-- ----------------------------
INSERT INTO `lesson_prep_file` VALUES (1, 1001, 3, '地球自转课堂观察记录.txt', 'txt', 27, 'text/plain', 'http://192.168.200.129:9000/geocourse/lesson-prep/2026/04/16/bfb7a38d55ff4386946edbecbab07584.txt', 'lesson-prep/2026/04/16/bfb7a38d55ff4386946edbecbab07584.txt', 'minio', 1, 3, '2026-04-16 16:12:05', 3, '2026-04-20 14:37:36', 0, 'legacy.biz_type=LESSON_PREP');
INSERT INTO `lesson_prep_file` VALUES (2, 1001, 3, '季风气候素材摘录.txt', 'txt', 22, 'text/plain', 'http://192.168.200.129:9000/geocourse/lesson-prep/2026/04/16/f216f34fcc6c4013813a627508a7968e.txt', 'lesson-prep/2026/04/16/f216f34fcc6c4013813a627508a7968e.txt', 'minio', 1, 3, '2026-04-16 16:13:19', 3, '2026-04-20 14:37:36', 0, 'legacy.biz_type=LESSON_PREP');
INSERT INTO `lesson_prep_file` VALUES (3, 1001, 3, '自然灾害案例资源包.pdf', 'pdf', 814728, 'application/pdf', 'http://192.168.200.129:9000/geocourse/lesson-prep/2026/04/16/f3888f2761ab439888471d1911cda0cd.pdf', 'lesson-prep/2026/04/16/f3888f2761ab439888471d1911cda0cd.pdf', 'minio', 1, 3, '2026-04-16 16:37:16', 3, '2026-04-20 14:37:36', 0, 'legacy.biz_type=LESSON_PREP');

-- ----------------------------
-- Table structure for lesson_prep_upload_file
-- ----------------------------
DROP TABLE IF EXISTS `lesson_prep_upload_file`;
CREATE TABLE `lesson_prep_upload_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `school_id` bigint NULL DEFAULT NULL,
  `uploader_id` bigint NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_ext` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `file_size` bigint NOT NULL,
  `storage_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MINIO',
  `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `access_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LESSON_PREP',
  `md5` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UPLOADED',
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lesson_prep_upload_file_tenant_uploader`(`tenant_id` ASC, `uploader_id` ASC) USING BTREE,
  INDEX `idx_lesson_prep_upload_file_tenant_biz_type`(`tenant_id` ASC, `biz_type` ASC) USING BTREE,
  INDEX `idx_lesson_prep_upload_file_md5`(`md5` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lesson_prep_upload_file
-- ----------------------------
INSERT INTO `lesson_prep_upload_file` VALUES (1, 1001, NULL, 3, '地球自转课堂观察记录.txt', 'txt', 'text/plain', 27, 'MINIO', 'lesson-prep/2026/04/16/bfb7a38d55ff4386946edbecbab07584.txt', 'http://192.168.200.129:9000/geocourse/lesson-prep/2026/04/16/bfb7a38d55ff4386946edbecbab07584.txt', 'LESSON_PREP', NULL, 'UPLOADED', 3, '2026-04-16 16:12:05', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_upload_file` VALUES (2, 1001, NULL, 3, '季风气候素材摘录.txt', 'txt', 'text/plain', 22, 'MINIO', 'lesson-prep/2026/04/16/f216f34fcc6c4013813a627508a7968e.txt', 'http://192.168.200.129:9000/geocourse/lesson-prep/2026/04/16/f216f34fcc6c4013813a627508a7968e.txt', 'LESSON_PREP', NULL, 'UPLOADED', 3, '2026-04-16 16:13:19', 3, '2026-04-20 14:37:36', 0);
INSERT INTO `lesson_prep_upload_file` VALUES (3, 1001, NULL, 3, '自然灾害案例资源包.pdf', 'pdf', 'application/pdf', 814728, 'MINIO', 'lesson-prep/2026/04/16/f3888f2761ab439888471d1911cda0cd.pdf', 'http://192.168.200.129:9000/geocourse/lesson-prep/2026/04/16/f3888f2761ab439888471d1911cda0cd.pdf', 'LESSON_PREP', NULL, 'UPLOADED', 3, '2026-04-16 16:37:16', 3, '2026-04-20 14:37:36', 0);

-- ----------------------------
-- Table structure for qb_category
-- ----------------------------
DROP TABLE IF EXISTS `qb_category`;
CREATE TABLE `qb_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `parent_id` bigint NOT NULL DEFAULT 0,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qb_category_tenant_parent`(`tenant_id` ASC, `parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_category
-- ----------------------------
INSERT INTO `qb_category` VALUES (1, 1001, 0, '【测试】地理题库', '教师端考试模块联调用测试分类', 1, 1, 3, '2026-04-15 10:01:47', 3, '2026-04-15 10:01:47', 0, NULL);
INSERT INTO `qb_category` VALUES (2, 1001, 1, '【测试】中国地理', '中国地理测试题', 10, 1, 3, '2026-04-15 10:01:47', 3, '2026-04-15 10:01:47', 0, NULL);
INSERT INTO `qb_category` VALUES (3, 1001, 1, '【测试】世界地理', '世界地理测试题', 20, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_category` VALUES (4, 1001, 0, '【演示考试】地理', '考试联调演示根目录', 1, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_category` VALUES (5, 1001, 4, '【演示考试】中国地理', '中国地理题目', 10, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_category` VALUES (6, 1001, 4, '【演示考试】世界地理', '世界地理题目', 20, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);

-- ----------------------------
-- Table structure for qb_question
-- ----------------------------
DROP TABLE IF EXISTS `qb_question`;
CREATE TABLE `qb_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `category_id` bigint NOT NULL,
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `multi_select` tinyint NOT NULL DEFAULT 0,
  `difficulty` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `default_score` int NOT NULL,
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qb_question_tenant_category`(`tenant_id` ASC, `category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_question
-- ----------------------------
INSERT INTO `qb_question` VALUES (1, 1001, 2, '【测试】我国面积最大的省级行政区是下列哪一个？', 'CHOICE', 0, 'EASY', 10, '新疆维吾尔自治区面积最大。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question` VALUES (2, 1001, 2, '【测试】黄河最终注入渤海。', 'JUDGE', 0, 'EASY', 5, '黄河在山东注入渤海。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question` VALUES (3, 1001, 2, '【测试】简述季风气候对我国农业生产的影响。', 'TEXT', 0, 'MEDIUM', 15, '可从雨热同期、降水不稳定、区域差异等方面作答。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question` VALUES (4, 1001, 3, '【测试】下列哪条河流流经埃及并孕育了古埃及文明？', 'CHOICE', 0, 'EASY', 10, '答案为尼罗河。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question` VALUES (5, 1001, 3, '【测试】分析欧洲西部海洋性气候显著的主要原因。', 'TEXT', 0, 'HARD', 20, '可从纬度位置、西风带、北大西洋暖流和地形等角度作答。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question` VALUES (6, 1001, 5, '【演示考试】中国面积最大的省级行政区是哪个？', 'CHOICE', 0, 'EASY', 10, '新疆维吾尔自治区是中国面积最大的省级行政区。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question` VALUES (7, 1001, 5, '【演示考试】黄河最终注入渤海。', 'JUDGE', 0, 'EASY', 5, '黄河在山东省注入渤海。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question` VALUES (8, 1001, 5, '【演示考试】简述季风气候对中国农业的一个影响。', 'TEXT', 0, 'MEDIUM', 15, '要点：雨热同期有利于作物生长，但降水不稳定也会带来旱涝风险。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question` VALUES (9, 1001, 6, '【演示考试】尼罗河位于哪个洲？', 'CHOICE', 0, 'EASY', 10, '尼罗河位于非洲。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question` VALUES (10, 1001, 6, '【演示考试】请说明西欧海洋性气候形成的一个原因。', 'TEXT', 0, 'HARD', 20, '盛行西风、北大西洋暖流以及开阔地形共同影响。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);

-- ----------------------------
-- Table structure for qb_question_answer
-- ----------------------------
DROP TABLE IF EXISTS `qb_question_answer`;
CREATE TABLE `qb_question_answer`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `answer_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `grading_rule` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_qb_answer_question`(`question_id` ASC) USING BTREE,
  INDEX `idx_qb_answer_tenant_question`(`tenant_id` ASC, `question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_question_answer
-- ----------------------------
INSERT INTO `qb_question_answer` VALUES (1, 1001, 1, 'B', '单选题按唯一正确选项计分。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_answer` VALUES (2, 1001, 2, 'TRUE', '判断题正确记满分。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_answer` VALUES (3, 1001, 3, '季风气候使我国大部分地区雨热同期，有利于农作物生长；但降水年际变化大，易造成旱涝灾害，需要水利调节。', '主观题关注雨热同期、旱涝波动和农业生产联系。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_answer` VALUES (4, 1001, 4, 'A', '单选题按唯一正确选项计分。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_answer` VALUES (5, 1001, 5, '欧洲西部常年受盛行西风影响，濒临大西洋并受北大西洋暖流增温增湿作用，且平原利于海洋水汽深入内陆，因此海洋性气候显著。', '主观题关注西风、洋流、海陆位置和地形。', 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_answer` VALUES (6, 1001, 6, 'B', '单选题，完全匹配得满分。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_answer` VALUES (7, 1001, 7, 'TRUE', '判断题，标准值可用 TRUE/FALSE。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_answer` VALUES (8, 1001, 8, '雨热同期有利于作物生长，但降水不稳定也会带来旱涝风险。', '主观题，可根据要点完整性和表达清晰度评分。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_answer` VALUES (9, 1001, 9, 'A', '单选题，完全匹配得满分。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_answer` VALUES (10, 1001, 10, '盛行西风和北大西洋暖流把海洋湿润空气带向内陆。', '主观题，关注成因是否准确、表述是否完整。', 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);

-- ----------------------------
-- Table structure for qb_question_choice
-- ----------------------------
DROP TABLE IF EXISTS `qb_question_choice`;
CREATE TABLE `qb_question_choice`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `choice_key` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `choice_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `correct` tinyint NOT NULL DEFAULT 0,
  `sort_no` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qb_choice_tenant_question`(`tenant_id` ASC, `question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_question_choice
-- ----------------------------
INSERT INTO `qb_question_choice` VALUES (1, 1001, 1, 'A', '西藏自治区', 0, 0, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (2, 1001, 1, 'B', '新疆维吾尔自治区', 1, 1, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (3, 1001, 1, 'C', '内蒙古自治区', 0, 2, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (4, 1001, 1, 'D', '青海省', 0, 3, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (5, 1001, 4, 'A', '尼罗河', 1, 0, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (6, 1001, 4, 'B', '刚果河', 0, 1, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (7, 1001, 4, 'C', '亚马孙河', 0, 2, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (8, 1001, 4, 'D', '多瑙河', 0, 3, 1, 3, '2026-04-15 10:01:48', 3, '2026-04-15 10:01:48', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (9, 1001, 6, 'A', '西藏', 0, 0, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (10, 1001, 6, 'B', '新疆', 1, 1, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (11, 1001, 6, 'C', '青海', 0, 2, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (12, 1001, 6, 'D', '四川', 0, 3, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (13, 1001, 9, 'A', '非洲', 1, 0, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (14, 1001, 9, 'B', '欧洲', 0, 1, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (15, 1001, 9, 'C', '亚洲', 0, 2, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);
INSERT INTO `qb_question_choice` VALUES (16, 1001, 9, 'D', '南美洲', 0, 3, 1, 3, '2026-04-16 13:11:25', 3, '2026-04-16 14:11:44', 0, NULL);

-- ----------------------------
-- Table structure for school_class
-- ----------------------------
DROP TABLE IF EXISTS `school_class`;
CREATE TABLE `school_class`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `class_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `class_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `stage_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `grade_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `homeroom_teacher_id` bigint NULL DEFAULT NULL,
  `headcount` int NULL DEFAULT NULL,
  `enrollment_year` int NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PREPARING',
  `resource_space_id` bigint NULL DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_class_code`(`tenant_id` ASC, `class_code` ASC) USING BTREE,
  INDEX `idx_tenant_status`(`tenant_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_tenant_stage`(`tenant_id` ASC, `stage_code` ASC) USING BTREE,
  INDEX `idx_tenant_grade`(`tenant_id` ASC, `grade_code` ASC) USING BTREE,
  INDEX `idx_tenant_homeroom_teacher`(`tenant_id` ASC, `homeroom_teacher_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11003 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'School class master' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school_class
-- ----------------------------
INSERT INTO `school_class` VALUES (11001, 1001, 1001, 'GC-G7-01', '七年级一班', 'JUNIOR', 'G7', 3, 4, 2026, 'ACTIVE', NULL, '中文测试班级：地球自转与地图基础', 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class` VALUES (11002, 1001, 1001, 'GC-G8-02', '八年级二班', 'JUNIOR', 'G8', 3, 3, 2026, 'ACTIVE', NULL, '中文测试班级：季风气候与区域发展', 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);

-- ----------------------------
-- Table structure for school_class_course
-- ----------------------------
DROP TABLE IF EXISTS `school_class_course`;
CREATE TABLE `school_class_course`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `relation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PRIMARY',
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_class_course`(`tenant_id` ASC, `class_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_tenant_course`(`tenant_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_tenant_class_course`(`tenant_id` ASC, `class_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Class course relation' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school_class_course
-- ----------------------------
INSERT INTO `school_class_course` VALUES (1, 1001, 11001, 701, 'PRIMARY', 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_course` VALUES (2, 1001, 11001, 702, 'SECONDARY', 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_course` VALUES (3, 1001, 11001, 707, 'EXTENSION', 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_course` VALUES (4, 1001, 11002, 703, 'PRIMARY', 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_course` VALUES (5, 1001, 11002, 704, 'PRIMARY', 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_course` VALUES (6, 1001, 11002, 708, 'EXTENSION', 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);

-- ----------------------------
-- Table structure for school_class_student
-- ----------------------------
DROP TABLE IF EXISTS `school_class_student`;
CREATE TABLE `school_class_student`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `student_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `join_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `joined_time` datetime NULL DEFAULT NULL,
  `left_time` datetime NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_class_student`(`tenant_id` ASC, `class_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_tenant_student`(`tenant_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_tenant_class_join_status`(`tenant_id` ASC, `class_id` ASC, `join_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Class student relation' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school_class_student
-- ----------------------------
INSERT INTO `school_class_student` VALUES (1, 1001, 11001, 4, 'GC-STU-000', 'ACTIVE', '2026-04-20 14:37:36', NULL, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_student` VALUES (2, 1001, 11001, 2044375386258702338, 'GC-STU-002', 'ACTIVE', '2026-04-20 14:37:36', NULL, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_student` VALUES (3, 1001, 11001, 2044375386258702339, 'GC-STU-003', 'ACTIVE', '2026-04-20 14:37:36', NULL, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_student` VALUES (4, 1001, 11001, 2044375386258702340, 'GC-STU-004', 'ACTIVE', '2026-04-20 14:37:36', NULL, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_student` VALUES (5, 1001, 11002, 2044339999999990001, 'GC-STU-001', 'ACTIVE', '2026-04-20 14:37:36', NULL, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_student` VALUES (6, 1001, 11002, 2044375386258702341, 'GC-STU-005', 'ACTIVE', '2026-04-20 14:37:36', NULL, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_student` VALUES (7, 1001, 11002, 2044375386258702342, 'GC-STU-006', 'ACTIVE', '2026-04-20 14:37:36', NULL, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);

-- ----------------------------
-- Table structure for school_class_teacher
-- ----------------------------
DROP TABLE IF EXISTS `school_class_teacher`;
CREATE TABLE `school_class_teacher`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `role_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_primary` tinyint NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_class_teacher_role`(`tenant_id` ASC, `class_id` ASC, `teacher_id` ASC, `role_code` ASC) USING BTREE,
  INDEX `idx_tenant_teacher`(`tenant_id` ASC, `teacher_id` ASC) USING BTREE,
  INDEX `idx_class_teacher`(`tenant_id` ASC, `class_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Class teacher relation' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school_class_teacher
-- ----------------------------
INSERT INTO `school_class_teacher` VALUES (1, 1001, 11001, 3, 'HOMEROOM', 1, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_teacher` VALUES (2, 1001, 11001, 3, 'SUBJECT', 0, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_teacher` VALUES (3, 1001, 11002, 3, 'HOMEROOM', 1, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);
INSERT INTO `school_class_teacher` VALUES (4, 1001, 11002, 3, 'SUBJECT', 0, 2, '2026-04-20 14:37:36', 2, '2026-04-20 14:37:36', 0);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `parent_id` bigint NOT NULL DEFAULT 0,
  `menu_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `menu_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `route_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `component_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `permission_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT 0,
  `visible` tinyint NOT NULL DEFAULT 1,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '???????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 1001, 0, 'MENU', '数据看板', '/dashboard', 'admin/dashboard/index', 'dashboard:view', 'dashboard', 10, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_menu` VALUES (2, 1001, 0, 'CATALOG', '平台配置', '/platform', 'Layout', NULL, 'setting', 20, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_menu` VALUES (3, 1001, 2, 'MENU', '基础配置', '/platform/config', 'admin/platform/config/index', 'platform:config:view', 'tool', 21, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_menu` VALUES (4, 1001, 2, 'MENU', '机构入驻审核', '/platform/institutions/review', 'admin/institution/review/index', 'organization:review:list', 'audit', 22, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_menu` VALUES (5, 1001, 2, 'MENU', '课程审核', '/platform/courses/review', 'admin/course/review/index', 'course:review:list', 'book', 23, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_menu` VALUES (6, 1001, 0, 'MENU', '用户管理', '/users', 'admin/user/index', 'user:list', 'user', 30, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_menu` VALUES (7, 1001, 0, 'MENU', '统计分析', '/analytics', 'admin/analytics/index', 'analytics:dashboard:view', 'chart', 40, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_menu` VALUES (11, 1001, 0, 'MENU', '学校看板', '/school/dashboard', 'school/dashboard/index', 'school:dashboard:view', 'school-dashboard', 110, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_menu` VALUES (12, 1001, 0, 'MENU', '教师管理', '/school/teachers', 'school/teacher/index', 'school:teacher:list', 'teacher', 120, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_menu` VALUES (13, 1001, 0, 'MENU', '课程管理', '/school/courses', 'school/course/index', 'school:course:list', 'course', 130, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_menu` VALUES (14, 1001, 0, 'MENU', '学生管理', '/school/students', 'school/student/index', 'school:student:list', 'student', 140, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_menu` VALUES (15, 1001, 0, 'MENU', '班级管理', '/school/classes', 'school/class/index', 'school:class:list', 'class', 150, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_menu` VALUES (16, 1001, 0, 'MENU', '教学安排', '/school/schedules', 'school/schedule/index', 'school:schedule:view', 'schedule', 160, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_menu` VALUES (21, 1001, 0, 'MENU', '教师看板', '/teacher/dashboard', 'teacher/dashboard/index', 'teacher:dashboard:view', 'dashboard', 210, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '教师工作台首页');
INSERT INTO `sys_menu` VALUES (22, 1001, 0, 'MENU', '我的课程', '/teacher/courses', 'teacher/course/index', 'teacher:course:list', 'course', 220, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '已审核课程管理');
INSERT INTO `sys_menu` VALUES (23, 1001, 0, 'MENU', '学生管理', '/teacher/students', 'teacher/student/index', 'teacher:student:list', 'student', 230, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '管理授课学生');
INSERT INTO `sys_menu` VALUES (24, 1001, 0, 'MENU', '成绩录入', '/teacher/grades', 'teacher/grade/index', 'teacher:grade:write', 'score', 240, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '录入并提交成绩');
INSERT INTO `sys_menu` VALUES (25, 1001, 0, 'MENU', '资源发布', '/teacher/resources', 'teacher/resource/index', 'teacher:resource:publish', 'resource', 250, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '发布教学资源');
INSERT INTO `sys_menu` VALUES (31, 1001, 0, 'MENU', '学生首页', '/student/dashboard', 'student/dashboard/index', 'student:dashboard:view', 'dashboard', 310, 1, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '学生端首页');
INSERT INTO `sys_menu` VALUES (32, 1001, 0, 'MENU', '本校免费课程', '/student/courses', 'student/course-selection/index', 'student:course:select', 'course', 320, 1, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '学习本校免费课程');
INSERT INTO `sys_menu` VALUES (33, 1001, 0, 'MENU', '作业中心', '/student/assignments', 'student/assignment/index', 'student:assignment:submit', 'assignment', 330, 1, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '提交课程作业');
INSERT INTO `sys_menu` VALUES (34, 1001, 0, 'MENU', '成绩查询', '/student/grades', 'student/grade/index', 'student:grade:view', 'score', 340, 1, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '查看课程成绩');
INSERT INTO `sys_menu` VALUES (35, 1001, 0, 'MENU', '校园公告', '/student/announcements', 'student/announcement/index', 'student:announcement:view', 'announcement', 350, 1, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '查看校园公告');
INSERT INTO `sys_menu` VALUES (41, 1001, 0, 'MENU', '课程广场', '/consumer/courses', 'consumer/course-plaza/index', 'consumer:course:market:view', 'course', 410, 1, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '跨校课程广场');
INSERT INTO `sys_menu` VALUES (42, 1001, 0, 'MENU', '学校发现', '/consumer/schools', 'consumer/school/index', 'consumer:school:browse', 'school', 420, 1, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '浏览学校课程');
INSERT INTO `sys_menu` VALUES (43, 1001, 0, 'MENU', '订单中心', '/consumer/orders', 'consumer/order/index', 'consumer:order:list', 'order', 430, 1, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '查看订单与支付记录');
INSERT INTO `sys_menu` VALUES (44, 1001, 0, 'MENU', '已购课程', '/consumer/learning', 'consumer/learning/index', 'consumer:learning:enter', 'learning', 440, 1, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '进入已购课程学习');
INSERT INTO `sys_menu` VALUES (45, 1001, 0, 'MENU', '学习档案', '/consumer/profile', 'consumer/profile/index', 'consumer:profile:view', 'profile', 450, 1, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '查看个人学习档案');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '?????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 1001, 'admin', '平台管理员', 'platform', 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员角色');
INSERT INTO `sys_role` VALUES (2, 1001, 'school_admin', '学校管理员', 'school', 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员角色');
INSERT INTO `sys_role` VALUES (3, 1001, 'teacher', '教师', 'school', 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '教师端角色');
INSERT INTO `sys_role` VALUES (4, 1001, 'student', '学生', 'school', 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '学生端角色');
INSERT INTO `sys_role` VALUES (5, 1001, 'consumer', '普通用户', 'market', 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '普通用户端角色');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '???????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1001, 1, 1, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (2, 1001, 1, 2, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (3, 1001, 1, 3, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (4, 1001, 1, 4, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (5, 1001, 1, 5, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (6, 1001, 1, 6, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (7, 1001, 1, 7, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '平台管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (11, 1001, 2, 11, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (12, 1001, 2, 12, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (13, 1001, 2, 13, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (14, 1001, 2, 14, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (15, 1001, 2, 15, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (16, 1001, 2, 16, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '学校管理员后台初始化');
INSERT INTO `sys_role_menu` VALUES (21, 1001, 3, 21, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '教师菜单授权');
INSERT INTO `sys_role_menu` VALUES (22, 1001, 3, 22, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '教师菜单授权');
INSERT INTO `sys_role_menu` VALUES (23, 1001, 3, 23, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '教师菜单授权');
INSERT INTO `sys_role_menu` VALUES (24, 1001, 3, 24, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '教师菜单授权');
INSERT INTO `sys_role_menu` VALUES (25, 1001, 3, 25, 1, NULL, '2026-04-14 10:46:25', NULL, '2026-04-14 10:46:25', 0, '教师菜单授权');
INSERT INTO `sys_role_menu` VALUES (31, 1001, 4, 31, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '学生菜单授权');
INSERT INTO `sys_role_menu` VALUES (32, 1001, 4, 32, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '学生菜单授权');
INSERT INTO `sys_role_menu` VALUES (33, 1001, 4, 33, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '学生菜单授权');
INSERT INTO `sys_role_menu` VALUES (34, 1001, 4, 34, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '学生菜单授权');
INSERT INTO `sys_role_menu` VALUES (35, 1001, 4, 35, 1, NULL, '2026-04-14 10:50:09', NULL, '2026-04-14 10:50:09', 0, '学生菜单授权');
INSERT INTO `sys_role_menu` VALUES (41, 1001, 5, 41, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '普通用户菜单授权');
INSERT INTO `sys_role_menu` VALUES (42, 1001, 5, 42, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '普通用户菜单授权');
INSERT INTO `sys_role_menu` VALUES (43, 1001, 5, 43, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '普通用户菜单授权');
INSERT INTO `sys_role_menu` VALUES (44, 1001, 5, 44, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '普通用户菜单授权');
INSERT INTO `sys_role_menu` VALUES (45, 1001, 5, 45, 1, NULL, '2026-04-14 10:59:00', NULL, '2026-04-14 10:59:00', 0, '普通用户菜单授权');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `salt` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` tinyint NULL DEFAULT NULL,
  `user_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `register_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_login_time` timestamp NULL DEFAULT NULL,
  `last_login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2044375386258702343 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '?????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 1001, 'GC-ADMIN-001', 'admin', '平台管理员', '系统管理员', '123456', NULL, '13900000001', 'admin@geocourse.demo', NULL, NULL, 'admin', NULL, '2026-04-20 10:19:27', '::1', 1, NULL, '2026-04-14 10:46:25', 1, '2026-04-20 10:19:27', 0, '平台演示账号，密码123456');
INSERT INTO `sys_user` VALUES (2, 1001, 'GC-SA-000', 'school_admin', '学校管理员', '王敏', '123456', NULL, '13900000002', 'schooladmin@geocourse.demo', NULL, NULL, 'admin', NULL, '2026-04-20 11:11:08', '::1', 1, NULL, '2026-04-14 10:46:25', 1, '2026-04-20 11:11:08', 0, '学校管理端演示账号，密码123456');
INSERT INTO `sys_user` VALUES (3, 1001, 'GC-TCH-000', 'teacher', '张明老师', '张明', '123456', NULL, '13900000003', 'teacher@geocourse.demo', 'http://192.168.200.129:9000/geocourse/courses/profile/2026/0416/avatar-c3e9671dd63d4fb589a641a7be1f5259.png', NULL, 'teacher', NULL, '2026-04-20 11:14:22', '::1', 1, NULL, '2026-04-14 10:46:25', 1, '2026-04-20 11:14:22', 0, '教师工作台演示账号，密码123456');
INSERT INTO `sys_user` VALUES (4, 1001, 'GC-STU-000', 'student', '李晓雨', '李晓雨', '123456', NULL, '13900000004', 'student@geocourse.demo', NULL, NULL, 'student', NULL, '2026-04-20 10:28:57', '::1', 1, NULL, '2026-04-14 10:50:09', 1, '2026-04-20 10:28:57', 0, '学生端演示账号，密码123456');
INSERT INTO `sys_user` VALUES (5, 1001, 'GC-CSM-000', 'consumer', '陈一帆', '陈一帆', '123456', NULL, '13900000005', 'consumer@geocourse.demo', NULL, NULL, 'consumer', NULL, '2026-04-17 13:40:57', '::1', 1, NULL, '2026-04-14 10:59:00', 1, '2026-04-17 13:40:57', 0, '普通用户演示账号，密码123456');
INSERT INTO `sys_user` VALUES (2044331736963289090, 1001, 'GC-CSM-001', '周沐晨', '周沐晨', '周沐晨', '123456', NULL, '13900001001', 'consumer01@geocourse.demo', NULL, NULL, 'consumer', 'frontend-register', '2026-04-15 16:27:23', '0:0:0:0:0:0:0:1', 1, NULL, '2026-04-15 16:27:23', 1, '2026-04-15 16:27:23', 0, '{\"schoolName\":\"示范中学\"}');
INSERT INTO `sys_user` VALUES (2044339999999990001, 1001, 'GC-STU-001', '周子航', '周子航', '周子航', '123456', NULL, '13900002001', 'student01@geocourse.demo', NULL, NULL, 'student', 'manual-test', NULL, NULL, 1, NULL, '2026-04-15 16:43:32', 1, '2026-04-15 16:43:32', 0, '{\"schoolName\":\"示范中学\",\"gradeClass\":\"八年级二班\"}');
INSERT INTO `sys_user` VALUES (2044342785548496898, 1001, 'GC-CSM-002', '林可心', '林可心', '林可心', '123456', NULL, '13900001002', 'consumer02@geocourse.demo', NULL, NULL, 'consumer', 'frontend-register', '2026-04-15 19:21:02', '0:0:0:0:0:0:0:1', 1, NULL, '2026-04-15 17:11:17', 1, '2026-04-15 19:21:02', 0, '{\"schoolName\":\"示范中学\"}');
INSERT INTO `sys_user` VALUES (2044375386258702337, 1001, 'GC-SA-001', '赵雪梅', '赵雪梅主任', '赵雪梅', '123456', NULL, '13900000012', 'schooladmin01@geocourse.demo', NULL, NULL, 'admin', 'frontend-register', '2026-04-15 19:20:50', '0:0:0:0:0:0:0:1', 1, NULL, '2026-04-15 19:20:49', 1, '2026-04-15 19:20:50', 0, '{\"schoolName\":\"示范中学\"}');
INSERT INTO `sys_user` VALUES (2044375386258702338, 1001, 'GC-STU-002', '王若彤', '王若彤', '王若彤', '123456', NULL, '13900002002', 'student02@geocourse.demo', NULL, NULL, 'student', 'frontend-register', NULL, NULL, 1, NULL, '2026-04-15 20:05:20', 1, '2026-04-15 20:05:20', 0, '{\"schoolName\":\"示范中学\",\"gradeClass\":\"七年级一班\"}');
INSERT INTO `sys_user` VALUES (2044375386258702339, 1001, 'GC-STU-003', '林书瑶', '林书瑶', '林书瑶', '123456', NULL, '13900002003', 'student03@geocourse.demo', NULL, NULL, 'student', 'frontend-register', '2026-04-16 14:12:56', '::1', 1, NULL, '2026-04-15 23:00:15', 1, '2026-04-16 14:12:56', 0, '{\"schoolName\":\"示范中学\",\"gradeClass\":\"七年级一班\"}');
INSERT INTO `sys_user` VALUES (2044375386258702340, 1001, 'GC-STU-004', '孙浩然', '孙浩然', '孙浩然', '123456', NULL, '13900002004', 'student04@geocourse.demo', NULL, NULL, 'student', 'school-roster', NULL, NULL, 1, NULL, '2026-04-17 16:13:41', 1, '2026-04-17 16:13:41', 0, '{\"schoolName\":\"示范中学\",\"gradeClass\":\"七年级一班\"}');
INSERT INTO `sys_user` VALUES (2044375386258702341, 1001, 'GC-STU-005', '陈思齐', '陈思齐', '陈思齐', '123456', NULL, '13900002005', 'student05@geocourse.demo', NULL, NULL, 'student', 'school-roster', NULL, NULL, 1, NULL, '2026-04-17 16:50:11', 1, '2026-04-17 16:50:11', 0, '{\"schoolName\":\"示范中学\",\"gradeClass\":\"八年级二班\"}');
INSERT INTO `sys_user` VALUES (2044375386258702342, 1001, 'GC-STU-006', '赵子涵', '赵子涵', '赵子涵', '123456', NULL, '13900002006', 'student06@geocourse.demo', NULL, NULL, 'student', 'school-roster', NULL, NULL, 1, NULL, '2026-04-17 16:51:23', 1, '2026-04-17 16:51:23', 0, '{\"schoolName\":\"示范中学\",\"gradeClass\":\"八年级二班\"}');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2044375386598440968 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '???????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1001, 1, 1, 1, NULL, '2026-04-14 10:46:25', 1, '2026-04-14 10:46:25', 0, '平台管理员角色绑定');
INSERT INTO `sys_user_role` VALUES (2, 1001, 2, 2, 1, NULL, '2026-04-14 10:46:25', 1, '2026-04-14 10:46:25', 0, '学校管理员角色绑定');
INSERT INTO `sys_user_role` VALUES (3, 1001, 3, 3, 1, NULL, '2026-04-14 10:46:25', 1, '2026-04-14 10:46:25', 0, '教师角色绑定');
INSERT INTO `sys_user_role` VALUES (4, 1001, 4, 4, 1, NULL, '2026-04-14 10:50:09', 1, '2026-04-14 10:50:09', 0, '学生角色绑定');
INSERT INTO `sys_user_role` VALUES (5, 1001, 5, 5, 1, NULL, '2026-04-14 10:59:00', 1, '2026-04-14 10:59:00', 0, '普通用户角色绑定');
INSERT INTO `sys_user_role` VALUES (2044331737013620737, 1001, 2044331736963289090, 5, 1, NULL, '2026-04-15 16:27:23', 1, '2026-04-15 16:27:23', 0, '普通用户角色绑定');
INSERT INTO `sys_user_role` VALUES (2044339999999990002, 1001, 2044339999999990001, 4, 1, NULL, '2026-04-15 16:43:32', 1, '2026-04-15 16:43:32', 0, '学生角色绑定');
INSERT INTO `sys_user_role` VALUES (2044342786064396290, 1001, 2044342785548496898, 5, 1, NULL, '2026-04-15 17:11:17', 1, '2026-04-15 17:11:17', 0, '普通用户角色绑定');
INSERT INTO `sys_user_role` VALUES (2044375386598440962, 1001, 2044375386258702337, 2, 1, NULL, '2026-04-15 19:20:49', 1, '2026-04-15 19:20:49', 0, '学校管理员角色绑定');
INSERT INTO `sys_user_role` VALUES (2044375386598440963, 1001, 2044375386258702338, 4, 1, NULL, '2026-04-15 20:05:20', 1, '2026-04-15 20:05:20', 0, '学生角色绑定');
INSERT INTO `sys_user_role` VALUES (2044375386598440964, 1001, 2044375386258702339, 4, 1, NULL, '2026-04-15 23:00:15', 1, '2026-04-15 23:00:15', 0, '学生角色绑定');
INSERT INTO `sys_user_role` VALUES (2044375386598440965, 1001, 2044375386258702340, 4, 1, NULL, '2026-04-17 16:13:41', 1, '2026-04-17 16:13:41', 0, '学生角色绑定');
INSERT INTO `sys_user_role` VALUES (2044375386598440966, 1001, 2044375386258702341, 4, 1, NULL, '2026-04-17 16:50:11', 1, '2026-04-17 16:50:11', 0, '学生角色绑定');
INSERT INTO `sys_user_role` VALUES (2044375386598440967, 1001, 2044375386258702342, 4, 1, NULL, '2026-04-17 16:51:23', 1, '2026-04-17 16:51:23', 0, '学生角色绑定');

SET FOREIGN_KEY_CHECKS = 1;
