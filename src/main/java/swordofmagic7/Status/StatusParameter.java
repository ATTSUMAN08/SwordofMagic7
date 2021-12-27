package swordofmagic7.Status;

import static swordofmagic7.Function.decoLore;

public enum StatusParameter {
    MaxHealth("最大体力"),
    HealthRegen("体力回復"),
    MaxMana("最大マナ"),
    ManaRegen("マナ回復"),
    ATK("攻撃力"),
    DEF("防御力"),
    ACC("命中"),
    EVA("回避"),
    CriticalRate("クリティカル発生"),
    CriticalResist("クリティカル耐性"),
    SkillCooltime("スキル再使用時間"),
    SkillCastTime("スキル詠唱時間"),
    SkillRigidTime("スキル硬直時間"),
    ;

    public String Display;
    public String DecoDisplay;

    StatusParameter(String Display) {
        this.Display = Display;
        this.DecoDisplay = decoLore(Display);
    }
}