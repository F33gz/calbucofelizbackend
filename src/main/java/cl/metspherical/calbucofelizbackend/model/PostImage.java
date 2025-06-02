package cl.metspherical.calbucofelizbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "post_image")
@Getter
@Setter
@ToString(exclude = "post")
@EqualsAndHashCode(exclude = "post")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Lob
    @Column(name = "img", nullable = false)
    private byte[] img;

    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "post_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_image_post"))
    private Post post;
}
