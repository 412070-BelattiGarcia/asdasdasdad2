package ar.edu.utn.frc.tup.piii.decks.domain;

import java.util.UUID;

public class DeckCard {
    private UUID id;
    private UUID deckId;
    private String cardId;
    private int quantity;

    public UUID getId() { return id; }
    public UUID getDeckId() { return deckId; }
    public String getCardId() { return cardId; }
    public int getQuantity() { return quantity; }
}
