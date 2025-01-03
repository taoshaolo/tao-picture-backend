package com.taoshao.taopicture.config;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author taoshao
 */
@Configuration
public class RetryConfig {
 
    @Bean
    public Retryer<Boolean> retryer() {
        return RetryerBuilder.<Boolean>newBuilder()
                // 设置出现 Exception 异常就重试
                .retryIfExceptionOfType(Exception.class)
                // 设置结果为 false 才重试
                .retryIfResult(aBoolean -> Objects.equals(aBoolean, false))
                // 设置每次重试间隔为 1s
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                // 设置重试次数为 3 次，超过 3 次就停止
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .build();
    }
}