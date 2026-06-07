package ar.edu.utn.frc.tup.piii.mappers.cards;

import ar.edu.utn.frc.tup.piii.dtos.cards.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.cards.PokemonTcgApiCardDto;
import ar.edu.utn.frc.tup.piii.dtos.cards.ResistanceDto;
import ar.edu.utn.frc.tup.piii.dtos.cards.WeaknessDto;
import ar.edu.utn.frc.tup.piii.dtos.cards.CardDetailResponse;
import ar.edu.utn.frc.tup.piii.dtos.cards.CardSummaryResponse;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardAttackEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardResistanceEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardWeaknessEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CardMapper {

    public CardEntity toCardEntity(PokemonTcgApiCardDto request) {
        CardEntity entity = new CardEntity();
        entity.setId(request.id());
        entity.setName(request.name());
        entity.setSupertype(normalizeSupertype(request.supertype()));
        entity.setSubtypes(listToCommaString(request.subtypes()));
        entity.setSetCode(request.set() != null ? request.set().id() : null);
        if (request.id() != null && request.id().contains("-")) {
            entity.setNumber(request.id().substring(request.id().indexOf("-") + 1));
        }
        entity.setRarity(request.rarity());
        entity.setImageSmallUrl(request.images() != null ? request.images().small() : null);
        entity.setImageLargeUrl(request.images() != null ? request.images().large() : null);
        entity.setHp(request.hp() != null ? Integer.parseInt(request.hp()) : null);
        entity.setPokemonStage(determineStage(request.subtypes()));
        entity.setEvolvesFrom(request.evolvesFrom());
        entity.setPokemonTypes(listToCommaString(request.types()));
        entity.setRetreatCost(listToCommaString(request.retreatCost()));
        entity.setConvertedRetreatCost(request.convertedRetreatCost());
        entity.setIsEx(request.subtypes() != null && request.subtypes().stream().anyMatch(s -> s.equalsIgnoreCase("EX")));
        entity.setIsMega(request.subtypes() != null && request.subtypes().stream().anyMatch(s -> s.equalsIgnoreCase("MEGA")));
        entity.setRulesText(request.rules() != null ? String.join("|", request.rules()) : null);

        entity.setEvolvesTo(listToCommaString(request.evolvesTo()));

        if (request.abilities() != null && !request.abilities().isEmpty()) {
            try {
                entity.setAbilities(new ObjectMapper().writeValueAsString(request.abilities()));
            } catch (JsonProcessingException e) {
                entity.setAbilities(null);
            }
        }

        if ("Energy".equalsIgnoreCase(request.supertype())) {
            boolean isBasic = request.subtypes() != null && request.subtypes().stream().anyMatch(s -> s.equalsIgnoreCase("Basic"));
            entity.setEnergyCardType(isBasic ? "BASIC" : "SPECIAL");

            if (request.rules() != null && !request.rules().isEmpty()) {
                entity.setProvidesEnergyTypes(String.join(",", request.rules()));
            } else if (request.name() != null) {
                String energyName = request.name().replace(" Energy", "").toUpperCase();
                entity.setProvidesEnergyTypes(energyName);
            }
        }

        if ("Trainer".equalsIgnoreCase(request.supertype())) {
            String subtype = request.subtypes() != null && !request.subtypes().isEmpty()
                    ? request.subtypes().get(0)
                    : null;
            if (subtype != null) {
                if (subtype.equalsIgnoreCase("SUPPORTER")) {
                    entity.setTrainerSubtype("SUPPORTER");
                } else if (subtype.equalsIgnoreCase("STADIUM")) {
                    entity.setTrainerSubtype("STADIUM");
                } else if (subtype.equalsIgnoreCase("ITEM")) {
                    entity.setTrainerSubtype("ITEM");
                } else if (subtype.equalsIgnoreCase("TOOL")) {
                    entity.setTrainerSubtype("TOOL");
                } else {
                    entity.setTrainerSubtype(subtype.toUpperCase());
                }
            }
            entity.setIsAceSpec(request.subtypes() != null && request.subtypes().stream().anyMatch(s -> s.equalsIgnoreCase("ACE SPEC")));
        }

        if (request.attacks() != null) {
            List<CardAttackEntity> attacks = new ArrayList<>();
            for (int i = 0; i < request.attacks().size(); i++) {
                attacks.add(toAttackEntity(request.attacks().get(i), entity, i));
            }
            entity.setAttacks(attacks);
        }

        if (request.weaknesses() != null) {
            entity.setWeaknesses(request.weaknesses().stream()
                    .map(w -> toWeaknessEntity(w, entity))
                    .collect(Collectors.toList()));
        }

        if (request.resistances() != null) {
            entity.setResistances(request.resistances().stream()
                    .map(r -> toResistanceEntity(r, entity))
                    .collect(Collectors.toList()));
        }

        return entity;
    }

    public CardSummaryResponse toSummaryResponse(CardEntity entity) {
        return new CardSummaryResponse(
                entity.getId(),
                entity.getName(),
                entity.getSupertype(),
                entity.getSetCode(),
                entity.getNumber(),
                entity.getImageSmallUrl()
        );
    }

    public CardDetailResponse toDetailResponse(CardEntity entity) {
        List<String> subtypes = commaStringToList(entity.getSubtypes());
        List<String> rulesText = entity.getRulesText() != null
                ? Arrays.asList(entity.getRulesText().split("\\|"))
                : List.of();
        List<String> types = commaStringToList(entity.getPokemonTypes());
        List<String> retreatCost = commaStringToList(entity.getRetreatCost());

        List<AttackDto> attacks = entity.getAttacks() != null
                ? entity.getAttacks().stream().map(this::toAttackDto).collect(Collectors.toList())
                : List.of();

        List<WeaknessDto> weaknesses = entity.getWeaknesses() != null
                ? entity.getWeaknesses().stream().map(this::toWeaknessDto).collect(Collectors.toList())
                : List.of();

        List<ResistanceDto> resistances = entity.getResistances() != null
                ? entity.getResistances().stream().map(this::toResistanceDto).collect(Collectors.toList())
                : List.of();

        return new CardDetailResponse(
                entity.getId(),
                entity.getName(),
                entity.getSupertype(),
                subtypes,
                entity.getSetCode(),
                entity.getNumber(),
                entity.getImageSmallUrl(),
                entity.getImageLargeUrl(),
                rulesText,
                entity.getHp(),
                entity.getPokemonStage(),
                entity.getEvolvesFrom(),
                types,
                attacks,
                weaknesses,
                resistances,
                retreatCost,
                entity.getIsEx(),
                entity.getIsMega()
        );
    }

    private CardAttackEntity toAttackEntity(AttackDto dto, CardEntity card, int index) {
        CardAttackEntity entity = new CardAttackEntity();
        entity.setCard(card);
        entity.setAttackIndex(index);
        entity.setName(dto.name());
        entity.setPrintedCost(listToCommaString(dto.cost()));
        entity.setConvertedEnergyCost(dto.convertedEnergyCost() != null ? dto.convertedEnergyCost() : 0);
        entity.setDamageText(dto.damage());
        entity.setEffectText(dto.text());
        entity.setBaseDamage(dto.baseDamage());
        return entity;
    }

    private CardWeaknessEntity toWeaknessEntity(WeaknessDto dto, CardEntity card) {
        CardWeaknessEntity entity = new CardWeaknessEntity();
        entity.setCard(card);
        entity.setEnergyType(dto.type());
        try {
            entity.setMultiplier(dto.value() != null ? Integer.parseInt(dto.value()) : 2);
        } catch (NumberFormatException e) {
            entity.setMultiplier(2);
        }
        return entity;
    }

    private CardResistanceEntity toResistanceEntity(ResistanceDto dto, CardEntity card) {
        CardResistanceEntity entity = new CardResistanceEntity();
        entity.setCard(card);
        entity.setEnergyType(dto.type());
        try {
            entity.setValue(dto.value() != null ? Integer.parseInt(dto.value()) : -20);
        } catch (NumberFormatException e) {
            entity.setValue(-20);
        }
        return entity;
    }

    private AttackDto toAttackDto(CardAttackEntity entity) {
        return new AttackDto(
                entity.getAttackIndex() != null ? entity.getAttackIndex() : 0,
                entity.getName(),
                commaStringToList(entity.getPrintedCost()),
                entity.getConvertedEnergyCost(),
                entity.getDamageText(),
                entity.getEffectText(),
                entity.getBaseDamage()
        );
    }

    private WeaknessDto toWeaknessDto(CardWeaknessEntity entity) {
        return new WeaknessDto(
                entity.getEnergyType(),
                entity.getMultiplier() != null ? String.valueOf(entity.getMultiplier()) : null
        );
    }

    private ResistanceDto toResistanceDto(CardResistanceEntity entity) {
        return new ResistanceDto(
                entity.getEnergyType(),
                entity.getValue() != null ? String.valueOf(entity.getValue()) : null
        );
    }

    private String listToCommaString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private List<String> commaStringToList(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String normalizeSupertype(String supertype) {
        if (supertype == null) return null;
        return supertype.replace('é', 'e').replace('É', 'E').toUpperCase();
    }

    private String determineStage(List<String> subtypes) {
        if (subtypes == null) return null;
        if (subtypes.contains("MEGA")) return "MEGA";
        if (subtypes.contains("STAGE 2")) return "STAGE_2";
        if (subtypes.contains("STAGE 1")) return "STAGE_1";
        if (subtypes.contains("BASIC")) return "BASIC";
        return null;
    }
}
