package cl.metspherical.calbucofelizbackend.features.posts.repository;

import cl.metspherical.calbucofelizbackend.features.posts.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByNameIgnoreCase(String name);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) IN :names")
    Set<Category> findByNamesIgnoreCase(@Param("names") Set<String> names);
}
