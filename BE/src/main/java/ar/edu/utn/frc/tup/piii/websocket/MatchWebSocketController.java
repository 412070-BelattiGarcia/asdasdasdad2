package ar.edu.utn.frc.tup.piii.websocket;

import ar.edu.utn.frc.tup.piii.dtos.matches.GameActionRequest;
import ar.edu.utn.frc.tup.piii.dtos.matches.GameActionResponse;
import ar.edu.utn.frc.tup.piii.engine.model.PrivatePlayerState;
import ar.edu.utn.frc.tup.piii.services.matches.MatchApplicationService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class MatchWebSocketController {

    private final MatchApplicationService matchApplicationService;
    private final MatchWebSocketPublisher publisher;

    public MatchWebSocketController(MatchApplicationService matchApplicationService,
                                    MatchWebSocketPublisher publisher) {
        this.matchApplicationService = matchApplicationService;
        this.publisher = publisher;
    }

    @MessageMapping("/matches/{matchId}/actions")
    public void handleMatchAction(
            @DestinationVariable String matchId,
            GameActionRequest request
    ) {
        UUID matchUuid = UUID.fromString(matchId);
        GameActionResponse response = matchApplicationService.executeAction(matchUuid, request);

        UUID actingPlayerId = UUID.fromString(request.playerId());
        publisher.publishPublicState(matchUuid, response);

        if (response.privateState() instanceof PrivatePlayerState actingPrivateState) {
            publisher.publishPrivateState(matchUuid, actingPlayerId, actingPrivateState);
        }

        for (UUID pid : matchApplicationService.getPlayerIds(matchUuid)) {
            if (!pid.equals(actingPlayerId)) {
                PrivatePlayerState otherState = matchApplicationService.getPrivateState(matchUuid, pid);
                if (otherState != null) {
                    publisher.publishPrivateState(matchUuid, pid, otherState);
                }
            }
        }
    }
}
