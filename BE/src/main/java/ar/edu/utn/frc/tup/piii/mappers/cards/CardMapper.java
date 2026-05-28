package ar.edu.utn.frc.tup.piii.mappers.cards;

import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.AttackRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.PokemonTcgApiCardRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.ResistanceRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.WeaknessRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.CardDetailResponse;
import ar.edu.utn.frc.tup.piii.dtos.cards.CardDetailResponse.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.cards.CardDetailResponse.WeaknessDto;
import ar.edu.utn.frc.tup.piii.dtos.cards.CardDetailResponse.ResistanceDto;
import ar.edu.utn.frc.tup.piii.dtos.cards.CardSummaryResponse;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardAttackEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardResistanceEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardWeaknessEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CardMapper {

    public CardEntity toCardEntity(PokemonTcgApiCardRequest request) {
        CardEntity entity = new CardEntity();
        entity.setId(request.getId());
        entity.setName(request.getName());
        entity.setSupertype(request.getSupertype());
        entity.setSubtypes(listToCommaString(request.getSubtypes()));
        entity.setSetCode(request.getSet() != null ? request.getSet().getId() : null);
        entity.setNumber(null);
        entity.setRarity(request.getRarity());
        entity.setImageSmallUrl(request.getImages() != null ? request.getImages().getSmall() : null);
        entity.setImageLargeUrl(request.getImages() != null ? request.getImages().getLarge() : null);
        entity.setHp(request.getHp());
        entity.setPokemonStage(determineStage(request.getSubtypes()));
        entity.setEvolvesFrom(request.getEvolvesFrom());
        entity.setPokemonTypes(listToCommaString(request.getTypes()));
        entity.setRetreatCost(listToCommaString(request.getRetreatCost()));
        entity.setConvertedRetreatCost(request.getConvertedRetreatCost());
        entity.setIsEx(request.getSubtypes() != null && request.getSubtypes().stream().anyMatch(s -> s.equalsIgnoreCase("EX")));
        entity.setIsMega(request.getSubtypes() != null && request.getSubtypes().stream().anyMatch(s -> s.equalsIgnoreCase("MEGA")));
        entity.setRulesText(request.getRules() != null ? String.join("|", request.getRules()) : null);

        if (request.getAttacks() != null) {
            List<CardAttackEntity> attacks = new ArrayList<>();
            for (int i = 0; i < request.getAttacks().size(); i++) {
                attacks.add(toAttackEntity(request.getAttacks().get(i), entity, i));
            }
            entity.setAttacks(attacks);
        }

        if (request.getWeakness() != null) {
            entity.setWeaknesses(request.getWeakness().stream()
                    .map(w -> toWeaknessEntity(w, entity))
                    .collect(Collectors.toList()));
        }

        if (request.getResistance() != null) {
            entity.setResistances(request.getResistance().stream()
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

    private CardAttackEntity toAttackEntity(AttackRequest request, CardEntity card, int index) {
        CardAttackEntity entity = new CardAttackEntity();
        entity.setCard(card);
        entity.setAttackIndex(index);
        entity.setName(request.getName());
        entity.setPrintedCost(listToCommaString(request.getCost()));
        entity.setConvertedEnergyCost(request.getConvertedEnergyCost() != null ? request.getConvertedEnergyCost() : 0);
        entity.setDamageText(request.getDamage());
        entity.setEffectText(request.getText());
        return entity;
    }

    private CardWeaknessEntity toWeaknessEntity(WeaknessRequest request, CardEntity card) {
        CardWeaknessEntity entity = new CardWeaknessEntity();
        entity.setCard(card);
        entity.setEnergyType(request.getType());
        try {
            entity.setMultiplier(request.getValue() != null ? Integer.parseInt(request.getValue()) : 2);
        } catch (NumberFormatException e) {
            entity.setMultiplier(2);
        }
        return entity;
    }

    private CardResistanceEntity toResistanceEntity(ResistanceRequest request, CardEntity card) {
        CardResistanceEntity entity = new CardResistanceEntity();
        entity.setCard(card);
        entity.setEnergyType(request.getType());
        try {
            entity.setValue(request.getValue() != null ? Integer.parseInt(request.getValue()) : -20);
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
                entity.getDamageText(),
                entity.getEffectText()
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

    private String determineStage(List<String> subtypes) {
        if (subtypes == null) return null;
        if (subtypes.contains("MEGA")) return "MEGA";
        if (subtypes.contains("STAGE 2")) return "STAGE_2";
        if (subtypes.contains("STAGE 1")) return "STAGE_1";
        if (subtypes.contains("BASIC")) return "BASIC";
        return null;
    }
}
