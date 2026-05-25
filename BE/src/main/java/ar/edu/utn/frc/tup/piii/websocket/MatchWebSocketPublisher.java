package ar.edu.utn.frc.tup.piii.websocket;

import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MatchWebSocketPublisher implements EventPublisherPort {

    private final SimpMessagingTemplate messagingTemplate;

    public MatchWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishPublicEvent(GameEvent event) {
        String destination = "/topic/matches/" + event.getMatchId();
        messagingTemplate.convertAndSend(destination, event);
    }

    @Override
    public void publishPrivateEvent(String playerId, GameEvent event) {
        String destination = "/queue/matches/" + event.getMatchId() + "/" + playerId;
        messagingTemplate.convertAndSend(destination, event);
    }
}
