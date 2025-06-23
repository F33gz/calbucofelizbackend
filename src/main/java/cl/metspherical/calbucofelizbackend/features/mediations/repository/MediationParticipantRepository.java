package cl.metspherical.calbucofelizbackend.features.mediations.repository;

import cl.metspherical.calbucofelizbackend.features.mediations.model.MediationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediationParticipantRepository extends JpaRepository<MediationParticipant, MediationParticipant.MediationParticipantId> {
    @Query("SELECT mp FROM MediationParticipant mp " +
           "JOIN FETCH mp.user " +
           "WHERE mp.mediation.id = :mediationId")
    List<MediationParticipant> findByMediationIdWithUser(@Param("mediationId") UUID mediationId);
    
    @Query("SELECT mp FROM MediationParticipant mp " +
           "JOIN FETCH mp.mediation " +
           "WHERE mp.user.id = :userId")
    List<MediationParticipant> findByUserIdWithMediation(@Param("userId") UUID userId);
}
