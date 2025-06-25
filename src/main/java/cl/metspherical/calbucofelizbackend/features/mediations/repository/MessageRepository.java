package cl.metspherical.calbucofelizbackend.features.mediations.repository;

import cl.metspherical.calbucofelizbackend.features.mediations.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query("SELECT m FROM Message m " +
           "JOIN FETCH m.sender " +
           "WHERE m.mediation.id = :mediationId " +
           "ORDER BY m.sentAt ASC")
    List<Message> findByMediationIdOrderBySentAtAsc(@Param("mediationId") UUID mediationId);
}
