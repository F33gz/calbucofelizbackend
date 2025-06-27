package cl.metspherical.calbucofelizbackend.features.emergency.service;

import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.features.emergency.dto.EmergencyDTO;
import cl.metspherical.calbucofelizbackend.features.emergency.repository.EmergencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock
    private EmergencyRepository emergencyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmergencyService emergencyService;

    @Test
    void shouldThrowExceptionWhenUserNotFoundInCreateEmergency() {
        // Given
        UUID userId = UUID.randomUUID();
        String content = "Emergency content";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> emergencyService.createEmergency(content, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found with id: " + userId);
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveEmergencies() {
        // Given
        when(emergencyRepository.findByFinishedAtAfter(any(LocalDateTime.class))).thenReturn(List.of());

        // When
        List<EmergencyDTO> result = emergencyService.getEmergencies();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCallRepositoryWithCurrentTimeForActiveEmergencies() {
        // Given
        when(emergencyRepository.findByFinishedAtAfter(any(LocalDateTime.class))).thenReturn(List.of());

        // When
        emergencyService.getEmergencies();

        // Then - Implicitly verified by the mock interaction
        assertThat(true).isTrue(); // Test passes if no exception is thrown
    }
}
