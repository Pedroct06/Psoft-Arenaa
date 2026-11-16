package com.psoft.arena.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class FilaSubmissaoConfig {

    @Bean(name = "filaSubmissaoExecutor")
    public ThreadPoolTaskExecutor filaSubmissaoExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setThreadNamePrefix("fila-submissao-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
