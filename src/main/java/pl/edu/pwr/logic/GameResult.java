package pl.edu.pwr.logic;

public record GameResult(int blackScore, int whiteScore) {
    public String winner() {
        if (blackScore > whiteScore) return "BLACK";
        if (whiteScore > blackScore) return "WHITE";
        return "DRAW";
    }
}

