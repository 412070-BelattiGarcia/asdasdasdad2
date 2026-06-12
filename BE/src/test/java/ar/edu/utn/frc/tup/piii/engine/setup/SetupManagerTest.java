package ar.edu.utn.frc.tup.piii.engine.setup;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import ar.edu.utn.frc.tup.piii.engine.ports.DeckLoadPort;
import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import ar.edu.utn.frc.tup.piii.engine.ports.RandomizerPort;
import ar.edu.utn.frc.tup.piii.decks.domain.Deck;
import ar.edu.utn.frc.tup.piii.decks.domain.DeckCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetupManagerTest {

    @Mock private DeckLoadPort deckLoadPort;
    @Mock private CardLookupPort cardLookupPort;
    @Mock private RandomizerPort randomizerPort;
    @Mock private EventPublisherPort eventPublisher;

    private SetupManager setupManager;
    private UUID matchId;
    private UUID p1Id;
    private UUID p2Id;
    private UUID deck1Id;
    private UUID deck2Id;

    @BeforeEach
    void setUp() {
        setupManager = new SetupManager(deckLoadPort, cardLookupPort, randomizerPort, eventPublisher);
        matchId = UUID.randomUUID();
        p1Id = UUID.randomUUID();
        p2Id = UUID.randomUUID();
        deck1Id = UUID.randomUUID();
        deck2Id = UUID.randomUUID();
    }

    private Deck createDeck(int cardCount) {
        Deck deck = new Deck();
        DeckCard dc = new DeckCard();
        try {
            var cardIdField = DeckCard.class.getDeclaredField("cardId");
            cardIdField.setAccessible(true);
            cardIdField.set(dc, "basic-1");
            var qtyField = DeckCard.class.getDeclaredField("quantity");
            qtyField.setAccessible(true);
            qtyField.set(dc, cardCount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        deck.setCards(List.of(dc));
        return deck;
    }

    @Test
    void shouldSetupWithCustomPrizeCount() {
        Deck deck1 = createDeck(7); // 7 cards: 1 hand + 6 bench... actually we need enough for setup
        Deck deck2 = createDeck(7);

        // Need enough cards: hand(7) + active + bench(5) + prizes
        // For 6 prizes: 7 + 1 + 5 + 6 = 19 cards minimum
        // For 1 prize: 7 + 1 + 5 + 1 = 14 cards minimum
        // Let's use a bigger deck
        Deck bigDeck = new Deck();
        DeckCard bigDc = new DeckCard();
        try {
            var cardIdField = DeckCard.class.getDeclaredField("cardId");
            cardIdField.setAccessible(true);
            cardIdField.set(bigDc, "basic-1");
            var qtyField = DeckCard.class.getDeclaredField("quantity");
            qtyField.setAccessible(true);
            qtyField.set(bigDc, 20);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        bigDeck.setCards(List.of(bigDc));

        when(deckLoadPort.loadDeck(deck1Id)).thenReturn(bigDeck);
        when(deckLoadPort.loadDeck(deck2Id)).thenReturn(bigDeck);

        CardDefinition basicDef = new ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition();
        basicDef.setName("Basic Pokemon");
        ((ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition) basicDef).setStage("BASIC");
        when(cardLookupPort.getCardById("basic-1")).thenReturn(basicDef);

        var state = setupManager.setup(matchId, p1Id, p2Id, deck1Id, deck2Id, 1);

        assertNotNull(state);
        assertTrue(state.isSuddenDeath());
        assertEquals(1, state.getPrizeCountPerPlayer());
        assertEquals(1, state.getPlayers()[0].getPrizes().size());
        assertEquals(1, state.getPlayers()[1].getPrizes().size());
        assertEquals(matchId, state.getMatchId());
    }

    @Test
    void shouldSetupWithDefault6Prizes() {
        Deck bigDeck = new Deck();
        DeckCard bigDc = new DeckCard();
        try {
            var cardIdField = DeckCard.class.getDeclaredField("cardId");
            cardIdField.setAccessible(true);
            cardIdField.set(bigDc, "basic-1");
            var qtyField = DeckCard.class.getDeclaredField("quantity");
            qtyField.setAccessible(true);
            qtyField.set(bigDc, 20);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        bigDeck.setCards(List.of(bigDc));

        when(deckLoadPort.loadDeck(deck1Id)).thenReturn(bigDeck);
        when(deckLoadPort.loadDeck(deck2Id)).thenReturn(bigDeck);

        CardDefinition basicDef = new ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition();
        basicDef.setName("Basic Pokemon");
        ((ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition) basicDef).setStage("BASIC");
        when(cardLookupPort.getCardById("basic-1")).thenReturn(basicDef);

        var state = setupManager.setup(matchId, p1Id, p2Id, deck1Id, deck2Id);

        assertNotNull(state);
        assertFalse(state.isSuddenDeath());
        assertEquals(6, state.getPrizeCountPerPlayer());
        assertEquals(6, state.getPlayers()[0].getPrizes().size());
        assertEquals(6, state.getPlayers()[1].getPrizes().size());
    }
}
