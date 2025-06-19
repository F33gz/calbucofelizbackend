package cl.metspherical.calbucofelizbackend.features.posts.repository;

import cl.metspherical.calbucofelizbackend.features.posts.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, UUID> {
}

