package ar.edu.utn.frc.tup.piii.controllers.matches;

import ar.edu.utn.frc.tup.piii.dtos.matches.CreateMatchRequest;
import ar.edu.utn.frc.tup.piii.dtos.matches.JoinMatchRequest;
import ar.edu.utn.frc.tup.piii.dtos.matches.MatchResponse;
import ar.edu.utn.frc.tup.piii.dtos.matches.MatchStateResponse;
import ar.edu.utn.frc.tup.piii.services.matches.MatchApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchApplicationService matchApplicationService;

    public MatchController(MatchApplicationService matchApplicationService) {
        this.matchApplicationService = matchApplicationService;
    }

    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(@RequestBody CreateMatchRequest request) {
        MatchResponse response = matchApplicationService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<MatchResponse> joinMatch(@PathVariable UUID id,
                                                    @RequestBody JoinMatchRequest request) {
        MatchResponse response = matchApplicationService.joinMatch(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/state")
    public ResponseEntity<MatchStateResponse> getMatchState(@PathVariable UUID id,
                                                             @RequestParam UUID playerId) {
        MatchStateResponse response = matchApplicationService.getMatchState(id, playerId);
        return ResponseEntity.ok(response);
    }
}
