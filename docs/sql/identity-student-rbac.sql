INSERT INTO sys_role (id, tenant_id, role_code, role_name, role_type, status, deleted, remark)
SELECT 4, 1001, 'student', '学生', 'school', 1, 0, '学生端角色'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE tenant_id = 1001 AND id = 4);

INSERT INTO sys_user (id, tenant_id, user_no, username, nickname, real_name, password_hash, user_type, status, deleted, remark)
SELECT 4, 1001, 'U0004', 'student_bj', '李晓雨', '李晓雨', '123456', 'student', 1, 0, '学生演示账号'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE tenant_id = 1001 AND id = 4);

INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, status, deleted, remark)
SELECT 4, 1001, 4, 4, 1, 0, '学生角色绑定'
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE tenant_id = 1001 AND user_id = 4 AND role_id = 4);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 31, 1001, 0, 'MENU', '学生首页', '/student/dashboard', 'student/dashboard/index', 'student:dashboard:view', 'dashboard', 310, 1, 1, 0, '学生端首页'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 31);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 32, 1001, 0, 'MENU', '本校免费课程', '/student/courses', 'student/course-selection/index', 'student:course:select', 'course', 320, 1, 1, 0, '学习本校免费课程'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 32);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 33, 1001, 0, 'MENU', '作业中心', '/student/assignments', 'student/assignment/index', 'student:assignment:submit', 'assignment', 330, 1, 1, 0, '提交课程作业'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 33);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 34, 1001, 0, 'MENU', '成绩查询', '/student/grades', 'student/grade/index', 'student:grade:view', 'score', 340, 1, 1, 0, '查看课程成绩'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 34);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 35, 1001, 0, 'MENU', '校园公告', '/student/announcements', 'student/announcement/index', 'student:announcement:view', 'announcement', 350, 1, 1, 0, '查看校园公告'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 35);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 31, 1001, 4, 31, 1, 0, '学生菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 4 AND menu_id = 31);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 32, 1001, 4, 32, 1, 0, '学生菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 4 AND menu_id = 32);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 33, 1001, 4, 33, 1, 0, '学生菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 4 AND menu_id = 33);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 34, 1001, 4, 34, 1, 0, '学生菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 4 AND menu_id = 34);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 35, 1001, 4, 35, 1, 0, '学生菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 4 AND menu_id = 35);
