package ar.edu.utn.frc.tup.piii.engine.action;

import java.util.UUID;

public class AttachEnergyPayload implements GameActionPayload {
    private int handIndex;
    private UUID targetPokemonInstanceId;

    public AttachEnergyPayload() {}

    public AttachEnergyPayload(int handIndex, UUID targetPokemonInstanceId) {
        this.handIndex = handIndex;
        this.targetPokemonInstanceId = targetPokemonInstanceId;
    }

    public int getHandIndex() { return handIndex; }
    public void setHandIndex(int handIndex) { this.handIndex = handIndex; }

    public UUID getTargetPokemonInstanceId() { return targetPokemonInstanceId; }
    public void setTargetPokemonInstanceId(UUID targetPokemonInstanceId) { this.targetPokemonInstanceId = targetPokemonInstanceId; }
}
