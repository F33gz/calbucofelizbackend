package cl.metspherical.calbucofelizbackend.features.posts.repository;

import cl.metspherical.calbucofelizbackend.features.posts.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLike.PostLikeId> {
    
    /**
     * Finds a like by post ID and user ID
     * 
     * @param postId ID of the post
     * @param userId ID of the user
     * @return Optional containing the PostLike if found
     */
    Optional<PostLike> findByPost_IdAndUser_Id(UUID postId, UUID userId);
    
    /**
     * Checks if a user has liked a specific post
     * 
     * @param postId ID of the post
     * @param userId ID of the user
     * @return true if the user has liked the post, false otherwise
     */
    boolean existsByPost_IdAndUser_Id(UUID postId, UUID userId);
    
    /**
     * Deletes a like by post ID and user ID
     * 
     * @param postId ID of the post
     * @param userId ID of the user
     */
    void deleteByPost_IdAndUser_Id(UUID postId, UUID userId);
}
