package cl.metspherical.calbucofelizbackend.features.mediations.websocket;

import cl.metspherical.calbucofelizbackend.common.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtWebSocketInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, 
                                   @NonNull ServerHttpResponse response, 
                                   @NonNull WebSocketHandler wsHandler, 
                                   @NonNull Map<String, Object> attributes) {

        String token = extractTokenFromRequest(request);

        if (token != null && jwtService.isTokenValid(token)) {
            try {
                UUID userId = jwtService.extractUserId(token);
                String username = jwtService.extractUsername(token);

                attributes.put("userId", userId);
                attributes.put("username", username);
                attributes.put("token", token);

                return true;
            } catch (Exception e) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, 
                              @NonNull ServerHttpResponse response, 
                              @NonNull WebSocketHandler wsHandler, 
                              @Nullable Exception exception) {
    }

    private String extractTokenFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
