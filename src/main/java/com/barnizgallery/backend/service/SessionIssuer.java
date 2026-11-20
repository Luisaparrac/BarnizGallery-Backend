package com.barnizgallery.backend.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AuthResponse;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.mapper.UserMapper;
import com.barnizgallery.backend.security.AccessTokenService;
import com.barnizgallery.backend.security.IssuedAccessToken;
import com.barnizgallery.backend.security.JwtProperties;

/**
 * Opens a session: a short access token (JWT) and a refresh token stored as a hash.
 */
@Service
public class SessionIssuer {

    private final AccessTokenService accessTokenService;
    private final AuthTokenService authTokenService;
    private final JwtProperties jwtProperties;

    public SessionIssuer(AccessTokenService accessTokenService, AuthTokenService authTokenService,
            JwtProperties jwtProperties) {
        this.accessTokenService = accessTokenService;
        this.authTokenService = authTokenService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse open(User user) {
        IssuedAccessToken access = accessTokenService.issue(user);
        Duration refreshValidity = Duration.ofDays(jwtProperties.refreshDays());
        String refresh = authTokenService.create(user, AuthTokenPurpose.REFRESH, refreshValidity);
        return new AuthResponse("Bearer", access.token(), access.expiresAt(), refresh,
                authTokenService.expiry(refreshValidity), UserMapper.toResponse(user));
    }
}
