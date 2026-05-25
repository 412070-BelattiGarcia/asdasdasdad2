package ar.edu.utn.frc.tup.piii.engine.model;

import java.util.List;
import java.util.UUID;

public class BoardState {
    private PokemonInPlay activePokemon;
    private List<PokemonInPlay> bench;
    private List<UUID> discard;
    private List<UUID> deck;

    public PokemonInPlay getActivePokemon() { return activePokemon; }
    public List<PokemonInPlay> getBench() { return bench; }
    public List<UUID> getDiscard() { return discard; }
    public List<UUID> getDeck() { return deck; }
}
