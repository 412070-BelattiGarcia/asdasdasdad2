package ar.edu.utn.frc.tup.piii.cards.domain;

public class TrainerCardDefinition extends CardDefinition {
    private String trainerSubtype;
    private boolean isAceSpec;
    private String effectCode;

    public String getTrainerSubtype() { return trainerSubtype; }
    public void setTrainerSubtype(String trainerSubtype) { this.trainerSubtype = trainerSubtype; }
    public boolean isAceSpec() { return isAceSpec; }
    public void setAceSpec(boolean aceSpec) { isAceSpec = aceSpec; }
    public String getEffectCode() { return effectCode; }
    public void setEffectCode(String effectCode) { this.effectCode = effectCode; }
}
