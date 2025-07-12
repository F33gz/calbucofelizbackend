package cl.metspherical.calbucofelizbackend.features.events.service;

import cl.metspherical.calbucofelizbackend.features.events.dto.*;
import cl.metspherical.calbucofelizbackend.features.events.enums.AssistanceType;
import cl.metspherical.calbucofelizbackend.features.events.model.Assistance;
import cl.metspherical.calbucofelizbackend.features.events.model.Event;
import cl.metspherical.calbucofelizbackend.features.events.model.EventAssistant;
import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.features.events.repository.AssistanceRepository;
import cl.metspherical.calbucofelizbackend.features.events.repository.EventRepository;
import cl.metspherical.calbucofelizbackend.features.events.repository.EventAssistantRepository;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventAssistantRepository eventAssistantRepository;

    @Mock
    private AssistanceRepository assistanceRepository;

    @InjectMocks
    private EventService eventService;

    private User testUser;
    private Event testEvent;
    private EventAssistant testEventAssistant;
    private Assistance testAssistance;
    private UUID testUserId;
    private Integer testEventId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEventId = 1;

        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .avatar("avatar.jpg")
                .build();

        testEvent = Event.builder()
                .id(testEventId)
                .title("Test Event")
                .desc("Test Description")
                .address("Test Address")
                .init(LocalDateTime.now().plusDays(1))
                .ending(LocalDateTime.now().plusDays(1).plusHours(2))
                .createdBy(testUser)
                .build();

        testAssistance = Assistance.builder()
                .id((byte) 1)
                .name(AssistanceType.ATTENDING)
                .build();

        testEventAssistant = EventAssistant.builder()
                .user(testUser)
                .event(testEvent)
                .assistance(testAssistance)
                .build();
    }

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

    @Test
    void shouldGetEventsByMonthSuccessfully() {
        // Given
        List<Event> events = List.of(testEvent);
        when(eventRepository.findEventsByMonth(1)).thenReturn(events);

        // When
        EventsByMonthResponseDTO result = eventService.getEventsByMonth("January");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.monthName()).isEqualTo("January");
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0).id()).isEqualTo(testEventId);
        assertThat(result.events().get(0).title()).isEqualTo("Test Event");
    }

    @Test
    void shouldThrowExceptionForInvalidMonthName() {
        // When & Then
        assertThatThrownBy(() -> eventService.getEventsByMonth("InvalidMonth"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid month name");
    }

    @Test
    void shouldGetEventByIdSuccessfully() {
        // Given
        when(eventRepository.findByIdWithDetails(testEventId)).thenReturn(Optional.of(testEvent));

        // When
        EventDetailDTO result = eventService.getEventById(testEventId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testEventId);
        assertThat(result.title()).isEqualTo("Test Event");
        assertThat(result.desc()).isEqualTo("Test Description");
        assertThat(result.address()).isEqualTo("Test Address");
        assertThat(result.createdBy()).isEqualTo("testuser");
    }

    @Test
    void shouldDeleteEventSuccessfully() {
        // Given
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

        // When
        eventService.deleteEvent(testEventId, testUserId);

        // Then
        verify(eventRepository).delete(testEvent);
    }

    @Test
    void shouldThrowExceptionWhenDeletingEventWithWrongUser() {
        // Given
        UUID wrongUserId = UUID.randomUUID();
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

        // When & Then
        assertThatThrownBy(() -> eventService.deleteEvent(testEventId, wrongUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You are not authorized to delete this event");
    }

    @Test
    void shouldGetEventsByAssistantIdSuccessfully() {
        // Given
        List<EventAssistant> eventAssistants = List.of(testEventAssistant);
        when(eventRepository.existsById(testEventId)).thenReturn(true);
        when(eventAssistantRepository.findByEventIdWithDetails(testEventId)).thenReturn(eventAssistants);

        // When
        List<AssistansResponseDTO> result = eventService.getEventsByAssistantId(testEventId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).assistanceType()).isEqualTo("Attending");
        assertThat(result.get(0).user().username()).isEqualTo("testuser");
        assertThat(result.get(0).user().avatar()).isEqualTo("avatar.jpg");
    }

    @Test
    void shouldAddAssistantSuccessfully() {
        // Given
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(assistanceRepository.findByName(AssistanceType.ATTENDING)).thenReturn(Optional.of(testAssistance));
        when(eventAssistantRepository.findById(any())).thenReturn(Optional.empty());
        when(eventAssistantRepository.save(any(EventAssistant.class))).thenReturn(testEventAssistant);

        // When
        CreateAssistantResponseDTO result = eventService.addAssistant(testEventId, testUserId, "Attending");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("created");
        assertThat(result.type()).isEqualTo("Attending");
        verify(eventAssistantRepository).save(any(EventAssistant.class));
    }

    @Test
    void shouldUpdateExistingAssistant() {
        // Given
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(assistanceRepository.findByName(AssistanceType.ATTENDING)).thenReturn(Optional.of(testAssistance));
        when(eventAssistantRepository.findById(any())).thenReturn(Optional.of(testEventAssistant));
        when(eventAssistantRepository.save(any(EventAssistant.class))).thenReturn(testEventAssistant);

        // When
        CreateAssistantResponseDTO result = eventService.addAssistant(testEventId, testUserId, "Attending");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("updated");
        assertThat(result.type()).isEqualTo("Attending");
        verify(eventAssistantRepository).save(testEventAssistant);
    }

    @Test
    void shouldThrowExceptionForInvalidAssistanceType() {
        // Given
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> eventService.addAssistant(testEventId, testUserId, "InvalidType"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid assistance type");
    }

    @Test
    void shouldCreateEventSuccessfully() {
        // Given
        CreateEventRequestDTO request = new CreateEventRequestDTO(
                "New Event",
                "New Description",
                "New Address",
                LocalDateTime.now().plusDays(1).toString(),
                LocalDateTime.now().plusDays(1).plusHours(2).toString()
        );

        when(userRepository.getReferenceById(testUserId)).thenReturn(testUser);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId(2);
            return event;
        });

        // When
        EventDetailDTO result = eventService.createEvent(request, testUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("New Event");
        assertThat(result.desc()).isEqualTo("New Description");
        assertThat(result.address()).isEqualTo("New Address");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldThrowExceptionForInvalidDateFormat() {
        // Given
        CreateEventRequestDTO request = new CreateEventRequestDTO(
                "New Event",
                "New Description",
                "New Address",
                "invalid-date",
                "invalid-date"
        );

        when(userRepository.getReferenceById(testUserId)).thenReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> eventService.createEvent(request, testUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid date format");
    }

    @Test
    void shouldThrowExceptionWhenEndingBeforeInit() {
        // Given
        CreateEventRequestDTO request = new CreateEventRequestDTO(
                "New Event",
                "New Description",
                "New Address",
                LocalDateTime.now().plusDays(2).toString(),
                LocalDateTime.now().plusDays(1).toString()
        );

        when(userRepository.getReferenceById(testUserId)).thenReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> eventService.createEvent(request, testUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Event ending time must be after init time");
    }
}
