package com.serveflow.config;

import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock JwtService jwtService;
    @Mock UserRepository userRepository;
    @Mock MessageChannel channel;

    WebSocketConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = new WebSocketConfig(jwtService, userRepository);
        Field f = WebSocketConfig.class.getDeclaredField("allowedOriginsRaw");
        f.setAccessible(true);
        f.set(config, "http://localhost:5173");
    }

    @Nested
    @DisplayName("registerStompEndpoints()")
    class StompEndpoints {

        @Test
        @DisplayName("registra endpoint /ws")
        void registers_wsEndpoint() {
            StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
            StompWebSocketEndpointRegistration registration =
                    mock(StompWebSocketEndpointRegistration.class);
            when(registry.addEndpoint("/ws")).thenReturn(registration);
            when(registration.setAllowedOriginPatterns(any(String[].class)))
                    .thenReturn(registration);

            config.registerStompEndpoints(registry);

            verify(registry).addEndpoint("/ws");
        }

        @Test
        @DisplayName("configura origins permitidas no endpoint")
        void sets_allowedOriginPatterns() {
            StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
            StompWebSocketEndpointRegistration registration =
                    mock(StompWebSocketEndpointRegistration.class);
            when(registry.addEndpoint("/ws")).thenReturn(registration);
            when(registration.setAllowedOriginPatterns(any(String[].class)))
                    .thenReturn(registration);

            config.registerStompEndpoints(registry);

            verify(registration).setAllowedOriginPatterns("http://localhost:5173");
        }
    }

    @Nested
    @DisplayName("configureMessageBroker()")
    class MessageBroker {

        @Test
        @DisplayName("configura broker e prefixo sem lançar exceção")
        void configureBroker_doesNotThrow() {
            MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);
            when(registry.enableSimpleBroker(any(String[].class))).thenReturn(null);

            config.configureMessageBroker(registry);

            verify(registry).enableSimpleBroker("/topic");
            verify(registry).setApplicationDestinationPrefixes("/app");
        }
    }

    @Nested
    @DisplayName("interceptor STOMP CONNECT")
    class StompInterceptor {

        private ChannelInterceptor captureInterceptor() {
            ChannelRegistration registration = mock(ChannelRegistration.class);
            ArgumentCaptor<ChannelInterceptor> captor =
                    ArgumentCaptor.forClass(ChannelInterceptor.class);
            when(registration.interceptors(captor.capture())).thenReturn(registration);

            config.configureClientInboundChannel(registration);

            return captor.getValue();
        }

        private Message<?> connectMessageWithToken(String token) {
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            if (token != null) {
                accessor.addNativeHeader("Authorization", "Bearer " + token);
            }
            accessor.setLeaveMutable(true);
            return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        }

        private Message<?> nonConnectMessage() {
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
            accessor.setLeaveMutable(true);
            return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        }

        @Test
        @DisplayName("registra um interceptor no canal")
        void interceptorIsRegistered() {
            ChannelRegistration registration = mock(ChannelRegistration.class);
            when(registration.interceptors(any(ChannelInterceptor.class))).thenReturn(registration);

            config.configureClientInboundChannel(registration);

            verify(registration).interceptors(any(ChannelInterceptor.class));
        }

        @Test
        @DisplayName("mensagem não-CONNECT é repassada sem verificar token")
        void nonConnectMessage_passesThrough() {
            ChannelInterceptor interceptor = captureInterceptor();
            Message<?> msg = nonConnectMessage();

            Message<?> result = interceptor.preSend(msg, channel);

            assertThat(result).isSameAs(msg);
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("CONNECT sem header Authorization — conexão anônima permitida")
        void connectWithoutAuthHeader_anonymousAllowed() {
            ChannelInterceptor interceptor = captureInterceptor();
            Message<?> msg = connectMessageWithToken(null);

            Message<?> result = interceptor.preSend(msg, channel);

            assertThat(result).isNotNull();
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("CONNECT com token válido autentica o usuário")
        void connectWithValidToken_authenticatesUser() {
            User user = new User(1L, "garcon", "g@test.com", "pass", UserRole.GARCON, "Garçom");
            when(jwtService.extractUsername("valid-token")).thenReturn("garcon");
            when(userRepository.findByUsername("garcon")).thenReturn(Optional.of(user));

            ChannelInterceptor interceptor = captureInterceptor();
            Message<?> msg = connectMessageWithToken("valid-token");

            Message<?> result = interceptor.preSend(msg, channel);

            assertThat(result).isNotNull();
            verify(jwtService).extractUsername("valid-token");
            verify(userRepository).findByUsername("garcon");
        }

        @Test
        @DisplayName("CONNECT com token inválido — retorna mensagem sem autenticar")
        void connectWithInvalidToken_noAuth() {
            when(jwtService.extractUsername("bad-token")).thenThrow(new RuntimeException("invalid"));

            ChannelInterceptor interceptor = captureInterceptor();
            Message<?> msg = connectMessageWithToken("bad-token");

            Message<?> result = interceptor.preSend(msg, channel);

            assertThat(result).isNotNull();
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("CONNECT com token válido mas usuário não encontrado — conexão anônima")
        void connectWithTokenButUserNotFound_noAuth() {
            when(jwtService.extractUsername("token-ok")).thenReturn("unknown");
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            ChannelInterceptor interceptor = captureInterceptor();
            Message<?> msg = connectMessageWithToken("token-ok");

            Message<?> result = interceptor.preSend(msg, channel);

            assertThat(result).isNotNull();
        }
    }
}
