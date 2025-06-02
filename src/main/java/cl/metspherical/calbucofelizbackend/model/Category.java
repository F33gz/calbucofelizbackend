package cl.metspherical.calbucofelizbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    private Integer id;    @Column(length = 20, nullable = false)

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 20, message = "Category name cannot exceed 20 characters")
    private String name;@ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)

    @Builder.Default
    private Set<Post> posts = new HashSet<>();
}
