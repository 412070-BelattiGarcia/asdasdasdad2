package ar.edu.utn.frc.tup.piii.engine.ports.impl;

import ar.edu.utn.frc.tup.piii.cards.domain.*;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardAttackEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardResistanceEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardWeaknessEntity;
import ar.edu.utn.frc.tup.piii.repositories.jpa.CardJpaRepository;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CardLookupAdapter implements CardLookupPort {

    private final CardJpaRepository cardJpaRepository;

    public CardLookupAdapter(CardJpaRepository cardJpaRepository) {
        this.cardJpaRepository = cardJpaRepository;
    }

    @Override
    @Cacheable(value = "cards", key = "#cardId")
    public CardDefinition getCardById(String cardId) {
        Optional<CardEntity> optional = cardJpaRepository.findById(cardId);
        if (optional.isEmpty()) {
            return null;
        }
        CardEntity entity = optional.get();
        return switch (entity.getSupertype()) {
            case "Pokémon", "Pokemon" -> toPokemon(entity);
            case "Trainer" -> toTrainer(entity);
            case "Energy" -> toEnergy(entity);
            default -> toPokemon(entity);
        };
    }

    private PokemonCardDefinition toPokemon(CardEntity e) {
        PokemonCardDefinition d = new PokemonCardDefinition();
        mapBase(e, d);
        d.setHp(e.getHp() != null ? e.getHp() : 0);
        d.setStage(e.getPokemonStage());
        d.setEvolvesFrom(e.getEvolvesFrom());
        d.setTypes(splitList(e.getPokemonTypes()));
        d.setRetreatCost(splitList(e.getRetreatCost()));
        d.setEx(e.getIsEx() != null && e.getIsEx());
        d.setMega(e.getIsMega() != null && e.getIsMega());
        if (e.getAttacks() != null) {
            d.setAttacks(e.getAttacks().stream()
                    .map(this::toAttack)
                    .collect(Collectors.toList()));
        }
        if (e.getWeaknesses() != null) {
            d.setWeaknesses(e.getWeaknesses().stream()
                    .map(this::toWeakness)
                    .collect(Collectors.toList()));
        }
        if (e.getResistances() != null) {
            d.setResistances(e.getResistances().stream()
                    .map(this::toResistance)
                    .collect(Collectors.toList()));
        }
        return d;
    }

    private TrainerCardDefinition toTrainer(CardEntity e) {
        TrainerCardDefinition d = new TrainerCardDefinition();
        mapBase(e, d);
        d.setTrainerSubtype(e.getTrainerSubtype());
        d.setAceSpec(e.getIsAceSpec() != null && e.getIsAceSpec());
        return d;
    }

    private EnergyCardDefinition toEnergy(CardEntity e) {
        EnergyCardDefinition d = new EnergyCardDefinition();
        mapBase(e, d);
        d.setEnergyCardType(e.getEnergyCardType());
        d.setProvides(splitList(e.getProvidesEnergyTypes()));
        return d;
    }

    private void mapBase(CardEntity e, CardDefinition d) {
        d.setId(e.getId());
        d.setName(e.getName());
        d.setSupertype(e.getSupertype());
        d.setSubtypes(splitList(e.getSubtypes()));
        d.setSetCode(e.getSetCode());
        d.setNumber(e.getNumber());
        d.setImageSmallUrl(e.getImageSmallUrl());
        d.setImageLargeUrl(e.getImageLargeUrl());
        d.setRulesText(splitList(e.getRulesText()));
    }

    private PokemonCardDefinition.AttackDefinition toAttack(CardAttackEntity a) {
        PokemonCardDefinition.AttackDefinition ad = new PokemonCardDefinition.AttackDefinition();
        try {
            ad.setIndex(a.getAttackIndex() != null ? a.getAttackIndex() : 0);
        } catch (Exception ex) {
            ad.setIndex(0);
        }
        ad.setName(a.getName());
        ad.setCost(splitList(a.getPrintedCost()));
        ad.setDamage(a.getDamageText());
        ad.setText(a.getEffectText());
        return ad;
    }

    private PokemonCardDefinition.WeaknessDefinition toWeakness(CardWeaknessEntity w) {
        PokemonCardDefinition.WeaknessDefinition wd = new PokemonCardDefinition.WeaknessDefinition();
        wd.setType(w.getEnergyType());
        wd.setValue(w.getMultiplier() != null ? String.valueOf(w.getMultiplier()) : null);
        return wd;
    }

    private PokemonCardDefinition.ResistanceDefinition toResistance(CardResistanceEntity r) {
        PokemonCardDefinition.ResistanceDefinition rd = new PokemonCardDefinition.ResistanceDefinition();
        rd.setType(r.getEnergyType());
        rd.setValue(r.getValue() != null ? String.valueOf(r.getValue()) : null);
        return rd;
    }

    private List<String> splitList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
