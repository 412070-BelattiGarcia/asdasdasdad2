package ar.edu.utn.frc.tup.piii.engine.model;

import ar.edu.utn.frc.tup.piii.engine.MatchStatus;
import ar.edu.utn.frc.tup.piii.engine.turn.TurnPhase;
import ar.edu.utn.frc.tup.piii.engine.victory.FinishReason;

import java.time.Instant;
import java.util.UUID;

public class GameState {
    private UUID matchId;
    private MatchStatus status;
    private TurnPhase phase;
    private int turnNumber;
    private UUID currentPlayerId;
    private UUID firstPlayerId;
    private PlayerState[] players;
    private UUID stadiumCardInstanceId;
    private TurnFlags turnFlags;
    private Object pendingDecision;
    private UUID pendingPrizeOwnerPlayerId;
    private UUID winnerPlayerId;
    private FinishReason finishReason;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getMatchId() { return matchId; }
    public void setMatchId(UUID matchId) { this.matchId = matchId; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public TurnPhase getPhase() { return phase; }
    public void setPhase(TurnPhase phase) { this.phase = phase; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public UUID getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(UUID currentPlayerId) { this.currentPlayerId = currentPlayerId; }

    public UUID getFirstPlayerId() { return firstPlayerId; }
    public void setFirstPlayerId(UUID firstPlayerId) { this.firstPlayerId = firstPlayerId; }

    public PlayerState[] getPlayers() { return players; }
    public void setPlayers(PlayerState[] players) { this.players = players; }

    public UUID getStadiumCardInstanceId() { return stadiumCardInstanceId; }
    public void setStadiumCardInstanceId(UUID stadiumCardInstanceId) { this.stadiumCardInstanceId = stadiumCardInstanceId; }

    public TurnFlags getTurnFlags() { return turnFlags; }
    public void setTurnFlags(TurnFlags turnFlags) { this.turnFlags = turnFlags; }

    public Object getPendingDecision() { return pendingDecision; }
    public void setPendingDecision(Object pendingDecision) { this.pendingDecision = pendingDecision; }

    public UUID getPendingPrizeOwnerPlayerId() { return pendingPrizeOwnerPlayerId; }
    public void setPendingPrizeOwnerPlayerId(UUID pendingPrizeOwnerPlayerId) { this.pendingPrizeOwnerPlayerId = pendingPrizeOwnerPlayerId; }

    public UUID getWinnerPlayerId() { return winnerPlayerId; }
    public void setWinnerPlayerId(UUID winnerPlayerId) { this.winnerPlayerId = winnerPlayerId; }

    public FinishReason getFinishReason() { return finishReason; }
    public void setFinishReason(FinishReason finishReason) { this.finishReason = finishReason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
