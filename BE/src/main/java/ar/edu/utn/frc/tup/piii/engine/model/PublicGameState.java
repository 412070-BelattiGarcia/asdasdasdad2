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

    public PublicGameState() {}
    public PublicGameState(UUID matchId, String status, String phase, int turnNumber, UUID currentPlayerId, UUID firstPlayerId, PublicPlayerState[] players) {
        this.matchId = matchId;
        this.status = status;
        this.phase = phase;
        this.turnNumber = turnNumber;
        this.currentPlayerId = currentPlayerId;
        this.firstPlayerId = firstPlayerId;
        this.players = players;
    }

    public UUID getMatchId() { return matchId; }
    public String getStatus() { return status; }
    public String getPhase() { return phase; }
    public int getTurnNumber() { return turnNumber; }
    public UUID getCurrentPlayerId() { return currentPlayerId; }
    public UUID getFirstPlayerId() { return firstPlayerId; }
    public PublicPlayerState[] getPlayers() { return players; }

    public void setMatchId(UUID matchId) { this.matchId = matchId; }
    public void setStatus(String status) { this.status = status; }
    public void setPhase(String phase) { this.phase = phase; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }
    public void setCurrentPlayerId(UUID currentPlayerId) { this.currentPlayerId = currentPlayerId; }
    public void setFirstPlayerId(UUID firstPlayerId) { this.firstPlayerId = firstPlayerId; }
    public void setPlayers(PublicPlayerState[] players) { this.players = players; }



    public static class PublicPlayerState {
        private UUID playerId;
        private String side;
        private PublicPokemonSlot activePokemon;
        private PublicPokemonSlot[] bench;
        private String[] prizes;

        public PublicPlayerState() {}
        public PublicPlayerState(UUID playerId, String side, PublicPokemonSlot activePokemon, PublicPokemonSlot[] bench, String[] prizes) {
            this.playerId = playerId;
            this.side = side;
            this.activePokemon = activePokemon;
            this.bench = bench;
            this.prizes = prizes;
        }

        public UUID getPlayerId() { return playerId; }
        public String getSide() { return side; }
        public PublicPokemonSlot getActivePokemon() { return activePokemon; }
        public PublicPokemonSlot[] getBench() { return bench; }
        public String[] getPrizes() { return prizes; }

        public void setPlayerId(UUID playerId) { this.playerId = playerId; }
        public void setSide(String side) { this.side = side; }
        public void setActivePokemon(PublicPokemonSlot activePokemon) { this.activePokemon = activePokemon; }
        public void setBench(PublicPokemonSlot[] bench) { this.bench = bench; }
        public void setPrizes(String[] prizes) { this.prizes = prizes; }
    }



    public static class PublicPokemonSlot {
        private String instanceId;
        private String cardId;
        private int damageCounters;
        private String[] specialConditions;
        private String[] attachedCards;

        public PublicPokemonSlot() {}
        public PublicPokemonSlot(String instanceId, String cardId, int damageCounters, String[] specialConditions, String[] attachedCards) {
            this.instanceId = instanceId;
            this.cardId = cardId;
            this.damageCounters = damageCounters;
            this.specialConditions = specialConditions;
            this.attachedCards = attachedCards;
        }

        public String getInstanceId() { return instanceId; }
        public String getCardId() { return cardId; }
        public int getDamageCounters() { return damageCounters; }
        public String[] getSpecialConditions() { return specialConditions; }
        public String[] getAttachedCards() { return attachedCards; }

        public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
        public void setCardId(String cardId) { this.cardId = cardId; }
        public void setDamageCounters(int damageCounters) { this.damageCounters = damageCounters; }
        public void setSpecialConditions(String[] specialConditions) { this.specialConditions = specialConditions; }
        public void setAttachedCards(String[] attachedCards) { this.attachedCards = attachedCards; }
    }
}
