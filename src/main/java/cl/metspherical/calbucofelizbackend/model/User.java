package cl.metspherical.calbucofelizbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = {"posts", "roles"})
@EqualsAndHashCode(exclude = {"posts", "roles"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(unique = true, nullable = false)
    @NonNull
    private String rut;

    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    @NonNull
    private String number;

    @Column(unique = true)
    private String username;

    @Column
    private String avatar;

    @Column(nullable = false)
    @NonNull
    private String names;

    @Column(name = "last_names")
    private String lastNames;

    @Column(nullable = false)
    @NonNull
    private String password;

    @Column(nullable = false)
    @NonNull
    private String address;

    @Column
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Post> posts = new HashSet<>();
}