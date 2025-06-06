package cl.metspherical.calbucofelizbackend.repository;

import cl.metspherical.calbucofelizbackend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.categories " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") UUID id);
}
