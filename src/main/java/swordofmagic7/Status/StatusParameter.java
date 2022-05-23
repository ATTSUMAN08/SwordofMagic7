package swordofmagic7.Status;

import static swordofmagic7.Function.decoLore;

public enum StatusParameter {
    MaxHealth("最大体力"),
    HealthRegen("体力回復"),
    MaxMana("最大マナ"),
    ManaRegen("マナ回復"),
    ATK("攻撃力"),
    DEF("防御力"),
    HLP("治癒力"),
    ACC("命中"),
    EVA("回避"),
    CriticalRate("クリティカル発生"),
    CriticalResist("クリティカル耐性"),
    SkillCooltime("スキル再使用短縮"),
    SkillCastTime("スキル詠唱短縮"),
    SkillRigidTime("スキル硬直短縮"),
    DamageMultiplyATK("物理与ダメージ"),
    DamageMultiplyMAT("魔法与ダメージ"),
    DamageResistanceATK("物理被ダメージ耐性"),
    DamageResistanceMAT("魔法被ダメージ耐性"),
    MagicCircleRange("魔法陣範囲"),
    ;

    public String Display;
    public String DecoDisplay;

    StatusParameter(String Display) {
        this.Display = Display;
        this.DecoDisplay = decoLore(Display);
    }
}