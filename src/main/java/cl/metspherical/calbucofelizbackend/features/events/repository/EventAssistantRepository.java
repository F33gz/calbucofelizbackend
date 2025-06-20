package cl.metspherical.calbucofelizbackend.features.events.repository;

import cl.metspherical.calbucofelizbackend.features.events.model.EventAssistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EventAssistantRepository extends JpaRepository<EventAssistant, EventAssistant.EventAssistantId> {

}
