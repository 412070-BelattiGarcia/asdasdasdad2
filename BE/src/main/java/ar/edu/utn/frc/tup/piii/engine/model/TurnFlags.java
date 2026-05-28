package ar.edu.utn.frc.tup.piii.engine.model;

public class TurnFlags {
    private boolean hasDrawnForTurn;
    private boolean hasAttachedEnergy;
    private boolean hasRetreated;
    private boolean hasPlayedSupporter;
    private boolean hasPlayedStadium;
    private boolean hasAttacked;

    public boolean hasDrawnForTurn() { return hasDrawnForTurn; }
    public void setHasDrawnForTurn(boolean hasDrawnForTurn) { this.hasDrawnForTurn = hasDrawnForTurn; }

    public boolean hasAttachedEnergy() { return hasAttachedEnergy; }
    public void setHasAttachedEnergy(boolean hasAttachedEnergy) { this.hasAttachedEnergy = hasAttachedEnergy; }

    public boolean hasRetreated() { return hasRetreated; }
    public void setHasRetreated(boolean hasRetreated) { this.hasRetreated = hasRetreated; }

    public boolean hasPlayedSupporter() { return hasPlayedSupporter; }
    public void setHasPlayedSupporter(boolean hasPlayedSupporter) { this.hasPlayedSupporter = hasPlayedSupporter; }

    public boolean hasPlayedStadium() { return hasPlayedStadium; }
    public void setHasPlayedStadium(boolean hasPlayedStadium) { this.hasPlayedStadium = hasPlayedStadium; }

    public boolean hasAttacked() { return hasAttacked; }
    public void setHasAttacked(boolean hasAttacked) { this.hasAttacked = hasAttacked; }
}
