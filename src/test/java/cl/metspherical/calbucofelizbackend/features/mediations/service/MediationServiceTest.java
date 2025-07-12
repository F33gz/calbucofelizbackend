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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediationServiceTest {

    @Mock
    private MediationRepository mediationRepository;

    @Mock
    private MediationParticipantRepository mediationParticipantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediationParticipantService participantService;

    @Mock
    private MediationWebSocketHandler webSocketHandler;

    @InjectMocks
    private MediationService mediationService;

    private User testUser;
    private User testCreator;
    private Mediation testMediation;
    private MediationParticipant testParticipant;
    private UUID testUserId;
    private UUID testCreatorId;
    private UUID testMediationId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCreatorId = UUID.randomUUID();
        testMediationId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .avatar("avatar.jpg")
                .build();

        testCreator = User.builder()
                .id(testCreatorId)
                .username("creator")
                .avatar("creator-avatar.jpg")
                .build();

        testMediation = Mediation.builder()
                .id(testMediationId)
                .title("Test Mediation")
                .mediationType(false) // public
                .isSolved(false)
                .createdBy(testCreator)
                .participants(new HashSet<>())
                .messages(new HashSet<>())
                .reason(null)
                .build();

        testParticipant = MediationParticipant.builder()
                .user(testUser)
                .mediation(testMediation)
                .canTalk(true)
                .isModerator(true)
                .build();
    }

    @Test
    void shouldCreatePublicMediationSuccessfully() {
        // Given
        CreateMediationRequestDTO request = new CreateMediationRequestDTO(
                "Test Public Mediation",
                false, // public
                Set.of()
        );

        Set<MediationParticipant> participants = Set.of(testParticipant);

        when(userRepository.findById(testCreatorId)).thenReturn(Optional.of(testCreator));
        when(mediationRepository.save(any(Mediation.class))).thenAnswer(invocation -> {
            Mediation mediation = invocation.getArgument(0);
            mediation.setId(testMediationId);
            return mediation;
        });
        when(participantService.createPublicMediationParticipants(any(Mediation.class))).thenReturn(participants);
        when(mediationParticipantRepository.saveAll(any())).thenReturn(participants);
        when(mediationRepository.save(any(Mediation.class))).thenReturn(testMediation);

        // When
        UUID result = mediationService.createMediation(request, testCreatorId);

        // Then
        assertThat(result).isEqualTo(testMediationId);
        verify(userRepository).findById(testCreatorId);
        verify(mediationRepository, times(2)).save(any(Mediation.class));
        verify(participantService).createPublicMediationParticipants(any(Mediation.class));
        verify(participantService).assignAutomaticModerator(testMediationId);
        verify(mediationParticipantRepository).saveAll(participants);
    }

    @Test
    void shouldCloseMediationSuccessfully() {
        // Given
        CloseMediationDTO closeRequest = new CloseMediationDTO("Resolved peacefully");
        
        when(mediationRepository.findById(testMediationId)).thenReturn(Optional.of(testMediation));
        when(mediationParticipantRepository.findByMediationIdAndUserId(testMediationId, testUserId))
                .thenReturn(Optional.of(testParticipant));
        when(mediationRepository.save(any(Mediation.class))).thenReturn(testMediation);

        // When
        CloseMediationDTO result = mediationService.closeMediation(testUserId, testMediationId, closeRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.reason()).isEqualTo("Resolved peacefully");
        verify(mediationRepository).save(any(Mediation.class));
        verify(webSocketHandler).broadcastMediationClosed(testMediationId, testUser.getUsername(), "Resolved peacefully");
    }

    @Test
    void shouldGetAllMediationsByUserSuccessfully() {
        // Given
        List<MediationParticipant> participations = List.of(testParticipant);
        
        when(mediationParticipantRepository.findByUserIdWithMediation(testUserId)).thenReturn(participations);

        // When
        MediationsResponseDTO result = mediationService.getAllMediationsByUser(testUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.mediations()).hasSize(1);
        assertThat(result.mediations().get(0).id()).isEqualTo(testMediationId);
        assertThat(result.mediations().get(0).title()).isEqualTo("Test Mediation");
        assertThat(result.mediations().get(0).type()).isEqualTo("public");
        assertThat(result.mediations().get(0).createdBy()).isEqualTo("creator");
        assertThat(result.mediations().get(0).isSolved()).isFalse();
    }

    @Test
    void shouldGetMediationByIdSuccessfully() {
        // Given
        when(mediationRepository.findById(testMediationId)).thenReturn(Optional.of(testMediation));

        // When
        Mediation result = mediationService.getMediationById(testMediationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testMediationId);
        assertThat(result.getTitle()).isEqualTo("Test Mediation");
        assertThat(result.getCreatedBy()).isEqualTo(testCreator);
    }

    @Test
    void shouldThrowExceptionWhenMediationNotFound() {
        // Given
        when(mediationRepository.findById(testMediationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> mediationService.getMediationById(testMediationId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Mediation not found with id: " + testMediationId);
    }
}