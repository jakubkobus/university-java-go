package pl.edu.pwr.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.database.entities.GameEntity;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {
  
  @Query("SELECT g FROM GameEntity g LEFT JOIN FETCH g.moves WHERE g.id = :id")
  Optional<GameEntity> findByIdWithMoves(@Param("id") Long id);
}