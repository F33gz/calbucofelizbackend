package cl.metspherical.calbucofelizbackend.common.config;

import cl.metspherical.calbucofelizbackend.features.mediations.websocket.MediationWebSocketHandler;
import cl.metspherical.calbucofelizbackend.features.mediations.websocket.JwtWebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MediationWebSocketHandler mediationWebSocketHandler;
    private final JwtWebSocketInterceptor jwtWebSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mediationWebSocketHandler, "/mediation/chat")
                .setAllowedOrigins("*")
                .addInterceptors(jwtWebSocketInterceptor);
    }
}
