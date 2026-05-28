package ar.edu.utn.frc.tup.piii.engine.model;

import ar.edu.utn.frc.tup.piii.engine.SpecialCondition;

import java.util.List;
import java.util.UUID;

public class PokemonInPlay {
    private UUID instanceId;
    private String cardDefinitionId;
    private UUID ownerPlayerId;
    private int enteredTurnNumber;
    private boolean evolvedThisTurn;
    private int damageCounters;
    private List<SpecialCondition> specialConditions;
    private List<CardInstance> attachedEnergies;
    private UUID toolCardInstanceId;

    public UUID getInstanceId() { return instanceId; }
    public void setInstanceId(UUID instanceId) { this.instanceId = instanceId; }

    public String getCardDefinitionId() { return cardDefinitionId; }
    public void setCardDefinitionId(String cardDefinitionId) { this.cardDefinitionId = cardDefinitionId; }

    public UUID getOwnerPlayerId() { return ownerPlayerId; }
    public void setOwnerPlayerId(UUID ownerPlayerId) { this.ownerPlayerId = ownerPlayerId; }

    public int getEnteredTurnNumber() { return enteredTurnNumber; }
    public void setEnteredTurnNumber(int enteredTurnNumber) { this.enteredTurnNumber = enteredTurnNumber; }

    public boolean isEvolvedThisTurn() { return evolvedThisTurn; }
    public void setEvolvedThisTurn(boolean evolvedThisTurn) { this.evolvedThisTurn = evolvedThisTurn; }

    public int getDamageCounters() { return damageCounters; }
    public void setDamageCounters(int damageCounters) { this.damageCounters = damageCounters; }

    public List<SpecialCondition> getSpecialConditions() { return specialConditions; }
    public void setSpecialConditions(List<SpecialCondition> specialConditions) { this.specialConditions = specialConditions; }

    public List<CardInstance> getAttachedEnergies() { return attachedEnergies; }
    public void setAttachedEnergies(List<CardInstance> attachedEnergies) { this.attachedEnergies = attachedEnergies; }

    public UUID getToolCardInstanceId() { return toolCardInstanceId; }
    public void setToolCardInstanceId(UUID toolCardInstanceId) { this.toolCardInstanceId = toolCardInstanceId; }
}
