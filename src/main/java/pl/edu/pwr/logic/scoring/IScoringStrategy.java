package pl.edu.pwr.logic.scoring;

import pl.edu.pwr.logic.Board;
import pl.edu.pwr.logic.GameResult;

public interface IScoringStrategy {
    GameResult score(Board board, int blackPrisoners, int whitePrisoners);
}

