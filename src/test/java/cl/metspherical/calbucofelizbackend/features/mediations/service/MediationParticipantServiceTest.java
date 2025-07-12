package cl.metspherical.calbucofelizbackend.features.mediations.service;

import cl.metspherical.calbucofelizbackend.common.enums.RoleName;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Mediation;
import cl.metspherical.calbucofelizbackend.features.mediations.model.MediationParticipant;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediationParticipantServiceTest {

    @Mock
    private MediationParticipantRepository mediationParticipantRepository;

    @Mock
    private MediationRepository mediationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MediationParticipantService mediationParticipantService;

    private User testUser1;
    private User testUser2;
    private User testModerator;
    private Mediation testMediation;
    private MediationParticipant testParticipant;
    private UUID testUserId1;
    private UUID testUserId2;
    private UUID testModeratorId;
    private UUID testMediationId;

    @BeforeEach
    void setUp() {
        testUserId1 = UUID.randomUUID();
        testUserId2 = UUID.randomUUID();
        testModeratorId = UUID.randomUUID();
        testMediationId = UUID.randomUUID();

        testUser1 = User.builder()
                .id(testUserId1)
                .username("user1")
                .build();

        testUser2 = User.builder()
                .id(testUserId2)
                .username("user2")
                .build();

        testModerator = User.builder()
                .id(testModeratorId)
                .username("moderator")
                .build();

        testMediation = Mediation.builder()
                .id(testMediationId)
                .title("Test Mediation")
                .mediationType(false) // public
                .isSolved(false)
                .createdBy(testUser1)
                .participants(new HashSet<>())
                .messages(new HashSet<>())
                .build();

        testParticipant = MediationParticipant.builder()
                .user(testUser1)
                .mediation(testMediation)
                .canTalk(true)
                .isModerator(false)
                .build();
    }

    @Test
    void shouldCreatePublicMediationParticipantsSuccessfully() {
        // Given
        List<User> allUsers = List.of(testUser1, testUser2, testModerator);
        when(userRepository.findAll()).thenReturn(allUsers);

        // When
        Set<MediationParticipant> result = mediationParticipantService.createPublicMediationParticipants(testMediation);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(participant -> 
                participant.getMediation().equals(testMediation) &&
                participant.getCanTalk() &&
                !participant.getIsModerator());
        verify(userRepository).findAll();
    }

    @Test
    void shouldCreatePrivateMediationParticipantsSuccessfully() {
        // Given
        List<String> usernames = List.of("user1", "user2");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser1));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(testUser2));

        // When
        Set<MediationParticipant> result = mediationParticipantService.createPrivateMediationParticipants(testMediation, usernames);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(participant -> 
                participant.getMediation().equals(testMediation) &&
                participant.getCanTalk() &&
                !participant.getIsModerator());
        verify(userRepository).findByUsername("user1");
        verify(userRepository).findByUsername("user2");
    }

    @Test
    void shouldHandleEmptyUsernamesForPrivateMediation() {
        // Given
        List<String> usernames = new ArrayList<>();

        // When
        Set<MediationParticipant> result = mediationParticipantService.createPrivateMediationParticipants(testMediation, usernames);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForPrivateMediation() {
        // Given
        List<String> usernames = List.of("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> mediationParticipantService.createPrivateMediationParticipants(testMediation, usernames))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found with username: nonexistent");
    }

    @Test
    void shouldGetAllParticipantsSuccessfully() {
        // Given
        List<MediationParticipant> participants = List.of(testParticipant);
        when(mediationParticipantRepository.findByMediationIdWithUser(testMediationId))
                .thenReturn(participants);

        // When
        List<MediationParticipant> result = mediationParticipantService.getAllParticipants(testMediationId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testParticipant);
        verify(mediationParticipantRepository).findByMediationIdWithUser(testMediationId);
    }

    @Test
    void shouldCheckCanUserJoinRoomSuccessfully() {
        // Given
        when(mediationParticipantRepository.findByMediationIdAndUserId(testMediationId, testUserId1))
                .thenReturn(Optional.of(testParticipant));

        // When
        boolean result = mediationParticipantService.canUserJoinRoom(testUserId1, testMediationId);

        // Then
        assertThat(result).isTrue();
        verify(mediationParticipantRepository).findByMediationIdAndUserId(testMediationId, testUserId1);
    }

    @Test
    void shouldReturnFalseWhenUserCannotJoinRoom() {
        // Given
        when(mediationParticipantRepository.findByMediationIdAndUserId(testMediationId, testUserId1))
                .thenReturn(Optional.empty());

        // When
        boolean result = mediationParticipantService.canUserJoinRoom(testUserId1, testMediationId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCheckCanUserTalkSuccessfully() {
        // Given
        when(mediationParticipantRepository.findByMediationIdAndUserId(testMediationId, testUserId1))
                .thenReturn(Optional.of(testParticipant));

        // When
        boolean result = mediationParticipantService.canUserTalk(testUserId1, testMediationId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserCannotTalk() {
        // Given
        MediationParticipant mutedParticipant = MediationParticipant.builder()
                .user(testUser1)
                .mediation(testMediation)
                .canTalk(false)
                .isModerator(false)
                .build();
        when(mediationParticipantRepository.findByMediationIdAndUserId(testMediationId, testUserId1))
                .thenReturn(Optional.of(mutedParticipant));

        // When
        boolean result = mediationParticipantService.canUserTalk(testUserId1, testMediationId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCheckCanUserModerateSuccessfully() {
        // Given
        MediationParticipant moderatorParticipant = MediationParticipant.builder()
                .user(testUser1)
                .mediation(testMediation)
                .canTalk(true)
                .isModerator(true)
                .build();
        when(mediationParticipantRepository.findByMediationIdAndUserId(testMediationId, testUserId1))
                .thenReturn(Optional.of(moderatorParticipant));

        // When
        boolean result = mediationParticipantService.canUserModerate(testUserId1, testMediationId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserCannotModerate() {
        // Given
        when(mediationParticipantRepository.findByMediationIdAndUserId(testMediationId, testUserId1))
                .thenReturn(Optional.of(testParticipant));

        // When
        boolean result = mediationParticipantService.canUserModerate(testUserId1, testMediationId);

        // Then
        assertThat(result).isFalse();
    }
}