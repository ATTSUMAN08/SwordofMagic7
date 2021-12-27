package swordofmagic7.Skill;

public enum SkillType {
    Active("アクティブ"),
    Passive("パッシブ"),
    ;

    public String Display;

    SkillType(String Display) {
        this.Display = Display;
    }

    public boolean isActive() {
        return this == Active;
    }

    public boolean isPassive() {
        return this == Passive;
    }
}