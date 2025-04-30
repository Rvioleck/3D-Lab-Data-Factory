package com.elwg.ai3dbackend.scheduler;

import com.elwg.ai3dbackend.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class PictureCleanupScheduler {

    @Resource
    private PictureService pictureService;

    /**
     * 每小时执行清理任务
     */
    @Scheduled(cron = "0 0 */1 * * ?")  // 修改后的cron表达式
    public void schedulePictureCleanup() {
        log.info("Starting scheduled picture cleanup task");
        try {
            Long currentTime = System.currentTimeMillis();
            int cleanedCount = pictureService.cleanupOrphanedFiles();
            Long endTime = System.currentTimeMillis();
            log.info("Scheduled picture cleanup task completed. Cleaned {} orphaned files in {} ms", cleanedCount, endTime - currentTime);
        } catch (Exception e) {
            log.error("Error during picture cleanup task", e);
        }
    }
}