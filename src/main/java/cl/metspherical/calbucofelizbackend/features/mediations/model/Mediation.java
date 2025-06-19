package cl.metspherical.calbucofelizbackend.features.mediations.model;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "mediation")
@Getter
@Setter
@ToString(exclude = {"createdBy", "participants", "messages"})
@EqualsAndHashCode(exclude = {"createdBy", "participants", "messages"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mediation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mediation_type", nullable = false)
    private Boolean mediationType;    @Column(length = 100, nullable = false)
    @NotBlank(message = "Mediation title cannot be blank")
    @Size(max = 100, message = "Mediation title cannot exceed 100 characters")
    private String title;

    @Column(name = "is_solved", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean isSolved = false;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp with time zone")
    @CreationTimestamp
    private LocalDateTime createdAt;    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "fk_mediation_created_by"))
    private User createdBy;

    @OneToMany(mappedBy = "mediation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<MediationParticipant> participants = new HashSet<>();

    @OneToMany(mappedBy = "mediation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Message> messages = new HashSet<>();
}
