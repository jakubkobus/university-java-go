package pl.edu.pwr.logic;

import pl.edu.pwr.logic.GameState;
import pl.edu.pwr.logic.scoring.IScoringStrategy;
import pl.edu.pwr.logic.scoring.ScoringStrategy;
import pl.edu.pwr.logic.scoring.ScoringService;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class Game {
  private final Board board;
  private Board previousBoard;
  private Stone currentPlayer = Stone.BLACK;
  
  private int blackPrisoners = 0;
  private int whitePrisoners = 0;

  private int passesInRow = 0;
  private int consecutivePasses = 0;
  private boolean gameOver = false;
  private String gameResult = "";

  public Game(int size) {
    this.board = BoardFactory.createBoard(size);
  }

  public synchronized boolean makeMove(int x, int y) {
      if (gameOver) return false;

      if(!board.isWithinBounds(x, y) || !board.isEmpty(x, y))
      return false;

    Board tempBoard = new Board(board);
    tempBoard.placeStone(x, y, currentPlayer);

    Stone opponent = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
    
    Board backupBoard = new Board(board);
    int backupBlack = blackPrisoners;
    int backupWhite = whitePrisoners;

    board.placeStone(x, y, currentPlayer);
    checkCaptures(x, y, opponent);

    // sprawdzanie samobojstwa
    if(countGroupLiberties(x, y, currentPlayer) == 0) {
      restoreBoard(backupBoard, backupBlack, backupWhite);
      return false;
    }

    // sprawdzanie ko
    if(previousBoard != null && board.isTheSameAs(previousBoard)) {
      restoreBoard(backupBoard, backupBlack, backupWhite);
      return false;
    }

    passesInRow = 0;
    previousBoard = backupBoard; 
    
    switchPlayer();
    return true;
  }

  private void restoreBoard(Board backup, int bPris, int wPris) {
    for(int i = 0; i < board.getSize(); i++) 
      for(int j = 0; j < board.getSize(); j++) 
        board.placeStone(i, j, backup.get(i, j));

    this.blackPrisoners = bPris;
    this.whitePrisoners = wPris;
  }

  private void checkCaptures(int x, int y, Stone opponentColor) {
    List<Board.Point> neighbors = board.getNeighbors(x, y);

    for(Board.Point n : neighbors) {
      Stone neighborStone = board.get(n.x(), n.y());
      
      if(neighborStone == opponentColor)
          if(countGroupLiberties(n.x(), n.y(), opponentColor) == 0)
              removeGroup(n.x(), n.y(), opponentColor);
    }
  }
  private int countGroupLiberties(int startX, int startY, Stone color) {
      Set<String> visited = new HashSet<>();
      Set<String> liberties = new HashSet<>();
      List<Board.Point> stack = new ArrayList<>();
      stack.add(new Board.Point(startX, startY));

      while(!stack.isEmpty()) {
          Board.Point current = stack.remove(0);
          String key = current.x() + "," + current.y();

          if(visited.contains(key))
              continue;
          visited.add(key);

          for(Board.Point neighbor : board.getNeighbors(current.x(), current.y())) {
              Stone s = board.get(neighbor.x(), neighbor.y());
              if(s == Stone.NONE)
                  liberties.add(neighbor.x() + "," + neighbor.y());
              else if(s == color)
                  stack.add(neighbor);
          }
      }
      return liberties.size();
  }
  private void removeGroup(int startX, int startY, Stone color) {
      List<Board.Point> stack = new ArrayList<>();
      stack.add(new Board.Point(startX, startY));
      Set<String> visited = new HashSet<>();

      int stonesCaptured = 0;

      while(!stack.isEmpty()) {
          Board.Point current = stack.remove(0);
          String key = current.x() + "," + current.y();

          if(visited.contains(key))
              continue;
          visited.add(key);

          board.placeStone(current.x(), current.y(), Stone.NONE);
          stonesCaptured++;

          for(Board.Point neighbor : board.getNeighbors(current.x(), current.y()))
              if(board.get(neighbor.x(), neighbor.y()) == color)
                  stack.add(neighbor);
      }

      if(currentPlayer == Stone.BLACK) {
          blackPrisoners += stonesCaptured;
      } else {
          whitePrisoners += stonesCaptured;
      }
  }

  public synchronized void pass() {
      if (gameOver) return;
      passesInRow++;

      if (passesInRow >= 2) {
          if (state == GameState.IN_PROGRESS) {
              state = GameState.CLEANUP;
              passesInRow = 0;
          } else if (state == GameState.CLEANUP){
              state = GameState.FINISHED;
              finishGame();
          }
      } else {
          switchPlayer();
      }
  }

  public synchronized void surrender(Stone who) {
    gameOver = true;
    Stone winner = (who == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
    gameResult = "Poddanie, wygrywa " + (winner == Stone.BLACK ? "CZARNY" : "BIALY");
  }

  private final IScoringStrategy scoringStrategy = new ScoringStrategy();

  public synchronized void finishGame() {
      GameResult result = scoringStrategy.score(
              board,
              blackPrisoners,
              whitePrisoners
      );

      gameOver = true;
      gameResult = "Wygrywa " + result.winner() +
              " (" + result.blackScore() + " : " + result.whiteScore() + ")";
  }

  private GameState state = GameState.IN_PROGRESS;
  private final ScoringService scoringService = new ScoringService();

    public synchronized String removeDeadStone(int x, int y) {
        if (state != GameState.CLEANUP) return "ERR Faza to nie CLEANUP";

        Stone stoneAtPos = board.get(x, y);
        if (stoneAtPos == Stone.NONE) return "ERR To pole jest juz puste";

        if (stoneAtPos == currentPlayer) {
            return "ERR Nie mozesz usunac wlasnego kamienia!";
        }

        if (!scoringService.isPrisonerRemovable(board, x, y, currentPlayer)) {
            return "ERR Ten kamien/grupa nie jest w pelni otoczona Twoim terytorium!";
        }

        if (stoneAtPos == Stone.WHITE) {
            blackPrisoners++;
        } else {
            whitePrisoners++;
        }

        board.placeStone(x, y, Stone.NONE);

        return "OK";
    }


  public GameState getState() {
      return state;
  }

  public synchronized Boolean placePrisonerAsDead(int x, int y) {
      if (state != GameState.CLEANUP) return false;
      if (!board.isEmpty(x, y)) return false;

      if (currentPlayer == Stone.BLACK) {
          if (blackPrisoners > 0) {
              board.placeStone(x, y, Stone.WHITE);
              blackPrisoners--;
              return true;
          }
      } else {
          if (whitePrisoners > 0) {
              board.placeStone(x, y, Stone.BLACK);
              whitePrisoners--;
              return true;
          }
      }
      return false;
  }


  public Board getBoard() {
    return board;
  }

  public Stone getCurrentPlayer() {
    return currentPlayer;
  }
  
  public int getBlackPrisoners() {
      return blackPrisoners;
  }

  public int getWhitePrisoners() {
      return whitePrisoners;
  }

  public boolean isGameOver() { 
    return gameOver; 
  }

  public String getGameResult() { 
    return gameResult; 
  }

  public void switchPlayer() {
    currentPlayer = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
  }
}