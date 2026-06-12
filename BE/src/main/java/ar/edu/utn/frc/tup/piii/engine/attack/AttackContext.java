package ar.edu.utn.frc.tup.piii.engine.attack;

import ar.edu.utn.frc.tup.piii.engine.attack.DamageCalculator;
import ar.edu.utn.frc.tup.piii.engine.model.PokemonInPlay;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AttackContext {
    private final PokemonInPlay attacker;
    private final PokemonInPlay defender;
    private final int attackIndex;
    private final Map<String, Object> damageModifiers;
    private final UUID targetPokemonInstanceId;
    private DamageCalculator.DamageCalculatorResult damageCalc;
    private boolean confusedSelfHit;
    private int selfDamageCounters;
    private boolean energyValid;
    private String errorMessage;
    private boolean knockoutOccurred;
    private List<UUID> discardEnergyInstanceIds;

    public AttackContext(PokemonInPlay attacker, PokemonInPlay defender, int attackIndex,
                         Map<String, Object> damageModifiers, UUID targetPokemonInstanceId) {
        this.attacker = attacker;
        this.defender = defender;
        this.attackIndex = attackIndex;
        this.damageModifiers = damageModifiers;
        this.targetPokemonInstanceId = targetPokemonInstanceId;
    }

    public PokemonInPlay getAttacker() { return attacker; }
    public PokemonInPlay getDefender() { return defender; }
    public int getAttackIndex() { return attackIndex; }
    public Map<String, Object> getDamageModifiers() { return damageModifiers; }
    public UUID getTargetPokemonInstanceId() { return targetPokemonInstanceId; }

    public DamageCalculator.DamageCalculatorResult getDamageCalc() { return damageCalc; }
    public void setDamageCalc(DamageCalculator.DamageCalculatorResult damageCalc) { this.damageCalc = damageCalc; }

    public boolean isConfusedSelfHit() { return confusedSelfHit; }
    public void setConfusedSelfHit(boolean confusedSelfHit) { this.confusedSelfHit = confusedSelfHit; }

    public int getSelfDamageCounters() { return selfDamageCounters; }
    public void setSelfDamageCounters(int selfDamageCounters) { this.selfDamageCounters = selfDamageCounters; }

    public boolean isEnergyValid() { return energyValid; }
    public void setEnergyValid(boolean energyValid) { this.energyValid = energyValid; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public boolean isKnockoutOccurred() { return knockoutOccurred; }
    public void setKnockoutOccurred(boolean knockoutOccurred) { this.knockoutOccurred = knockoutOccurred; }

    public List<UUID> getDiscardEnergyInstanceIds() { return discardEnergyInstanceIds; }
    public void setDiscardEnergyInstanceIds(List<UUID> discardEnergyInstanceIds) { this.discardEnergyInstanceIds = discardEnergyInstanceIds; }
}
