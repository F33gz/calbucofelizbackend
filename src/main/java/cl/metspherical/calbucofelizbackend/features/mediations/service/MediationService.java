package cl.metspherical.calbucofelizbackend.features.mediations.service;

import cl.metspherical.calbucofelizbackend.features.mediations.dto.CreateMediationRequestDTO;
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

        return savedMediation.getId();
    }

    /**
     * Creates mediation participants based on mediation type
     * 
     * @param mediation The mediation entity
     * @param request The creation request DTO
     */
    private void createMediationParticipants(Mediation mediation, CreateMediationRequestDTO request) {
        Set<MediationParticipant> participants = new HashSet<>();

        if (Boolean.FALSE.equals(request.type())) {
            // Public mediation - add all users from database
            participants = createPublicMediationParticipants(mediation);
        } else {
            // Private mediation - add only specified participants
            participants = createPrivateMediationParticipants(mediation, request.participants());
        }

        // Save all participants
        mediationParticipantRepository.saveAll(participants);
        
        // Update mediation with participants
        mediation.setParticipants(participants);
        mediationRepository.save(mediation);
    }

    /**
     * Creates participants for public mediation (all users can participate)
     * 
     * @param mediation The mediation entity
     * @return Set of MediationParticipant entities
     */
    private Set<MediationParticipant> createPublicMediationParticipants(Mediation mediation) {
        List<User> allUsers = userRepository.findAll();
        Set<MediationParticipant> participants = new HashSet<>();

        for (User user : allUsers) {
            MediationParticipant participant = MediationParticipant.builder()
                    .user(user)
                    .mediation(mediation)
                    .canTalk(true) // All participants can talk by default
                    .build();
            participants.add(participant);
        }

        return participants;
    }

    /**
     * Creates participants for private mediation (only specified users)
     * 
     * @param mediation The mediation entity
     * @param participantUsernames List of usernames to add as participants
     * @return Set of MediationParticipant entities
     */
    private Set<MediationParticipant> createPrivateMediationParticipants(Mediation mediation, List<String> participantUsernames) {
        Set<MediationParticipant> participants = new HashSet<>();

        if (participantUsernames == null || participantUsernames.isEmpty()) {
            return participants;
        }

        for (String username : participantUsernames) {
            User user = userRepository.findByUsername(username.trim())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, 
                            "User not found with username: " + username
                    ));

            MediationParticipant participant = MediationParticipant.builder()
                    .user(user)
                    .mediation(mediation)
                    .canTalk(true) // All participants can talk by default
                    .build();
            participants.add(participant);
        }

        return participants;
    }
}
