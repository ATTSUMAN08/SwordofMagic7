package net.somrpg.swordofmagic7.player.attribute

import org.bukkit.Material
import swordofmagic7.Status.StatusParameter

enum class AttributeType(
    val display: String,
    val icon: Material,
    val lore: String,
    val stats: Map<StatusParameter, Double> = emptyMap(),
) {
    STR(
        "§c§l筋力",
        Material.RED_DYE,
        "§a§l物理攻撃に関するステータスに影響します",
        mapOf(
            StatusParameter.DamageMultiplyATK to 0.5,
            StatusParameter.ATK to 2.5,
        ),
    ),
    INT(
        "§d§l魔力",
        Material.PURPLE_DYE,
        "§a§l魔法攻撃に関するステータスに影響します",
        mapOf(
            StatusParameter.DamageMultiplyMAT to 0.4,
            StatusParameter.DamageResistanceMAT to 0.1,
            StatusParameter.ATK to 2.5,
        ),
    ),

    DEX(
        "§e§l敏捷",
        Material.YELLOW_DYE,
        "§a§l回避とクリティカルダメージに影響します",
        mapOf(
            StatusParameter.EVA to 4.2,
            StatusParameter.CriticalMultiply to 0.8,
        ),
    ),
    TEC(
        "§2§l技量",
        Material.GREEN_DYE,
        "§a§lクリティカル発生と命中に関するステータスに影響します",
        mapOf(
            StatusParameter.ACC to 4.2,
            StatusParameter.CriticalRate to 4.2,
        ),
    ),
    SPI(
        "§b§l精神",
        Material.LIGHT_BLUE_DYE,
        "§a§lマナと魔法防御に関するステータスに影響します",
        mapOf(
            StatusParameter.MaxMana to 0.8,
            StatusParameter.ManaRegen to 0.05,
            StatusParameter.HLP to 2.5,
            StatusParameter.DamageResistanceMAT to 0.1,
        ),
    ),
    VIT(
        "§6§l活力",
        Material.ORANGE_DYE,
        "§a§l体力と防御に関するステータスに影響します",
        mapOf(
            StatusParameter.MaxHealth to 0.8,
            StatusParameter.DEF to 2.5,
            StatusParameter.CriticalResist to 3.8,
            StatusParameter.DamageResistanceATK to 0.3,
            StatusParameter.DamageResistanceMAT to 0.1,
        ),
    ),
}
