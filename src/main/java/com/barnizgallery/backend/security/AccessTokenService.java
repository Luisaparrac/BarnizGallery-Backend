package com.barnizgallery.backend.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;

/**
 * Issues short-lived access tokens (JWT, HS256). The subject is the user id; the token also carries the role
 * and the preferred language, so a request needs no database read to know them.
 */
@Service
public class AccessTokenService {

    public static final String ROLE_CLAIM = "role";
    public static final String LANGUAGE_CLAIM = "lang";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;

    public AccessTokenService(JwtEncoder jwtEncoder, JwtProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    public IssuedAccessToken issue(User user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(Duration.ofMinutes(properties.accessMinutes()));
        Language language = user.getPreferredLanguage() == null ? Language.ES : user.getPreferredLanguage();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(user.getUserId().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(ROLE_CLAIM, user.getRole().name())
                .claim(LANGUAGE_CLAIM, language.getDbValue())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedAccessToken(token, expiresAt);
    }
}
