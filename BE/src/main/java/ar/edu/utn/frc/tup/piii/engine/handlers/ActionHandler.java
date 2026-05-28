package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;

public interface ActionHandler {
    void handle(GameAction action, EngineContext ctx);
}
