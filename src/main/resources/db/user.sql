create database if not exists ai_3d;

use ai_3d;

-- 用户表创建脚本
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    `userAccount` VARCHAR(256) NOT NULL COMMENT '用户账号',
    `userPassword` VARCHAR(512) NOT NULL COMMENT '用户密码',
    `userName` VARCHAR(256) NULL COMMENT '用户名',
    `userAvatar` VARCHAR(1024) NULL COMMENT '用户头像URL',
    `userProfile` TEXT NULL COMMENT '用户简介',
    `userRole` VARCHAR(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
    `editTime` DATETIME NULL COMMENT '编辑时间',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_userAccount` (`userAccount`),
    INDEX `idx_userName` (`userName`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';
