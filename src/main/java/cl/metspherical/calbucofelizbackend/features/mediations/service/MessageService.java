package cl.metspherical.calbucofelizbackend.features.mediations.service;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Mediation;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Message;
import cl.metspherical.calbucofelizbackend.features.mediations.repository.MediationRepository;
import cl.metspherical.calbucofelizbackend.features.mediations.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MediationRepository mediationRepository;
    private final UserRepository userRepository;
    private final MediationParticipantService participantService;    
    
    /**
     * Saves a new message in a mediation
     * Validates that user can talk (not muted) and mediation is not closed before saving
     * 
     * @param mediationId ID of the mediation
     * @param senderId ID of the message sender
     * @param content Message content
     * @return Saved message entity
     * @throws ResponseStatusException if user cannot send messages or mediation is closed
     */
    @Transactional
    public Message saveMessage(UUID mediationId, UUID senderId, String content) {
        // Validate mediation exists
        Mediation mediation = mediationRepository.findById(mediationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mediation not found with id: " + mediationId));

        // Check if mediation is closed
        if (Boolean.TRUE.equals(mediation.getIsSolved())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Cannot send messages: this mediation has been closed");
        }

        // Validate user exists
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + senderId));

        // Validate user can talk (not muted)
        if (!participantService.canUserTalk(senderId, mediationId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "User is muted and cannot send messages");
        }

        // Validate content is not empty
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Message content cannot be empty");
        }

        // Create and save message
        Message message = Message.builder()
                .content(content.trim())
                .mediation(mediation)
                .sender(sender)
                .build();

        return messageRepository.save(message);
    }

    /**
     * Gets all messages from a mediation ordered by sent date
     * 
     * @param mediationId ID of the mediation
     * @return List of messages ordered by sent date
     */
    @Transactional(readOnly = true)
    public List<Message> getMessagesByMediation(UUID mediationId) {
        return messageRepository.findByMediationIdOrderBySentAtAsc(mediationId);
    }
}
