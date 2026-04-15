INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'MENU', '学校看板', '/school/dashboard', 'school/dashboard/index', 'school:dashboard:view', 'school-dashboard', 110, 1, 1, '学校管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/school/dashboard');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'MENU', '教师管理', '/school/teachers', 'school/teacher/index', 'school:teacher:list', 'teacher', 120, 1, 1, '学校管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/school/teachers');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'MENU', '课程管理', '/school/courses', 'school/course/index', 'school:course:list', 'course', 130, 1, 1, '学校管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/school/courses');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'MENU', '学生管理', '/school/students', 'school/student/index', 'school:student:list', 'student', 140, 1, 1, '学校管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/school/students');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'MENU', '班级管理', '/school/classes', 'school/class/index', 'school:class:list', 'class', 150, 1, 1, '学校管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/school/classes');

INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`)
SELECT 1001, 0, 'MENU', '教学安排', '/school/schedules', 'school/schedule/index', 'school:schedule:view', 'schedule', 160, 1, 1, '学校管理员后台初始化'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `tenant_id` = 1001 AND `route_path` = '/school/schedules');

INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`, `status`, `remark`)
SELECT 1001, r.`id`, m.`id`, 1, '学校管理员后台初始化'
FROM `sys_role` r
JOIN `sys_menu` m ON m.`tenant_id` = 1001
WHERE r.`tenant_id` = 1001
  AND r.`role_code` = 'school_admin'
  AND m.`route_path` IN ('/school/dashboard', '/school/teachers', '/school/courses', '/school/students', '/school/classes', '/school/schedules')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.`tenant_id` = 1001 AND rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`
  );
