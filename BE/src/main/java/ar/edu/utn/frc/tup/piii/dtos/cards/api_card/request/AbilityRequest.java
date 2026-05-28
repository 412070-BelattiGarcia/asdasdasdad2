package ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request;

public class AbilityRequest {

    private String name;
        private String text;
        private String type;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

}
