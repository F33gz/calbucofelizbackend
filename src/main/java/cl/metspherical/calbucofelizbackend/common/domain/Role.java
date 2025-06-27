package cl.metspherical.calbucofelizbackend.common.domain;

import cl.metspherical.calbucofelizbackend.common.enums.RoleName;
import jakarta.persistence.*;
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
    private Byte id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, unique = true)
    private RoleName name;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();
}
