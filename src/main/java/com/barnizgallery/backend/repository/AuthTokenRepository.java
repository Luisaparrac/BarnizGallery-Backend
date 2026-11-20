package com.barnizgallery.backend.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.AuthToken;
import com.barnizgallery.backend.enums.AuthTokenPurpose;

/**
 * Spring Data repository for {@link AuthToken}.
 */
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @Query("select t from AuthToken t join fetch t.user where t.tokenHash = :tokenHash and t.purpose = :purpose")
    Optional<AuthToken> findByHash(String tokenHash, AuthTokenPurpose purpose);

    /** Marks every unused token of the user with this purpose as used (revoked). */
    @Modifying
    @Query("update AuthToken t set t.usedAt = :now "
            + "where t.user.userId = :userId and t.purpose = :purpose and t.usedAt is null")
    int revokeAll(Long userId, AuthTokenPurpose purpose, Instant now);
}
