package ar.edu.utn.frc.tup.piii.dtos.cards.api_card.request;

import java.util.List;

public class PokemonTcgApiCardRequest {
    private String id;
        private String name;
        private String supertype;
        private List<String> subtypes;
        private Integer hp;
        private List<String> types;
        private List<String> rules;
        private String evolvesFrom;
        private List<String> evolvesTo;
        private List<AbilityRequest> abilities;
        private List<AttackRequest> attacks;
        private List<WeaknessRequest> weakness;
        private List<ResistanceRequest> resistance;
        private List<String> retreatCost;
        private Integer convertedRetreatCost;
        private SetInfoRequest set;
        private String rarity;

    
        public String getRarity() {
            return rarity;
        }
        public void setRarity(String rarity) {
            this.rarity = rarity;
        }
        private ImagesRequest images;
        // Getters and Setters
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getSupertype() {
            return supertype;
        }
        public void setSupertype(String supertype) {
            this.supertype = supertype;
        }
        public List<String> getSubtypes() {
            return subtypes;
        }
        public void setSubtypes(List<String> subtypes) {
            this.subtypes = subtypes;
        }
        public Integer getHp() {
            return hp;
        }
        public void setHp(Integer hp) {
            this.hp = hp;
        }
        public List<String> getTypes() {
            return types;
        }
        public void setTypes(List<String> types) {
            this.types = types;
        }
        public List<String> getRules() {
            return rules;
        }
        public void setRules(List<String> rules) {
            this.rules = rules;
        }
        public String getEvolvesFrom() {
            return evolvesFrom;
        }
        public void setEvolvesFrom(String evolvesFrom) {
            this.evolvesFrom = evolvesFrom;
        }
        public List<String> getEvolvesTo() {
            return evolvesTo;
        }
        public void setEvolvesTo(List<String> evolvesTo) {
            this.evolvesTo = evolvesTo;
        }
        public List<AbilityRequest> getAbilities() {
            return abilities;
        }
        public void setAbilities(List<AbilityRequest> abilities) {
            this.abilities = abilities;
        }
        public List<AttackRequest> getAttacks() {
            return attacks;
        }
        public void setAttacks(List<AttackRequest> attacks) {
            this.attacks = attacks;
        }
        public List<WeaknessRequest> getWeakness() {
            return weakness;
        }
        public void setWeakness(List<WeaknessRequest> weakness) {
            this.weakness = weakness;
        }
        public List<ResistanceRequest> getResistance() {
            return resistance;
        }
        public void setResistance(List<ResistanceRequest> resistance) {
            this.resistance = resistance;
        }
        public List<String> getRetreatCost() {
            return retreatCost;
        }
        public void setRetreatCost(List<String> retreatCost) {
            this.retreatCost = retreatCost;
        }
        public Integer getConvertedRetreatCost() {
            return convertedRetreatCost;
        }
        public void setConvertedRetreatCost(Integer convertedRetreatCost) {
            this.convertedRetreatCost = convertedRetreatCost;
        }
        public SetInfoRequest getSet() {
            return set;
        }
        public void setSet(SetInfoRequest set) {
            this.set = set;
        }
        public ImagesRequest getImages() {
            return images;
        }
        public void setImages(ImagesRequest images) {
            this.images = images;
        }
}
