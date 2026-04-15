SET NAMES utf8mb4;

ALTER DATABASE geocourse CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE sys_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE sys_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE sys_user_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE sys_menu CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE sys_role_menu CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Notes:
-- 1. If a value has already been stored as '?', MySQL has lost the original bytes.
--    The only reliable fix is to write the correct Chinese text again.
-- 2. This script focuses on the built-in identity demo data and menu metadata.

UPDATE sys_role
SET role_name = '学生',
    remark = '学生端角色'
WHERE tenant_id = 1001 AND role_code = 'student';

UPDATE sys_role
SET role_name = '普通用户',
    remark = '普通用户端角色'
WHERE tenant_id = 1001 AND role_code = 'consumer';

UPDATE sys_user
SET nickname = '李晓雨',
    real_name = '李晓雨',
    remark = '学生演示账号'
WHERE tenant_id = 1001 AND username = 'student_bj';

UPDATE sys_user
SET nickname = '陈一帆',
    real_name = '陈一帆',
    remark = '普通用户演示账号'
WHERE tenant_id = 1001 AND username = 'consumer_bj';

UPDATE sys_user_role
SET remark = '学生角色绑定'
WHERE tenant_id = 1001 AND user_id = 4 AND role_id = 4;

UPDATE sys_user_role
SET remark = '普通用户角色绑定'
WHERE tenant_id = 1001 AND user_id = 5 AND role_id = 5;

UPDATE sys_menu
SET menu_name = '数据看板',
    remark = '平台管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/dashboard';

UPDATE sys_menu
SET menu_name = '平台配置',
    remark = '平台管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/platform';

UPDATE sys_menu
SET menu_name = '基础配置',
    remark = '平台管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/platform/config';

UPDATE sys_menu
SET menu_name = '机构入驻审核',
    remark = '平台管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/platform/institutions/review';

UPDATE sys_menu
SET menu_name = '课程审核',
    remark = '平台管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/platform/courses/review';

UPDATE sys_menu
SET menu_name = '用户管理',
    remark = '平台管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/users';

UPDATE sys_menu
SET menu_name = '统计分析',
    remark = '平台管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/analytics';

UPDATE sys_menu
SET menu_name = '学校看板',
    remark = '学校管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/school/dashboard';

UPDATE sys_menu
SET menu_name = '教师管理',
    remark = '学校管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/school/teachers';

UPDATE sys_menu
SET menu_name = '课程管理',
    remark = '学校管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/school/courses';

UPDATE sys_menu
SET menu_name = '学生管理',
    remark = '学校管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/school/students';

UPDATE sys_menu
SET menu_name = '班级管理',
    remark = '学校管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/school/classes';

UPDATE sys_menu
SET menu_name = '教学安排',
    remark = '学校管理员后台初始化'
WHERE tenant_id = 1001 AND route_path = '/school/schedules';

UPDATE sys_menu
SET menu_name = '教师看板',
    remark = '教师工作台首页'
WHERE tenant_id = 1001 AND route_path = '/teacher/dashboard';

UPDATE sys_menu
SET menu_name = '我的课程',
    remark = '已审核课程管理'
WHERE tenant_id = 1001 AND route_path = '/teacher/courses';

UPDATE sys_menu
SET menu_name = '学生管理',
    remark = '管理授课学生'
WHERE tenant_id = 1001 AND route_path = '/teacher/students';

UPDATE sys_menu
SET menu_name = '成绩录入',
    remark = '录入并提交成绩'
WHERE tenant_id = 1001 AND route_path = '/teacher/grades';

UPDATE sys_menu
SET menu_name = '资源发布',
    remark = '发布教学资源'
WHERE tenant_id = 1001 AND route_path = '/teacher/resources';

UPDATE sys_menu
SET menu_name = '学生首页',
    remark = '学生端首页'
WHERE tenant_id = 1001 AND route_path = '/student/dashboard';

UPDATE sys_menu
SET menu_name = '本校免费课程',
    remark = '学习本校免费课程'
WHERE tenant_id = 1001 AND route_path = '/student/courses';

UPDATE sys_menu
SET menu_name = '作业中心',
    remark = '提交课程作业'
WHERE tenant_id = 1001 AND route_path = '/student/assignments';

UPDATE sys_menu
SET menu_name = '成绩查询',
    remark = '查看课程成绩'
WHERE tenant_id = 1001 AND route_path = '/student/grades';

UPDATE sys_menu
SET menu_name = '校园公告',
    remark = '查看校园公告'
WHERE tenant_id = 1001 AND route_path = '/student/announcements';

UPDATE sys_menu
SET menu_name = '课程广场',
    remark = '跨校课程广场'
WHERE tenant_id = 1001 AND route_path = '/consumer/courses';

UPDATE sys_menu
SET menu_name = '学校发现',
    remark = '浏览学校课程'
WHERE tenant_id = 1001 AND route_path = '/consumer/schools';

UPDATE sys_menu
SET menu_name = '订单中心',
    remark = '查看订单与支付记录'
WHERE tenant_id = 1001 AND route_path = '/consumer/orders';

UPDATE sys_menu
SET menu_name = '已购课程',
    remark = '进入已购课程学习'
WHERE tenant_id = 1001 AND route_path = '/consumer/learning';

UPDATE sys_menu
SET menu_name = '学习档案',
    remark = '查看个人学习档案'
WHERE tenant_id = 1001 AND route_path = '/consumer/profile';

UPDATE sys_role_menu
SET remark = '平台管理员后台初始化'
WHERE tenant_id = 1001 AND role_id = 1;

UPDATE sys_role_menu
SET remark = '学校管理员后台初始化'
WHERE tenant_id = 1001 AND role_id = 2;

UPDATE sys_role_menu
SET remark = '教师菜单授权'
WHERE tenant_id = 1001 AND role_id = 3;

UPDATE sys_role_menu
SET remark = '学生菜单授权'
WHERE tenant_id = 1001 AND role_id = 4;

UPDATE sys_role_menu
SET remark = '普通用户菜单授权'
WHERE tenant_id = 1001 AND role_id = 5;
