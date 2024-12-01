package swordofmagic7.Attribute;

import org.bukkit.Material;

import java.util.List;

public enum AttributeType {
    STR("§c§l筋力", Material.RED_DYE, "§a§l物理攻撃に関するステータスに影響します"),
    INT("§d§l魔力", Material.PURPLE_DYE, "§a§l魔法攻撃に関するステータスに影響します"),
    DEX("§e§l敏捷", Material.YELLOW_DYE, "§a§l回避とクリティカルダメージに影響します"),
    TEC("§2§l技量", Material.GREEN_DYE, "§a§lクリティカル発生と命中に関するステータスに影響します"),
    SPI("§b§l精神", Material.LIGHT_BLUE_DYE, "§a§lマナと魔法防御に関するステータスに影響します"),
    VIT("§6§l活力", Material.ORANGE_DYE, "§a§l体力と防御に関するステータスに影響します"),
    ;

    public final String Display;
    public final Material Icon;
    public final List<String> Lore;

    AttributeType(String Display, Material Icon, String Lore) {
        this.Display = Display;
        this.Icon = Icon;
        this.Lore = List.of(Lore.split("\n"));
    }
}
