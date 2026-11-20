package com.barnizgallery.backend.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The real IP of the client, used by the login attempt limiter.
 * <p>
 * Behind a proxy (Render) the TCP address is the proxy's, and the client IP travels in {@code X-Forwarded-For}.
 * Each proxy <i>appends</i> the address it received the request from, so only the entries added by our own
 * proxies can be trusted: anything to their left may have been written by the client. With
 * {@code app.security.trusted-proxy-hops = n}, the client IP is the n-th entry counted from the right. With 0
 * (local development, no proxy) the header is ignored and the TCP address is used.
 */
@Component
public class ClientIpResolver {

    static final String FORWARDED_FOR = "X-Forwarded-For";

    private final int trustedProxyHops;

    public ClientIpResolver(@Value("${app.security.trusted-proxy-hops}") int trustedProxyHops) {
        this.trustedProxyHops = Math.max(0, trustedProxyHops);
    }

    public String resolve(HttpServletRequest request) {
        String header = request.getHeader(FORWARDED_FOR);
        if (trustedProxyHops == 0 || header == null || header.isBlank()) {
            return request.getRemoteAddr();
        }
        List<String> hops = Arrays.stream(header.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (hops.size() < trustedProxyHops) {
            return request.getRemoteAddr();
        }
        return hops.get(hops.size() - trustedProxyHops);
    }
}
