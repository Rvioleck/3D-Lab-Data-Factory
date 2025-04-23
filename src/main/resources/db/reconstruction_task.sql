CREATE DATABASE IF NOT EXISTS ai_3d;

-- 使用数据库
USE ai_3d;

CREATE TABLE IF NOT EXISTS `reconstruction_task` (  
  `id`                BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,  
  `status`            VARCHAR(32)                        NOT NULL DEFAULT 'PENDING' COMMENT '任务状态（PENDING/PROCESSING/COMPLETED/FAILED）',
  `sourceImageId`     BIGINT                             NOT NULL COMMENT '源图片ID',  
  `resultModelId`     BIGINT                             NULL COMMENT '结果模型ID',  
  `errorMessage`      TEXT                               NULL COMMENT '错误信息',  
  `processingTime`    INT                                NULL COMMENT '处理时间（秒）',  
  `userId`            BIGINT                             NOT NULL COMMENT '创建用户ID',  
  `createTime`        DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',  
  `updateTime`        DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',  
  `isDelete`          TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
  INDEX `idx_status` (`status`),  
  INDEX `idx_sourceImageId` (`sourceImageId`),  
  INDEX `idx_resultModelId` (`resultModelId`),  
  INDEX `idx_userId` (`userId`),  
  INDEX `idx_createTime` (`createTime`)  
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '3D重建任务' COLLATE = utf8mb4_unicode_ci;
