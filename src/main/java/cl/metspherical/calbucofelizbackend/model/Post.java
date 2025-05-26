package cl.metspherical.calbucofelizbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Entity
@Table(name = "post")
@Data
@ToString(exclude = {"author", "categories", "images"})
@EqualsAndHashCode(exclude = {"author", "categories", "images"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "text_content", nullable = false)
    @NonNull
    private String content;

    @Column(
            name = "created_at",
            nullable = false,
            columnDefinition = "timestamp with time zone")
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @ManyToOne()
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_user"))
    @NonNull
    private User author;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_has_category",
            joinColumns = @JoinColumn(
                    name = "post_id",
                    foreignKey = @ForeignKey(name = "fk_phc_post")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "category_id",
                    foreignKey = @ForeignKey(name = "fk_phc_category"))
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private Set<PostImage> images = new HashSet<>();

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this);
    }

    public void addCategory(Category category) {
        categories.add(category);
        category.getPosts().add(this);
    }
}
