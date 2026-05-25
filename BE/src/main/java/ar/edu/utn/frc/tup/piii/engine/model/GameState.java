package ar.edu.utn.frc.tup.piii.engine.model;

import java.time.Instant;
import java.util.UUID;

public class GameState {
    private UUID matchId;
    private String status;
    private String phase;
    private int turnNumber;
    private UUID currentPlayerId;
    private UUID firstPlayerId;
    private PlayerState[] players;
    private UUID stadiumCardInstanceId;
    private TurnFlags turnFlags;
    private Object pendingDecision;
    private UUID winnerPlayerId;
    private String finishReason;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getMatchId() { return matchId; }
    public String getStatus() { return status; }
    public String getPhase() { return phase; }
    public int getTurnNumber() { return turnNumber; }
    public UUID getCurrentPlayerId() { return currentPlayerId; }
    public UUID getFirstPlayerId() { return firstPlayerId; }
    public PlayerState[] getPlayers() { return players; }
    public TurnFlags getTurnFlags() { return turnFlags; }
    public UUID getWinnerPlayerId() { return winnerPlayerId; }
    public String getFinishReason() { return finishReason; }
}
