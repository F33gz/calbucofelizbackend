package cl.metspherical.calbucofelizbackend.features.emergency.repository;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.features.emergency.model.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, UUID> {
    
    List<Emergency> findByFinishedAtAfter(LocalDateTime dateTime);
    Optional<Emergency> findTopByAuthorOrderByCreatedAtDesc(User author);
}
