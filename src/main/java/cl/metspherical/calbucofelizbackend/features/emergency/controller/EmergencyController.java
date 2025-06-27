package cl.metspherical.calbucofelizbackend.features.emergency.controller;

import cl.metspherical.calbucofelizbackend.common.security.utils.SecurityUtils;
import cl.metspherical.calbucofelizbackend.features.emergency.dto.EmergencyDTO;
import cl.metspherical.calbucofelizbackend.features.emergency.dto.EmergencyRequestDTO;
import cl.metspherical.calbucofelizbackend.features.emergency.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping("/post")
    public EmergencyDTO createEmergency(@RequestBody EmergencyRequestDTO request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return emergencyService.createEmergency(request.content(), userId);
    }

    @GetMapping()
    public List<EmergencyDTO> getEmergency(){
        return emergencyService.getEmergencies();
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<Void> cancelEmergency(){
        UUID userId = SecurityUtils.getCurrentUserId();
        emergencyService.cancelEmergency(userId);
        return ResponseEntity.ok().build();
    }
}
