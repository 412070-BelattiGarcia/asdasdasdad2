package ar.edu.utn.frc.tup.piii.engine.model;

import java.util.UUID;

public class PublicGameState {
    private UUID matchId;
    private String status;
    private String phase;
    private int turnNumber;
    private UUID currentPlayerId;
    private UUID firstPlayerId;
    private PublicPlayerState[] players;

    public UUID getMatchId() { return matchId; }
    public String getStatus() { return status; }
    public String getPhase() { return phase; }
    public int getTurnNumber() { return turnNumber; }
    public UUID getCurrentPlayerId() { return currentPlayerId; }
    public PublicPlayerState[] getPlayers() { return players; }

    public static class PublicPlayerState {
        private UUID playerId;
        private String side;
        private PublicPokemonSlot activePokemon;
        private PublicPokemonSlot[] bench;
        private String[] prizes;
    }

    public static class PublicPokemonSlot {
        private String instanceId;
        private String cardId;
        private int damageCounters;
        private String[] specialConditions;
        private String[] attachedCards;
    }
}
