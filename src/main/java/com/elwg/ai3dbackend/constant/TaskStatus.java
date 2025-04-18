package com.elwg.ai3dbackend.constant;

/**
 * 任务状态常量
 */
public class TaskStatus {
    /**
     * 等待处理
     */
    public static final String PENDING = "PENDING";
    
    /**
     * 处理中
     */
    public static final String PROCESSING = "PROCESSING";
    
    /**
     * 已完成
     */
    public static final String COMPLETED = "COMPLETED";
    
    /**
     * 失败
     */
    public static final String FAILED = "FAILED";
    
    private TaskStatus() {
        // 私有构造函数，防止实例化
    }
}
