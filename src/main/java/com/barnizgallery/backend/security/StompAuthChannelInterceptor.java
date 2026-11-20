package com.barnizgallery.backend.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

/**
 * Security of the STOMP channel:
 * <ul>
 * <li>{@code CONNECT} needs a valid access token in the {@code Authorization: Bearer ...} STOMP header;</li>
 * <li>a user may only subscribe to {@code /topic/users/<own id>/notifications};</li>
 * <li>{@code /topic/admin/**} is only for administrators;</li>
 * <li>{@code /topic/auctions/**} stays open to every connected client, read only;</li>
 * <li>clients never send messages: every {@code SEND} is refused.</li>
 * </ul>
 * A refused frame closes the session with a STOMP {@code ERROR} frame.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    static final Pattern USER_TOPIC = Pattern.compile("/topic/users/(\\d+)/notifications");

    private final JwtDecoder jwtDecoder;

    public StompAuthChannelInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            accessor.setUser(authenticate(accessor.getFirstNativeHeader("Authorization")));
        } else if (command == StompCommand.SUBSCRIBE) {
            checkSubscription(accessor.getDestination(), (Authentication) accessor.getUser());
        } else if (command == StompCommand.SEND) {
            throw new MessageDeliveryException("Clients cannot send messages");
        }
        return message;
    }

    private Authentication authenticate(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new MessageDeliveryException("A Bearer access token is required to connect");
        }
        try {
            AbstractAuthenticationToken authentication = SecurityConfig.jwtAuthenticationConverter()
                    .convert(jwtDecoder.decode(header.substring("Bearer ".length()).trim()));
            if (authentication == null) {
                throw new MessageDeliveryException("Invalid access token");
            }
            return authentication;
        } catch (JwtException ex) {
            throw new MessageDeliveryException("Invalid or expired access token");
        }
    }

    static void checkSubscription(String destination, Authentication user) {
        if (destination == null || user == null) {
            throw new MessageDeliveryException("Subscription refused");
        }
        Matcher userTopic = USER_TOPIC.matcher(destination);
        if (userTopic.matches()) {
            if (!userTopic.group(1).equals(user.getName())) {
                throw new MessageDeliveryException("Only your own notifications can be followed");
            }
            return;
        }
        if (destination.startsWith("/topic/admin/")) {
            boolean admin = user.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (!admin) {
                throw new MessageDeliveryException("Only administrators can follow this topic");
            }
            return;
        }
        if (!destination.startsWith("/topic/auctions/")) {
            throw new MessageDeliveryException("Unknown topic");
        }
    }
}
