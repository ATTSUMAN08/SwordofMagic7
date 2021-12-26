package swordofmagic7;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.DataBase.getClassList;
import static swordofmagic7.DataBase.playerData;
import static swordofmagic7.Function.*;

enum AttributeType {
    STR("§c§l筋力", Material.RED_DYE, "§a§l物理攻撃に関するステータスに影響します"),
    INT("§d§l魔力", Material.PURPLE_DYE, "§a§l魔法攻撃に関するステータスに影響します"),
    DEX("§e§l敏捷", Material.YELLOW_DYE, "§a§l回避とクリティカルダメージに影響します"),
    TEC("§2§l技量", Material.GREEN_DYE, "§a§lクリティカル発生と命中に関するステータスに影響します"),
    SPI("§b§l精神", Material.LIGHT_BLUE_DYE, "§a§lマナと魔法防御に関するステータスに影響します"),
    VIT("§6§l活力", Material.ORANGE_DYE, "§a§l体力と防御に関するステータスに影響します"),
    ;

    String Display;
    Material Icon;
    List<String> Lore;

    AttributeType(String Display, Material Icon, String Lore) {
        this.Display = Display;
        this.Icon = Icon;
        this.Lore = List.of(Lore.split("\n"));
    }
}

public class Attribute {
    private final Player player;
    private final PlayerData playerData;
    private final HashMap<AttributeType, Integer> Parameter = new HashMap<>();
    private int AttributePoint = 0;

    Attribute(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        for (AttributeType attr : AttributeType.values()) {
            Parameter.put(attr, 0);
        }
    }

    HashMap<AttributeType, Integer> getAttribute() {
        return Parameter;
    }

    int getAttributePoint() {
        return AttributePoint;
    }

    int getAttribute(AttributeType type) {
        return Parameter.get(type);
    }

    void addPoint(int add) {
        AttributePoint += add;
    }
    void setPoint(int point) {
        AttributePoint = point;
    }

    void addAttribute(AttributeType type, int add) {
        if (getAttributePoint() >= add) {
            AttributePoint -= add;
            Parameter.put(type, Parameter.get(type) + add);
        } else {
            player.sendMessage("§eポイント§aが足りません");
        }
    }

    void setAttribute(AttributeType type, int attr) {
        Parameter.put(type, attr);
    }

    void resetAttribute() {
        for (AttributeType attr : AttributeType.values()) {
            Parameter.put(attr, 0);
        }
        int point = 0;
        for (ClassData classData : getClassList().values()) {
            point += playerData.Classes.getLevel(classData)-1;
        }
        AttributePoint = point;
    }

    ItemStack attributeView(AttributeType type) {
        ItemStack item = new ItemStack(type.Icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(type.Display + " §e§l[" + Parameter.get(type) + "] "));
        List<String> Lore = new ArrayList<>(type.Lore);
        Lore.add(decoText("§3§l追加ステータス"));
        final String format = "%.1f";
        if (type == AttributeType.STR) {
            Lore.add(decoLore("物理与ダメージ") + "+" + String.format(format, Parameter.get(type)*0.5) + "%");
            Lore.add(decoLore("攻撃力") + "+" + String.format(format, Parameter.get(type)*0.5) + "%");
        } else if (type == AttributeType.INT) {
            Lore.add(decoLore("魔法与ダメージ") + "+" + String.format(format, Parameter.get(type)*0.4) + "%");
            Lore.add(decoLore("魔法被ダメージ軽減") + "+" + String.format(format, Parameter.get(type) * 0.1) + "%");
            Lore.add(decoLore("攻撃力") + "+" + String.format(format, Parameter.get(type)*0.5) + "%");
        } else if (type == AttributeType.DEX) {
            Lore.add(decoLore("回避") + "+" + String.format(format, Parameter.get(type)*0.8) + "%");
            Lore.add(decoLore("クリティカルダメージ") + "+" + String.format(format, Parameter.get(type)*0.8) + "%");
        } else if (type == AttributeType.TEC) {
            Lore.add(decoLore("命中") + "+" + String.format(format, Parameter.get(type)*0.8) + "%");
            Lore.add(decoLore("クリティカル発生") + "+" + String.format(format, Parameter.get(type)*0.8) + "%");
        } else if (type == AttributeType.SPI) {
            Lore.add(decoLore("最大マナ") + "+" + String.format(format, Parameter.get(type) * 0.8) + "%");
            Lore.add(decoLore("マナ自動回復") + "+" + String.format(format, Parameter.get(type) * 0.6) + "%");
            Lore.add(decoLore("防御力") + "+" + String.format(format, Parameter.get(type) * 0.2) + "%");
            Lore.add(decoLore("クリティカル耐性") + "+" + String.format(format, Parameter.get(type)*0.2) + "%");
            Lore.add(decoLore("魔法被ダメージ軽減") + "+" + String.format(format, Parameter.get(type) * 0.1) + "%");
        } else if (type == AttributeType.VIT) {
            Lore.add(decoLore("最大体力") + "+" + String.format(format, Parameter.get(type) * 0.8) + "%");
            Lore.add(decoLore("体力自動回復") + "+" + String.format(format, Parameter.get(type) * 0.2) + "%");
            Lore.add(decoLore("防御力") + "+" + String.format(format, Parameter.get(type) * 0.5) + "%");
            Lore.add(decoLore("物理被ダメージ軽減") + "+" + String.format(format, Parameter.get(type) * 0.3) + "%");
            Lore.add(decoLore("魔法被ダメージ軽減") + "+" + String.format(format, Parameter.get(type) * 0.1) + "%");
        }
        meta.setLore(Lore);
        item.setItemMeta(meta);
        return item;
    }
}
