package ar.edu.utn.frc.tup.piii.engine.model;

import ar.edu.utn.frc.tup.piii.engine.PlayerSide;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerState {
    private UUID playerId;
    private PlayerSide side;
    private List<CardInstance> deck;
    private List<CardInstance> hand;
    private List<CardInstance> prizes;
    private List<CardInstance> discard;
    private PokemonInPlay activePokemon;
    private List<PokemonInPlay> bench;
    private int mulliganCount;
    private boolean setupConfirmed;
    private boolean initialMulliganResolved;
    private List<List<String>> mulliganRevealedCards;

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }

    public PlayerSide getSide() { return side; }
    public void setSide(PlayerSide side) { this.side = side; }

    public List<CardInstance> getDeck() { return deck; }
    public void setDeck(List<CardInstance> deck) { this.deck = deck; }

    public List<CardInstance> getHand() { return hand; }
    public void setHand(List<CardInstance> hand) { this.hand = hand; }

    public List<CardInstance> getPrizes() { return prizes; }
    public void setPrizes(List<CardInstance> prizes) { this.prizes = prizes; }

    public List<CardInstance> getDiscard() { return discard; }
    public void setDiscard(List<CardInstance> discard) { this.discard = discard; }

    public PokemonInPlay getActivePokemon() { return activePokemon; }
    public void setActivePokemon(PokemonInPlay activePokemon) { this.activePokemon = activePokemon; }

    public List<PokemonInPlay> getBench() { return bench; }
    public void setBench(List<PokemonInPlay> bench) { this.bench = bench; }

    public int getMulliganCount() { return mulliganCount; }
    public void setMulliganCount(int mulliganCount) { this.mulliganCount = mulliganCount; }

    public boolean isSetupConfirmed() { return setupConfirmed; }
    public void setSetupConfirmed(boolean setupConfirmed) { this.setupConfirmed = setupConfirmed; }

    public boolean isInitialMulliganResolved() { return initialMulliganResolved; }
    public void setInitialMulliganResolved(boolean initialMulliganResolved) { this.initialMulliganResolved = initialMulliganResolved; }

    public List<List<String>> getMulliganRevealedCards() { return mulliganRevealedCards; }
    public void setMulliganRevealedCards(List<List<String>> mulliganRevealedCards) { this.mulliganRevealedCards = mulliganRevealedCards; }

    public void addMulliganReveal(List<String> revealedCardIds) {
        if (this.mulliganRevealedCards == null) {
            this.mulliganRevealedCards = new ArrayList<>();
        }
        this.mulliganRevealedCards.add(revealedCardIds);
    }
}
