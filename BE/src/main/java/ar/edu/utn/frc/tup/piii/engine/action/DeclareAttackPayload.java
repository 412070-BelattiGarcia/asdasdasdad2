package ar.edu.utn.frc.tup.piii.engine.action;

import java.util.UUID;

public class DeclareAttackPayload implements GameActionPayload {
    private int attackIndex;
    private UUID targetPokemonInstanceId;

    public DeclareAttackPayload() {}

    public DeclareAttackPayload(int attackIndex, UUID targetPokemonInstanceId) {
        this.attackIndex = attackIndex;
        this.targetPokemonInstanceId = targetPokemonInstanceId;
    }

    public int getAttackIndex() { return attackIndex; }
    public void setAttackIndex(int attackIndex) { this.attackIndex = attackIndex; }

    public UUID getTargetPokemonInstanceId() { return targetPokemonInstanceId; }
    public void setTargetPokemonInstanceId(UUID targetPokemonInstanceId) { this.targetPokemonInstanceId = targetPokemonInstanceId; }
}
