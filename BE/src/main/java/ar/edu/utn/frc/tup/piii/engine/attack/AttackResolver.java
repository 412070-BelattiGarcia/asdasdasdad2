package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;

import java.util.Map;

public class AttackResolver {

    public record AttackResolutionResult(
            boolean energyValid,
            DamageCalculator.DamageCalculatorResult damageCalc,
            String errorMessage,
            boolean confusedSelfHit,
            int selfDamageCounters
    ) {}

    public static AttackResolutionResult resolve(
            PokemonInPlay attacker,
            PokemonInPlay defender,
            CardLookupPort cardLookup,
            RandomizerPort randomizer,
            int attackIndex) {
        return resolve(attacker, defender, cardLookup, randomizer, attackIndex, null);
    }

    public static AttackResolutionResult resolve(
            PokemonInPlay attacker,
            PokemonInPlay defender,
            CardLookupPort cardLookup,
            RandomizerPort randomizer,
            int attackIndex,
            Map<String, Object> damageModifiers) {

        if (StatusEffectManager.isConfused(attacker) && randomizer.nextInt(2) == 0) {
            int selfDmgCounters = 3;
            // Do NOT mutate attacker damage counters here — delegate to DeclareAttackHandler
            return new AttackResolutionResult(
                    false, null, "CONFUSED_SELF_HIT",
                    true, selfDmgCounters
            );
        }

        if (!checkEnergyRequirements(attacker, cardLookup, attackIndex)) {
            return new AttackResolutionResult(
                    false, null, "Energy requirements not met",
                    false, 0
            );
        }

        DamageCalculator.DamageCalculatorResult damageCalc = DamageCalculator.calculate(attacker, defender, cardLookup, attackIndex, damageModifiers);

        return new AttackResolutionResult(
                true, damageCalc, null, false, 0
        );
    }

    private static boolean checkEnergyRequirements(PokemonInPlay attacker, CardLookupPort cardLookup, int attackIndex) {
        return new EnergyRequirementValidator().checkEnergyRequirements(attacker, cardLookup, attackIndex);
    }
}