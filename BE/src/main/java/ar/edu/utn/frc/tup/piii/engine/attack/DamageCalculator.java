package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.ability.hooks.FurCoatHook;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;

import java.util.Map;

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
        return calculate(attacker, defender, cardLookup, attackIndex, null);
    }

    public static DamageCalculatorResult calculate(
            PokemonInPlay attacker,
            PokemonInPlay defender,
            CardLookupPort cardLookup,
            int attackIndex,
            Map<String, Object> damageModifiers) {

        PokemonCardDefinition attackerDef = (PokemonCardDefinition) cardLookup.getCardById(attacker.getCardDefinitionId());
        PokemonCardDefinition defenderDef = (PokemonCardDefinition) cardLookup.getCardById(defender.getCardDefinitionId());

        String damageStr = attackerDef.getAttacks().get(attackIndex).getDamage();
        int baseDamage = parseIntDamage(damageStr);

        if (baseDamage == 0) {
            return new DamageCalculatorResult(0, 1, 0, 0, 0, false, false);
        }

        if (damageModifiers != null) {
            Object attackerMod = damageModifiers.get(attacker.getInstanceId().toString());
            if (attackerMod instanceof Number n) {
                baseDamage += n.intValue();
            }
        }

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
                    resistanceValue = parseIntResistanceValue(r.getValue());
                    resistanceApplied = true;
                    break;
                }
            }
        }

        int finalDamage = Math.max(baseDamage * weaknessMultiplier + resistanceValue, 0);

        if (damageModifiers != null) {
            Object defenderMod = damageModifiers.get(defender.getInstanceId().toString());
            if (defenderMod instanceof Number n) {
                finalDamage = Math.max(finalDamage + n.intValue(), 0);
            }
        }

        finalDamage = FurCoatHook.reduceDamage(finalDamage, defender, cardLookup);

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

    static int parseIntDamage(String damage) {
        if (damage == null || damage.isBlank()) {
            return 0;
        }
        String numeric = damage.replaceAll("[^0-9-]", "");
        if (numeric.isEmpty() || "-".equals(numeric)) return 0;
        try {
            return Integer.parseInt(numeric);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static int parseIntResistanceValue(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
