package com.notfound.bookstorenotificationservice.config;

import com.notfound.bookstorenotificationservice.exception.NotificationDeliveryException;
import java.util.Map;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PromotionEmailExecutorConfig {

    @Bean(name = "promotionEmailExecutor")
    public Executor promotionEmailExecutor(
            @Value("${notification.promotion.executor.core-pool-size:4}") int corePoolSize,
            @Value("${notification.promotion.executor.max-pool-size:16}") int maxPoolSize,
            @Value("${notification.promotion.executor.queue-capacity:500}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Math.max(1, corePoolSize));
        executor.setMaxPoolSize(Math.max(executor.getCorePoolSize(), maxPoolSize));
        executor.setQueueCapacity(Math.max(1, queueCapacity));
        executor.setThreadNamePrefix("promotion-email-");
        
        // Cấu hình in ra log thông số của executor khi khởi tạo hoặc sử dụng
        executor.setTaskDecorator(runnable -> () -> {
            System.out.println(String.format("[Executor Stats] Active: %d, PoolSize: %d, CorePoolSize: %d, MaxPoolSize: %d, QueueSize: %d/%d",
                executor.getActiveCount(),
                executor.getPoolSize(),
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getThreadPoolExecutor() != null ? executor.getThreadPoolExecutor().getQueue().size() : 0,
                queueCapacity
            ));
            runnable.run();
        });
        
        executor.initialize();
        return executor;
    }

    @Bean(name = "promotionEmailRetryTemplate")
    public RetryTemplate promotionEmailRetryTemplate(
            @Value("${notification.promotion.retry.max-attempts:3}") int maxAttempts,
            @Value("${notification.promotion.retry.initial-interval-ms:2000}") long initialIntervalMs,
            @Value("${notification.promotion.retry.multiplier:2.0}") double multiplier,
            @Value("${notification.promotion.retry.max-interval-ms:10000}") long maxIntervalMs) {
        int safeMaxAttempts = Math.max(1, maxAttempts);
        long safeInitialIntervalMs = Math.max(1, initialIntervalMs);
        long safeMaxIntervalMs = Math.max(safeInitialIntervalMs, maxIntervalMs);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                safeMaxAttempts,
                Map.of(NotificationDeliveryException.class, true));

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(safeInitialIntervalMs);
        backOffPolicy.setMultiplier(Math.max(1.0, multiplier));
        backOffPolicy.setMaxInterval(safeMaxIntervalMs);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}
