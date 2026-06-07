package ar.edu.utn.frc.tup.piii.controllers.decks;

import ar.edu.utn.frc.tup.piii.dtos.decks.CreateDeckRequest;
import ar.edu.utn.frc.tup.piii.dtos.decks.DeckResponse;
import ar.edu.utn.frc.tup.piii.dtos.decks.DeckValidationResponse;
import ar.edu.utn.frc.tup.piii.dtos.decks.UpdateDeckRequest;
import ar.edu.utn.frc.tup.piii.services.decks.DeckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DeckControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DeckService deckService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new DeckController(deckService)).build();
    }

    @Test
    void shouldCreateDeck() throws Exception {
        CreateDeckRequest request = new CreateDeckRequest("Test Deck", "p1", List.of());
        DeckResponse response = new DeckResponse("id", "Test Deck", "p1", "USER", 0, true, List.of(),
                new DeckValidationResponse(true, List.of()));

        when(deckService.createDeck(any())).thenReturn(response);

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Deck"));
    }

    @Test
    void shouldGetDeck() throws Exception {
        UUID id = UUID.randomUUID();
        DeckResponse response = new DeckResponse(id.toString(), "Test Deck", null, "USER", 60, true, List.of(),
                new DeckValidationResponse(true, List.of()));

        when(deckService.getDeck(id)).thenReturn(response);

        mockMvc.perform(get("/api/decks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void shouldUpdateDeck() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateDeckRequest request = new UpdateDeckRequest("Updated", List.of());
        DeckResponse response = new DeckResponse(id.toString(), "Updated", null, "USER", 60, true, List.of(),
                new DeckValidationResponse(true, List.of()));

        when(deckService.updateDeck(any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/decks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldDeleteDeck() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/decks/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldListDecksByPlayer() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(deckService.listDecksByPlayer(playerId)).thenReturn(List.of());

        mockMvc.perform(get("/api/decks").param("playerId", playerId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldValidateDeck() throws Exception {
        UUID id = UUID.randomUUID();
        DeckValidationResponse response = new DeckValidationResponse(true, List.of());

        when(deckService.validateDeck(id)).thenReturn(response);

        mockMvc.perform(post("/api/decks/{id}/validate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }
}
