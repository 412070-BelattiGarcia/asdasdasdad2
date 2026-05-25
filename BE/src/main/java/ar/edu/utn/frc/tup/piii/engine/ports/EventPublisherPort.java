package ar.edu.utn.frc.tup.piii.engine.ports;

import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;

public interface EventPublisherPort {
    void publishPublicEvent(GameEvent event);
    void publishPrivateEvent(String playerId, GameEvent event);
}
