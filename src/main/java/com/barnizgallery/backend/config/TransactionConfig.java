package com.barnizgallery.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.barnizgallery.backend.security.CurrentUser;

import jakarta.persistence.EntityManagerFactory;

/**
 * Replaces Spring Boot's JPA transaction manager with {@link AuditUserTransactionManager}, so database audit
 * rows know which user made each change.
 */
@Configuration
public class TransactionConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory,
            CurrentUser currentUser) {
        return new AuditUserTransactionManager(entityManagerFactory, currentUser);
    }
}
