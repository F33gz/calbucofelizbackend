package cl.metspherical.calbucofelizbackend.repository;

import cl.metspherical.calbucofelizbackend.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, UUID> {
}

