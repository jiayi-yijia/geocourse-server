INSERT INTO sys_role (id, tenant_id, role_code, role_name, role_type, status, deleted, remark)
SELECT 5, 1001, 'consumer', '普通用户', 'market', 1, 0, '普通用户端角色'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE tenant_id = 1001 AND id = 5);

INSERT INTO sys_user (id, tenant_id, user_no, username, nickname, real_name, password_hash, user_type, status, deleted, remark)
SELECT 5, 1001, 'U0005', 'consumer_bj', '陈一帆', '陈一帆', '123456', 'consumer', 1, 0, '普通用户演示账号'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE tenant_id = 1001 AND id = 5);

INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, status, deleted, remark)
SELECT 5, 1001, 5, 5, 1, 0, '普通用户角色绑定'
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE tenant_id = 1001 AND user_id = 5 AND role_id = 5);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 41, 1001, 0, 'MENU', '课程广场', '/consumer/courses', 'consumer/course-plaza/index', 'consumer:course:market:view', 'course', 410, 1, 1, 0, '跨校课程广场'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 41);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 42, 1001, 0, 'MENU', '学校发现', '/consumer/schools', 'consumer/school/index', 'consumer:school:browse', 'school', 420, 1, 1, 0, '浏览学校课程'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 42);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 43, 1001, 0, 'MENU', '订单中心', '/consumer/orders', 'consumer/order/index', 'consumer:order:list', 'order', 430, 1, 1, 0, '查看订单与支付记录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 43);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 44, 1001, 0, 'MENU', '已购课程', '/consumer/learning', 'consumer/learning/index', 'consumer:learning:enter', 'learning', 440, 1, 1, 0, '进入已购课程学习'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 44);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_no, visible, status, deleted, remark)
SELECT 45, 1001, 0, 'MENU', '学习档案', '/consumer/profile', 'consumer/profile/index', 'consumer:profile:view', 'profile', 450, 1, 1, 0, '查看个人学习档案'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE tenant_id = 1001 AND id = 45);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 41, 1001, 5, 41, 1, 0, '普通用户菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 5 AND menu_id = 41);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 42, 1001, 5, 42, 1, 0, '普通用户菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 5 AND menu_id = 42);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 43, 1001, 5, 43, 1, 0, '普通用户菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 5 AND menu_id = 43);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 44, 1001, 5, 44, 1, 0, '普通用户菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 5 AND menu_id = 44);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status, deleted, remark)
SELECT 45, 1001, 5, 45, 1, 0, '普通用户菜单授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE tenant_id = 1001 AND role_id = 5 AND menu_id = 45);
