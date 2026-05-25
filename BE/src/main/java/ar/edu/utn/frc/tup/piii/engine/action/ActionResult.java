package ar.edu.utn.frc.tup.piii.engine.action;

import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.model.PrivatePlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PublicGameState;

import java.util.List;

public class ActionResult {
    private boolean success;
    private String clientRequestId;
    private PublicGameState publicState;
    private PrivatePlayerState privateState;
    private List<GameEvent> events;
    private GameError error;

    public boolean isSuccess() { return success; }
    public String getClientRequestId() { return clientRequestId; }
    public PublicGameState getPublicState() { return publicState; }
    public PrivatePlayerState getPrivateState() { return privateState; }
    public List<GameEvent> getEvents() { return events; }
    public GameError getError() { return error; }
}
