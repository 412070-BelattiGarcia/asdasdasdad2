package ar.edu.utn.frc.tup.piii.engine.ability.hooks;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.EnergyCardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.engine.model.CardInstance;
import ar.edu.utn.frc.tup.piii.engine.model.PlayerState;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;

public class SweetVeilHook {

    public static boolean isImmune(PokemonInPlay target, PlayerState owner, CardLookupPort cardLookup) {
        if (target == null || owner == null || cardLookup == null) return false;

        boolean hasFairyEnergy = target.getAttachedEnergies() != null
                && target.getAttachedEnergies().stream().anyMatch(ci -> {
                    CardDefinition def = cardLookup.getCardById(ci.getCardDefinitionId());
                    if (def instanceof EnergyCardDefinition energyDef) {
                        return energyDef.getProvides() != null
                                && energyDef.getProvides().contains(ar.edu.utn.frc.tup.piii.cards.domain.EnergyType.FAIRY);
                    }
                    return false;
                });
        if (!hasFairyEnergy) return false;

        for (PokemonInPlay pkm : getAllPlayerPokemon(owner)) {
            CardDefinition def = cardLookup.getCardById(pkm.getCardDefinitionId());
            if (def instanceof PokemonCardDefinition pkmDef && pkmDef.getAbilities() != null) {
                boolean hasSweetVeil = pkmDef.getAbilities().stream()
                        .anyMatch(a -> "Sweet Veil".equals(a.getName()));
                if (hasSweetVeil) return true;
            }
        }
        return false;
    }

    private static java.util.List<PokemonInPlay> getAllPlayerPokemon(PlayerState player) {
        java.util.List<PokemonInPlay> all = new java.util.ArrayList<>();
        if (player.getActivePokemon() != null) all.add(player.getActivePokemon());
        all.addAll(player.getBench());
        return all;
    }
}
