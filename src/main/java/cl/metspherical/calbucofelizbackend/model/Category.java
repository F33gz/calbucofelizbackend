package cl.metspherical.calbucofelizbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "category",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_category_name", 
                columnNames = "name"))
@Getter
@Setter
@ToString(exclude = "posts")
@EqualsAndHashCode(exclude = "posts")
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(length = 20, nullable = false)
    @NonNull
    private String name;

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<Post> posts = new HashSet<>();
}
