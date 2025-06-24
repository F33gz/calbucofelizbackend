package cl.metspherical.calbucofelizbackend.features.emergency.repository;

import cl.metspherical.calbucofelizbackend.features.emergency.model.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, UUID> {
    
    /**
     * Find all emergencies where finishedAt is after the given date
     * This returns only active emergencies (not finished yet)
     * 
     * @param dateTime the current date time
     * @return List of active emergencies
     */
    List<Emergency> findByFinishedAtAfter(LocalDateTime dateTime);
}
