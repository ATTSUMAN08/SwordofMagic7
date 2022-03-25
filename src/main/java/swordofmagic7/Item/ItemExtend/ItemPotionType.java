package swordofmagic7.Item.ItemExtend;

public enum ItemPotionType {
    Health("体力ポーション"),
    Mana("マナポーション"),
    HealthElixir("体力エリクサー"),
    ManaElixir("マナエリクサー"),
    ;

    public String Display;

    ItemPotionType(String Display) {
        this.Display = Display;
    }

    public boolean isHealth() {
        return this == Health || this == HealthElixir;
    }

    public boolean isMana() {
        return this == Mana || this == ManaElixir;
    }

    public boolean isElixir() {
        return this == HealthElixir || this == ManaElixir;
    }
}