package com.dss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // Set the core pool size to 20
        executor.setMaxPoolSize(20);  // Set the maximum pool size to 20
        executor.setQueueCapacity(100); // Set the queue capacity, adjust as needed
        executor.setThreadNamePrefix("EmailService-"); // Set a custom thread name prefix
        executor.initialize();
        return executor;
    }
}
