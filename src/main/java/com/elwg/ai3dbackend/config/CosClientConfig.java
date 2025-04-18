package com.elwg.ai3dbackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云对象存储(COS)客户端配置
 * <p>
 * 提供COSClient实例用于访问腾讯云对象存储服务
 * </p>
 */
@Configuration
@Data
public class CosClientConfig {

    @Value("${cos.client.secretId}")
    private String secretId;

    @Value("${cos.client.secretKey}")
    private String secretKey;

    @Value("${cos.client.region}")
    private String region;

    @Value("${cos.client.bucket}")
    private String bucket;

    @Value("${cos.client.host}")
    private String host;

    /**
     * 创建COSClient实例
     * 用于访问腾讯云对象存储服务
     *
     * @return COSClient实例
     */
    @Bean
    public COSClient cosClient() {
        // 1. 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        
        // 2. 设置bucket的区域
        Region cosRegion = new Region(region);
        ClientConfig clientConfig = new ClientConfig(cosRegion);
        
        // 3. 设置使用HTTPS协议
        clientConfig.setHttpProtocol(HttpProtocol.https);
        
        // 4. 生成COS客户端
        return new COSClient(cred, clientConfig);
    }
}
