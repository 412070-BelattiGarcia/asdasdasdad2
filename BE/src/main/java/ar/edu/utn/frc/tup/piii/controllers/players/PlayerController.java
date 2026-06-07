package ar.edu.utn.frc.tup.piii.controllers.players;

import ar.edu.utn.frc.tup.piii.dtos.players.PlayerResponse;
import ar.edu.utn.frc.tup.piii.dtos.players.UpdatePlayerRequest;
import ar.edu.utn.frc.tup.piii.services.players.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<List<PlayerResponse>> listPlayers() {
        return ResponseEntity.ok(playerService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getById(@PathVariable UUID id) {
        PlayerResponse response = playerService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> updatePlayer(@PathVariable UUID id, @RequestBody UpdatePlayerRequest request) {
        PlayerResponse response = playerService.update(id, request);
        return ResponseEntity.ok(response);
    }
}