package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;

public class DamageCalculator {

    public record DamageCalculatorResult(
            int baseDamage,
            int weaknessMultiplier,
            int resistanceValue,
            int finalDamage,
            int damageCountersAdded,
            boolean weaknessApplied,
            boolean resistanceApplied
    ) {}

    public static DamageCalculatorResult calculate(
            PokemonInPlay attacker,
            PokemonInPlay defender,
            CardLookupPort cardLookup,
            int attackIndex) {

        PokemonCardDefinition attackerDef = (PokemonCardDefinition) cardLookup.getCardById(attacker.getCardDefinitionId());
        PokemonCardDefinition defenderDef = (PokemonCardDefinition) cardLookup.getCardById(defender.getCardDefinitionId());

        String damageStr = attackerDef.getAttacks().get(attackIndex).getDamage();
        int baseDamage = parseIntDamage(damageStr);

        int weaknessMultiplier = 1;
        boolean weaknessApplied = false;
        if (defenderDef.getWeaknesses() != null && attackerDef.getTypes() != null) {
            for (PokemonCardDefinition.WeaknessDefinition w : defenderDef.getWeaknesses()) {
                if (attackerDef.getTypes().contains(w.getType())) {
                    weaknessMultiplier = 2;
                    weaknessApplied = true;
                    break;
                }
            }
        }

        int resistanceValue = 0;
        boolean resistanceApplied = false;
        if (defenderDef.getResistances() != null && attackerDef.getTypes() != null) {
            for (PokemonCardDefinition.ResistanceDefinition r : defenderDef.getResistances()) {
                if (attackerDef.getTypes().contains(r.getType())) {
                    resistanceValue = -20;
                    resistanceApplied = true;
                    break;
                }
            }
        }

        int finalDamage = Math.max(baseDamage * weaknessMultiplier + resistanceValue, 0);
        int damageCountersAdded = finalDamage / 10;

        return new DamageCalculatorResult(
                baseDamage,
                weaknessMultiplier,
                resistanceValue,
                finalDamage,
                damageCountersAdded,
                weaknessApplied,
                resistanceApplied
        );
    }

    private static int parseIntDamage(String damage) {
        if (damage == null || damage.isBlank()) {
            return 0;
        }
        String numeric = damage.replaceAll("[^0-9]", "");
        return numeric.isEmpty() ? 0 : Integer.parseInt(numeric);
    }
}
