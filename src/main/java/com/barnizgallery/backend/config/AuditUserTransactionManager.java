package com.barnizgallery.backend.config;

import java.util.Optional;

import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.barnizgallery.backend.security.CurrentUser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * JPA transaction manager that tells the database who is writing. When a read-write transaction starts and the
 * request has a logged-in user, it runs {@code SELECT set_config('app.user_id', <id>, true)}; the audit trigger of
 * the database reads that value and saves it in {@code audit_log.changed_by}. The value is local to the
 * transaction, so it never leaks to another request using the same connection.
 */
public class AuditUserTransactionManager extends JpaTransactionManager {

    static final String SET_USER = "select set_config('app.user_id', :userId, true)";

    private final transient CurrentUser currentUser;

    public AuditUserTransactionManager(EntityManagerFactory entityManagerFactory, CurrentUser currentUser) {
        super(entityManagerFactory);
        this.currentUser = currentUser;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        if (definition.isReadOnly()) {
            return;
        }
        Optional<Long> userId = currentUser.id();
        if (userId.isEmpty()) {
            return;
        }
        EntityManagerHolder holder = (EntityManagerHolder) TransactionSynchronizationManager
                .getResource(obtainEntityManagerFactory());
        if (holder != null) {
            EntityManager entityManager = holder.getEntityManager();
            entityManager.createNativeQuery(SET_USER)
                    .setParameter("userId", userId.get().toString())
                    .getSingleResult();
        }
    }
}
