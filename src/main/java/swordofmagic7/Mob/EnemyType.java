package swordofmagic7.Mob;

public enum EnemyType {
    Normal("一般"),
    MiddleBoss("中ボス"),
    Boss("ボス"),
    RaidBoss("レイドボス"),
    ;

    public String Display;

    EnemyType(String Display) {
        this.Display = Display;
    }

    public boolean isIgnoreSkillsNotAvailable() {
        return this == MiddleBoss || this == Boss || this == RaidBoss;
    }

    public boolean isIgnoreCrowdControl() {
        return this == RaidBoss;
    }

    public boolean isNormal() {
        return this == Normal;
    }

    public boolean isBoss() {
        return this != Normal;
    }
}
