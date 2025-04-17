package com.elwg.ai3dbackend.service.impl;

import com.elwg.ai3dbackend.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 存储服务工厂
 * <p>
 * 根据配置选择合适的存储服务实现
 * </p>
 */
@Slf4j
@Component
public class StorageServiceFactory {

    @Value("${storage.type:local}")
    private String storageType;

    @Autowired
    @Qualifier("localStorageService")
    private StorageService localStorageService;

    // 当实现阿里云OSS存储服务时，可以添加如下注入
    /*
    @Autowired(required = false)
    @Qualifier("ossStorageService")
    private StorageService ossStorageService;
    */

    private StorageService activeStorageService;

    @PostConstruct
    public void init() {
        switch (storageType.toLowerCase()) {
            case "local":
                activeStorageService = localStorageService;
                log.info("Using local file system storage service");
                break;
            case "oss":
                // 当实现阿里云OSS存储服务时，可以添加如下代码
                /*
                if (ossStorageService != null) {
                    activeStorageService = ossStorageService;
                    log.info("Using Aliyun OSS storage service");
                } else {
                    log.warn("Aliyun OSS storage service is not available, falling back to local storage");
                    activeStorageService = localStorageService;
                }
                */
                log.warn("Aliyun OSS storage service is not implemented yet, falling back to local storage");
                activeStorageService = localStorageService;
                break;
            default:
                log.warn("Unknown storage type: {}, falling back to local storage", storageType);
                activeStorageService = localStorageService;
        }
    }

    /**
     * 获取当前激活的存储服务
     *
     * @return 存储服务实例
     */
    public StorageService getStorageService() {
        return activeStorageService;
    }
}
