package ar.edu.utn.frc.tup.piii.websocket;

import ar.edu.utn.frc.tup.piii.dtos.matches.GameActionResponse;
import ar.edu.utn.frc.tup.piii.engine.event.GameEvent;
import ar.edu.utn.frc.tup.piii.engine.model.PrivatePlayerState;
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
    public void publishEvents(UUID matchId, List<GameEvent> events) {
        String destination = "/topic/matches/" + matchId + "/events";
        messagingTemplate.convertAndSend(destination, events);
    }

    public void publishPublicState(UUID matchId, GameActionResponse response) {
        String destination = "/topic/matches/" + matchId + "/events";
        GameActionResponse publicResponse = new GameActionResponse(
                response.success(),
                response.clientRequestId(),
                response.publicState(),
                null,
                response.events(),
                response.error()
        );
        messagingTemplate.convertAndSend(destination, publicResponse);
    }

    public void publishPrivateState(UUID matchId, UUID playerId, PrivatePlayerState privateState) {
        String destination = "/queue/matches/" + matchId + "/" + playerId;
        messagingTemplate.convertAndSend(destination, privateState);
    }
}
