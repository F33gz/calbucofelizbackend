package cl.metspherical.calbucofelizbackend.features.mediations.controller;


import cl.metspherical.calbucofelizbackend.common.security.utils.SecurityUtils;
import cl.metspherical.calbucofelizbackend.features.mediations.dto.CreateMediationRequestDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.dto.MediationsResponseDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Mediation;
import cl.metspherical.calbucofelizbackend.features.mediations.service.MediationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/mediations")
@RequiredArgsConstructor
public class MediationsController {

    private final MediationService mediationService;

    @PostMapping("/create")
    public ResponseEntity<UUID> createMediation(@RequestBody CreateMediationRequestDTO createMediationRequestDTO) {
        UUID userId = SecurityUtils.getCurrentUserId();
        UUID mediationId = mediationService.createMediation(createMediationRequestDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mediationId);
    }

    @GetMapping()
    public ResponseEntity<MediationsResponseDTO> getAllMediations() {
        UUID userId = SecurityUtils.getCurrentUserId();
        MediationsResponseDTO mediations = mediationService.getAllMediationsByUser(userId);
        return ResponseEntity.ok(mediations);
    }
}
