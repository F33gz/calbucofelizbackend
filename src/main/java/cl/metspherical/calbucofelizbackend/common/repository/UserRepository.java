package cl.metspherical.calbucofelizbackend.common.repository;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByRut(String rut);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.posts " +
           "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.names) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastNames) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findUsersWithPostsAndLikesBySearchTerm(@Param("searchTerm") String searchTerm);

    @Query("SELECT CAST(COUNT(pl) AS INTEGER) FROM PostLike pl WHERE pl.post.author.id = :userId")
    Integer countLikesByUserId(@Param("userId") UUID userId);
}

