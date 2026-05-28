package ar.edu.utn.frc.tup.piii.engine.action;

public class RetreatPayload implements GameActionPayload {
    private int benchIndex;

    public RetreatPayload() {}

    public RetreatPayload(int benchIndex) {
        this.benchIndex = benchIndex;
    }

    public int getBenchIndex() { return benchIndex; }
    public void setBenchIndex(int benchIndex) { this.benchIndex = benchIndex; }
}
