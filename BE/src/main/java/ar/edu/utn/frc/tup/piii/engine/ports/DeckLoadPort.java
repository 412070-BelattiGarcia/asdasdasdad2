package ar.edu.utn.frc.tup.piii.engine.ports;

import ar.edu.utn.frc.tup.piii.decks.domain.Deck;

import java.util.UUID;

public interface DeckLoadPort {
    Deck loadDeck(UUID deckId);
}
