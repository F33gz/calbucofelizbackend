package cl.metspherical.calbucofelizbackend.features.mediations.service;

import cl.metspherical.calbucofelizbackend.common.enums.RoleName;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service responsible for handling mediation participant operations
 * including permissions, moderation, and participant management
 */
@Service
@RequiredArgsConstructor
public class MediationParticipantService {

    private final MediationParticipantRepository mediationParticipantRepository;
    private final MediationRepository mediationRepository;
    private final UserRepository userRepository;

    /**
     * Creates participants for public mediation (all users can participate)
     *
     * @param mediation The mediation entity
     * @return Set of MediationParticipant entities
     */
    @Transactional
    public Set<MediationParticipant> createPublicMediationParticipants(Mediation mediation) {
        List<User> allUsers = userRepository.findAll();
        Set<MediationParticipant> participants = new HashSet<>();

        for (User user : allUsers) {
            MediationParticipant participant = MediationParticipant.builder()
                    .user(user)
                    .mediation(mediation)
                    .canTalk(true) // All participants can talk by default
                    .isModerator(false) // Regular participants (not moderators)
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
    @Transactional
    public Set<MediationParticipant> createPrivateMediationParticipants(Mediation mediation, List<String> participantUsernames) {
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
                    .isModerator(false) // Regular participants (not moderators)
                    .build();
            participants.add(participant);
        }

        return participants;
    }

    /**
     * Automatically assigns the best available moderator to a mediation
     * For public mediations: selects from existing participants
     * For private mediations: selects from users not in the conflict
     *
     * @param mediationId ID of the mediation
     */
    @Transactional
    public void assignAutomaticModerator(UUID mediationId) {
        // Check if mediation already has a moderator
        List<MediationParticipant> existingModerators = mediationParticipantRepository
                .findByMediationIdAndIsModeratorTrueWithUser(mediationId);

        if (!existingModerators.isEmpty()) {
            return; // Already has moderator
        }

        // Get mediation to check if it's public or private
        Mediation mediation = mediationRepository.findById(mediationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mediation not found"));

        User selectedModerator;

        if (Boolean.FALSE.equals(mediation.getMediationType())) {
            // Public mediation - select moderator from existing participants
            selectedModerator = selectModeratorFromParticipants(mediationId);
        } else {
            // Private mediation - select moderator from non-participants
            selectedModerator = selectModeratorFromNonParticipants(mediationId);
        }
          if (selectedModerator == null) {
            return; // No suitable moderator found
        }

        // Add or update as moderator
        assignUserAsModerator(selectedModerator, mediation);
    }

    /**
     * Selects moderator from existing participants (for public mediations)
     */
    private User selectModeratorFromParticipants(UUID mediationId) {
        List<MediationParticipant> participants = mediationParticipantRepository.findByMediationIdWithUser(mediationId);
        List<RoleName> moderationRoles = RoleName.getModerationRoles();

        // Optimización: usar comparador directo en lugar de min() con lógica compleja
        return participants.stream()
                .map(MediationParticipant::getUser)
                .filter(user -> user.getRoleEntities().stream()
                        .anyMatch(role -> moderationRoles.contains(role.getName())))
                .max((u1, u2) -> Integer.compare(
                        getHighestRolePriority(u1, moderationRoles),
                        getHighestRolePriority(u2, moderationRoles)
                ))
                .orElse(null);
    }

    /**
     * Selects moderator from non-participants (for private mediations)
     */
    private User selectModeratorFromNonParticipants(UUID mediationId) {
        List<RoleName> moderationRoles = RoleName.getModerationRoles();
        List<User> availableModerators = userRepository.findAvailableModerators(mediationId, moderationRoles);
        
        // Ordenamiento usando hierarchy del enum en lugar de hardcoding en query
        return availableModerators.stream()
                .max((user1, user2) -> {
                    int maxHierarchy1 = user1.getRoleEntities().stream()
                            .filter(role -> role.getName().canModerate())
                            .mapToInt(role -> role.getName().getHierarchy())
                            .max()
                            .orElse(0);
                    
                    int maxHierarchy2 = user2.getRoleEntities().stream()
                            .filter(role -> role.getName().canModerate())
                            .mapToInt(role -> role.getName().getHierarchy())
                            .max()
                            .orElse(0);
                    
                    return Integer.compare(maxHierarchy1, maxHierarchy2);
                })
                .orElse(null);
    }

    /**
     * Gets the highest role priority for a user
     */
    private int getHighestRolePriority(User user, List<RoleName> moderationRoles) {
        return user.getRoleEntities().stream()
                .filter(role -> moderationRoles.contains(role.getName()))
                .mapToInt(role -> role.getName().getHierarchy()) // Usar hierarchy del enum
                .max()
                .orElse(0);
    }

    /**
     * Assigns a user as moderator for a mediation
     */
    private void assignUserAsModerator(User selectedModerator, Mediation mediation) {
        // Check if user is already a participant
        Optional<MediationParticipant> existingParticipant = mediationParticipantRepository
                .findByMediationIdAndUserId(mediation.getId(), selectedModerator.getId());

        if (existingParticipant.isPresent()) {
            // Update existing participant to moderator
            MediationParticipant participant = existingParticipant.get();
            participant.setIsModerator(true);
            mediationParticipantRepository.save(participant);
        } else {
            // Add as new moderator participant
            MediationParticipant moderatorParticipant = MediationParticipant.builder()
                    .user(selectedModerator)
                    .mediation(mediation)
                    .canTalk(true)
                    .isModerator(true)
                    .build();
            mediationParticipantRepository.save(moderatorParticipant);
        }
    }

    /**
     * Gets all participants for a mediation (for moderator view)
     *
     * @param mediationId ID of the mediation
     * @return List of all mediation participants
     */
    @Transactional(readOnly = true)
    public List<MediationParticipant> getAllParticipants(UUID mediationId) {
        return mediationParticipantRepository.findByMediationIdWithUser(mediationId);
    }

    /**
     * Checks if a user can join a mediation room
     *
     * @param userId ID of the user
     * @param mediationId ID of the mediation
     * @return true if user can join, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canUserJoinRoom(UUID userId, UUID mediationId) {
        return mediationParticipantRepository.findByUserIdWithMediation(userId)
                .stream()
                .anyMatch(participant -> participant.getMediation().getId().equals(mediationId));
    }

    /**
     * Checks if a user can talk in a mediation (not muted)
     *
     * @param userId ID of the user
     * @param mediationId ID of the mediation
     * @return true if user can talk, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canUserTalk(UUID userId, UUID mediationId) {
        return mediationParticipantRepository.findByUserIdAndMediationIdWithUser(userId, mediationId)
                .map(MediationParticipant::getCanTalk)
                .orElse(false);
    }

    /**
     * Checks if user can moderate this specific mediation
     */
    @Transactional(readOnly = true)
    public boolean canUserModerate(UUID userId, UUID mediationId) {
        return mediationParticipantRepository.findByUserIdAndMediationIdWithUser(userId, mediationId)
                .filter(participant -> Boolean.TRUE.equals(participant.getIsModerator()))
                .map(participant -> hasModeratorRole(participant.getUser()))
                .orElse(false);
    }

    /**
     * Updates mute permissions - only moderators can do this
     */
    @Transactional
    public boolean muteUser(UUID targetUserId, UUID mediationId, UUID requesterId) {
        // Inline moderator verification and mute operation in a single query
        var requesterParticipant = mediationParticipantRepository.findByUserIdAndMediationIdWithUser(requesterId, mediationId);
        if (requesterParticipant.isEmpty() ||
            !Boolean.TRUE.equals(requesterParticipant.get().getIsModerator()) ||
            !hasModeratorRole(requesterParticipant.get().getUser())) {
            return false;
        }

        return mediationParticipantRepository.findByUserIdAndMediationIdWithUser(targetUserId, mediationId)
                .filter(participant -> !Boolean.TRUE.equals(participant.getIsModerator()) &&
                                     Boolean.TRUE.equals(participant.getCanTalk()))
                .map(participant -> {
                    participant.setCanTalk(false);
                    mediationParticipantRepository.save(participant);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Updates unmute permissions - only moderators can do this
     */
    @Transactional
    public boolean unmuteUser(UUID targetUserId, UUID mediationId, UUID requesterId) {
        // Verificación inline del moderador y operación de unmute optimizada
        var requesterParticipant = mediationParticipantRepository.findByUserIdAndMediationIdWithUser(requesterId, mediationId);
        if (requesterParticipant.isEmpty() || 
            !Boolean.TRUE.equals(requesterParticipant.get().getIsModerator()) || 
            !hasModeratorRole(requesterParticipant.get().getUser())) {
            return false;
        }

        return mediationParticipantRepository.findByUserIdAndMediationIdWithUser(targetUserId, mediationId)
                .filter(participant -> !Boolean.TRUE.equals(participant.getCanTalk()))
                .map(participant -> {
                    participant.setCanTalk(true);
                    mediationParticipantRepository.save(participant);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Checks if user has a role that can moderate using enum validation
     */
    private boolean hasModeratorRole(User user) {
        return user.getRoleEntities().stream()
                .anyMatch(role -> role.getName().canModerate());
    }
}
