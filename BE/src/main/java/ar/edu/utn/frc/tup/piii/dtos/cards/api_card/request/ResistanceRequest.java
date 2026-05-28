package ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request;

/**
     * DTO representing a Pokémon TCG card resistance.
     */
    public class ResistanceRequest {
        private String type;
        private String value;

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
