package cl.metspherical.calbucofelizbackend.features.mediations.service;

import cl.metspherical.calbucofelizbackend.features.mediations.dto.CloseMediationDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.dto.CreateMediationRequestDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.dto.MediationOverviewDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.dto.MediationsResponseDTO;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Mediation;
import cl.metspherical.calbucofelizbackend.features.mediations.model.MediationParticipant;
import cl.metspherical.calbucofelizbackend.features.mediations.websocket.MediationWebSocketHandler;
import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.features.mediations.repository.MediationRepository;
import cl.metspherical.calbucofelizbackend.features.mediations.repository.MediationParticipantRepository;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
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
public class MediationService {

    private final MediationRepository mediationRepository;
    private final MediationParticipantRepository mediationParticipantRepository;
    private final UserRepository userRepository;
    private final MediationParticipantService participantService;
    private final MediationWebSocketHandler webSocketHandler;

    public MediationService(MediationRepository mediationRepository,
                          MediationParticipantRepository mediationParticipantRepository,
                          UserRepository userRepository,
                          MediationParticipantService participantService,
                          @Lazy MediationWebSocketHandler webSocketHandler) {
        this.mediationRepository = mediationRepository;
        this.mediationParticipantRepository = mediationParticipantRepository;
        this.userRepository = userRepository;
        this.participantService = participantService;
        this.webSocketHandler = webSocketHandler;
    }

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
                .reason(null)
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
     * Closes an existing mediation in the system
     *
     * @param userId UUID of the user attempting to close the mediation
     * @param mediationId UUID of the mediation to be closed
     * @param closeMediationDTO DTO containing the reason for closing the mediation
     * @return CloseMediationDTO containing the updated mediation details
     */
    @Transactional
    public CloseMediationDTO closeMediation(UUID userId, UUID mediationId, CloseMediationDTO closeMediationDTO) {
        // Verify the mediation exists
        Mediation mediation = mediationRepository.findById(mediationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mediation not found with id: " + mediationId));

        // Check if the user is a participant and a moderator
        MediationParticipant participant = mediationParticipantRepository.findByMediationIdAndUserId(mediationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a participant of this mediation"));

        if (!Boolean.TRUE.equals(participant.getIsModerator())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a moderator of this mediation");
        }

        // Update mediation fields
        mediation.setIsSolved(true);
        mediation.setReason(closeMediationDTO.reason());

        // Save the updated mediation
        mediationRepository.save(mediation);

        // Broadcast mediation closure to all participants
        webSocketHandler.broadcastMediationClosed(mediationId, participant.getUser().getUsername(), closeMediationDTO.reason());

        return closeMediationDTO;
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

    /**
     * Gets a mediation by its ID
     *
     * @param mediationId UUID of the mediation to retrieve
     * @return Mediation entity
     * @throws ResponseStatusException if mediation is not found
     */
    @Transactional(readOnly = true)
    public Mediation getMediationById(UUID mediationId) {
        return mediationRepository.findById(mediationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mediation not found with id: " + mediationId));
    }
}
