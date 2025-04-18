-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS ai_3d;

-- 使用数据库
USE ai_3d;

-- 3D模型表  
CREATE TABLE IF NOT EXISTS `model`  
(  
    `id`              BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,  
    `name`            VARCHAR(128)                       NOT NULL COMMENT '模型名称',  
    `introduction`    VARCHAR(512)                       NULL COMMENT '简介',  
    `category`        VARCHAR(64)                        NULL COMMENT '分类',  
    `tags`            VARCHAR(512)                       NULL COMMENT '标签（JSON 数组）',  
    `sourceImageId`   BIGINT                             NOT NULL COMMENT '源图片ID',  
    `objFileUrl`      VARCHAR(512)                       NOT NULL COMMENT 'OBJ文件URL',  
    `mtlFileUrl`      VARCHAR(512)                       NULL COMMENT 'MTL文件URL',  
    `textureImageUrl` VARCHAR(512)                       NULL COMMENT '纹理图像URL',  
    `pixelImagesUrl`  VARCHAR(512)                       NULL COMMENT '像素图像URL',  
    `xyzImagesUrl`    VARCHAR(512)                       NULL COMMENT 'XYZ图像URL',  
    `modelSize`       BIGINT                             NULL COMMENT '模型文件大小（字节）',  
    `modelFormat`     VARCHAR(32)                        NOT NULL DEFAULT 'OBJ' COMMENT '模型格式',  
    `taskId`          VARCHAR(64)                        NULL COMMENT '重建任务ID',  
    `status`          VARCHAR(32)                        NOT NULL DEFAULT 'completed' COMMENT '状态（processing/completed/failed）',  
    `userId`          BIGINT                             NOT NULL COMMENT '创建用户 id',  
    `createTime`      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',  
    `editTime`        DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',  
    `updateTime`      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',  
    `isDelete`        TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',  
    INDEX `idx_name` (`name`),                 -- 提升基于模型名称的查询性能  
    INDEX `idx_introduction` (`introduction`), -- 用于模糊搜索模型简介  
    INDEX `idx_category` (`category`),         -- 提升基于分类的查询性能  
    INDEX `idx_tags` (`tags`),                 -- 提升基于标签的查询性能  
    INDEX `idx_sourceImageId` (`sourceImageId`), -- 提升基于源图片ID的查询性能  
    INDEX `idx_taskId` (`taskId`),             -- 提升基于任务ID的查询性能  
    INDEX `idx_status` (`status`),             -- 提升基于状态的查询性能  
    INDEX `idx_userId` (`userId`)              -- 提升基于用户 ID 的查询性能  
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '3D模型' COLLATE = utf8mb4_unicode_ci;
