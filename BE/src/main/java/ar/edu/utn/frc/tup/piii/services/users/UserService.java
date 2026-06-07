package ar.edu.utn.frc.tup.piii.services.users;

import ar.edu.utn.frc.tup.piii.dtos.users.CreateUserRequest;
import ar.edu.utn.frc.tup.piii.dtos.users.LoginRequest;
import ar.edu.utn.frc.tup.piii.dtos.users.UserResponse;
import ar.edu.utn.frc.tup.piii.exceptions.NotFoundException;
import ar.edu.utn.frc.tup.piii.repositories.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.repositories.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.repositories.jpa.PlayerJpaRepository;
import ar.edu.utn.frc.tup.piii.repositories.jpa.UserJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;

    public UserService(UserJpaRepository userJpaRepository, PlayerJpaRepository playerJpaRepository) {
        this.userJpaRepository = userJpaRepository;
        this.playerJpaRepository = playerJpaRepository;
    }

    public UserResponse register(CreateUserRequest request) {
        UserEntity entity = new UserEntity();
        entity.setUsername(request.email());
        entity.setEmail(request.email());
        entity.setPassword(request.password());
        entity.setRole("PLAYER");
        entity.setStatus("ACTIVE");
        entity = userJpaRepository.save(entity);

        PlayerEntity player = new PlayerEntity();
        player.setUser(entity);
        player.setDisplayName(request.displayName());
        playerJpaRepository.save(player);

        return toResponse(entity);
    }

    public UserResponse login(LoginRequest request) {
        UserEntity entity = userJpaRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!entity.getPassword().equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return toResponse(entity);
    }

    public UserResponse getById(UUID id) {
        UserEntity entity = userJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return toResponse(entity);
    }

    public List<UserResponse> listAll() {
        return userJpaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public boolean existsById(UUID id) {
        return userJpaRepository.existsById(id);
    }

    private UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getId().toString(),
                entity.getEmail(),
                entity.getPlayer() != null ? entity.getPlayer().getDisplayName() : null,
                entity.getPlayer() != null ? entity.getPlayer().getId().toString() : null
        );
    }
}