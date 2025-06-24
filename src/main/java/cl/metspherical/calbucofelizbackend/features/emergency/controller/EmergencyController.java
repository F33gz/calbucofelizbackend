package cl.metspherical.calbucofelizbackend.features.emergency.controller;

import cl.metspherical.calbucofelizbackend.common.security.utils.SecurityUtils;
import cl.metspherical.calbucofelizbackend.features.emergency.dto.EmergencyDTO;
import cl.metspherical.calbucofelizbackend.features.emergency.dto.EmergencyRequestDTO;
import cl.metspherical.calbucofelizbackend.features.emergency.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

}
