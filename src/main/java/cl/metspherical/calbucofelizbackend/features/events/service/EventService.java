package cl.metspherical.calbucofelizbackend.features.events.service;

import cl.metspherical.calbucofelizbackend.features.events.dto.AssistansResponseDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.CreateAssistantResponseDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.CreateEventRequestDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.EventDetailDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.EventOverviewDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.EventsByMonthResponseDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.UserBasicDTO;
import cl.metspherical.calbucofelizbackend.features.events.enums.AssistanceType;
import cl.metspherical.calbucofelizbackend.features.events.model.Assistance;
import cl.metspherical.calbucofelizbackend.features.events.model.Event;
import cl.metspherical.calbucofelizbackend.features.events.model.EventAssistant;
import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.features.events.repository.AssistanceRepository;
import cl.metspherical.calbucofelizbackend.features.events.repository.EventRepository;
import cl.metspherical.calbucofelizbackend.features.events.repository.EventAssistantRepository;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventAssistantRepository eventAssistantRepository;
    private final AssistanceRepository assistanceRepository;
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event not found with id: ";

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
                .toList();
        
        return new EventsByMonthResponseDTO(monthName, eventDTOs);
    }

    /**
     * Gets an event by its ID
     * 
     * @param id ID of the event to retrieve
     * @return EventDetailDTO containing event information
     */
    public EventDetailDTO getEventById(Integer id) {
        Event event = eventRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE + id));
        
        return convertToEventDetailDTO(event);
    }

    /**
     * Deletes an event and all its associated data
     *
     * @param id ID of the event to delete
     */
    public void deleteEvent(Integer id,UUID userId) {
        // 1. Validate event exists
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE + id));
        // 2. Validate user is the creator of the event
        if (!event.getCreatedBy().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this event");
        }

        // 3. Delete the event (cascade will handle event assistants and other relationships)
        eventRepository.delete(event);
    }

    /**
     * Gets all assistants for a specific event
     *
     * @param eventId ID of the event to get assistants for
     * @return List of AssistansResponseDTO containing assistant information
     */
    public List<AssistansResponseDTO> getEventsByAssistantId(Integer eventId) {
        // 1. Validate event exists
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE + eventId);
        }

        // 2. Get all assistants for the event
        List<EventAssistant> eventAssistants = eventAssistantRepository.findByEventIdWithDetails(eventId);

        // 3. Convert to DTOs
        return eventAssistants.stream()
                .map(this::convertToAssistansResponseDTO)
                .toList();
    }

    /**
     * Adds or updates an assistant to an event
     *
     * @param eventId ID of the event
     * @param userId ID of the user
     * @param type String representing the assistance type
     * @return CreateAssistantResponseDTO with status and type
     */
    @Transactional
    public CreateAssistantResponseDTO addAssistant(Integer eventId, UUID userId,String type) {
        // 1. Validate event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENT_NOT_FOUND_MESSAGE + eventId));

        // 2. Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        // 3. Convert string to AssistanceType enum
        AssistanceType assistanceType = AssistanceType.fromDisplayName(type)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assistance type: " + type));

        // 4. Get or create assistance by name (which is the AssistanceType)
        Assistance assistance = assistanceRepository.findByName(assistanceType)
                .orElseGet(() -> {
                    // Create new assistance if it doesn't exist
                    Assistance newAssistance = Assistance.builder()
                            .id((byte) (assistanceType.ordinal() + 1))
                            .name(assistanceType)
                            .build();
                    return assistanceRepository.save(newAssistance);
                });

        // 5. Check if user is already registered for this event
        EventAssistant.EventAssistantId assistantId = new EventAssistant.EventAssistantId(userId, eventId);
        EventAssistant existingAssistant = eventAssistantRepository.findById(assistantId).orElse(null);

        String status;
        if (existingAssistant != null) {
            // Update existing assistance
            existingAssistant.setAssistance(assistance);
            eventAssistantRepository.save(existingAssistant);
            status = "updated";
        } else {
            // Create new assistant
            EventAssistant newAssistant = EventAssistant.builder()
                    .user(user)
                    .event(event)
                    .assistance(assistance)
                    .build();
            eventAssistantRepository.save(newAssistant);
            status = "created";
        }

        return new CreateAssistantResponseDTO(status, assistance.getName().getDisplayName());
    }

    /**
     * Creates a new event
     * 
     * @param createEventRequest DTO containing event data
     * @return EventDetailDTO of the created event
     */
    public EventDetailDTO createEvent(CreateEventRequestDTO createEventRequest,UUID  authorId) {
        // Validate user exists
        User author = userRepository.getReferenceById(authorId);
        
        // Parse dates
        LocalDateTime initDateTime;
        LocalDateTime endingDateTime;
        try {
            initDateTime = LocalDateTime.parse(createEventRequest.init());
            endingDateTime = LocalDateTime.parse(createEventRequest.ending());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
        }
        
        // Validate that ending is after init
        if (endingDateTime.isBefore(initDateTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event ending time must be after init time");
        }
        
        // Create event entity
        Event event = Event.builder()
                .title(createEventRequest.title())
                .desc(createEventRequest.desc())
                .address(createEventRequest.adress()) // Note: typo in DTO field name
                .init(initDateTime)
                .ending(endingDateTime)
                .createdBy(author)
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
     * Converts EventAssistant entity to AssistansResponseDTO
     *
     * @param eventAssistant EventAssistant entity
     * @return AssistansResponseDTO
     */
    private AssistansResponseDTO convertToAssistansResponseDTO(EventAssistant eventAssistant) {
        UserBasicDTO userBasicDTO = new UserBasicDTO(
                eventAssistant.getUser().getUsername(),
                eventAssistant.getUser().getAvatar()
        );        return new AssistansResponseDTO(
                eventAssistant.getAssistance().getName().getDisplayName(),
                userBasicDTO
        );
    }

    /**
     * Converts month name to month number
     * 
     * @param monthName Month name (case insensitive)
     * @return Month number (1-12)
     */
    private int getMonthNumber(String monthName) {
        try {
            Month month = Month.valueOf(monthName.toUpperCase());
            return month.getValue();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid month name: " + monthName +
                ". Valid months are: January, February, March, April, May, June, " +
                "July, August, September, October, November, December");
        }
    }
}
