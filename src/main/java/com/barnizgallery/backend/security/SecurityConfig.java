package com.barnizgallery.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless HTTP security of the API:
 * <ul>
 * <li>no session and no CSRF: every request carries its access token ({@code Authorization: Bearer ...});</li>
 * <li>CORS from {@code CorsConfigurationSource}, answered before authentication;</li>
 * <li>the token is a JWT checked by the resource server; its {@code role} claim becomes {@code ROLE_<role>};</li>
 * <li>security errors are answered as JSON (401 and 403).</li>
 * </ul>
 * Access by role is described in {@link #authorizeRequests}.
 */
@Configuration
public class SecurityConfig {

    static final String ADMIN = "ADMIN";
    static final String ARTISAN = "ARTISAN";

    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(SecurityConfig::authorizeRequests)
                .build();
    }

    /**
     * Who may call what. Reading the public catalog (gallery, stores, masters, artworks, auctions) and the
     * account endpoints are open; the administration is only for administrators; content changes need an
     * artisan or an administrator; everything else needs a logged-in user. Rules that depend on the data (own
     * account, own store) are checked with {@code @PreAuthorize} and in the services.
     */
    static void authorizeRequests(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**", "/ws/**", "/error").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/admin/**").hasRole(ADMIN)
                .requestMatchers("/api/artisans/**").hasRole(ARTISAN)
                .requestMatchers(HttpMethod.GET, "/api/artworks/*/suggested-price").hasAnyRole(ADMIN, ARTISAN)
                .requestMatchers(HttpMethod.GET, "/api/health", "/api/gallery/**", "/api/stores/**", "/api/masters/**",
                        "/api/artworks/**", "/api/auctions/**", "/api/catalogs/**").permitAll()
                .requestMatchers("/api/masters/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/auctions", "/api/auctions/*/start", "/api/auctions/*/finish",
                        "/api/auctions/*/cancel").hasRole(ADMIN)
                .requestMatchers("/api/rooms/**", "/api/artworks/**", "/api/photos/**").hasAnyRole(ADMIN, ARTISAN)
                .anyRequest().authenticated();
    }

    /** Maps the {@code role} claim (for example {@code ADMIN}) to the authority {@code ROLE_ADMIN}. */
    static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(AccessTokenService.ROLE_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
