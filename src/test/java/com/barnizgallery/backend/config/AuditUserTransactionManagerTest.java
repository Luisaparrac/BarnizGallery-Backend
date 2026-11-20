package com.barnizgallery.backend.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.barnizgallery.backend.security.CurrentUser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

/** The JPA objects are mocks: no database is used. */
class AuditUserTransactionManagerTest {

    private final EntityManagerFactory factory = mock(EntityManagerFactory.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final Query query = mock(Query.class);
    private final AuditUserTransactionManager manager = new AuditUserTransactionManager(factory, new CurrentUser());

    @BeforeEach
    void setUp() {
        when(factory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.getTransaction()).thenReturn(mock(EntityTransaction.class));
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter("userId", "42")).thenReturn(query);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void writeTransactionOfALoggedInUserSetsTheAuditUser() {
        login();

        run(false);

        verify(entityManager).createNativeQuery(AuditUserTransactionManager.SET_USER);
        verify(query).setParameter("userId", "42");
        verify(query).getSingleResult();
    }

    @Test
    void readOnlyTransactionsAndAnonymousRequestsDoNotTouchTheDatabase() {
        login();
        run(true);
        SecurityContextHolder.clearContext();
        run(false);

        verify(entityManager, never()).createNativeQuery(anyString());
    }

    private void run(boolean readOnly) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setReadOnly(readOnly);
        TransactionStatus status = manager.getTransaction(definition);
        manager.commit(status);
    }

    private static void login() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("42").claim("role", "CUSTOMER")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
