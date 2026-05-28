package ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request;

import java.util.List;

/**
     * DTO representing a Pokémon TCG card attack.
     */
    public class AttackRequest {
        private String name;
        private List<String> cost;
        private Integer convertedEnergyCost;
        private String damage;
        private String text;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getCost() {
            return cost;
        }

        public void setCost(List<String> cost) {
            this.cost = cost;
        }

        public Integer getConvertedEnergyCost() {
            return convertedEnergyCost;
        }

        public void setConvertedEnergyCost(Integer convertedEnergyCost) {
            this.convertedEnergyCost = convertedEnergyCost;
        }

        public String getDamage() {
            return damage;
        }

        public void setDamage(String damage) {
            this.damage = damage;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }