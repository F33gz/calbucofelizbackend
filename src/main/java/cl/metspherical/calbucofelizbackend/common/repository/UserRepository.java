package cl.metspherical.calbucofelizbackend.common.repository;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByRut(String rut);
}

