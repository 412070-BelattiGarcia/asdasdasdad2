package ar.edu.utn.frc.tup.piii.engine.model;

import java.util.UUID;

public class AttachedCard {
    private UUID instanceId;
    private String cardId;
    private String attachedAs;

    public UUID getInstanceId() { return instanceId; }
    public String getCardId() { return cardId; }
    public String getAttachedAs() { return attachedAs; }
}
