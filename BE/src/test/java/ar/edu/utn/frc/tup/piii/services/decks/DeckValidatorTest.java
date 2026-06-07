package ar.edu.utn.frc.tup.piii.services.decks;

import ar.edu.utn.frc.tup.piii.cards.domain.PokemonCardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.decks.domain.DeckCard;
import ar.edu.utn.frc.tup.piii.decks.domain.DeckValidationError;
import ar.edu.utn.frc.tup.piii.decks.domain.DeckValidationResult;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeckValidatorTest {

    private CardLookupPort cardLookupPort;
    private DeckValidator deckValidator;

    @BeforeEach
    void setUp() {
        cardLookupPort = mock(CardLookupPort.class);
        deckValidator = new DeckValidator(cardLookupPort);
    }

    @Test
    void shouldReturnValidForValidDeck() {
        String[] cardIds = new String[15];
        for (int i = 0; i < 15; i++) {
            cardIds[i] = "xy1-" + (i + 1);
            PokemonCardDefinition basic = new PokemonCardDefinition();
            basic.setStage("BASIC");
            when(cardLookupPort.getCardById(cardIds[i])).thenReturn(basic);
        }
        List<DeckCard> cards = java.util.Arrays.stream(cardIds)
                .map(id -> deckCard(id, 4))
                .collect(java.util.stream.Collectors.toList());
        DeckValidationResult result = deckValidator.validate(cards);

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void shouldFailWhenLessThan60Cards() {
        List<DeckCard> cards = List.of(deckCard("xy1-1", 30));
        DeckValidationResult result = deckValidator.validate(cards);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains(DeckValidationError.DECK_SIZE_INVALID));
    }

    @Test
    void shouldFailWhenMoreThan60Cards() {
        List<DeckCard> cards = List.of(deckCard("xy1-1", 61));
        DeckValidationResult result = deckValidator.validate(cards);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains(DeckValidationError.DECK_SIZE_INVALID));
    }

    @Test
    void shouldFailWhenMoreThan4Copies() {
        List<DeckCard> cards = List.of(
                deckCard("xy1-1", 5),
                deckCard("xy1-2", 55)
        );
        PokemonCardDefinition basic = new PokemonCardDefinition();
        basic.setStage("BASIC");
        when(cardLookupPort.getCardById("xy1-2")).thenReturn(basic);

        DeckValidationResult result = deckValidator.validate(cards);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains(DeckValidationError.MORE_THAN_4_COPIES));
    }

    @Test
    void shouldFailWhenNoBasicPokemon() {
        PokemonCardDefinition stage1 = new PokemonCardDefinition();
        stage1.setStage("STAGE_1");
        when(cardLookupPort.getCardById("xy1-1")).thenReturn(stage1);

        List<DeckCard> cards = List.of(deckCard("xy1-1", 60));
        DeckValidationResult result = deckValidator.validate(cards);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains(DeckValidationError.MISSING_BASIC_POKEMON));
    }

    private DeckCard deckCard(String cardId, int quantity) {
        DeckCard card = new DeckCard();
        try {
            var idField = card.getClass().getDeclaredField("cardId");
            idField.setAccessible(true);
            idField.set(card, cardId);
            var qtyField = card.getClass().getDeclaredField("quantity");
            qtyField.setAccessible(true);
            qtyField.set(card, quantity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return card;
    }
}
