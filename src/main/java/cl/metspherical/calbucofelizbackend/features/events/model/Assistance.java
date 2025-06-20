package cl.metspherical.calbucofelizbackend.features.events.model;

import cl.metspherical.calbucofelizbackend.features.events.enums.AssistanceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "assistance",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_assistance_name",
                columnNames = "name"))
@Getter
@Setter
@ToString(exclude = "eventAssistants")
@EqualsAndHashCode(exclude = "eventAssistants")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assistance {

    @Id
    @Column(columnDefinition = "SMALLINT")
    private Byte id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    @NotNull(message = "Assistance type cannot be null")
    private AssistanceType name;

    @OneToMany(mappedBy = "assistance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EventAssistant> eventAssistants = new HashSet<>();
}
