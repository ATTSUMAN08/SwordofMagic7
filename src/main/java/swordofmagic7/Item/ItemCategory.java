package swordofmagic7.Item;

public enum ItemCategory {
    Equipment("装備"),
    Tool("ツール"),
    Potion("ポーション"),
    Cook("料理"),
    Item("アイテム"),
    Materialization("素材化装備"),
    Material("素材"),
    PetEgg("ペットエッグ"),
    PetFood("ペットフード"),
    None("未設定"),
    ;
    String Display;

    ItemCategory(String Display) {
        this.Display = Display;
    }

    public static ItemCategory getItemCategory(String str) {
        for (ItemCategory loop : ItemCategory.values()) {
            if (loop.toString().equalsIgnoreCase(str)) {
                return loop;
            }
        }
        return ItemCategory.Item;
    }

    public boolean isItem() {
        return this == Item;
    }

    public boolean isPotion() {
        return this == Potion;
    }

    public boolean isMaterial() {
        return this == Material;
    }

    public boolean isPetEgg() {
        return this == PetEgg;
    }

    public boolean isPetFood() {
        return this == PetFood;
    }

    public boolean isEquipment() {
        return this == Equipment;
    }

    public boolean isTool() {
        return this == Tool;
    }

    public boolean isCook() {
        return this == Cook;
    }

    public boolean isMaterialization() {
        return this == Materialization;
    }

    public boolean isTriggerAble() {
        return isEquipment() || isCook() || isPotion() || isTool();
    }
}
