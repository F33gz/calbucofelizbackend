package cl.metspherical.calbucofelizbackend.features.posts.repository;

import cl.metspherical.calbucofelizbackend.features.posts.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT COUNT(l) FROM PostLike l WHERE l.post.id = :postId")
    long countLikesByPostId(@Param("postId") UUID postId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countCommentsByPostId(@Param("postId") UUID postId);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.categories " +
            "LEFT JOIN FETCH p.images " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findAllPostsWithDetails(Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.categories c " +
            "LEFT JOIN FETCH p.images " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :category, '%')) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsByCategoryWithDetails(@Param("category") String category, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author a " +
            "LEFT JOIN FETCH p.categories " +
            "LEFT JOIN FETCH p.images " +
            "WHERE LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%')) " +
            "OR LOWER(a.names) LIKE LOWER(CONCAT('%', :username, '%')) " +
            "OR LOWER(a.lastNames) LIKE LOWER(CONCAT('%', :username, '%')) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsByUsernameWithDetails(@Param("username") String username, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author a " +
            "LEFT JOIN FETCH p.categories c " +
            "LEFT JOIN FETCH p.images " +
            "WHERE (LOWER(c.name) LIKE LOWER(CONCAT('%', :category, '%'))) " +
            "AND (LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%')) " +
            "OR LOWER(a.names) LIKE LOWER(CONCAT('%', :username, '%')) " +
            "OR LOWER(a.lastNames) LIKE LOWER(CONCAT('%', :username, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsByCategoryAndUsernameWithDetails(
            @Param("category") String category,
            @Param("username") String username,
            Pageable pageable);
}
