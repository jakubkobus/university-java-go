package pl.edu.pwr.viewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.database.entities.GameEntity;
import pl.edu.pwr.database.entities.MoveEntity;
import pl.edu.pwr.database.repositories.GameRepository;

import java.util.List;
import java.util.Optional;

/**
 * Serwis do ładowania i przeglądania gier z bazy danych.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Pobieranie wszystkich gier z bazy danych</li>
 *   <li>Pobieranie konkretnej gry z jej ruchami</li>
 *   <li>Sortowanie ruchów według numeru ruchu</li>
 * </ul>
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class GameViewerService {

  @Autowired
  private GameRepository gameRepository;

  /**
   * Pobiera wszystkie gry z bazy danych.
   * 
   * @return lista wszystkich gier
   */
  public List<GameEntity> getAllGames() {
    return gameRepository.findAll();
  }

  /**
   * Pobiera konkretną grę według ID wraz ze wszystkimi jej ruchami.
   * 
   * @param gameId ID gry do pobrania
   * @return Optional zawierający grę jeśli została znaleziona
   */
  public Optional<GameEntity> getGameById(Long gameId) {
    return gameRepository.findByIdWithMoves(gameId);
  }

  /**
   * Pobiera wszystkie ruchy dla konkretnej gry, posortowane według numeru ruchu.
   * 
   * @param game encja gry
   * @return lista ruchów posortowana według numeru ruchu
   */
  public List<MoveEntity> getMovesForGame(GameEntity game) {
    List<MoveEntity> moves = game.getMoves();
    moves.sort((m1, m2) -> Integer.compare(m1.getMoveNumber(), m2.getMoveNumber()));
    return moves;
  }
}
