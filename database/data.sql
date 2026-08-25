-- =====================================================================
-- 基于MQTT协议的企业设备运行监测与远程控制系统 初始化数据脚本
-- 说明: 应用启动时会通过 DataInitializer 自动初始化以下数据（含默认用户）。
--       本脚本用于手动初始化场景，与 DataInitializer 保持一致。
--       默认用户（密码均为 123456，由后端 BCrypt 编码后写入）:
--         admin / 123456   超级管理员
--         operator / 123456 运维人员
--         viewer / 123456  访客
-- =====================================================================

USE `device_monitor`;

-- 角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`, `create_time`, `update_time`) VALUES
  (1, '超级管理员', 'ADMIN',    '系统最高权限', 1, NOW(), NOW()),
  (2, '运维人员',   'OPERATOR', '日常运维',     1, NOW(), NOW()),
  (3, '访客',       'VIEWER',   '只读查看',     1, NOW(), NOW());

-- 权限/菜单（目录->菜单->按钮）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `perm`, `icon`, `sort`, `status`) VALUES
  (1,  0,  '设备监测',       1, '/monitor', NULL,          NULL,            'Monitor',       1, 1),
  (10, 1,  '设备管理',       2, 'device',   'device/index', NULL,           'Cpu',          10, 1),
  (11, 10, '设备新增',       3, NULL,       NULL,           'device:add',    NULL,            11, 1),
  (12, 10, '设备修改',       3, NULL,       NULL,           'device:edit',   NULL,            12, 1),
  (13, 10, '设备删除',       3, NULL,       NULL,           'device:delete', NULL,            13, 1),
  (20, 1,  '点位管理',       2, 'point',    'point/index',  NULL,           'SetUp',         20, 1),
  (21, 20, '点位新增',       3, NULL,       NULL,           'point:add',     NULL,            21, 1),
  (22, 20, '点位修改',       3, NULL,       NULL,           'point:edit',    NULL,            22, 1),
  (23, 20, '点位删除',       3, NULL,       NULL,           'point:delete',  NULL,            23, 1),
  (30, 1,  '数据查询',       2, 'data',     'data/index',   NULL,           'DataLine',      30, 1),
  (40, 1,  '图形分析',       2, 'chart',    'chart/index',  NULL,           'TrendCharts',   40, 1),
  (50, 1,  '控制面板',       2, 'control',  'control/index',NULL,          'SwitchButton',  50, 1),
  (51, 50, '控制下发',       3, NULL,       NULL,           'control:send',  NULL,            51, 1),
  (60, 1,  '统计报表',       2, 'report',   'report/index', NULL,           'PieChart',      60, 1),
  (2,  0,  '系统管理',       1, '/system',  NULL,           NULL,           'Setting',       2, 1),
  (70, 2,  '用户管理',       2, 'user',     'system/user/index',   NULL,    'User',          70, 1),
  (71, 70, '用户新增',       3, NULL,       NULL,           'user:add',      NULL,            71, 1),
  (72, 70, '用户修改',       3, NULL,       NULL,           'user:edit',     NULL,            72, 1),
  (73, 70, '用户删除',       3, NULL,       NULL,           'user:delete',   NULL,            73, 1),
  (80, 2,  '角色管理',       2, 'role',     'system/role/index',   NULL,    'UserFilled',    80, 1),
  (81, 80, '角色新增',       3, NULL,       NULL,           'role:add',      NULL,            81, 1),
  (82, 80, '角色修改',       3, NULL,       NULL,           'role:edit',     NULL,            82, 1),
  (83, 80, '角色删除',       3, NULL,       NULL,           'role:delete',   NULL,            83, 1),
  (90, 2,  '菜单管理',       2, 'permission','system/permission/index', NULL, 'Menu',       90, 1);

-- 示例设备
INSERT INTO `device` (`id`, `dev_id`, `device_name`, `device_type`, `model`, `manufacturer`, `location`, `status`, `install_date`, `description`, `create_time`, `update_time`) VALUES
  (1, '492C230518046576', '1号电热水壶',   '电热设备', 'KT-1000', '某电器厂',  '一号车间A区', 1, '2023-06-01', '示例设备：上报主题 /kettle/pub', NOW(), NOW()),
  (2, '492C230518046577', '2号电热水壶',   '电热设备', 'KT-1000', '某电器厂',  '一号车间B区', 1, '2023-06-01', '示例设备', NOW(), NOW()),
  (3, '492C230518046578', '3号电热水壶',   '电热设备', 'KT-1000', '某电器厂',  '二号车间A区', 0, '2023-06-05', '示例设备（离线）', NOW(), NOW());

-- 示例点位（1号设备: AI1-AI4 采集点 + DO1-DO4 控制点）
INSERT INTO `device_point` (`device_id`, `point_code`, `point_name`, `point_type`, `data_type`, `unit`, `min_value`, `max_value`, `default_value`, `description`, `create_time`) VALUES
  (1, 'AI1', '温度',     1, 1, '℃',   0, 100, '0',    '温度采集值', NOW()),
  (1, 'AI2', '水位',     1, 1, 'mm',  0, 5000, '4889','水位采集值', NOW()),
  (1, 'AI3', '加热状态', 1, 2, NULL,  0, 1,   '0',    '0停止 1加热', NOW()),
  (1, 'AI4', '功率',     1, 1, 'W',   0, 2000, '20',  '功率采集值', NOW()),
  (1, 'DO1', '加热开关', 2, 2, NULL,  0, 1,   '0',    '0关 1开', NOW()),
  (1, 'DO2', '保温开关', 2, 2, NULL,  0, 1,   '0',    '0关 1开', NOW()),
  (1, 'DO3', '蜂鸣器',   2, 2, NULL,  0, 1,   '0',    '0关 1开', NOW()),
  (1, 'DO4', '指示灯',   2, 2, NULL,  0, 1,   '0',    '0关 1开', NOW()),
  (2, 'AI1', '温度',     1, 1, '℃',   0, 100, '0',    NULL, NOW()),
  (2, 'AI2', '水位',     1, 1, 'mm',  0, 5000, '4889',NULL, NOW()),
  (2, 'AI3', '加热状态', 1, 2, NULL,  0, 1,   '0',    NULL, NOW()),
  (2, 'AI4', '功率',     1, 1, 'W',   0, 2000, '20',  NULL, NOW()),
  (2, 'DO1', '加热开关', 2, 2, NULL,  0, 1,   '0',    NULL, NOW()),
  (2, 'DO2', '保温开关', 2, 2, NULL,  0, 1,   '0',    NULL, NOW()),
  (2, 'DO3', '蜂鸣器',   2, 2, NULL,  0, 1,   '0',    NULL, NOW()),
  (2, 'DO4', '指示灯',   2, 2, NULL,  0, 1,   '0',    NULL, NOW());
