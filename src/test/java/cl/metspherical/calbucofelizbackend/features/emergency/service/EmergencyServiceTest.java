package cl.metspherical.calbucofelizbackend.features.emergency.service;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.features.emergency.dto.EmergencyDTO;
import cl.metspherical.calbucofelizbackend.features.emergency.model.Emergency;
import cl.metspherical.calbucofelizbackend.features.emergency.repository.EmergencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock
    private EmergencyRepository emergencyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmergencyService emergencyService;

    @Test
    void createEmergency_shouldCreateAndReturnDTO_whenUserExists() {
        UUID authorId = UUID.randomUUID();
        String content = "Test emergency content";
        User author = User.builder().id(authorId).username("testuser").build();

        // Mock what the Emergency object will look like *after* @PrePersist and save
        Emergency emergencyToSave = Emergency.builder()
                .content(content)
                .author(author)
                // createdAt and finishedAt are set by @PrePersist
                .build();

        Emergency savedEmergency = Emergency.builder()
                .id(UUID.randomUUID())
                .content(content)
                .author(author)
                .createdAt(LocalDateTime.now().minusSeconds(1)) // Simulate it was just persisted
                .finishedAt(LocalDateTime.now().plusHours(1).minusSeconds(1)) // Simulate it was just persisted
                .build();


        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        // We need to match the argument passed to save, which won't have id, createdAt, finishedAt yet.
        when(emergencyRepository.save(any(Emergency.class))).thenAnswer(invocation -> {
            Emergency em = invocation.getArgument(0);
            // Simulate what @PrePersist and DB save would do if we need specific values back
            return Emergency.builder()
                    .id(UUID.randomUUID()) // Simulate DB generated ID
                    .content(em.getContent())
                    .author(em.getAuthor())
                    .createdAt(savedEmergency.getCreatedAt()) // Use pre-determined values for assertion
                    .finishedAt(savedEmergency.getFinishedAt())
                    .build();
        });

        EmergencyDTO result = emergencyService.createEmergency(content, authorId);

        assertNotNull(result);
        // ID will be dynamic due to UUID.randomUUID() in the 'thenAnswer'
        assertNotNull(result.id());
        assertEquals(content, result.content());
        assertEquals(author.getUsername(), result.username());
        assertEquals(savedEmergency.getCreatedAt(), result.createdAt());

        verify(userRepository).findById(authorId);
        verify(emergencyRepository).save(any(Emergency.class));
    }

    @Test
    void createEmergency_shouldThrowNotFound_whenUserDoesNotExist() {
        UUID authorId = UUID.randomUUID();
        String content = "Test emergency content";

        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            emergencyService.createEmergency(content, authorId);
        });

        verify(userRepository).findById(authorId);
        verify(emergencyRepository, never()).save(any(Emergency.class));
    }

    @Test
    void getEmergencies_shouldReturnListOfActiveEmergencies() {
        LocalDateTime now = LocalDateTime.now();
        User author = User.builder().id(UUID.randomUUID()).username("testuser").build();
        Emergency activeEmergency = Emergency.builder()
                .id(UUID.randomUUID())
                .author(author)
                .content("Active")
                .createdAt(now.minusMinutes(5)) // Set in the past
                .finishedAt(now.plusHours(1)) // Set in the future
                .build();

        // The service calls findByFinishedAtAfter(LocalDateTime.now())
        // So our mock should be prepared for any LocalDateTime argument
        when(emergencyRepository.findByFinishedAtAfter(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(activeEmergency));

        List<EmergencyDTO> results = emergencyService.getEmergencies();

        assertNotNull(results);
        assertEquals(1, results.size());
        EmergencyDTO dto = results.get(0);
        assertEquals(activeEmergency.getId(), dto.id());
        assertEquals(activeEmergency.getContent(), dto.content());
        assertEquals(author.getUsername(), dto.username());
        assertEquals(activeEmergency.getCreatedAt(), dto.createdAt());

        verify(emergencyRepository).findByFinishedAtAfter(any(LocalDateTime.class));
    }

    @Test
    void getEmergencies_shouldReturnEmptyList_whenNoActiveEmergencies() {
        when(emergencyRepository.findByFinishedAtAfter(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<EmergencyDTO> results = emergencyService.getEmergencies();

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(emergencyRepository).findByFinishedAtAfter(any(LocalDateTime.class));
    }

    @Test
    void cancelEmergency_shouldDeleteLastEmergency_whenUserAndEmergencyExist() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("testuser").build();
        Emergency lastEmergency = Emergency.builder()
            .id(UUID.randomUUID())
            .author(user)
            .content("To be deleted")
            .createdAt(LocalDateTime.now().minusMinutes(10))
            .finishedAt(LocalDateTime.now().plusHours(1))
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(emergencyRepository.findTopByAuthorOrderByCreatedAtDesc(user)).thenReturn(Optional.of(lastEmergency));
        doNothing().when(emergencyRepository).delete(lastEmergency);

        assertDoesNotThrow(() -> emergencyService.cancelEmergency(userId));

        verify(userRepository).findById(userId);
        verify(emergencyRepository).findTopByAuthorOrderByCreatedAtDesc(user);
        verify(emergencyRepository).delete(lastEmergency);
    }

    @Test
    void cancelEmergency_shouldThrowNotFound_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> emergencyService.cancelEmergency(userId));

        verify(userRepository).findById(userId);
        verify(emergencyRepository, never()).findTopByAuthorOrderByCreatedAtDesc(any(User.class));
        verify(emergencyRepository, never()).delete(any(Emergency.class));
    }

    @Test
    void cancelEmergency_shouldThrowNotFound_whenNoEmergenciesForUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("testuser").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(emergencyRepository.findTopByAuthorOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> emergencyService.cancelEmergency(userId));

        verify(userRepository).findById(userId);
        verify(emergencyRepository).findTopByAuthorOrderByCreatedAtDesc(user);
        verify(emergencyRepository, never()).delete(any(Emergency.class));
    }
}
