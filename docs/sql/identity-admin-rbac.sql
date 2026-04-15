CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_menu` (`tenant_id`,`role_id`,`menu_id`),
  KEY `idx_sys_role_menu_role` (`tenant_id`,`role_id`,`status`),
  KEY `idx_sys_role_menu_menu` (`tenant_id`,`menu_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'MENU', '数据看板', '/dashboard', 'admin/dashboard/index', 'dashboard:view', 'dashboard', 10, 1, 1, '平台管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/dashboard');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'CATALOG', '平台配置', '/platform', 'Layout', NULL, 'setting', 20, 1, 1, '平台管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/platform');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, (SELECT `id` FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/platform' LIMIT 1), 'MENU', '基础配置', '/platform/config', 'admin/platform/config/index', 'platform:config:view', 'tool', 21, 1, 1, '平台管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/platform/config');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, (SELECT `id` FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/platform' LIMIT 1), 'MENU', '机构入驻审核', '/platform/institutions/review', 'admin/institution/review/index', 'organization:review:list', 'audit', 22, 1, 1, '平台管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/platform/institutions/review');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, (SELECT `id` FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/platform' LIMIT 1), 'MENU', '课程审核', '/platform/courses/review', 'admin/course/review/index', 'course:review:list', 'book', 23, 1, 1, '平台管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/platform/courses/review');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'MENU', '用户管理', '/users', 'admin/user/index', 'user:list', 'user', 30, 1, 1, '平台管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/users');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'MENU', '统计分析', '/analytics', 'admin/analytics/index', 'analytics:dashboard:view', 'chart', 40, 1, 1, '平台管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/analytics');

INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`, `status`, `remark`)
SELECT 1001, r.`id`, m.`id`, 1, '平台管理员后台初始化'
FROM `sys_role` r
JOIN `sys_menu` m ON m.`tenant_id` = 1001
WHERE r.`tenant_id` = 1001
  AND r.`role_code` = 'admin'
  AND m.`route_path` IN ('/dashboard', '/platform', '/platform/config', '/platform/institutions/review', '/platform/courses/review', '/users', '/analytics')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.`tenant_id` = 1001 AND rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`
  );
