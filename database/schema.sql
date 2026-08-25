-- =====================================================================
-- 基于MQTT协议的企业设备运行监测与远程控制系统 数据库建表脚本 (MySQL 8/9)
-- 数据库: device_monitor  字符集: utf8mb4
-- 说明: 应用默认使用 JPA (ddl-auto=update) 自动建表；本脚本用于手动初始化，
--       与 JPA 实体定义保持一致。可直接执行:  mysql -uroot < database/schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `device_monitor` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `device_monitor`;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `username`    VARCHAR(50)  NOT NULL,
  `password`    VARCHAR(100) NOT NULL,
  `real_name`   VARCHAR(50)  DEFAULT NULL,
  `phone`       VARCHAR(20)  DEFAULT NULL,
  `email`       VARCHAR(100) DEFAULT NULL,
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `remark`      VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME     DEFAULT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `role_name`   VARCHAR(50)  NOT NULL,
  `role_code`   VARCHAR(50)  NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `status`      TINYINT      NOT NULL DEFAULT 1,
  `create_time` DATETIME     DEFAULT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限/菜单表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `parent_id`   BIGINT       NOT NULL DEFAULT 0,
  `name`        VARCHAR(50)  NOT NULL,
  `type`        TINYINT      NOT NULL DEFAULT 2 COMMENT '1目录 2菜单 3按钮',
  `path`        VARCHAR(200) DEFAULT NULL,
  `component`   VARCHAR(200) DEFAULT NULL,
  `perm`        VARCHAR(100) DEFAULT NULL,
  `icon`        VARCHAR(50)  DEFAULT NULL,
  `sort`        INT          NOT NULL DEFAULT 0,
  `status`      TINYINT      NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_perm_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限/菜单表';

-- 用户角色关联
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `role_id`       BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 设备表
CREATE TABLE IF NOT EXISTS `device` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `dev_id`       VARCHAR(32)  NOT NULL,
  `device_name`  VARCHAR(100) NOT NULL,
  `device_type`  VARCHAR(50)  DEFAULT NULL,
  `model`        VARCHAR(50)  DEFAULT NULL,
  `manufacturer` VARCHAR(100) DEFAULT NULL,
  `location`     VARCHAR(100) DEFAULT NULL,
  `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '1在线 0离线',
  `install_date` DATE         DEFAULT NULL,
  `description`  VARCHAR(255) DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT NULL,
  `update_time`  DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_devid` (`dev_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 设备点位表（属性/控制参数）
CREATE TABLE IF NOT EXISTS `device_point` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `device_id`     BIGINT        NOT NULL,
  `point_code`    VARCHAR(16)   NOT NULL,
  `point_name`    VARCHAR(50)   NOT NULL,
  `point_type`    TINYINT       NOT NULL DEFAULT 1 COMMENT '1采集AI 2控制DO',
  `data_type`     TINYINT       NOT NULL DEFAULT 1 COMMENT '1数值 2布尔',
  `unit`          VARCHAR(20)   DEFAULT NULL,
  `min_value`     DECIMAL(12,3) DEFAULT NULL,
  `max_value`     DECIMAL(12,3) DEFAULT NULL,
  `default_value` VARCHAR(32)   DEFAULT NULL,
  `description`   VARCHAR(255)  DEFAULT NULL,
  `create_time`   DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_point_dev_code` (`device_id`, `point_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备点位表';

-- 采集数据表
CREATE TABLE IF NOT EXISTS `device_data` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT,
  `device_id`    BIGINT      DEFAULT NULL,
  `dev_id`       VARCHAR(32) NOT NULL,
  `point_code`   VARCHAR(16) NOT NULL,
  `point_name`   VARCHAR(50) DEFAULT NULL,
  `value`        VARCHAR(64) DEFAULT NULL,
  `num_value`    DOUBLE      DEFAULT NULL,
  `unit`         VARCHAR(20) DEFAULT NULL,
  `collect_time` DATETIME    NOT NULL,
  `create_time`  DATETIME    DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_data_dev_time` (`dev_id`, `point_code`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采集数据表';

-- 控制记录表
CREATE TABLE IF NOT EXISTS `control_record` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `device_id`     BIGINT      DEFAULT NULL,
  `dev_id`        VARCHAR(32) NOT NULL,
  `point_code`    VARCHAR(16) DEFAULT NULL,
  `point_name`    VARCHAR(50) DEFAULT NULL,
  `value`         VARCHAR(32) DEFAULT NULL,
  `msg_type`      VARCHAR(32) DEFAULT NULL,
  `operator_id`   BIGINT      DEFAULT NULL,
  `operator_name` VARCHAR(50) DEFAULT NULL,
  `status`        TINYINT     NOT NULL DEFAULT 0 COMMENT '0已发送 1成功 2失败',
  `response`      TEXT        DEFAULT NULL,
  `create_time`   DATETIME    DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_control_dev` (`dev_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='控制记录表';
