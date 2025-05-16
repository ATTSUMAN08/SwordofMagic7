package swordofmagic7.Data.Type;

public enum StrafeType {
    DoubleJump("ダブルジャンプ"),
    AirDash("空中ダッシュ"),
    All("すべての条件"),
    NONE("無効"),
    ;

    public final String Display;

    StrafeType(String Display) {
        this.Display = Display;
    }

    public boolean isAirDash() {
        if (this == NONE) return false;
        return this == AirDash || this == All;
    }

    public boolean isDoubleJump() {
        if (this == NONE) return false;
        return this == DoubleJump || this == All;
    }

    public static StrafeType fromString(String str) {
        if (str != null) for (StrafeType strafeType : StrafeType.values()) {
            if (strafeType.toString().equalsIgnoreCase(str)) {
                return strafeType;
            }
        }
        return DoubleJump;
    }
}

