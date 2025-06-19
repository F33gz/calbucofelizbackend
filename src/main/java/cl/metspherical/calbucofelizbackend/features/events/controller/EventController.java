package cl.metspherical.calbucofelizbackend.features.events.controller;


import cl.metspherical.calbucofelizbackend.common.security.utils.SecurityUtils;
import cl.metspherical.calbucofelizbackend.features.events.dto.CreateEventRequestDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.EventDetailDTO;
import cl.metspherical.calbucofelizbackend.features.events.dto.EventsByMonthResponseDTO;
import cl.metspherical.calbucofelizbackend.features.events.service.EventService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<?> createEvent(@RequestBody CreateEventRequestDTO createEventRequest) {
        try {
            UUID authorId = SecurityUtils.getCurrentUserId();
            EventDetailDTO response = eventService.createEvent(createEventRequest, authorId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-month/{month}")
    public ResponseEntity<?> getAllEvents(@PathVariable String month) {
        try {
            EventsByMonthResponseDTO response = eventService.getEventsByMonth(month);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Integer id){
        try {
            EventDetailDTO response = eventService.getEventById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/assistans")
    public ResponseEntity<?> getEventsByAssistanId(@PathVariable Integer id){
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer id){
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/assist")
    public ResponseEntity<?> addAssistant(@PathVariable Integer id){
        return ResponseEntity.ok().build();
    }
}