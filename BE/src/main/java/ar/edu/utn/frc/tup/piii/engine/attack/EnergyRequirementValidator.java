package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.cards.domain.EnergyCardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.EnergyType;
import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;

import java.util.ArrayList;
import java.util.List;

public class EnergyRequirementValidator {

    public static boolean checkEnergyRequirements(
            PokemonInPlay attacker,
            CardLookupPort cardLookup,
            int attackIndex) {
        if (attacker == null || cardLookup == null) {
            return false;
        }

        PokemonCardDefinition pokemonDef = (PokemonCardDefinition) cardLookup.getCardById(attacker.getCardDefinitionId());
        if (pokemonDef == null) {
            return false;
        }

        if (attackIndex < 0 || pokemonDef.getAttacks() == null || attackIndex >= pokemonDef.getAttacks().size()) {
            return false;
        }

        PokemonCardDefinition.AttackDefinition attackDef = pokemonDef.getAttacks().get(attackIndex);
        List<EnergyType> requiredCost = attackDef.getCost();

        if (requiredCost == null || requiredCost.isEmpty()) {
            return true;
        }

        if (attacker.getAttachedEnergies() == null || attacker.getAttachedEnergies().isEmpty()) {
            return false;
        }

        List<EnergyType> availableEnergies = new ArrayList<>();
        for (CardInstance ci : attacker.getAttachedEnergies()) {
            EnergyCardDefinition energyDef = (EnergyCardDefinition) cardLookup.getCardById(ci.getCardDefinitionId());
            if (energyDef != null && energyDef.getProvides() != null) {
                availableEnergies.addAll(energyDef.getProvides());
            }
        }

        int colorsNeeded = 0;

        for (EnergyType energy : requiredCost) {
            if (energy == EnergyType.COLORLESS) {
                colorsNeeded++;
            } else {
                if (!availableEnergies.remove(energy)) {
                    return false;
                }
            }
        }

        return availableEnergies.size() >= colorsNeeded;
    }
}
