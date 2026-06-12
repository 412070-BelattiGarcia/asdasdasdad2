package ar.edu.utn.frc.tup.piii.services.matches;

import ar.edu.utn.frc.tup.piii.cards.domain.CardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.EnergyCardDefinition;
import ar.edu.utn.frc.tup.piii.cards.domain.EnergyType;
import ar.edu.utn.frc.tup.piii.engine.model.*;
import ar.edu.utn.frc.tup.piii.engine.ports.CardLookupPort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchQueryService {

    private final CardLookupPort cardLookupPort;

    public MatchQueryService(CardLookupPort cardLookupPort) {
        this.cardLookupPort = cardLookupPort;
    }

    public PublicGameState buildPublicState(GameState state) {
        if (state == null) return null;

        PublicGameState.PublicPlayerState[] publicPlayers = new PublicGameState.PublicPlayerState[2];
        for (int i = 0; i < 2; i++) {
            PlayerState ps = state.getPlayers()[i];
            if (ps == null) continue;

            PublicGameState.PublicPokemonSlot activeSlot = null;
            if (ps.getActivePokemon() != null) {
                activeSlot = toPublicSlot(ps.getActivePokemon());
            }

            PublicGameState.PublicPokemonSlot[] benchSlots = new PublicGameState.PublicPokemonSlot[ps.getBench() != null ? ps.getBench().size() : 0];
            if (ps.getBench() != null) {
                for (int j = 0; j < ps.getBench().size(); j++) {
                    benchSlots[j] = toPublicSlot(ps.getBench().get(j));
                }
            }

            String[] prizes = new String[ps.getPrizes() != null ? ps.getPrizes().size() : 0];
            Arrays.fill(prizes, "FACE_DOWN");

            publicPlayers[i] = new PublicGameState.PublicPlayerState(
                    ps.getPlayerId(),
                    ps.getSide() != null ? ps.getSide().name() : null,
                    activeSlot,
                    benchSlots,
                    prizes,
                    ps.isSetupConfirmed()
            );
            publicPlayers[i].setMulliganCount(ps.getMulliganCount());
            publicPlayers[i].setTotalPrizeCount(state.getPrizeCountPerPlayer());
            publicPlayers[i].setMulliganRevealedCards(ps.getMulliganRevealedCards());
        }

        PublicGameState pgs = new PublicGameState(
                state.getMatchId(),
                state.getStatus() != null ? state.getStatus().name() : null,
                state.getPhase() != null ? state.getPhase().name() : null,
                state.getTurnNumber(),
                state.getCurrentPlayerId(),
                state.getFirstPlayerId(),
                publicPlayers
        );
        pgs.setMulliganDrawPending(state.isMulliganDrawPending());
        pgs.setMulliganDrawDeadline(state.getMulliganDrawDeadline() != null ? state.getMulliganDrawDeadline().toString() : null);
        Set<UUID> pending = state.getPendingInitialMulliganPlayers();
        pgs.setPendingInitialMulliganPlayers(pending != null && !pending.isEmpty() ? pending.toArray(new UUID[0]) : null);
        return pgs;
    }

    public PrivatePlayerState buildPrivateState(GameState state, UUID playerId) {
        if (state == null) return null;

        PlayerState playerState = null;
        for (PlayerState ps : state.getPlayers()) {
            if (ps != null && ps.getPlayerId().equals(playerId)) {
                playerState = ps;
                break;
            }
        }
        if (playerState == null) return null;

        List<PrivatePlayerState.PrivateHandCard> handCards = new ArrayList<>();
        if (playerState.getHand() != null) {
            for (CardInstance ci : playerState.getHand()) {
                CardDefinition def = cardLookupPort.getCardById(ci.getCardDefinitionId());
                handCards.add(new PrivatePlayerState.PrivateHandCard(
                        ci.getInstanceId().toString(),
                        ci.getCardDefinitionId(),
                        def != null ? def.getName() : "Unknown",
                        def != null ? def.getSupertype() : "Unknown"
                ));
            }
        }

        List<PrivatePlayerState.PrizeSlot> prizeSlots = new ArrayList<>();
        if (playerState.getPrizes() != null) {
            for (int i = 0; i < playerState.getPrizes().size(); i++) {
                CardInstance ci = playerState.getPrizes().get(i);
                prizeSlots.add(new PrivatePlayerState.PrizeSlot(
                        i + 1,
                        false,
                        ci.getCardDefinitionId()
                ));
            }
        }

        PrivatePlayerState pps = new PrivatePlayerState(
                playerState.getPlayerId(),
                handCards,
                playerState.getDeck() != null ? playerState.getDeck().size() : 0,
                playerState.getDiscard() != null ? playerState.getDiscard().size() : 0,
                prizeSlots
        );
        int pendingDrawCount = 0;
        if (state.isMulliganDrawPending() && state.getMulliganDrawCounts() != null) {
            boolean alreadyResolved = state.getMulliganDrawResolved() != null
                && state.getMulliganDrawResolved().contains(playerId);
            if (!alreadyResolved) {
                pendingDrawCount = state.getMulliganDrawCounts().getOrDefault(playerId, 0);
            }
        }
        pps.setPendingMulliganDrawCount(pendingDrawCount);
        return pps;
    }

    private PublicGameState.PublicPokemonSlot toPublicSlot(PokemonInPlay pkm) {
        String[] energyTypes;
        if (pkm.getAttachedEnergies() != null) {
            List<String> types = new ArrayList<>();
            for (CardInstance ci : pkm.getAttachedEnergies()) {
                CardDefinition def = cardLookupPort.getCardById(ci.getCardDefinitionId());
                if (def instanceof EnergyCardDefinition ed && ed.getProvides() != null) {
                    for (EnergyType et : ed.getProvides()) {
                        types.add(et.name());
                    }
                }
            }
            energyTypes = types.toArray(String[]::new);
        } else {
            energyTypes = new String[0];
        }

        return new PublicGameState.PublicPokemonSlot(
                pkm.getInstanceId().toString(),
                pkm.getCardDefinitionId(),
                pkm.getDamageCounters(),
                pkm.getSpecialConditions() != null
                        ? pkm.getSpecialConditions().stream().map(Enum::name).toArray(String[]::new)
                        : new String[0],
                energyTypes,
                pkm.isEvolvedThisTurn()
        );
    }
}
