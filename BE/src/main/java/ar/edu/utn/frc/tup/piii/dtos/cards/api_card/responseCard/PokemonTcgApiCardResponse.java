package ar.edu.utn.frc.tup.piii.dtos.cards.api_card.responseCard;

import java.util.List;

public class PokemonTcgApiCardResponse {
     private String id;
        private String name;
        private String supertype;
        private List<String> subtypes;
        private String hp;
        private List<String> types;
        private List<String> rules;
        private String evolvesFrom;
        private List<String> evolvesTo;
        private List<PokemonTcgApiAbility> abilities;
        private List<PokemonTcgApiAttack> attacks;
        private List<PokemonTcgApiWeakness> weaknesses;
        private List<PokemonTcgApiResistance> resistances;
        private List<String> retreatCost;
        private Integer convertedRetreatCost;
        private PokemonTcgApiSet set;
        private PokemonTcgApiImages images;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSupertype() { return supertype; }
        public void setSupertype(String supertype) { this.supertype = supertype; }
        public List<String> getSubtypes() { return subtypes; }
        public void setSubtypes(List<String> subtypes) { this.subtypes = subtypes; }
        public String getHp() { return hp; }
        public void setHp(String hp) { this.hp = hp; }
        public List<String> getTypes() { return types; }
        public void setTypes(List<String> types) { this.types = types; }
        public List<String> getRules() { return rules; }
        public void setRules(List<String> rules) { this.rules = rules; }
        public String getEvolvesFrom() { return evolvesFrom; }
        public void setEvolvesFrom(String evolvesFrom) { this.evolvesFrom = evolvesFrom; }
        public List<String> getEvolvesTo() { return evolvesTo; }
        public void setEvolvesTo(List<String> evolvesTo) { this.evolvesTo = evolvesTo; }
        public List<PokemonTcgApiAbility> getAbilities() { return abilities; }
        public void setAbilities(List<PokemonTcgApiAbility> abilities) { this.abilities = abilities; }
        public List<PokemonTcgApiAttack> getAttacks() { return attacks; }
        public void setAttacks(List<PokemonTcgApiAttack> attacks) { this.attacks = attacks; }
        public List<PokemonTcgApiWeakness> getWeaknesses() { return weaknesses; }
        public void setWeaknesses(List<PokemonTcgApiWeakness> weaknesses) { this.weaknesses = weaknesses; }
        public List<PokemonTcgApiResistance> getResistances() { return resistances; }
        public void setResistances(List<PokemonTcgApiResistance> resistances) { this.resistances = resistances; }
        public List<String> getRetreatCost() { return retreatCost; }
        public void setRetreatCost(List<String> retreatCost) { this.retreatCost = retreatCost; }
        public Integer getConvertedRetreatCost() { return convertedRetreatCost; }
        public void setConvertedRetreatCost(Integer convertedRetreatCost) { this.convertedRetreatCost = convertedRetreatCost; }
        public PokemonTcgApiSet getSet() { return set; }
        public void setSet(PokemonTcgApiSet set) { this.set = set; }
        public PokemonTcgApiImages getImages() { return images; }
        public void setImages(PokemonTcgApiImages images) { this.images = images; }
}
