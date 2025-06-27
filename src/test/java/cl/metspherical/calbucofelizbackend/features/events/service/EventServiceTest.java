package cl.metspherical.calbucofelizbackend.features.events.service;

import cl.metspherical.calbucofelizbackend.features.events.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldThrowExceptionWhenEventNotFound() {
        // Given
        Integer eventId = 999;
        when(eventRepository.findByIdWithDetails(eventId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventService.getEventById(eventId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Event not found with id: 999");
    }

    @Test
    void shouldThrowExceptionWhenEventNotFoundForAssistants() {
        // Given
        Integer eventId = 999;
        when(eventRepository.existsById(eventId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> eventService.getEventsByAssistantId(eventId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Event not found with id: 999");
    }
}
