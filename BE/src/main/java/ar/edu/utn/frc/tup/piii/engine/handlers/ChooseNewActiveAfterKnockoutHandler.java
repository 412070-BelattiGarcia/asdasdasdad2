package ar.edu.utn.frc.tup.piii.engine.handlers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.action.GameAction;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.GameState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChooseNewActiveAfterKnockoutHandler implements GameHandler {
    public void handle(EngineContext ctx, GameAction action) {
        GameState state = ctx.getState();
        var player = ctx.getPlayer(action.getPlayerId());

        String newActiveIdStr = action.getPayloadString("newActivePokemonInstanceId");
        if (newActiveIdStr == null) return;
        UUID newActiveId = UUID.fromString(newActiveIdStr);
        PokemonInPlay selected = HandlerHelper.findPokemon(player, newActiveId);
        if (selected == null) return;

        player.getBench().remove(selected);
        player.setActivePokemon(selected);

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("newActivePokemonInstanceId", newActiveId.toString());

        ctx.addEvent(new GameEvent(
                GameEventType.STATE_UPDATED.name(),
                state.getMatchId(),
                state.getTurnNumber(),
                Instant.now(),
                "New active Pokemon chosen after knockout.",
                eventPayload
        ));
    }
}
