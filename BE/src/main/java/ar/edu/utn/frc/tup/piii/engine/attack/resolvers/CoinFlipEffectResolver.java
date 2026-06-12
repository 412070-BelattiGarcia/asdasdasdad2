package ar.edu.utn.frc.tup.piii.engine.attack.resolvers;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.SpecialCondition;
import ar.edu.utn.frc.tup.piii.engine.attack.*;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.util.HashMap;
import java.util.Map;

public class CoinFlipEffectResolver implements AttackEffectResolver {

    private final AttackEffectRegistry registry;

    public CoinFlipEffectResolver(AttackEffectRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void resolve(EngineContext ctx, PokemonInPlay attacker, PokemonInPlay defender, AttackEffect effect, Map<String, Object> payload) {
        boolean heads = ctx.getRandomizer().nextInt(2) == 0;
        if (!heads) return;

        String subEffectType = (String) effect.getParams().get("effectType");
        String subEffectParam = (String) effect.getParams().get("effectParam");
        if (subEffectType == null) return;

        AttackEffectType subType;
        try {
            subType = AttackEffectType.valueOf(subEffectType);
        } catch (IllegalArgumentException e) {
            return;
        }

        Map<String, Object> subParams = new HashMap<>();
        if (subEffectParam != null) {
            switch (subType) {
                case APPLY_SPECIAL_CONDITION:
                    subParams.put("condition", subEffectParam);
                    break;
                case DISCARD_ENERGY:
                case DRAW_CARDS:
                    try {
                        subParams.put("count", Integer.parseInt(subEffectParam));
                    } catch (NumberFormatException ignored) {}
                    break;
                case DAMAGE_BENCH:
                case HEAL_USER:
                    try {
                        subParams.put("damage", Integer.parseInt(subEffectParam));
                    } catch (NumberFormatException ignored) {}
                    break;
                default:
                    break;
            }
        }

        AttackEffect subEffect = new AttackEffect(subType, subParams);
        registry.resolve(ctx, attacker, defender, java.util.List.of(subEffect), payload);
    }

    @Override
    public AttackEffectType getType() {
        return AttackEffectType.COIN_FLIP_AFTER_DAMAGE;
    }
}
