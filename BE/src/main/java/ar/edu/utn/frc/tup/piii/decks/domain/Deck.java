package ar.edu.utn.frc.tup.piii.decks.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Deck {
    private UUID id;
    private String name;
    private UUID ownerPlayerId;
    private String source;
    private Instant createdAt;
    private Instant updatedAt;
    private List<DeckCard> cards;

    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getOwnerPlayerId() { return ownerPlayerId; }
    public String getSource() { return source; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<DeckCard> getCards() { return cards; }
}
