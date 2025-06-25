package cl.metspherical.calbucofelizbackend.features.emergency.service;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.features.emergency.dto.EmergencyDTO;
import cl.metspherical.calbucofelizbackend.features.emergency.model.Emergency;
import cl.metspherical.calbucofelizbackend.features.emergency.repository.EmergencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyRepository emergencyRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new emergency
     * 
     * @param content Emergency content
     * @param authorId ID of the user creating the emergency
     * @return EmergencyDTO of the created emergency
     */
    @Transactional
    public EmergencyDTO createEmergency(String content, UUID authorId) {
        // Validate user exists
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + authorId));
        
        // Create emergency entity
        Emergency emergency = Emergency.builder()
                .content(content)
                .author(author)
                .build();
        
        // Save emergency
        Emergency savedEmergency = emergencyRepository.save(emergency);
        
        // Convert to DTO and return
        return convertToEmergencyDTO(savedEmergency);
    }

    /**
     * Gets all active emergencies (not finished yet)
     * Only returns emergencies where finishedAt is still in the future
     *
     * @return List of active EmergencyDTO
     */
    @Transactional(readOnly = true)
    public List<EmergencyDTO> getEmergencies() {
        LocalDateTime now = LocalDateTime.now();

        // Get all emergencies where finishedAt is after current time
        List<Emergency> activeEmergencies = emergencyRepository.findByFinishedAtAfter(now);

        return activeEmergencies.stream()
                .map(this::convertToEmergencyDTO)
                .toList();
    }

    /**
     * Converts Emergency entity to EmergencyDTO
     * 
     * @param emergency Emergency entity
     * @return EmergencyDTO
     */
    private EmergencyDTO convertToEmergencyDTO(Emergency emergency) {
        return new EmergencyDTO(
                emergency.getId(),
                emergency.getAuthor().getUsername(),
                emergency.getContent(),
                emergency.getCreatedAt()
        );
    }
}
