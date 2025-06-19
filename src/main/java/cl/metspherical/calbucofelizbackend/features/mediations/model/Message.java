package cl.metspherical.calbucofelizbackend.features.mediations.model;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "message")
@Getter
@Setter
@ToString(exclude = {"mediation", "sender"})
@EqualsAndHashCode(exclude = {"mediation", "sender"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Message content cannot be blank")
    private String content;

    @Column(name = "sent_at", nullable = false, columnDefinition = "timestamp with time zone")
    @CreationTimestamp
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mediation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_mediation"))
    private Mediation mediation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_sender"))
    private User sender;
}
