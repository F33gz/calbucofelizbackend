package cl.metspherical.calbucofelizbackend.features.mediations.websocket;

import cl.metspherical.calbucofelizbackend.features.mediations.dto.WebSocketRequestDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Message;
import cl.metspherical.calbucofelizbackend.features.mediations.service.MediationService;
import cl.metspherical.calbucofelizbackend.features.mediations.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MediationWebSocketHandler extends TextWebSocketHandler {

    private final MediationService mediationService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    // Map to manage sessions by room
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        UUID userId = (UUID) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");

        // Connection established response
        Map<String, Object> response = Map.of(
                "event", "connected",
                "status", "success",
                "data", Map.of("message", "WebSocket connection established")
        );
        sendMessage(session, response);

    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        try {
            WebSocketRequestDTO request = objectMapper.readValue(message.getPayload(), WebSocketRequestDTO.class);
            UUID userId = (UUID) session.getAttributes().get("userId");
            String username = (String) session.getAttributes().get("username");

            switch (request.event()) {
                case "joinRoom":
                    handleJoinRoom(session, request, userId, username);
                    break;
                case "sendMessage":
                    handleSendMessage(session, request, userId, username);
                    break;
                case "leaveRoom":
                    handleLeaveRoom(session, request, userId, username);
                    break;
                default:
                    sendErrorResponse(session, "Unknown event type");
            }
        } catch (Exception e) {
            sendErrorResponse(session, "Error processing message");
        }
    }

    private void handleJoinRoom(WebSocketSession session, WebSocketRequestDTO request, UUID userId, String username) throws Exception {
        UUID mediationId = UUID.fromString(request.data().get("mediation_id").toString());
        String roomKey = "room_" + mediationId;

        // Verify if user can join the room
        if (!mediationService.canUserJoinRoom(userId, mediationId)) {
            sendErrorResponse(session, "You don't have permission to join this room");
            return;
        }

        // Add session to room
        roomSessions.computeIfAbsent(roomKey, k -> ConcurrentHashMap.newKeySet()).add(session);
        session.getAttributes().put("currentRoom", roomKey);
        session.getAttributes().put("mediationId", mediationId);

        // Success response
        Map<String, Object> response = Map.of(
                "event", "roomJoined",
                "status", "success",
                "data", Map.of(
                        "mediation_id", mediationId.toString(),
                        "room", roomKey
                )
        );
        sendMessage(session, response);

    }

    private void handleSendMessage(WebSocketSession session, WebSocketRequestDTO request, UUID userId, String username) throws Exception {
        UUID mediationId = UUID.fromString(request.data().get("mediation_id").toString());
        String content = request.data().get("content").toString();
        String roomKey = "room_" + mediationId;

        // Verify if user can talk (not muted)
        if (!mediationService.canUserTalk(userId, mediationId)) {
            sendErrorResponse(session, "You don't have permission to send messages in this room");
            return;
        }

        // Save message
        Message savedMessage = messageService.saveMessage(mediationId, userId, content);

        // Response to sender
        Map<String, Object> senderResponse = Map.of(
                "event", "messageSent",
                "status", "success",
                "data", Map.of(
                        "mediation_id", mediationId.toString(),
                        "message", mapMessage(savedMessage)
                )
        );
        sendMessage(session, senderResponse);

        // Broadcast message to all in room
        Map<String, Object> broadcastMessage = Map.of(
                "event", "newMessage",
                "data", Map.of(
                        "mediation_id", mediationId.toString(),
                        "message", mapMessage(savedMessage)
                )
        );

        broadcastToRoom(roomKey, broadcastMessage, session);

    }

    private void handleLeaveRoom(WebSocketSession session, WebSocketRequestDTO request, UUID userId, String username) throws Exception {
        UUID mediationId = UUID.fromString(request.data().get("mediation_id").toString());
        String roomKey = "room_" + mediationId;

        removeSessionFromRoom(session, roomKey);

        Map<String, Object> response = Map.of(
                "event", "roomLeft",
                "status", "success",
                "data", Map.of("mediation_id", mediationId.toString())
        );
        sendMessage(session, response);

    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String currentRoom = (String) session.getAttributes().get("currentRoom");
        if (currentRoom != null) {
            removeSessionFromRoom(session, currentRoom);
        }

        String username = (String) session.getAttributes().get("username");
    }

    // Helper methods
    private void removeSessionFromRoom(WebSocketSession session, String roomKey) {
        Set<WebSocketSession> sessions = roomSessions.get(roomKey);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomKey);
            }
        }
    }

    private void broadcastToRoom(String roomKey, Object message, WebSocketSession excludeSession) {
        Set<WebSocketSession> sessions = roomSessions.get(roomKey);
        if (sessions != null) {
            sessions.parallelStream()
                    .filter(s -> !s.equals(excludeSession) && s.isOpen()).forEach(s -> {
                        try {
                            sendMessage(s, message);
                        } catch (Exception e) {
                            // Failed to send message to session, continue with others
                        }
                    });
        }
    }

    private void sendMessage(WebSocketSession session, Object message) throws Exception {
        if (session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }

    private void sendErrorResponse(WebSocketSession session, String errorMessage) throws Exception {
        Map<String, Object> errorResponse = Map.of(
                "event", "error",
                "status", "error",
                "data", Map.of("message", errorMessage)
        );
        sendMessage(session, errorResponse);
    }

    private Map<String, Object> mapMessage(Message message) {
        return Map.of(
                "id", message.getId().toString(),
                "sender_id", message.getSender().getId().toString(),
                "sender_username", message.getSender().getUsername(),
                "content", message.getContent(),
                "sent_at", message.getSentAt().toString()
        );
    }
}
