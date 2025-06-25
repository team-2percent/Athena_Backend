package goorm.athena.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "fcmTaskExecutor")
    public Executor fcmTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("Fcm-Task-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "fcmCallBackExecutor")
    public Executor fcmCallBackExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("Fcm-callback-");
        executor.initialize();
        return executor;
    }

}
