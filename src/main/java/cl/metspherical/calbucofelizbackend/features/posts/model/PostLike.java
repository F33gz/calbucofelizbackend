package cl.metspherical.calbucofelizbackend.features.posts.model;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "post_like")
@IdClass(PostLike.PostLikeId.class)
@Getter
@Setter
@ToString(exclude = {"post", "user"})
@EqualsAndHashCode(exclude = {"post", "user"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLike {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "post_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_like_post"))
    private Post post;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_like_user"))
    private User user;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostLikeId implements Serializable {
        private UUID post;
        private UUID user;
    }
}
