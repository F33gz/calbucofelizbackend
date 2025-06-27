package cl.metspherical.calbucofelizbackend.features.mediations.model;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "mediation_participant")
@IdClass(MediationParticipant.MediationParticipantId.class)
@Getter
@Setter
@ToString(exclude = {"user", "mediation"})
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediationParticipant {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mediation_participant_user"))
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mediation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mediation_participant_mediation"))
    private Mediation mediation;

    @Column(name = "can_talk", nullable = false)
    private Boolean canTalk;
    
    @Column(name = "is_moderator", nullable = false)
    @Builder.Default
    private Boolean isModerator = false;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediationParticipantId implements Serializable {
        private UUID user;
        private UUID mediation;
    }
}
