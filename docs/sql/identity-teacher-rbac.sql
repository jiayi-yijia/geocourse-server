CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_by BIGINT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark VARCHAR(255) NULL,
    UNIQUE KEY uk_sys_role_menu_tenant_role_menu (tenant_id, role_id, menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Role-menu mapping';
ALTER TABLE sys_role_menu COMMENT = '角色菜单关联表';

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 21, 1001, 0, 'MENU', '教师看板', '/teacher/dashboard', 'teacher/dashboard/index', 'teacher:dashboard:view', 'dashboard', 210, 1, 1, 0, '教师工作台首页'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 21);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 22, 1001, 0, 'MENU', '我的课程', '/teacher/courses', 'teacher/course/index', 'teacher:course:list', 'course', 220, 1, 1, 0, '已审核课程管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 22);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 23, 1001, 0, 'MENU', '学生管理', '/teacher/students', 'teacher/student/index', 'teacher:student:list', 'student', 230, 1, 1, 0, '管理授课学生'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 23);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 24, 1001, 0, 'MENU', '成绩录入', '/teacher/grades', 'teacher/grade/index', 'teacher:grade:write', 'score', 240, 1, 1, 0, '录入并提交成绩'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 24);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 25, 1001, 0, 'MENU', '资源发布', '/teacher/resources', 'teacher/resource/index', 'teacher:resource:publish', 'resource', 250, 1, 1, 0, '发布教学资源'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 25);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 21, 1001, 3, 21, 1, 0, '教师菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 3 AND menu_id = 21);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 22, 1001, 3, 22, 1, 0, '教师菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 3 AND menu_id = 22);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 23, 1001, 3, 23, 1, 0, '教师菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 3 AND menu_id = 23);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 24, 1001, 3, 24, 1, 0, '教师菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 3 AND menu_id = 24);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 25, 1001, 3, 25, 1, 0, '教师菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 3 AND menu_id = 25);
