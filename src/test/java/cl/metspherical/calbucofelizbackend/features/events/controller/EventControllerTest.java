package cl.metspherical.calbucofelizbackend.features.events.controller;

import cl.metspherical.calbucofelizbackend.common.security.utils.SecurityUtils;
import cl.metspherical.calbucofelizbackend.features.events.dto.*;
import cl.metspherical.calbucofelizbackend.features.events.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private UUID testUserId;
    private Integer testEventId;
    private CreateEventRequestDTO testCreateEventRequest;
    private EventDetailDTO testEventDetail;
    private EventsByMonthResponseDTO testEventsByMonth;
    private List<AssistansResponseDTO> testAssistants;
    private CreateAssistantResponseDTO testAssistantResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEventId = 1;

        testCreateEventRequest = new CreateEventRequestDTO(
                "Test Event",
                "Test Description",
                "Test Address",
                LocalDateTime.now().plusDays(1).toString(),
                LocalDateTime.now().plusDays(1).plusHours(2).toString()
        );

        testEventDetail = new EventDetailDTO(
                testEventId,
                "Test Event",
                "Test Description",
                "Test Address",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "testuser"
        );

        List<EventOverviewDTO> events = List.of(
                new EventOverviewDTO(testEventId, "Test Event", LocalDateTime.now().plusDays(1))
        );
        testEventsByMonth = new EventsByMonthResponseDTO("January", events);

        UserBasicDTO userBasic = new UserBasicDTO("testuser", "avatar.jpg");
        testAssistants = List.of(
                new AssistansResponseDTO("Attending", userBasic)
        );

        testAssistantResponse = new CreateAssistantResponseDTO("created", "Attending");
    }

    @Test
    void shouldCreateEventSuccessfully() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            when(eventService.createEvent(testCreateEventRequest, testUserId)).thenReturn(testEventDetail);

            // When
            var response = eventController.createEvent(testCreateEventRequest);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody()).isEqualTo(testEventDetail);
            verify(eventService).createEvent(testCreateEventRequest, testUserId);
        }
    }

    @Test
    void shouldGetAllEventsSuccessfully() {
        // Given
        String month = "January";
        when(eventService.getEventsByMonth(month)).thenReturn(testEventsByMonth);

        // When
        var response = eventController.getAllEvents(month);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(testEventsByMonth);
        verify(eventService).getEventsByMonth(month);
    }

    @Test
    void shouldGetEventByIdSuccessfully() {
        // Given
        when(eventService.getEventById(testEventId)).thenReturn(testEventDetail);

        // When
        var response = eventController.getEventById(testEventId);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(testEventDetail);
        verify(eventService).getEventById(testEventId);
    }

    @Test
    void shouldGetEventsByAssistantIdSuccessfully() {
        // Given
        when(eventService.getEventsByAssistantId(testEventId)).thenReturn(testAssistants);

        // When
        var response = eventController.getEventsByAssistanId(testEventId);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(testAssistants);
        verify(eventService).getEventsByAssistantId(testEventId);
    }

    @Test
    void shouldDeleteEventSuccessfully() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            doNothing().when(eventService).deleteEvent(testEventId, testUserId);

            // When
            var response = eventController.deleteEvent(testEventId);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(204);
            verify(eventService).deleteEvent(testEventId, testUserId);
        }
    }

    @Test
    void shouldAddAssistantSuccessfully() {
        // Given
        String assistanceType = "Attending";

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            when(eventService.addAssistant(testEventId, testUserId, assistanceType)).thenReturn(testAssistantResponse);

            // When
            var response = eventController.addAssistant(testEventId, assistanceType);

            // Then
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody()).isEqualTo(testAssistantResponse);
            verify(eventService).addAssistant(testEventId, testUserId, assistanceType);
        }
    }
}