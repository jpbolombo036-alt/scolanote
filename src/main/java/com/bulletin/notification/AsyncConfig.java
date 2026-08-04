package com.bulletin.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Active le traitement asynchrone pour les notifications.
 *
 * L'envoi des e-mails s'exécute dans un pool de threads séparé afin de
 * NE JAMAIS bloquer la réponse HTTP (exigence du cahier des charges).
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Pool de threads dédié aux notifications.
     * Dimensionné modestement : les e-mails sont des opérations I/O (réseau SMTP).
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }
}
