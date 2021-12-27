package swordofmagic7.Skill;

public enum CastType {
    Legacy("レガジー"),
    Renewed("リニュード"),
    Hold("ホールド"),
    ;

    public String Display;

    CastType(String Display) {
        this.Display = Display;
    }

    public boolean isLegacy() {
        return this == Legacy;
    }

    public boolean isRenewed() {
        return this == Renewed;
    }

    public boolean isHold() {
        return this == Hold;
    }
}
