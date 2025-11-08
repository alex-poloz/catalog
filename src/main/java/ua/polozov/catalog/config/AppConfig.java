package ua.polozov.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@Configuration
public class AppConfig {

    @Bean
    public ReentrantReadWriteLock rateLock() {
        return new ReentrantReadWriteLock();
    }
}

