package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;

public class AttackResolver {

    public record AttackResolutionResult(
            boolean energyValid,
            DamageCalculator.DamageCalculatorResult damageCalc,
            String errorMessage
    ) {}

    public static AttackResolutionResult resolve(
            PokemonInPlay attacker,
            PokemonInPlay defender,
            CardLookupPort cardLookup,
            int attackIndex) {

        EnergyRequirementValidator energyValidator = new EnergyRequirementValidator(attacker, cardLookup, attackIndex);
        boolean energyValid = energyValidator.checkEnergyRequirements(attacker, cardLookup, attackIndex);
        if (!energyValid) {
            return new AttackResolutionResult(
                    false, null,
                    "Energy requirements not met"
            );
        }

        DamageCalculator.DamageCalculatorResult damageCalc = DamageCalculator.calculate(attacker, defender, cardLookup, attackIndex);

        return new AttackResolutionResult(
                true, damageCalc, null
        );
    }
}
