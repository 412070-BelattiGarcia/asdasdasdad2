package ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request;

/**
     * DTO representing Pokémon TCG card images.
     */
    public class ImagesRequest {
        private String small;
        private String large;

        // Getters and Setters
        public String getSmall() {
            return small;
        }

        public void setSmall(String small) {
            this.small = small;
        }

        public String getLarge() {
            return large;
        }

        public void setLarge(String large) {
            this.large = large;
        }
    }
