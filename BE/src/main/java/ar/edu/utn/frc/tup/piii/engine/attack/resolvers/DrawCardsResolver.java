package ar.edu.utn.frc.tup.piii.engine.attack.resolvers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffect;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectResolver;
import ar.edu.utn.frc.tup.piii.engine.attack.AttackEffectType;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.event.GameEventType;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class DrawCardsResolver implements AttackEffectResolver {

    @Override
    public void resolve(EngineContext ctx, PokemonInPlay attacker, PokemonInPlay defender, AttackEffect effect, Map<String, Object> payload) {
        Object countObj = effect.getParams().get("count");
        if (!(countObj instanceof Number count)) return;

        PlayerState player = findPlayer(ctx, attacker);
        if (player == null) return;

        int toDraw = Math.min(count.intValue(), player.getDeck() != null ? player.getDeck().size() : 0);
        int drawn = 0;
        for (int i = 0; i < toDraw; i++) {
            CardInstance card = player.getDeck().remove(0);
            player.getHand().add(card);
            drawn++;
        }

        if (drawn > 0) {
            ctx.addEvent(new GameEvent(
                    GameEventType.CARDS_DRAWN.name(),
                    ctx.getState().getMatchId(),
                    ctx.getState().getTurnNumber(),
                    Instant.now(),
                    "Drew " + drawn + " cards from attack effect.",
                    Map.of("count", drawn, "playerId", player.getPlayerId().toString())
            ));
        }
    }

    private PlayerState findPlayer(EngineContext ctx, PokemonInPlay attacker) {
        for (PlayerState ps : ctx.getState().getPlayers()) {
            if (ps.getActivePokemon() != null &&
                    ps.getActivePokemon().getInstanceId().equals(attacker.getInstanceId())) {
                return ps;
            }
            if (ps.getBench() != null) {
                boolean onBench = ps.getBench().stream()
                        .anyMatch(p -> p.getInstanceId().equals(attacker.getInstanceId()));
                if (onBench) return ps;
            }
        }
        return null;
    }

    @Override
    public AttackEffectType getType() {
        return AttackEffectType.DRAW_CARDS;
    }
}
