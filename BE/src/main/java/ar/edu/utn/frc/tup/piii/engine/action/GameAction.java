package ar.edu.utn.frc.tup.piii.engine.action;

import java.util.UUID;

public class GameAction {
    private GameActionType type;
    private UUID playerId;
    private GameActionPayload payload;
    private String clientRequestId;

    public GameAction() {}

    public GameAction(GameActionType type, UUID playerId, GameActionPayload payload, String clientRequestId) {
        this.type = type;
        this.playerId = playerId;
        this.payload = payload;
        this.clientRequestId = clientRequestId;
    }

    public GameActionType getType() { return type; }
    public void setType(GameActionType type) { this.type = type; }

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }

    public GameActionPayload getPayload() { return payload; }
    public void setPayload(GameActionPayload payload) { this.payload = payload; }

    public String getClientRequestId() { return clientRequestId; }
    public void setClientRequestId(String clientRequestId) { this.clientRequestId = clientRequestId; }
}
