package cl.metspherical.calbucofelizbackend.features.events.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    private Byte id;    @Column(length = 20, nullable = false, unique = true)

    @NotBlank(message = "Assistance name cannot be blank")
    @Size(max = 20, message = "Assistance name cannot exceed 20 characters")
    private String name;

    @OneToMany(mappedBy = "assistance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EventAssistant> eventAssistants = new HashSet<>();
}
