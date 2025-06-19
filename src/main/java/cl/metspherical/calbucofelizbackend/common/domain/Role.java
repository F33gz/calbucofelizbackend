package cl.metspherical.calbucofelizbackend.common.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "role",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_role_name",
                columnNames = "name"))
@Getter
@Setter
@ToString(exclude = "users")
@EqualsAndHashCode(exclude = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @Column(columnDefinition = "SMALLINT")
    private Byte id;    @Column(length = 10, nullable = false, unique = true)

    @NotBlank(message = "Role name cannot be blank")
    @Size(max = 10, message = "Role name cannot exceed 10 characters")
    private String name;@ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)

    @Builder.Default
    private Set<User> users = new HashSet<>();
}
