package com.elwg.ai3dbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * 事务配置类
 * 提供TransactionTemplate用于编程式事务管理
 */
@Configuration
public class TransactionConfig {
    
    /**
     * 配置事务管理器
     * 
     * @param dataSource 数据源
     * @return 平台事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    
    /**
     * 配置事务模板
     * 用于编程式事务管理
     * 
     * @param transactionManager 事务管理器
     * @return 事务模板
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
