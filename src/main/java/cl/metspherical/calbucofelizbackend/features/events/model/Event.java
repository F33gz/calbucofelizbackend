package cl.metspherical.calbucofelizbackend.features.events.model;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "event")
@Getter
@Setter
@ToString(exclude = {"createdBy", "eventAssistants"})
@EqualsAndHashCode(exclude = {"createdBy", "eventAssistants"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;    @Column(length = 50, nullable = false)

    @NotBlank(message = "Event title cannot be blank")
    @Size(max = 50, message = "Event title cannot exceed 50 characters")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String desc;

    @Column(length = 200, nullable = false)
    @NotBlank(message = "Event address cannot be blank")
    @Size(max = 200, message = "Event address cannot exceed 200 characters")
    private String address;

    @Column(name = "init_time", columnDefinition = "timestamp with time zone")
    private LocalDateTime init;

    @Column(name = "ending_time", columnDefinition = "timestamp with time zone")
    private LocalDateTime ending;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "fk_event_created_by"))
    private User createdBy;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EventAssistant> eventAssistants = new HashSet<>();
}
