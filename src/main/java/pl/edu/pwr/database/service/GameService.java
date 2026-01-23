package pl.edu.pwr.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.database.entities.GameEntity;
import pl.edu.pwr.database.entities.MoveEntity;
import pl.edu.pwr.database.repositories.GameRepository;

import java.util.Optional;

@Service
public class GameService {

  private final GameRepository gameRepository;

  @Autowired
  public GameService(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  @Transactional
  public Long startNewGame(String blackType, String whiteType) {
    GameEntity game = new GameEntity();
    game.setPlayerBlackType(blackType);
    game.setPlayerWhiteType(whiteType);
    game = gameRepository.save(game);
    return game.getId();
  }

  @Transactional
  public void saveMove(Long gameId, int moveNo, int x, int y, String color, String type) {
    if (gameId == null)
      return;

    Optional<GameEntity> gameOpt = gameRepository.findById(gameId);
    if (gameOpt.isPresent()) {
      GameEntity game = gameOpt.get();
      MoveEntity move = new MoveEntity(game, moveNo, x, y, color, type);
      game.getMoves().add(move);
      gameRepository.save(game);
    }
  }

  @Transactional
  public void saveGameResult(Long gameId, String result) {
    if (gameId == null)
      return;

    gameRepository.findById(gameId).ifPresent(game -> {
      game.setResult(result);
      gameRepository.save(game);
    });
  }
}