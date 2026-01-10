package pl.edu.pwr.logic.scoring;

import pl.edu.pwr.logic.*;

public class ScoringStrategy implements IScoringStrategy {

    private final ScoringService scoringService = new ScoringService();

    @Override
    public GameResult score(Board board, int blackPrisoners, int whitePrisoners) {
        TerritoryResult territory = scoringService.calculateTerritory(board);

        int blackScore = territory.blackTerritory();
        int whiteScore = territory.whiteTerritory();

        return new GameResult(blackScore, whiteScore);
    }
}

