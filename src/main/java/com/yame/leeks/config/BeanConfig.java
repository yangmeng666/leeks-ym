package com.yame.leeks.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description TODO
 * @Date 2024/1/25
 * @Created by yangmeng
 */
@Configuration
@Slf4j
public class BeanConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 配置 restTemplate 支持重定向
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        HttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        factory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }

    @Bean("realDataFlushExecutor")
    public ThreadPoolTaskExecutor realDataFlushExecutor() {
        log.info("start realDataFlushEx...");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5000);
        executor.setMaxPoolSize(10000);
        executor.setQueueCapacity(50000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("realDataFlushEx-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("lzjzExecutor")
    public ThreadPoolTaskExecutor lzjzExecutor() {
        log.info("start lzjzEx...");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5000);
        executor.setMaxPoolSize(10000);
        executor.setQueueCapacity(50000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("lzjzEx-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("ziXuanFlushexecutor")
    public ThreadPoolTaskExecutor ziXuanFlushexecutor() {
        log.info("start ziXuanFlushEx...");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(50000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("ziXuanFlushEx-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
