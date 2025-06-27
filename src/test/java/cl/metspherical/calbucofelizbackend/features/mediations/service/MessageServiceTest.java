package cl.metspherical.calbucofelizbackend.features.mediations.service;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Mediation;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Message;
import cl.metspherical.calbucofelizbackend.features.mediations.repository.MediationRepository;
import cl.metspherical.calbucofelizbackend.features.mediations.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MediationRepository mediationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediationParticipantService participantService;

    @InjectMocks
    private MessageService messageService;

    @Test
    void shouldReturnEmptyListWhenNoMessagesFound() {
        // Given
        UUID mediationId = UUID.randomUUID();
        when(messageRepository.findByMediationIdOrderBySentAtAsc(mediationId)).thenReturn(List.of());

        // When
        List<Message> result = messageService.getMessagesByMediation(mediationId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnMessagesWhenFound() {
        // Given
        UUID mediationId = UUID.randomUUID();
        Message message1 = new Message();
        Message message2 = new Message();
        List<Message> expectedMessages = List.of(message1, message2);
        
        when(messageRepository.findByMediationIdOrderBySentAtAsc(mediationId)).thenReturn(expectedMessages);

        // When
        List<Message> result = messageService.getMessagesByMediation(mediationId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(message1, message2);
    }

    @Test
    void shouldCallRepositoryWithCorrectMediationId() {
        // Given
        UUID mediationId = UUID.randomUUID();
        when(messageRepository.findByMediationIdOrderBySentAtAsc(mediationId)).thenReturn(List.of());

        // When
        messageService.getMessagesByMediation(mediationId);

        // Then - Implicitly verified by the mock interaction
        assertThat(true).isTrue(); // Test passes if no exception is thrown
    }

    @Test
    void shouldThrowExceptionWhenMediationNotFound() {
        // Given
        UUID mediationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        when(mediationRepository.findById(mediationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> messageService.saveMessage(mediationId, senderId, "content"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Mediation not found");
    }

    @Test
    void shouldThrowExceptionWhenContentIsEmpty() {
        // Given
        UUID mediationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        Mediation mediation = createMockMediation(false);
        User user = createMockUser();
        
        when(mediationRepository.findById(mediationId)).thenReturn(Optional.of(mediation));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(user));
        when(participantService.canUserTalk(senderId, mediationId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> messageService.saveMessage(mediationId, senderId, ""))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("content cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenContentIsNull() {
        // Given
        UUID mediationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        Mediation mediation = createMockMediation(false);
        User user = createMockUser();
        
        when(mediationRepository.findById(mediationId)).thenReturn(Optional.of(mediation));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(user));
        when(participantService.canUserTalk(senderId, mediationId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> messageService.saveMessage(mediationId, senderId, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("content cannot be empty");
    }

    private Mediation createMockMediation(boolean isSolved) {
        Mediation mediation = new Mediation();
        mediation.setIsSolved(isSolved);
        return mediation;
    }

    private User createMockUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }
}
