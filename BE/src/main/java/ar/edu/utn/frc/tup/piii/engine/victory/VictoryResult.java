package ar.edu.utn.frc.tup.piii.engine.victory;

import java.util.UUID;

public class VictoryResult {
    private UUID winnerPlayerId;
    private FinishReason finishReason;

    public VictoryResult() {}

    public VictoryResult(UUID winnerPlayerId, FinishReason finishReason) {
        this.winnerPlayerId = winnerPlayerId;
        this.finishReason = finishReason;
    }

    public UUID getWinnerPlayerId() { return winnerPlayerId; }
    public void setWinnerPlayerId(UUID winnerPlayerId) { this.winnerPlayerId = winnerPlayerId; }

    public FinishReason getFinishReason() { return finishReason; }
    public void setFinishReason(FinishReason finishReason) { this.finishReason = finishReason; }
}
