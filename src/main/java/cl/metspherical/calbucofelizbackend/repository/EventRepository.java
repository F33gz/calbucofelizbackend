package cl.metspherical.calbucofelizbackend.repository;

import cl.metspherical.calbucofelizbackend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    
    @Query("SELECT e FROM Event e " +
           "JOIN FETCH e.createdBy " +
           "WHERE EXTRACT(MONTH FROM e.init) = :month " +
           "ORDER BY e.init ASC")
    List<Event> findEventsByMonth(@Param("month") int month);

    @Query("SELECT e FROM Event e " +
           "JOIN FETCH e.createdBy " +
           "WHERE e.id = :id")
    Optional<Event> findByIdWithDetails(@Param("id") Integer id);
}
