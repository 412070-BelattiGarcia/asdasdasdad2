package ar.edu.utn.frc.tup.piii.websocket;

import ar.edu.utn.frc.tup.piii.engine.ports.EventPublisherPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MatchWebSocketPublisher implements EventPublisherPort {

    private final SimpMessagingTemplate messagingTemplate;

    public MatchWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishEvents(UUID matchId, List<String> events) {
        String destination = "/topic/matches/" + matchId;
        messagingTemplate.convertAndSend(destination, events);
    }
}
