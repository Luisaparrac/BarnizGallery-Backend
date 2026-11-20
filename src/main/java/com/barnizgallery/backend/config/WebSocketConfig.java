package com.barnizgallery.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.barnizgallery.backend.security.StompAuthChannelInterceptor;

/**
 * STOMP over WebSocket for live auctions.
 * <ul>
 * <li>Endpoint: {@code /ws} (SockJS fallback enabled; native WebSocket at {@code /ws/websocket})</li>
 * <li>Application prefix: {@code /app}; simple broker: {@code /topic}</li>
 * <li>Security: see {@link StompAuthChannelInterceptor}</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CorsProperties properties;
    private final StompAuthChannelInterceptor authInterceptor;

    public WebSocketConfig(CorsProperties properties, StompAuthChannelInterceptor authInterceptor) {
        this.properties = properties;
        this.authInterceptor = authInterceptor;
    }

    /** Checks the access token on CONNECT and who may subscribe to each topic. */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(properties.allowedOrigins().toArray(String[]::new))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
