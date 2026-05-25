package ar.edu.utn.frc.tup.piii.engine.model;

public class TurnFlags {
    private boolean hasDrawnForTurn;
    private boolean hasAttachedEnergy;
    private boolean hasRetreated;
    private boolean hasPlayedSupporter;
    private boolean hasPlayedStadium;
    private boolean hasAttacked;

    public boolean hasDrawnForTurn() { return hasDrawnForTurn; }
    public boolean hasAttachedEnergy() { return hasAttachedEnergy; }
    public boolean hasRetreated() { return hasRetreated; }
    public boolean hasPlayedSupporter() { return hasPlayedSupporter; }
    public boolean hasPlayedStadium() { return hasPlayedStadium; }
    public boolean hasAttacked() { return hasAttacked; }
}
