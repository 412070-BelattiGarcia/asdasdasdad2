package ar.edu.utn.frc.tup.piii.cards.domain;

import java.util.List;

public class EnergyCardDefinition extends CardDefinition {
    private String energyCardType;
    private List<String> provides;

    public String getEnergyCardType() { return energyCardType; }
    public void setEnergyCardType(String energyCardType) { this.energyCardType = energyCardType; }
    public List<String> getProvides() { return provides; }
    public void setProvides(List<String> provides) { this.provides = provides; }
}
