package swordofmagic7.Item.ItemExtend;

public enum ItemPotionType {
    Health("体力ポーション"),
    Mana("マナポーション"),
    ;

    public String Display;

    ItemPotionType(String Display) {
        this.Display = Display;
    }

    public boolean isHealth() {
        return this == Health;
    }

    public boolean isMana() {
        return this == Mana;
    }
}