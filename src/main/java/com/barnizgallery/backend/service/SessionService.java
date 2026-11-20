package com.barnizgallery.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AuthResponse;
import com.barnizgallery.backend.entity.AuthToken;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * Keeps sessions alive with refresh tokens. Each refresh token works once and is replaced by a new pair
 * (rotation). Using a refresh token that was already used means it may have been stolen, so every session of
 * that user is closed.
 */
@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final AuthTokenService authTokenService;
    private final SessionIssuer sessionIssuer;

    public SessionService(AuthTokenService authTokenService, SessionIssuer sessionIssuer) {
        this.authTokenService = authTokenService;
        this.sessionIssuer = sessionIssuer;
    }

    /**
     * New access and refresh tokens for a valid refresh token. Unknown, expired or reused → 401
     * {@code invalid_refresh_token}; a suspended account → 403.
     */
    @Transactional(noRollbackFor = BusinessRuleException.class)
    public AuthResponse refresh(String refreshToken) {
        AuthToken token = authTokenService.find(refreshToken, AuthTokenPurpose.REFRESH)
                .orElseThrow(() -> BusinessRuleException.unauthorized(ErrorCode.INVALID_REFRESH_TOKEN));
        User user = token.getUser();
        if (token.getUsedAt() != null) {
            log.warn("Reused refresh token of user {}: closing all their sessions", user.getUserId());
            authTokenService.revokeAll(user, AuthTokenPurpose.REFRESH);
            throw BusinessRuleException.unauthorized(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (!authTokenService.isUsable(token)) {
            throw BusinessRuleException.unauthorized(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        authTokenService.markUsed(token);
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DELETED) {
            throw BusinessRuleException.forbidden(ErrorCode.SUSPENDED);
        }
        return sessionIssuer.open(user);
    }

    /**
     * Closes the session of this refresh token. Unknown or already used tokens are ignored, so logging out twice
     * is harmless. The short access token simply expires.
     */
    @Transactional
    public void logout(String refreshToken) {
        authTokenService.find(refreshToken, AuthTokenPurpose.REFRESH)
                .filter(token -> token.getUsedAt() == null)
                .ifPresent(authTokenService::markUsed);
    }
}
