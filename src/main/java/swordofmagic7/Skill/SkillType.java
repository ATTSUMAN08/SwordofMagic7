package swordofmagic7.Skill;

public enum SkillType {
    Active("アクティブ"),
    PetAttack("ペット攻撃"),
    PetSupport("ペット補助"),
    Passive("パッシブ"),
    ;

    public String Display;

    SkillType(String Display) {
        this.Display = Display;
    }

    public boolean isActive() {
        return this == Active || this.isPetSkill();
    }

    public boolean isPassive() {
        return this == Passive;
    }

    public boolean isPetAttack() {
        return this == PetAttack;
    }

    public boolean isPetSupport() {
        return this == PetSupport;
    }

    public boolean isPetSkill() {
        return this.isPetAttack() || this.isPetSupport();
    }
}