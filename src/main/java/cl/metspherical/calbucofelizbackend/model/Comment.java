package cl.metspherical.calbucofelizbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comment")
@Getter
@Setter
@ToString(exclude = {"post", "user"})
@EqualsAndHashCode(exclude = {"post", "user"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;@Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Comment content cannot be blank")
    private String content;

    @Column(
            name = "created_at",
            nullable = false,
            columnDefinition = "timestamp with time zone")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "post_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_post"))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_user"))
    private User user;
}
