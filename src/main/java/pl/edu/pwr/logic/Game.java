package pl.edu.pwr.logic;

import java.util.List;

public class Game {
  private final Board board;
  private Stone currentPlayer = Stone.BLACK;
  
  private int blackPrisoners = 0;
  private int whitePrisoners = 0;

  public Game(int size) {
    this.board = BoardFactory.createBoard(size);
  }

  public synchronized boolean makeMove(int x, int y) {
    if(!board.isWithinBounds(x, y) || !board.isEmpty(x, y))
      return false;

    board.placeStone(x, y, currentPlayer);

    Stone opponent = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
    checkCaptures(x, y, opponent);

    switchPlayer();
    return true;
  }

  private void checkCaptures(int x, int y, Stone opponentColor) {
    List<Board.Point> neighbors = board.getNeighbors(x, y);

    for(Board.Point n : neighbors) {
      Stone neighborStone = board.get(n.x(), n.y());
      
      if(neighborStone == opponentColor)
        if(countSingleStoneLiberties(n.x(), n.y()) == 0)
          captureSingleStone(n.x(), n.y());
    }
  }

  private int countSingleStoneLiberties(int x, int y) {
    int liberties = 0;
    List<Board.Point> neighbors = board.getNeighbors(x, y);
    
    for(Board.Point n : neighbors)
      if(board.isEmpty(n.x(), n.y()))
        liberties++;

    return liberties;
  }

  private void captureSingleStone(int x, int y) {
    board.placeStone(x, y, Stone.NONE);
    
    if(currentPlayer == Stone.BLACK)
      blackPrisoners++;
    else
      whitePrisoners++;
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

  private void switchPlayer() {
    currentPlayer = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
  }
}