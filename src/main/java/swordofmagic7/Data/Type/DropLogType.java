package swordofmagic7.Data.Type;

public enum DropLogType {
    None("非表示"),
    All("すべて表示"),
    Item("アイテムのみ"),
    Rune("ルーンのみ"),
    Rare("レアドロのみ"),
    ;

    public final String Display;

    DropLogType(String Display) {
        this.Display = Display;
    }

    public boolean isItem() {
        return this == Item || this == All;
    }

    public boolean isRune() {
        return this == Rune || this == All;
    }

    public boolean isRare() {
        return this == Rare || this == All;
    }

    public static DropLogType fromString(String str) {
        if (str != null) for (DropLogType dropLogType : DropLogType.values()) {
            if (dropLogType.toString().equalsIgnoreCase(str)) {
                return dropLogType;
            }
        }
        return None;
    }
}


