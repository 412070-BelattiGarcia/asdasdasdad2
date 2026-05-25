package ar.edu.utn.frc.tup.piii.engine.model;

import java.util.List;
import java.util.UUID;

public class PokemonInPlay {
    private UUID instanceId;
    private String cardId;
    private UUID ownerPlayerId;
    private int enteredTurnNumber;
    private boolean evolvedThisTurn;
    private int damageCounters;
    private List<String> specialConditions;
    private List<AttachedCard> attachedCards;
    private UUID toolCardInstanceId;

    public UUID getInstanceId() { return instanceId; }
    public String getCardId() { return cardId; }
    public UUID getOwnerPlayerId() { return ownerPlayerId; }
    public int getEnteredTurnNumber() { return enteredTurnNumber; }
    public boolean isEvolvedThisTurn() { return evolvedThisTurn; }
    public int getDamageCounters() { return damageCounters; }
    public List<String> getSpecialConditions() { return specialConditions; }
    public List<AttachedCard> getAttachedCards() { return attachedCards; }
}
