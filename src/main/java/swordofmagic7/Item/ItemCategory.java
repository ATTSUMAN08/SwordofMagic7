package swordofmagic7.Item;

public enum ItemCategory {
    Item("アイテム"),
    Material("素材"),
    PetEgg("ペットエッグ"),
    Equipment("装備"),
    ;
    String Display;

    ItemCategory(String Display) {
        this.Display = Display;
    }

    public ItemCategory getItemCategory(String str) {
        for (ItemCategory loop : ItemCategory.values()) {
            if (loop.toString().equalsIgnoreCase(str)) {
                return loop;
            }
        }
        return ItemCategory.Item;
    }
}
