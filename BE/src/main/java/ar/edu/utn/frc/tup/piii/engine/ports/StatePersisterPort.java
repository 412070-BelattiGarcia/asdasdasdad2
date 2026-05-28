package ar.edu.utn.frc.tup.piii.engine.ports;

import ar.edu.utn.frc.tup.piii.engine.model.GameState;

import java.util.Optional;
import java.util.UUID;

public interface StatePersisterPort {
    void saveState(UUID matchId, GameState state);
    Optional<GameState> loadState(UUID matchId);
}
