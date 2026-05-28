package ar.edu.utn.frc.tup.piii.services.cards;

import ar.edu.utn.frc.tup.piii.clients.PokemonTcgApiClient;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.PokemonTcgApiCardRequest;
import ar.edu.utn.frc.tup.piii.mappers.cards.CardMapper;
import ar.edu.utn.frc.tup.piii.repositories.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.repositories.jpa.CardJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardCacheSyncService {

    private final PokemonTcgApiClient pokemonTcgApiClient;
    private final CardJpaRepository cardJpaRepository;
    private final CardMapper cardMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void synchronizeAllCards() {
        log.info("Starting synchronization of Pokemon TCG cards from XY1 set");
        try {
            syncCards(pokemonTcgApiClient.fetchPokemonCards(), "Pokemon");
            syncCards(pokemonTcgApiClient.fetchTrainerCards(), "Trainer");
            syncCards(pokemonTcgApiClient.fetchEnergyCards(), "Energy");
            log.info("Synchronization of Pokemon TCG cards completed successfully");
        } catch (Exception e) {
            log.error("Failed to synchronize cards: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void syncAll() {
        synchronizeAllCards();
    }

    private void syncCards(List<PokemonTcgApiCardRequest> dtos, String typeName) {
        log.info("Processing {} {} cards", dtos.size(), typeName);
        int successCount = 0;
        int failureCount = 0;
        for (PokemonTcgApiCardRequest dto : dtos) {
            try {
                CardEntity entity = cardMapper.toCardEntity(dto);
                if (cardJpaRepository.existsById(entity.getId())) {
                    CardEntity existing = cardJpaRepository.findById(entity.getId()).orElseThrow();
                    updateEntity(existing, entity);
                    cardJpaRepository.save(existing);
                } else {
                    cardJpaRepository.save(entity);
                }
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Error processing {} card with id {}: {}", typeName, dto.getId(), e.getMessage(), e);
            }
        }
        log.info("Processed {} {} cards: {} successful, {} failed", dtos.size(), typeName, successCount, failureCount);
    }

    private void updateEntity(CardEntity existing, CardEntity updated) {
        existing.setName(updated.getName());
        existing.setSupertype(updated.getSupertype());
        existing.setSubtypes(updated.getSubtypes());
        existing.setSetCode(updated.getSetCode());
        existing.setNumber(updated.getNumber());
        existing.setRarity(updated.getRarity());
        existing.setImageSmallUrl(updated.getImageSmallUrl());
        existing.setImageLargeUrl(updated.getImageLargeUrl());
        existing.setHp(updated.getHp());
        existing.setPokemonStage(updated.getPokemonStage());
        existing.setEvolvesFrom(updated.getEvolvesFrom());
        existing.setPokemonTypes(updated.getPokemonTypes());
        existing.setRetreatCost(updated.getRetreatCost());
        existing.setConvertedRetreatCost(updated.getConvertedRetreatCost());
        existing.setIsEx(updated.getIsEx());
        existing.setIsMega(updated.getIsMega());
        existing.setRulesText(updated.getRulesText());
        existing.setAttacks(updated.getAttacks());
        existing.getAttacks().forEach(a -> a.setCard(existing));
        existing.setWeaknesses(updated.getWeaknesses());
        existing.getWeaknesses().forEach(w -> w.setCard(existing));
        existing.setResistances(updated.getResistances());
        existing.getResistances().forEach(r -> r.setCard(existing));
    }
}
