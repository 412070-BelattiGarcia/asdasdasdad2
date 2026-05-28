package ar.edu.utn.frc.tup.piii.engine;

import ar.edu.utn.frc.tup.piii.engine.action.ActionResult;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.action.GameActionType;
import ar.edu.utn.frc.tup.piii.engine.action.GameError;
import ar.edu.utn.frc.tup.piii.engine.handlers.ActionHandler;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import ar.edu.utn.frc.tup.piii.engine.ports.StatePersisterPort;
import ar.edu.utn.frc.tup.piii.engine.rules.RuleValidator;
import ar.edu.utn.frc.tup.piii.engine.victory.VictoryConditionChecker;
import ar.edu.utn.frc.tup.piii.engine.victory.VictoryResult;
import ar.edu.utn.frc.tup.piii.matches.domain.MatchStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GameEngine {
    private final CardLookupPort cardLookup;
    private final RandomizerPort randomizer;
    private final StatePersisterPort persister;
    private final EventPublisherPort eventPublisher;
    private final RuleValidator ruleValidator;
    private final VictoryConditionChecker victoryChecker;
    private final Map<GameActionType, ActionHandler> handlers;

    public GameEngine(CardLookupPort cardLookup, RandomizerPort randomizer,
                      StatePersisterPort persister, EventPublisherPort eventPublisher,
                      RuleValidator ruleValidator, VictoryConditionChecker victoryChecker,
                      Map<GameActionType, ActionHandler> handlers) {
        this.cardLookup = cardLookup;
        this.randomizer = randomizer;
        this.persister = persister;
        this.eventPublisher = eventPublisher;
        this.ruleValidator = ruleValidator;
        this.victoryChecker = victoryChecker;
        this.handlers = handlers;
    }

    public ActionResult applyAction(UUID matchId, UUID playerId, GameAction action) {
        // 1. Load state
        Optional<GameState> stateOpt = persister.loadState(matchId);
        if (stateOpt.isEmpty()) {
            return createErrorResult(action.getClientRequestId(), "MATCH_NOT_FOUND", "Partida no encontrada.");
        }
        GameState state = stateOpt.get();

        // 2. Verify match is ACTIVE
        if (state.getStatus() != MatchStatus.ACTIVE) {
            return createErrorResult(action.getClientRequestId(), ErrorCode.MATCH_NOT_ACTIVE.name(),
                    "La partida no está activa.");
        }

        // 3. Verify it's the player's turn
        if (!state.getCurrentPlayerId().equals(playerId)) {
            return createErrorResult(action.getClientRequestId(), ErrorCode.NOT_YOUR_TURN.name(),
                    "No es tu turno.");
        }

        // 4. Create EngineContext
        EngineContext ctx = new EngineContext(state, cardLookup, randomizer);

        // 5. Validate
        if (!ruleValidator.validate(ctx, action)) {
            return createErrorResult(action.getClientRequestId(), "VALIDATION_ERROR",
                    "La acción no es válida.");
        }

        // 6. Dispatch handler
        ActionHandler handler = handlers.get(action.getType());
        if (handler == null) {
            return createErrorResult(action.getClientRequestId(), "UNSUPPORTED_ACTION",
                    "Tipo de acción no soportado.");
        }
        handler.handle(action, ctx);

        // 7. Check victory
        Optional<VictoryResult> victory = victoryChecker.check(ctx);
        if (victory.isPresent()) {
            // 8. Set winner
            state.setWinnerPlayerId(victory.get().getWinnerPlayerId());
            state.setFinishReason(victory.get().getFinishReason());
            state.setStatus(MatchStatus.FINISHED);
        }

        // 9. Persist state
        persister.saveState(matchId, state);

        // 10. Publish events
        List<String> allEvents = new ArrayList<>(ctx.getEvents());
        eventPublisher.publishEvents(matchId, allEvents);

        // 11. Return success
        ActionResult result = new ActionResult();
        result.setSuccess(true);
        result.setClientRequestId(action.getClientRequestId());
        result.setEvents(allEvents);
        return result;
    }

    public Optional<GameState> loadState(UUID matchId) {
        return persister.loadState(matchId);
    }

    private ActionResult createErrorResult(String clientRequestId, String code, String message) {
        ActionResult result = new ActionResult();
        result.setSuccess(false);
        result.setClientRequestId(clientRequestId);
        result.setError(new GameError(code, message));
        result.setEvents(List.of());
        return result;
    }
}
