package ar.edu.utn.frc.tup.piii.engine;

import ar.edu.utn.frc.tup.piii.engine.action.ActionResult;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import ar.edu.utn.frc.tup.piii.engine.ports.StatePersisterPort;

import java.util.UUID;

public class GameEngine {
    private final CardLookupPort cardLookup;
    private final RandomizerPort randomizer;
    private final StatePersisterPort persister;
    private final EventPublisherPort eventPublisher;

    public GameEngine(CardLookupPort cardLookup, RandomizerPort randomizer,
                      StatePersisterPort persister, EventPublisherPort eventPublisher) {
        this.cardLookup = cardLookup;
        this.randomizer = randomizer;
        this.persister = persister;
        this.eventPublisher = eventPublisher;
    }

    public ActionResult applyAction(UUID matchId, UUID playerId, GameAction action) {
        return null;
    }

    public GameState loadState(UUID matchId) {
        return null;
    }
}
