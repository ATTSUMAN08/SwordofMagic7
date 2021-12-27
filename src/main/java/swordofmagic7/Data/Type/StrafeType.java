package swordofmagic7.Data.Type;

public enum StrafeType {
    DoubleJump("ダブルジャンプ"),
    AirDash("空中ダッシュ"),
    All("すべての条件"),
    ;

    public String Display;

    StrafeType(String Display) {
        this.Display = Display;
    }

    public boolean isAirDash() {
        return this == AirDash || this == All;
    }

    public boolean isDoubleJump() {
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

