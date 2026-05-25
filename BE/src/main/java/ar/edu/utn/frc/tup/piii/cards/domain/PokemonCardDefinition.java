package ar.edu.utn.frc.tup.piii.cards.domain;

import java.util.List;

public class PokemonCardDefinition extends CardDefinition {
    private int hp;
    private String stage;
    private String evolvesFrom;
    private List<String> types;
    private List<AttackDefinition> attacks;
    private List<WeaknessDefinition> weaknesses;
    private List<ResistanceDefinition> resistances;
    private List<String> retreatCost;
    private boolean isEx;
    private boolean isMega;

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getEvolvesFrom() { return evolvesFrom; }
    public void setEvolvesFrom(String evolvesFrom) { this.evolvesFrom = evolvesFrom; }
    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }
    public List<AttackDefinition> getAttacks() { return attacks; }
    public void setAttacks(List<AttackDefinition> attacks) { this.attacks = attacks; }
    public List<WeaknessDefinition> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<WeaknessDefinition> weaknesses) { this.weaknesses = weaknesses; }
    public List<ResistanceDefinition> getResistances() { return resistances; }
    public void setResistances(List<ResistanceDefinition> resistances) { this.resistances = resistances; }
    public List<String> getRetreatCost() { return retreatCost; }
    public void setRetreatCost(List<String> retreatCost) { this.retreatCost = retreatCost; }
    public boolean isEx() { return isEx; }
    public void setEx(boolean ex) { isEx = ex; }
    public boolean isMega() { return isMega; }
    public void setMega(boolean mega) { isMega = mega; }

    public static class AttackDefinition {
        private int index;
        private String name;
        private List<String> cost;
        private String damage;
        private String text;

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getCost() { return cost; }
        public void setCost(List<String> cost) { this.cost = cost; }
        public String getDamage() { return damage; }
        public void setDamage(String damage) { this.damage = damage; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class WeaknessDefinition {
        private String type;
        private String value;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class ResistanceDefinition {
        private String type;
        private String value;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
