package cl.metspherical.calbucofelizbackend.features.mediations.websocket;

import cl.metspherical.calbucofelizbackend.features.mediations.dto.WebSocketRequestDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.dto.WebSocketResponseDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.model.MediationParticipant;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Message;
import cl.metspherical.calbucofelizbackend.features.mediations.service.MediationParticipantService;
import cl.metspherical.calbucofelizbackend.features.mediations.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class MediationWebSocketHandler extends TextWebSocketHandler {

    private final MediationParticipantService participantService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String MEDIATION_ID = "mediation_id";
    private static final String TARGET_USER_ID = "target_user_id";
    private static final String ROOM_PREFIX = "room_";

    // Event handlers map for functional programming approach
    private final Map<String, EventHandler> eventHandlers = Map.of(
            "joinRoom", (session, request, userId, username) -> handleJoinRoom(session, request, userId),
            "sendMessage", (session, request, userId, username) -> handleSendMessage(session, request, userId),
            "leaveRoom", (session, request, userId, username) -> handleLeaveRoom(session, request),
            "muteUser", this::handleMuteUser,
            "unmuteUser", this::handleUnmuteUser
    );

    @FunctionalInterface
    private interface EventHandler {
        void handle(WebSocketSession session, WebSocketRequestDTO request, UUID userId, String username);
    }
    // Helper functions for mapping
    private final Function<MediationParticipant, Map<String, Object>> mapParticipant = participant ->
            Map.of(
                    "user_id", participant.getUser().getId().toString(),
                    "username", participant.getUser().getUsername(),
                    "is_muted", !Boolean.TRUE.equals(participant.getCanTalk())
            );

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sendMessage(session, createResponse("connected", SUCCESS,
                Map.of(MESSAGE, "WebSocket connection established")));
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        try {
            WebSocketRequestDTO request = objectMapper.readValue(message.getPayload(), WebSocketRequestDTO.class);
            UUID userId = (UUID) session.getAttributes().get("userId");
            String username = (String) session.getAttributes().get("username");            eventHandlers.getOrDefault(request.event(),
                    (s, r, u, un) -> sendErrorResponse(s, "Unknown event type"))
                    .handle(session, request, userId, username);
        } catch (Exception e) {
            sendErrorResponse(session, "Error processing message");
        }
    }

    private void handleJoinRoom(WebSocketSession session, WebSocketRequestDTO request, UUID userId) {
        try {
            UUID mediationId = UUID.fromString(request.data().get(MEDIATION_ID).toString());
            String roomKey = ROOM_PREFIX + mediationId;

            // Verify if user can join the room
            if (!participantService.canUserJoinRoom(userId, mediationId)) {
                sendErrorResponse(session, "You don't have permission to join this room");
                return;
            }

            // Add session to room

            roomSessions.computeIfAbsent(roomKey, k -> ConcurrentHashMap.newKeySet()).add(session);
            session.getAttributes().put("currentRoom", roomKey);
            session.getAttributes().put("mediationId", mediationId);

            // Check if user is muted
            boolean canTalk = participantService.canUserTalk(userId, mediationId);

            // Check if user is moderator
            boolean isModerator = participantService.canUserModerate(userId, mediationId);

            // Build response data
            Map<String, Object> responseData = new HashMap<>();            responseData.put(MEDIATION_ID, mediationId.toString());
            responseData.put("is_muted", !canTalk);
            responseData.put("is_moderator", isModerator);// If user is moderator, add information about all participants
            if (isModerator) {
                Optional.of(participantService.getAllParticipants(mediationId))
                        .map(participants -> participants.stream()
                                .map(mapParticipant)
                                .toList())                        .ifPresentOrElse(
                                participantsStatus -> responseData.put("participants_status", participantsStatus),
                                () -> { /* Error getting participants status - ignore */ }
                        );
            }

            // Success response with mute status and moderator info
            sendMessage(session, createResponse("roomJoined", SUCCESS, responseData));
            // Send message history if available
            messageService.getMessagesByMediation(mediationId).stream()
                    .findAny()
                    .ifPresent(ignored -> {
                        try {
                            List<Map<String, Object>> mappedMessages = messageService.getMessagesByMediation(mediationId)
                                    .stream().map(this::mapMessage).toList();
                                    sendMessage(session, createResponse("messageHistory", SUCCESS,
                                    Map.of(MEDIATION_ID, mediationId.toString(), "messages", mappedMessages)));
                        } catch (Exception ignored2) {
                            /* Failed to send message history - not critical */
                        }
                    });
        } catch (Exception e) {
            sendErrorResponse(session, "Error joining room: " + e.getMessage());
        }
    }

    private void handleSendMessage(WebSocketSession session, WebSocketRequestDTO request, UUID userId) {
        UUID mediationId = UUID.fromString(request.data().get(MEDIATION_ID).toString());
        String content = request.data().get("content").toString();
        String roomKey = ROOM_PREFIX + mediationId;

        try {
            // Save message (validation handled in MessageService)
            Message savedMessage = messageService.saveMessage(mediationId, userId, content);
            Map<String, Object> messageData = Map.of(MEDIATION_ID, mediationId.toString(), MESSAGE, mapMessage(savedMessage));

            // Response to sender and broadcast
            sendMessage(session, createResponse("messageSent", SUCCESS, messageData));
            broadcastToRoom(roomKey, createResponse("newMessage", SUCCESS, messageData), session);
        } catch (ResponseStatusException e) {
            sendErrorResponse(session, e.getReason());
        }
    }

    private void handleLeaveRoom(WebSocketSession session, WebSocketRequestDTO request) {
        UUID mediationId = UUID.fromString(request.data().get(MEDIATION_ID).toString());
        String roomKey = ROOM_PREFIX + mediationId;        removeSessionFromRoom(session, roomKey);
        sendMessage(session, createResponse("roomLeft", SUCCESS, Map.of(MEDIATION_ID, mediationId.toString())));

    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        Optional.ofNullable((String) session.getAttributes().get("currentRoom"))
                .ifPresent(currentRoom -> removeSessionFromRoom(session, currentRoom));
    }

    private void removeSessionFromRoom(WebSocketSession session, String roomKey) {
        Optional.ofNullable(roomSessions.get(roomKey))
                .ifPresent(sessions -> {
                    sessions.remove(session);
                    if (sessions.isEmpty()) roomSessions.remove(roomKey);
                });
    }

    private void broadcastToRoom(String roomKey, Object message, WebSocketSession excludeSession) {
        Optional.ofNullable(roomSessions.get(roomKey))
                .ifPresent(sessions -> sessions.parallelStream()
                        .filter(s -> !s.equals(excludeSession) && s.isOpen())
                        .forEach(s -> sendMessage(s, message)));
    }

    private void sendMessage(WebSocketSession session, Object message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (Exception ignored) {
            /* Failed to send message through WebSocket - session may be closed */
        }
    }

    private void sendErrorResponse(WebSocketSession session, String errorMessage) {
        sendMessage(session, createResponse("error", "error", Map.of(MESSAGE, errorMessage)));
    }

    private WebSocketResponseDTO createResponse(String event, String status, Map<String, Object> data) {
        return new WebSocketResponseDTO(event, status, data);
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

    private void handleMuteUser(WebSocketSession session, WebSocketRequestDTO request, UUID moderatorId, String moderatorUsername) {
        handleMuteUnmuteUser(session, request, moderatorId, moderatorUsername, true);
    }

    private void handleUnmuteUser(WebSocketSession session, WebSocketRequestDTO request, UUID moderatorId, String moderatorUsername) {
        handleMuteUnmuteUser(session, request, moderatorId, moderatorUsername, false);
    }

    private void handleMuteUnmuteUser(WebSocketSession session, WebSocketRequestDTO request, UUID moderatorId, String moderatorUsername, boolean isMute) {
        UUID mediationId = UUID.fromString(request.data().get(MEDIATION_ID).toString());
        String roomKey = ROOM_PREFIX + mediationId;

        try {
            UUID targetUserId = UUID.fromString(request.data().get(TARGET_USER_ID).toString());
              boolean success = isMute ? 
                    participantService.muteUser(targetUserId, mediationId, moderatorId) :
                    participantService.unmuteUser(targetUserId, mediationId, moderatorId);
            
            if (success) {
                String action = isMute ? "muted" : "unmuted";
                String eventType = isMute ? "userMuted" : "userUnmuted";
                String notificationEvent = isMute ? "userMutedNotification" : "userUnmutedNotification";
                Map<String, Object> responseData = Map.of(
                        MEDIATION_ID, mediationId.toString(),
                        TARGET_USER_ID, targetUserId.toString(),
                        "moderator_username", moderatorUsername,
                        MESSAGE, "User has been " + action + " successfully"
                );

                // Send response to moderator and broadcast notification
                sendMessage(session, createResponse(eventType, SUCCESS, responseData));
                broadcastToRoom(roomKey, createResponse(notificationEvent, "info", Map.of(
                        MEDIATION_ID, mediationId.toString(),
                        TARGET_USER_ID, targetUserId.toString(),
                        "moderator_username", moderatorUsername,
                        MESSAGE, "User " + targetUserId + " has been " + action + " by " + moderatorUsername
                )), null);
            } else {
                sendErrorResponse(session, "Failed to " + (isMute ? "mute" : "unmute") + " user. You may not have permission or user is already " + (isMute ? "muted" : "unmuted") + ".");
            }        } catch (IllegalArgumentException e) {
            sendErrorResponse(session, "Invalid target user ID");
        }
    }
}
