package cl.metspherical.calbucofelizbackend.features.events.repository;

import cl.metspherical.calbucofelizbackend.features.events.model.Assistance;
import cl.metspherical.calbucofelizbackend.features.events.enums.AssistanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssistanceRepository extends JpaRepository<Assistance, Byte> {
    
    Optional<Assistance> findByName(AssistanceType name);
}
