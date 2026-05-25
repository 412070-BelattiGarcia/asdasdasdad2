package ar.edu.utn.frc.tup.piii.engine.model;

import java.util.List;
import java.util.UUID;

public class PrivatePlayerState {
    private UUID playerId;
    private List<PrivateHandCard> hand;
    private int deckCount;
    private int discardCount;
    private List<PrizeSlot> prizes;

    public UUID getPlayerId() { return playerId; }
    public List<PrivateHandCard> getHand() { return hand; }
    public int getDeckCount() { return deckCount; }
    public int getDiscardCount() { return discardCount; }
    public List<PrizeSlot> getPrizes() { return prizes; }

    public static class PrivateHandCard {
        private String instanceId;
        private String cardId;
        private String name;
        private String supertype;
    }

    public static class PrizeSlot {
        private int slot;
        private boolean known;
        private String cardId;
    }
}
