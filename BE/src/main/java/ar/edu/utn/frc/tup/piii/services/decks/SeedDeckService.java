package ar.edu.utn.frc.tup.piii.services.decks;

import ar.edu.utn.frc.tup.piii.dtos.decks.CreateDeckRequest;
import ar.edu.utn.frc.tup.piii.repositories.jpa.CardJpaRepository;
import ar.edu.utn.frc.tup.piii.repositories.jpa.DeckJpaRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile("dev")
@Slf4j
public class SeedDeckService {

    private final DeckService deckService;
    private final DeckJpaRepository deckJpaRepository;
    private final CardJpaRepository cardJpaRepository;

    public SeedDeckService(DeckService deckService, DeckJpaRepository deckJpaRepository, CardJpaRepository cardJpaRepository) {
        this.deckService = deckService;
        this.deckJpaRepository = deckJpaRepository;
        this.cardJpaRepository = cardJpaRepository;
    }

    @PostConstruct
    public void seedDecks() {
        if (deckJpaRepository.count() > 0) {
            return;
        }

        Set<String> existingIds = cardJpaRepository.findAll().stream()
                .map(c -> c.getId())
                .collect(Collectors.toSet());
        trySeedDeck("Fire", fireDeckCards(), existingIds, this::createFireDeck);
        trySeedDeck("Water", waterDeckCards(), existingIds, this::createWaterDeck);
    }

    private void trySeedDeck(String name, List<String> cardIds, Set<String> existingIds, Runnable creator) {
        List<String> missing = cardIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            log.warn("Skipping {} deck: cards not found in catalog: {}", name, missing);
            return;
        }
        creator.run();
    }

    private List<String> fireDeckCards() {
        return List.of(
                "xy1-10", "xy1-11", "xy1-12", "xy1-1", "xy1-133",
                "xy1-2", "xy1-3", "xy1-4", "xy1-5", "xy1-6",
                "xy1-7", "xy1-8", "xy1-9", "xy1-13", "xy1-14"
        );
    }

    private List<String> waterDeckCards() {
        return List.of(
                "xy1-15", "xy1-16", "xy1-17", "xy1-18", "xy1-134",
                "xy1-19", "xy1-20", "xy1-21", "xy1-22", "xy1-23",
                "xy1-24", "xy1-25", "xy1-26", "xy1-27", "xy1-28"
        );
    }

    private void createFireDeck() {
        CreateDeckRequest fireDeck = new CreateDeckRequest(
                "Seed Fire Deck",
                null,
                List.of(
                        new CreateDeckRequest.DeckCardRequest("xy1-10", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-11", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-12", 3),
                        new CreateDeckRequest.DeckCardRequest("xy1-1", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-133", 18),
                        new CreateDeckRequest.DeckCardRequest("xy1-2", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-3", 3),
                        new CreateDeckRequest.DeckCardRequest("xy1-4", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-5", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-6", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-7", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-8", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-9", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-13", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-14", 4)
                )
        );
        deckService.createDeck(fireDeck);
    }

    private void createWaterDeck() {
        CreateDeckRequest waterDeck = new CreateDeckRequest(
                "Seed Water Deck",
                null,
                List.of(
                        new CreateDeckRequest.DeckCardRequest("xy1-15", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-16", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-17", 3),
                        new CreateDeckRequest.DeckCardRequest("xy1-18", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-134", 18),
                        new CreateDeckRequest.DeckCardRequest("xy1-19", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-20", 3),
                        new CreateDeckRequest.DeckCardRequest("xy1-21", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-22", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-23", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-24", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-25", 2),
                        new CreateDeckRequest.DeckCardRequest("xy1-26", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-27", 4),
                        new CreateDeckRequest.DeckCardRequest("xy1-28", 2)
                )
        );
        deckService.createDeck(waterDeck);
    }
}

