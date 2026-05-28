package ar.edu.utn.frc.tup.piii.clients;

import java.util.List;

import ar.edu.utn.frc.tup.piii.dtos.cards.api_card.responseCard.PokemonTcgApiCardResponse;

public class PokemonTcgApiResponse {
    private List<PokemonTcgApiCardResponse> data;

        public List<PokemonTcgApiCardResponse> getData() {
            return data;
        }

        public void setData(List<PokemonTcgApiCardResponse> data) {
            this.data = data;
        }
}
