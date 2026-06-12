package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.engine.EngineContext;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.util.Map;

public interface AttackEffectResolver {
    void resolve(EngineContext ctx, PokemonInPlay attacker, PokemonInPlay defender, AttackEffect effect, Map<String, Object> payload);
    AttackEffectType getType();
}
