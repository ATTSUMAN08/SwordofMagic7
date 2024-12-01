package swordofmagic7.Data.Type;

public enum DamageLogType {
    None("非表示"),
    DamageOnly("ダメージのみ"),
    Detail("詳細情報"),
    All("すべて表示"),
    ;

    public final String Display;

    DamageLogType(String Display) {
        this.Display = Display;
    }

    public boolean isDamageOnly() {
        return this == DamageOnly || this == Detail || this == All;
    }

    public boolean isDetail() {
        return this == Detail || this == All;
    }

    public boolean isAll() {
        return this == All;
    }

    public static DamageLogType fromString(String str) {
        for (DamageLogType damageLogType : DamageLogType.values()) {
            if (damageLogType.toString().equalsIgnoreCase(str)) {
                return damageLogType;
            }
        }
        return None;
    }
}

