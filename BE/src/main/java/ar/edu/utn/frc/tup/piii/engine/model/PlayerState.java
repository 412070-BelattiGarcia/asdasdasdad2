package ar.edu.utn.frc.tup.piii.engine.model;

import java.util.List;
import java.util.UUID;

public class PlayerState {
    private UUID playerId;
    private String side;
    private List<UUID> deck;
    private List<UUID> hand;
    private List<UUID> prizes;
    private List<UUID> discard;
    private PokemonInPlay activePokemon;
    private List<PokemonInPlay> bench;
    private int mulliganCount;

    public UUID getPlayerId() { return playerId; }
    public String getSide() { return side; }
    public List<UUID> getDeck() { return deck; }
    public List<UUID> getHand() { return hand; }
    public List<UUID> getPrizes() { return prizes; }
    public List<UUID> getDiscard() { return discard; }
    public PokemonInPlay getActivePokemon() { return activePokemon; }
    public List<PokemonInPlay> getBench() { return bench; }
    public int getMulliganCount() { return mulliganCount; }
}
