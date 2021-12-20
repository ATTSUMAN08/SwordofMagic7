package swordofmagic7;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Function.*;

enum AttributeType {
    STR("筋力", Material.RED_DYE, "物理攻撃に関するステータスに影響します"),
    INT("魔力", Material.PURPLE_DYE, "魔法攻撃に関するステータスに影響します"),
    DEX("敏捷", Material.YELLOW_DYE, "クリティカル率に影響します"),
    SPI("精神", Material.LIGHT_BLUE_DYE, "マナと魔法防御に関するステータスに影響します"),
    VIT("体力", Material.ORANGE_DYE, "体力と防御に関するステータスに影響します"),
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
    private final HashMap<AttributeType, Integer> Parameter = new HashMap<>();
    private int AttributePoint = 0;

    Attribute(Player player) {
        this.player = player;
        for (AttributeType attr : AttributeType.values()) {
            Parameter.put(attr, 0);
        }
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

    void addAttribute(AttributeType type, int add) {
        if (getAttributePoint() > 0) {
            AttributePoint -= add;
            Parameter.put(type, Parameter.get(type) + add);
        } else {
            player.sendMessage("&eポイント&aが足りません");
        }
    }

    ItemStack attributeView(AttributeType type) {
        ItemStack item = new ItemStack(type.Icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(type.Display) + "[" + Parameter.get(type) + "]");
        List<String> Lore = new ArrayList<>(type.Lore);
        Lore.add(decoText("&3追加ステータス"));
        if (type == AttributeType.STR) {
            Lore.add(decoLore("物理与ダメージ") + "+" + Parameter.get(type)*0.5 + "%");
        } else if (type == AttributeType.INT) {
            Lore.add(decoLore("魔法与ダメージ") + "+" + Parameter.get(type)*0.4 + "%");
            Lore.add(decoLore("魔法被ダメージ軽減") + "+" + Parameter.get(type) * 0.1 + "%");
        } else if (type == AttributeType.DEX) {
            Lore.add(decoLore("クリティカル発生") + "+" + Parameter.get(type) + ".0");
        } else if (type == AttributeType.SPI) {
            Lore.add(decoLore("最大マナ") + "+" + Parameter.get(type) * 0.8 + "%");
            Lore.add(decoLore("マナ自動回復") + "+" + Parameter.get(type) * 0.6);
            Lore.add(decoLore("魔法被ダメージ軽減") + "+" + Parameter.get(type) * 0.1 + "%");
        } else if (type == AttributeType.VIT) {
            Lore.add(decoLore("最大体力") + "+" + Parameter.get(type) * 0.8 + "%");
            Lore.add(decoLore("体力自動回復") + "+" + Parameter.get(type) * 0.2);
            Lore.add(decoLore("物理被ダメージ軽減") + "+" + Parameter.get(type) * 0.3 + "%");
            Lore.add(decoLore("魔法被ダメージ軽減") + "+" + Parameter.get(type) * 0.1 + "%");
        }
        meta.setLore(Lore);
        item.setItemMeta(meta);
        return item;
    }
}
