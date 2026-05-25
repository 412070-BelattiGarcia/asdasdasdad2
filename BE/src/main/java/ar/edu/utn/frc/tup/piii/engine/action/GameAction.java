package ar.edu.utn.frc.tup.piii.engine.action;

import java.util.Map;
import java.util.UUID;

public class GameAction {
    private String type;
    private UUID playerId;
    private Map<String, Object> payload;
    private String clientRequestId;

    public String getType() { return type; }
    public UUID getPlayerId() { return playerId; }
    public Map<String, Object> getPayload() { return payload; }
    public String getClientRequestId() { return clientRequestId; }
}
