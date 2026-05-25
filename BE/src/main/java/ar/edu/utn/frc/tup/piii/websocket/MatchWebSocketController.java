package ar.edu.utn.frc.tup.piii.websocket;

import ar.edu.utn.frc.tup.piii.dtos.matches.GameActionRequest;
import ar.edu.utn.frc.tup.piii.dtos.matches.GameActionResponse;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class MatchWebSocketController {

    @MessageMapping("/matches/{matchId}/actions")
    @SendTo("/topic/matches/{matchId}/events")
    public GameActionResponse handleMatchAction(
            @DestinationVariable String matchId,
            GameActionRequest request
    ) {
        return new GameActionResponse(
                true,
                request.clientRequestId(),
                null,
                null,
                List.of(),
                null
        );
    }
}
