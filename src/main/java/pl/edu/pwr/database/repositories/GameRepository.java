package pl.edu.pwr.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.database.entities.GameEntity;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {}