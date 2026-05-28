package ar.edu.utn.frc.tup.piii.clients;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.AbilityRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.AttackRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.ImagesRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.PokemonTcgApiCardRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.ResistanceRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.SetInfoRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request.WeaknessRequest;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.responseCard.PokemonTcgApiAbility;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.responseCard.PokemonTcgApiAttack;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.responseCard.PokemonTcgApiCardResponse;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.responseCard.PokemonTcgApiImages;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.responseCard.PokemonTcgApiResistance;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.responseCard.PokemonTcgApiSet;
import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.responseCard.PokemonTcgApiWeakness;

/**
 * Client for interacting with the Pokémon TCG API to fetch card data.
 * This client is used internally only and is not exposed via any API endpoint.
 */
@Component
public class PokemonTcgApiClient {

    private static final Logger logger = LoggerFactory.getLogger(PokemonTcgApiClient.class);
    private static final String BASE_URL = "https://api.pokemontcg.io/v2/cards";
    
    private final RestTemplate restTemplate;

    public PokemonTcgApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches all Pokémon cards from the XY1 set.
     * @return List of Pokémon card DTOs
     */
    public List<PokemonTcgApiCardRequest> fetchPokemonCards() {
        return fetchCardsByType("pokemon", "Pokémon");
    }

    /**
     * Fetches all Trainer cards from the XY1 set.
     * @return List of Trainer card DTOs
     */
    public List<PokemonTcgApiCardRequest> fetchTrainerCards() {
        return fetchCardsByType("trainer", "Entrenador");
    }

    /**
     * Fetches all Energy cards from the XY1 set.
     * @return List of Energy card DTOs
     */
    public List<PokemonTcgApiCardRequest> fetchEnergyCards() {
        return fetchCardsByType("energy", "Energía");
    }

    /**
     * Generic method to fetch cards by type from the Pokémon TCG API.
     * @param type The supertype to filter by (pokemon, trainer, energy)
     * @param typeName Human-readable name for logging
     * @return List of card DTOs for the specified type
     */
    private List<PokemonTcgApiCardRequest> fetchCardsByType(String type, String typeName) {
        String url = BASE_URL + "?q=supertype:" + type + " set.id:xy1";
        
        try {
            logger.info("Fetching {} cards from XY1 set", typeName);
            
            ResponseEntity<PokemonTcgApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                PokemonTcgApiResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<PokemonTcgApiCardRequest> dtos = response.getBody()
                    .getData()
                    .stream()
                    .map(this::mapApiCardToDto)
                    .collect(Collectors.toList());
                
                logger.info("Successfully fetched {} {} cards from XY1 set", dtos.size(), typeName.toLowerCase());
                return dtos;
            } else {
                logger.error("Failed to fetch {} cards: HTTP {}", typeName, response.getStatusCode());
                throw new RuntimeException("Failed to fetch " + typeName + " cards: HTTP " + response.getStatusCode());
            }
        } catch (Exception ex) {
            logger.error("Exception occurred while fetching {} cards: {}", typeName, ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch " + typeName + " cards", ex);
        }
    }

    /**
     * Maps an API card response to our internal DTO.
     * @param apiCard The card object from the API response
     * @return Mapped PokemonTcgCardDto
     */
    private PokemonTcgApiCardRequest mapApiCardToDto(PokemonTcgApiCardResponse apiCard) {
        PokemonTcgApiCardRequest dto = new PokemonTcgApiCardRequest();
        
        // Basic fields
        dto.setId(apiCard.getId());
        dto.setName(apiCard.getName());
        dto.setSupertype(normalizeSupertype(apiCard.getSupertype()));
        dto.setSubtypes(apiCard.getSubtypes() != null ? 
                       new java.util.ArrayList<>(apiCard.getSubtypes()) : 
                       new java.util.ArrayList<>());
        dto.setHp(parseIntegerOrNull(apiCard.getHp()));
        dto.setTypes(apiCard.getTypes() != null ? 
                    new java.util.ArrayList<>(apiCard.getTypes()) : 
                    new java.util.ArrayList<>());
        dto.setRules(apiCard.getRules() != null ? 
                    new java.util.ArrayList<>(apiCard.getRules()) : 
                    null);
        dto.setEvolvesFrom(apiCard.getEvolvesFrom());
        dto.setEvolvesTo(apiCard.getEvolvesTo() != null ? 
                        new java.util.ArrayList<>(apiCard.getEvolvesTo()) : 
                        new java.util.ArrayList<>());
        
        // Abilities
        dto.setAbilities(mapAbilities(apiCard.getAbilities()));
        
        // Attacks
        dto.setAttacks(mapAttacks(apiCard.getAttacks()));
        
        // Weakness and Resistance (note: singular as per spec)
        dto.setWeakness(mapWeaknesses(apiCard.getWeaknesses()));
        dto.setResistance(mapResistances(apiCard.getResistances()));
        
        // Retreat cost
        dto.setRetreatCost(apiCard.getRetreatCost() != null ? 
                          new java.util.ArrayList<>(apiCard.getRetreatCost()) : 
                          new java.util.ArrayList<>());
        dto.setConvertedRetreatCost(apiCard.getConvertedRetreatCost());
        
        // Set information
        dto.setSet(mapSetInfo(apiCard.getSet()));
        
        // Images
        dto.setImages(mapImages(apiCard.getImages()));
        
        return dto;
    }

    // Mapping helper methods for nested DTOs
    
    private List<AbilityRequest> mapAbilities(List<PokemonTcgApiAbility> apiAbilities) {
        if (apiAbilities == null) return new java.util.ArrayList<>();
        return apiAbilities.stream()
            .map(apiAbility -> {
                AbilityRequest dto = new AbilityRequest();
                dto.setName(apiAbility.getName());
                dto.setText(apiAbility.getText());
                dto.setType(apiAbility.getType());
                return dto;
            })
            .collect(Collectors.toList());
    }

    private List<AttackRequest> mapAttacks(List<PokemonTcgApiAttack> apiAttacks) {
        if (apiAttacks == null) return new java.util.ArrayList<>();
        return apiAttacks.stream()
            .map(apiAttack -> {
                AttackRequest dto = new AttackRequest();
                dto.setName(apiAttack.getName());
                dto.setCost(apiAttack.getCost() != null ? 
                          new java.util.ArrayList<>(apiAttack.getCost()) : 
                          new java.util.ArrayList<>());
                dto.setConvertedEnergyCost(apiAttack.getConvertedEnergyCost());
                dto.setDamage(apiAttack.getDamage());
                dto.setText(apiAttack.getText());
                return dto;
            })
            .collect(Collectors.toList());
    }

    private List<WeaknessRequest> mapWeaknesses(List<PokemonTcgApiWeakness> apiWeaknesses) {
        if (apiWeaknesses == null) return new java.util.ArrayList<>();
        return apiWeaknesses.stream()
            .map(apiWeakness -> {
                WeaknessRequest dto = new WeaknessRequest();
                dto.setType(apiWeakness.getType());
                dto.setValue(apiWeakness.getValue());
                return dto;
            })
            .collect(Collectors.toList());
    }

    private List<ResistanceRequest> mapResistances(List<PokemonTcgApiResistance> apiResistances) {
        if (apiResistances == null) return new java.util.ArrayList<>();
        return apiResistances.stream()
            .map(apiResistance -> {
                ResistanceRequest dto = new ResistanceRequest();
                dto.setType(apiResistance.getType());
                dto.setValue(apiResistance.getValue());
                return dto;
            })
            .collect(Collectors.toList());
    }

    private SetInfoRequest mapSetInfo(PokemonTcgApiSet apiSet) {
        if (apiSet == null) return new SetInfoRequest();
        SetInfoRequest dto = new SetInfoRequest();
        dto.setId(apiSet.getId());
        return dto;
    }

    private ImagesRequest mapImages(PokemonTcgApiImages apiImages) {
        if (apiImages == null) return new ImagesRequest();
        ImagesRequest dto = new ImagesRequest();
        dto.setSmall(apiImages.getSmall());
        dto.setLarge(apiImages.getLarge());
        return dto;
    }

    // Utility methods
    
    private String normalizeSupertype(String supertype) {
        if (supertype == null) return null;
        return supertype.trim();
    }

    private Integer parseIntegerOrNull(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Inner classes representing the API response structure
    // These map directly to the JSON structure from the Pokémon TCG API
    

}
