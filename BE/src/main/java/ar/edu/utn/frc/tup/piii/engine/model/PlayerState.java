package ar.edu.utn.frc.tup.piii.engine.model;

import ar.edu.utn.frc.tup.piii.engine.PlayerSide;

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
}
