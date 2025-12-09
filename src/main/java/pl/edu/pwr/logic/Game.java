package pl.edu.pwr.logic;

public class Game {

    private final Board board;
    private Stone currentPlayer = Stone.BLACK; // Black starts

    public Game(int size) {
        this.board = new Board(size);
    }

    public Board getBoard() {
        return board;
    }

    public Stone getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean makeMove(int x, int y) {
        if (!board.isWithinBounds(x, y)) {
            return false;
        }

        if (!board.isEmpty(x, y)) {
            return false;
        }

        board.placeStone(x, y, currentPlayer);
        switchPlayer();
        return true;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
    }
}
