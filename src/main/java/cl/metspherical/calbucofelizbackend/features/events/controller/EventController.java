package cl.metspherical.calbucofelizbackend.features.events.controller;


import cl.metspherical.calbucofelizbackend.common.security.utils.SecurityUtils;
import cl.metspherical.calbucofelizbackend.features.events.dto.*;
import cl.metspherical.calbucofelizbackend.features.events.service.EventService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<EventDetailDTO> createEvent(@RequestBody CreateEventRequestDTO createEventRequest) {
        UUID authorId = SecurityUtils.getCurrentUserId();
        EventDetailDTO response = eventService.createEvent(createEventRequest, authorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<EventsByMonthResponseDTO> getAllEvents(@RequestParam String month) {
        EventsByMonthResponseDTO response = eventService.getEventsByMonth(month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDTO> getEventById(@PathVariable Integer id){
        EventDetailDTO response = eventService.getEventById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/assistans")
    public ResponseEntity<List<AssistansResponseDTO>> getEventsByAssistanId(@PathVariable Integer id){
        List<AssistansResponseDTO> response = eventService.getEventsByAssistantId(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer id){
        UUID userId = SecurityUtils.getCurrentUserId();
        eventService.deleteEvent(id,userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assist")
    public ResponseEntity<CreateAssistantResponseDTO> addAssistant(@PathVariable Integer id, @RequestParam String type){
        UUID userId = SecurityUtils.getCurrentUserId();
        CreateAssistantResponseDTO response = eventService.addAssistant(id, userId, type);
        return ResponseEntity.ok(response);
    }
}