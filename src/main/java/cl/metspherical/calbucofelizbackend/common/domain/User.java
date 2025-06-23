package cl.metspherical.calbucofelizbackend.common.domain;

import cl.metspherical.calbucofelizbackend.features.events.model.Event;
import cl.metspherical.calbucofelizbackend.features.events.model.EventAssistant;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Mediation;
import cl.metspherical.calbucofelizbackend.features.mediations.model.MediationParticipant;
import cl.metspherical.calbucofelizbackend.features.mediations.model.Message;
import cl.metspherical.calbucofelizbackend.features.posts.model.Comment;
import cl.metspherical.calbucofelizbackend.features.posts.model.Post;
import cl.metspherical.calbucofelizbackend.features.posts.model.PostLike;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_rut", columnNames = "rut"),
        @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_user_number", columnNames = "number")
    }
)
@Getter
@Setter
@ToString(exclude = {"posts", "postLikes", "comments", "createdEvents", "eventAssistants", "mediationParticipants", "createdMediations", "sentMessages"})
@EqualsAndHashCode(exclude = {"posts", "postLikes", "comments", "createdEvents", "eventAssistants", "mediationParticipants", "createdMediations", "sentMessages"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String rut;

    @Column(length = 100, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private Integer number;

    @Column(length = 30)
    private String username;

    @Lob
    private byte[] avatar;

    @Column(length = 50, nullable = false)
    private String names;

    private String lastNames;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String address;

    @Column
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_has_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<PostLike> postLikes = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Event> createdEvents = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<EventAssistant> eventAssistants = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<MediationParticipant> mediationParticipants = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Mediation> createdMediations = new HashSet<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Message> sentMessages = new HashSet<>();

    public String getAvatar() {
        return avatar != null ? Base64.getEncoder().encodeToString(avatar) : null;
    }
    
    public void setAvatar(String base64Avatar) {
        this.avatar = base64Avatar != null ? Base64.getDecoder().decode(base64Avatar) : null;
    }
    public List<String> getRoles() {
        return roles.stream()
            .map(Role::getName)
            .toList();
    }
}