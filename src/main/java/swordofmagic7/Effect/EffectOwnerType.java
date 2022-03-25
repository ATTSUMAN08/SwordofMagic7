package swordofmagic7.Effect;

public enum EffectOwnerType {
    Player,
    Enemy,
    Pet,
    ;

    public boolean isPlayer() {
        return this == Player;
    }

    public boolean isEnemy() {
        return this == Enemy;
    }

    public boolean isPet() {
        return this == Pet;
    }

}
