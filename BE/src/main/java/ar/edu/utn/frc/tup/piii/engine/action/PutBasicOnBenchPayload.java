package ar.edu.utn.frc.tup.piii.engine.action;

public class PutBasicOnBenchPayload implements GameActionPayload {
    private int handIndex;

    public PutBasicOnBenchPayload() {}

    public PutBasicOnBenchPayload(int handIndex) {
        this.handIndex = handIndex;
    }

    public int getHandIndex() { return handIndex; }
    public void setHandIndex(int handIndex) { this.handIndex = handIndex; }
}
