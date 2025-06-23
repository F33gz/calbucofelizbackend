package cl.metspherical.calbucofelizbackend.features.mediations.repository;

import cl.metspherical.calbucofelizbackend.features.mediations.model.Mediation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediationRepository extends JpaRepository<Mediation, UUID> {
    
    @Query("SELECT m FROM Mediation m " +
           "JOIN FETCH m.createdBy " +
           "WHERE m.id = :id")
    Optional<Mediation> findByIdWithDetails(@Param("id") UUID id);
    
    @Query("SELECT m FROM Mediation m " +
           "JOIN FETCH m.createdBy " +
           "JOIN FETCH m.participants " +
           "WHERE m.createdBy.id = :userId " +
           "ORDER BY m.createdAt DESC")
    List<Mediation> findByCreatedByIdWithDetails(@Param("userId") UUID userId);
}
