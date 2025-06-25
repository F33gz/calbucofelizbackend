package cl.metspherical.calbucofelizbackend.features.mediations.service;

import cl.metspherical.calbucofelizbackend.features.mediations.dto.CreateMediationRequestDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.dto.MediationOverviewDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.dto.MediationsResponseDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Mediation;
import cl.metspherical.calbucofelizbackend.features.mediations.model.MediationParticipant;
import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.features.mediations.repository.MediationRepository;
import cl.metspherical.calbucofelizbackend.features.mediations.repository.MediationParticipantRepository;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service responsible for handling mediation operations
 * including creation and management of mediations and their participants
 */
@Service
@RequiredArgsConstructor
public class MediationService {

    private final MediationRepository mediationRepository;
    private final MediationParticipantRepository mediationParticipantRepository;
    private final UserRepository userRepository;
    private final MediationParticipantService participantService;

    /**
     * Creates a new mediation in the system
     * 
     * @param request DTO containing mediation creation data
     * @param creatorId UUID of the user creating the mediation
     * @return UUID ID of the created mediation
     */
    @Transactional
    public UUID createMediation(CreateMediationRequestDTO request, UUID creatorId) {
        // Validate creator exists
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"User not found with id: " + creatorId));

        // Create mediation entity
        Mediation mediation = Mediation.builder()
                .title(request.title())
                .mediationType(request.type())
                .isSolved(false) // Default value when creating
                .createdBy(creator)
                .participants(new HashSet<>())
                .messages(new HashSet<>())
                .build();

        // Save mediation first to get ID
        Mediation savedMediation = mediationRepository.save(mediation);

        // Create participants based on mediation type
        createMediationParticipants(savedMediation, request);
        // Automatically assign a moderator
        participantService.assignAutomaticModerator(savedMediation.getId());

        return savedMediation.getId();
    }

    /**
     * Creates mediation participants based on mediation type
     */
    private void createMediationParticipants(Mediation mediation, CreateMediationRequestDTO request) {
        Set<MediationParticipant> participants;

        if (Boolean.FALSE.equals(request.type())) {
            // Public mediation - add all users from database
            participants = participantService.createPublicMediationParticipants(mediation);
        } else {
            // Private mediation - add specified participants and ensure creator is included
            participants = participantService.createPrivateMediationParticipants(mediation, request.participants());
            
            // Optimización: verificar si el creador ya está incluido de forma más eficiente
            boolean creatorAlreadyParticipant = participants.stream()
                    .map(p -> p.getUser().getId())
                    .anyMatch(mediation.getCreatedBy().getId()::equals);
            
            if (!creatorAlreadyParticipant) {
                participants.add(MediationParticipant.builder()
                        .user(mediation.getCreatedBy())
                        .mediation(mediation)
                        .canTalk(true)
                        .isModerator(false)
                        .build());
            }
        }

        // Optimización: operación batch en una sola transacción
        mediationParticipantRepository.saveAll(participants);
        mediation.setParticipants(participants);
        mediationRepository.save(mediation);
    }

    /**
     * Gets all mediations where the user is a participant
     * 
     * @param userId UUID of the user to get mediations for
     * @return MediationsResponseDTO containing list of mediations
     */
    public MediationsResponseDTO getAllMediationsByUser(UUID userId) {
        // Get all mediation participants for the user
        List<MediationParticipant> userParticipations = mediationParticipantRepository.findByUserIdWithMediation(userId);
        
        // Transform to DTO
        List<MediationOverviewDTO> mediationDTOs = userParticipations.stream()
                .map(participation -> convertToMediationOverviewDTO(participation.getMediation()))
                .toList();
        
        return new MediationsResponseDTO(mediationDTOs);
    }

    /**
     * Converts Mediation entity to MediationOverviewDTO
     */
    private MediationOverviewDTO convertToMediationOverviewDTO(Mediation mediation) {
        return new MediationOverviewDTO(
                mediation.getId(),
                mediation.getTitle(),
                Boolean.TRUE.equals(mediation.getMediationType()) ? "private" : "public",
                mediation.getCreatedBy().getUsername()
        );
    }
}
