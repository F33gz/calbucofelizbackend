package cl.metspherical.calbucofelizbackend.features.events.service;

import cl.metspherical.calbucofelizbackend.features.events.dto.CreateEventRequestDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.EventDetailDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.EventOverviewDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.EventsByMonthResponseDTO;
import cl.metspherical.calbucofelizbackend.features.events.model.Event;
import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.features.events.repository.EventRepository;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Gets all events for a specific month
     * 
     * @param monthName The name of the month (e.g., "January", "February", etc.)
     * @return EventsByMonthResponseDTO containing the month name and list of events
     */
    public EventsByMonthResponseDTO getEventsByMonth(String monthName) {
        // Convert month name to number
        int monthNumber = getMonthNumber(monthName);
        
        // Find events for the specified month
        List<Event> events = eventRepository.findEventsByMonth(monthNumber);
        
        // Convert to DTOs
        List<EventOverviewDTO> eventDTOs = events.stream()
                .map(this::convertToEventDTO)
                .collect(Collectors.toList());
        
        return new EventsByMonthResponseDTO(monthName, eventDTOs);
    }    /**
     * Gets an event by its ID
     * 
     * @param id ID of the event to retrieve
     * @return EventDetailDTO containing event information
     * @throws RuntimeException if event not found
     */
    public EventDetailDTO getEventById(Integer id) {
        Event event = eventRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        return convertToEventDetailDTO(event);
    }

    /**
     * Deletes an event and all its associated data
     *
     * @param id ID of the event to delete
     * @throws RuntimeException if event not found
     */
    public void deleteEvent(Integer id) {
        // 1. Validate event exists
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // 2. Delete the event (cascade will handle event assistants and other relationships)
        eventRepository.delete(event);
    }

    /**
     * Creates a new event
     * 
     * @param createEventRequest DTO containing event data
     * @return EventDetailDTO of the created event
     * @throws RuntimeException if user not found or invalid data
     */
    public EventDetailDTO createEvent(CreateEventRequestDTO createEventRequest) {
        // Validate user exists
        User user = userRepository.findByUsername(createEventRequest.username())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + createEventRequest.username()));
        
        // Parse dates - assuming ISO format like "2025-06-15T10:00:00"
        LocalDateTime initDateTime;
        LocalDateTime endingDateTime;
        try {
            initDateTime = LocalDateTime.parse(createEventRequest.init());
            endingDateTime = LocalDateTime.parse(createEventRequest.ending());
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
        }
        
        // Validate that ending is after init
        if (endingDateTime.isBefore(initDateTime)) {
            throw new RuntimeException("Event ending time must be after init time");
        }
        
        // Create event entity
        Event event = Event.builder()
                .title(createEventRequest.title())
                .desc(createEventRequest.desc())
                .address(createEventRequest.adress()) // Note: typo in DTO field name
                .init(initDateTime)
                .ending(endingDateTime)
                .createdBy(user)
                .build();
        
        // Save event
        Event savedEvent = eventRepository.save(event);
        
        // Convert to DTO and return
        return convertToEventDetailDTO(savedEvent);
    }

    /**
     * Converts Event entity to EventOverviewDTO
     * 
     * @param event Event entity
     * @return EventOverviewDTO
     */
    private EventOverviewDTO convertToEventDTO(Event event) {
        return new EventOverviewDTO(
                event.getId(),
                event.getTitle(),
                event.getInit()
        );
    }

    /**
     * Converts Event entity to EventDetailDTO
     * 
     * @param event Event entity
     * @return EventDetailDTO
     */
    private EventDetailDTO convertToEventDetailDTO(Event event) {
        return new EventDetailDTO(
                event.getId(),
                event.getTitle(),
                event.getDesc(),
                event.getAddress(),
                event.getInit(),
                event.getEnding(),
                event.getCreatedBy().getUsername()
        );
    }

    /**
     * Converts month name to month number
     * 
     * @param monthName Month name (case insensitive)
     * @return Month number (1-12)
     * @throws RuntimeException if month name is invalid
     */
    private int getMonthNumber(String monthName) {
        try {
            Month month = Month.valueOf(monthName.toUpperCase());
            return month.getValue();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid month name: " + monthName + 
                ". Valid months are: January, February, March, April, May, June, " +
                "July, August, September, October, November, December");
        }
    }
}
