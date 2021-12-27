package swordofmagic7.Data.Type;

public enum ViewInventoryType {
    ItemInventory("アイテムインベントリ"),
    RuneInventory("ルーンインベントリ"),
    PetInventory("ペットケージ"),
    HotBar("ホットバー"),
    ;

    public String Display;

    ViewInventoryType(String Display) {
        this.Display = Display;
    }

    public boolean isItem() {
        return this == ItemInventory;
    }

    public boolean isRune() {
        return this == RuneInventory;
    }

    public boolean isPet() {
        return this == PetInventory;
    }

    public boolean isHotBar() {
        return this == HotBar;
    }
}

