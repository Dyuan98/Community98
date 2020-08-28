package com.yuan.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 测试Spring 线程池使用
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
