package ar.edu.utn.frc.tup.piii.engine.action;

public class PlayTrainerPayload implements GameActionPayload {
    private int handIndex;

    public PlayTrainerPayload() {}

    public PlayTrainerPayload(int handIndex) {
        this.handIndex = handIndex;
    }

    public int getHandIndex() { return handIndex; }
    public void setHandIndex(int handIndex) { this.handIndex = handIndex; }
}
