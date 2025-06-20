package cl.metspherical.calbucofelizbackend.features.events.repository;

import cl.metspherical.calbucofelizbackend.features.events.model.EventAssistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventAssistantRepository extends JpaRepository<EventAssistant, EventAssistant.EventAssistantId> {

    @Query("SELECT ea FROM EventAssistant ea " +
           "JOIN FETCH ea.user " +
           "JOIN FETCH ea.assistance " +
           "WHERE ea.event.id = :eventId")
    List<EventAssistant> findByEventIdWithDetails(@Param("eventId") Integer eventId);
}
