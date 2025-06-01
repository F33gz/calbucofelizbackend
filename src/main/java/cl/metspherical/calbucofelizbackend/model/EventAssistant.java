package cl.metspherical.calbucofelizbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "event_assistant")
@IdClass(EventAssistant.EventAssistantId.class)
@Getter
@Setter
@ToString(exclude = {"user", "event", "assistance"})
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAssistant {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_event_assistant_user"))
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_event_assistant_event"))
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assistance_id", nullable = false, foreignKey = @ForeignKey(name = "fk_event_assistant_assistance"))
    private Assistance assistance;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventAssistantId implements Serializable {
        private UUID user;
        private Integer event;
    }
}
