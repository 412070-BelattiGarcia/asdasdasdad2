package ar.edu.utn.frc.tup.piii.cards.domain;

import java.util.List;

public class EnergyCardDefinition extends CardDefinition {
    private EnergyCardType energyCardType;
    private List<EnergyType> provides;

    public EnergyCardType getEnergyCardType() { return energyCardType; }
    public void setEnergyCardType(EnergyCardType energyCardType) { this.energyCardType = energyCardType; }
    public List<EnergyType> getProvides() { return provides; }
    public void setProvides(List<EnergyType> provides) { this.provides = provides; }
}
