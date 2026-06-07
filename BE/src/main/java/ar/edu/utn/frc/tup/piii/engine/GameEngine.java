package ar.edu.utn.frc.tup.piii.engine;

import ar.edu.utn.frc.tup.piii.engine.action.ActionResult;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.action.GameActionType;
import ar.edu.utn.frc.tup.piii.engine.action.GameError;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.handlers.*;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import ar.edu.utn.frc.tup.piii.engine.ports.StatePersisterPort;
import ar.edu.utn.frc.tup.piii.engine.rules.RuleValidator;
import ar.edu.utn.frc.tup.piii.engine.turn.TurnManager;

import java.util.*;

public class GameEngine {
    private final CardLookupPort cardLookup;
    private final RandomizerPort randomizer;
    private final StatePersisterPort persister;
    private final EventPublisherPort eventPublisher;
    private final RuleValidator ruleValidator;
    private final Map<GameActionType, GameHandler> handlers;

    public GameEngine(CardLookupPort cardLookup, RandomizerPort randomizer,
                      StatePersisterPort persister, EventPublisherPort eventPublisher,
                      TurnManager turnManager, RuleValidator ruleValidator) {
        this.cardLookup = cardLookup;
        this.randomizer = randomizer;
        this.persister = persister;
        this.eventPublisher = eventPublisher;
        this.ruleValidator = ruleValidator;
        this.handlers = buildDefaultHandlers(turnManager);
    }

    private Map<GameActionType, GameHandler> buildDefaultHandlers(TurnManager turnManager) {
        return Map.ofEntries(
                Map.entry(GameActionType.DRAW_CARD, new DrawCardHandler()),
                Map.entry(GameActionType.PUT_BASIC_ON_BENCH, new PutBasicOnBenchHandler()),
                Map.entry(GameActionType.ATTACH_ENERGY, new AttachEnergyHandler()),
                Map.entry(GameActionType.EVOLVE_POKEMON, new EvolvePokemonHandler()),
                Map.entry(GameActionType.PLAY_TRAINER, new PlayTrainerHandler()),
                Map.entry(GameActionType.RETREAT_ACTIVE, new RetreatActiveHandler()),
                Map.entry(GameActionType.DECLARE_ATTACK, new DeclareAttackHandler()),
                Map.entry(GameActionType.END_TURN, new EndTurnHandler(turnManager)),
                Map.entry(GameActionType.CHOOSE_KNOCKOUT_REPLACEMENT, new ChooseNewActiveAfterKnockoutHandler()),
                Map.entry(GameActionType.TAKE_PRIZE_CARD, new TakePrizeCardHandler())
        );
    }

    public ActionResult applyAction(UUID matchId, UUID playerId, GameAction action) {
        try {
            GameState state = persister.loadState(matchId);
            if (state == null) {
                return new ActionResult(false, action.getClientRequestId(), null, null, List.of(),
                        new GameError("MATCH_NOT_FOUND", "Match not found: " + matchId));
            }

            if (state.getStatus() != MatchStatus.ACTIVE) {
                return new ActionResult(false, action.getClientRequestId(), null, null, List.of(),
                        new GameError("MATCH_NOT_ACTIVE", "The match is not active."));
            }

            if (!state.getCurrentPlayerId().equals(playerId)) {
                return new ActionResult(false, action.getClientRequestId(), null, null, List.of(),
                        new GameError("NOT_YOUR_TURN", "It is not your turn."));
            }

            EngineContext ctx = new EngineContext(state, cardLookup, randomizer, persister, eventPublisher);

            if (!ruleValidator.validate(ctx, action)) {
                return new ActionResult(false, action.getClientRequestId(), null, null, List.of(),
                        new GameError("ACTION_REJECTED", "Action rejected by game rules: " + action.getType()));
            }

            GameHandler handler = handlers.get(action.getType());
            if (handler == null) {
                return new ActionResult(false, action.getClientRequestId(), null, null, List.of(),
                        new GameError("UNKNOWN_ACTION", "Unknown action type: " + action.getType()));
            }

            handler.handle(ctx, action);

            if (ctx.getError() != null) {
                return new ActionResult(false, action.getClientRequestId(), null, null, List.of(), ctx.getError());
            }

            persister.saveState(matchId, ctx.getState());

            List<GameEvent> pendingEvents = ctx.getPendingEvents();
            if (!pendingEvents.isEmpty()) {
                eventPublisher.publishEvents(matchId, pendingEvents);
            }

            return new ActionResult(true, action.getClientRequestId(), null, null, pendingEvents, null);

        } catch (Exception e) {
            return new ActionResult(false, action.getClientRequestId(), null, null, List.of(),
                    new GameError("INTERNAL_ERROR", e.getMessage()));
        }
    }

    public GameState loadState(UUID matchId) {
        return persister.loadState(matchId);
    }
}
