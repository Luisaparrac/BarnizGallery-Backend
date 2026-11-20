package com.barnizgallery.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;

class StompAuthChannelInterceptorTest {

    private final JwtConfig jwtConfig = new JwtConfig();
    private final JwtProperties properties = new JwtProperties(
            Base64.getEncoder().encodeToString((UUID.randomUUID() + "-" + UUID.randomUUID()).getBytes()), 15, 7);
    private final AccessTokenService tokens = new AccessTokenService(jwtConfig.jwtEncoder(properties), properties,
            Clock.systemUTC());
    private final StompAuthChannelInterceptor interceptor = new StompAuthChannelInterceptor(
            jwtConfig.jwtDecoder(properties));
    private final MessageChannel channel = mock(MessageChannel.class);

    @Test
    void connectNeedsAValidAccessToken() {
        String token = tokens.issue(TestEntities.user(7, Language.ES)).token();
        StompHeaderAccessor connect = StompHeaderAccessor.create(StompCommand.CONNECT);
        connect.setNativeHeader("Authorization", "Bearer " + token);

        Message<?> accepted = interceptor.preSend(message(connect), channel);

        Authentication user = (Authentication) StompHeaderAccessor.wrap(accepted).getUser();
        assertThat(user.getName()).isEqualTo("7");
        assertThatThrownBy(() -> interceptor.preSend(message(StompHeaderAccessor.create(StompCommand.CONNECT)),
                channel)).isInstanceOf(MessageDeliveryException.class);
        StompHeaderAccessor forged = StompHeaderAccessor.create(StompCommand.CONNECT);
        forged.setNativeHeader("Authorization", "Bearer not.a.token");
        assertThatThrownBy(() -> interceptor.preSend(message(forged), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void usersOnlyFollowTheirOwnNotifications() {
        Authentication user = authentication(7, UserRole.CUSTOMER);

        assertThatCode(() -> StompAuthChannelInterceptor.checkSubscription("/topic/users/7/notifications", user))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> StompAuthChannelInterceptor.checkSubscription("/topic/users/8/notifications", user))
                .isInstanceOf(MessageDeliveryException.class);
        assertThatCode(() -> StompAuthChannelInterceptor.checkSubscription("/topic/auctions/1", user))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> StompAuthChannelInterceptor.checkSubscription("/topic/admin/anomalies", user))
                .isInstanceOf(MessageDeliveryException.class);
        assertThatThrownBy(() -> StompAuthChannelInterceptor.checkSubscription("/topic/other", user))
                .isInstanceOf(MessageDeliveryException.class);
        assertThatCode(() -> StompAuthChannelInterceptor.checkSubscription("/topic/admin/anomalies",
                authentication(1, UserRole.ADMIN))).doesNotThrowAnyException();
    }

    @Test
    void clientsCannotSendMessages() {
        StompHeaderAccessor send = StompHeaderAccessor.create(StompCommand.SEND);
        send.setDestination("/topic/auctions/1");

        assertThatThrownBy(() -> interceptor.preSend(message(send), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    private Authentication authentication(long id, UserRole role) {
        User user = TestEntities.user(id, Language.ES);
        user.setRole(role);
        return SecurityConfig.jwtAuthenticationConverter()
                .convert(jwtConfig.jwtDecoder(properties).decode(tokens.issue(user).token()));
    }

    private static Message<byte[]> message(StompHeaderAccessor accessor) {
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
