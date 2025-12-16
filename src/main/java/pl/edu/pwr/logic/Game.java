package pl.edu.pwr.logic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class Game {
  private final Board board;
  private Stone currentPlayer = Stone.BLACK;

  public Game(int size) {
    this.board = BoardFactory.createBoard(size);
  }

  public synchronized boolean makeMove(int x, int y) {
    if(!board.isWithinBounds(x, y) || !board.isEmpty(x, y)) {
      return false;
    }

    board.placeStone(x, y, currentPlayer);

    Stone opponent = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
    boolean captured = checkCaptures(x, y, opponent);

    if(!captured && countLiberties(x, y) == 0) {
      board.placeStone(x, y, Stone.NONE);
      return false;
    }

    switchPlayer();
    return true;
  }

  private boolean checkCaptures(int x, int y, Stone opponentColor) {
    boolean anyCaptured = false;
    List<Board.Point> neighbors = board.getNeighbors(x, y);

    for(Board.Point n : neighbors) {
      Stone neighborStone = board.get(n.x(), n.y());
      if(neighborStone == opponentColor) {
        if(countGroupLiberties(n.x(), n.y(), opponentColor) == 0) {
          removeGroup(n.x(), n.y(), opponentColor);
          anyCaptured = true;
        }
      }
    }
    return anyCaptured;
  }

  private int countLiberties(int x, int y) {
    return countGroupLiberties(x, y, board.get(x, y));
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

    while(!stack.isEmpty()) {
      Board.Point current = stack.remove(0);
      String key = current.x() + "," + current.y();

      if(visited.contains(key))
        continue;
      visited.add(key);

      board.placeStone(current.x(), current.y(), Stone.NONE);

      for(Board.Point neighbor : board.getNeighbors(current.x(), current.y()))
        if(board.get(neighbor.x(), neighbor.y()) == color)
          stack.add(neighbor);
    }
  }

  public Board getBoard() {
    return board;
  }

  public Stone getCurrentPlayer() {
    return currentPlayer;
  }

  private void switchPlayer() {
    currentPlayer = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
  }
}