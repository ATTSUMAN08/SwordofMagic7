package swordofmagic7.Skill;

public enum SkillType {
    ACTIVE("アクティブ"),
    PASSIVE("パッシブ"),
    PET_ATTACK("ペット攻撃"),
    PET_SUPPORT("ペット補助"),
    ;

    public final String Display;

    SkillType(String Display) {
        this.Display = Display;
    }

    public boolean isActive() {
        return this == ACTIVE || this.isPetSkill();
    }

    public boolean isPassive() {
        return this == PASSIVE;
    }

    public boolean isPetAttack() {
        return this == PET_ATTACK;
    }

    public boolean isPetSupport() {
        return this == PET_SUPPORT;
    }

    public boolean isPetSkill() {
        return this.isPetAttack() || this.isPetSupport();
    }
}