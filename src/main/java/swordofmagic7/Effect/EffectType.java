package swordofmagic7.Effect;

public enum EffectType {
    Stun("スタン"),
    Invincible("無敵"),
    ;

    public String Display;

    EffectType(String Display) {
        this.Display = Display;
    }
}
