package swordofmagic7.Attribute;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Function;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.AttributeMenuDisplay;
import static swordofmagic7.Sound.CustomSound.playSound;


public class Attribute {
    private final Player player;
    private final PlayerData playerData;
    private final HashMap<AttributeType, Integer> Parameter = new HashMap<>();
    private int AttributePoint = 0;

    public Attribute(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        for (AttributeType attr : AttributeType.values()) {
            Parameter.put(attr, 0);
        }
    }

    HashMap<AttributeType, Integer> getAttribute() {
        return Parameter;
    }

    public int getAttributePoint() {
        return AttributePoint;
    }

    public int getAttribute(AttributeType type) {
        return Parameter.get(type);
    }

    public void addPoint(int add) {
        AttributePoint += add;
    }

    public void setPoint(int point) {
        AttributePoint = point;
    }

    public void addAttribute(AttributeType type, int add) {
        if (getAttributePoint() >= add) {
            AttributePoint -= add;
            Parameter.put(type, Parameter.get(type) + add);
        } else {
            player.sendMessage("§eポイント§aが足りません");
        }
    }

    public void revAttribute(AttributeType type, int rev) {
        if (Parameter.get(type) >= rev) {
            AttributePoint += rev;
            Parameter.put(type, Parameter.get(type) - rev);
        } else {
            player.sendMessage("§eポイント§aが足りません");
        }
    }

    public void setAttribute(AttributeType type, int attr) {
        Parameter.put(type, attr);
    }

    public void resetAttribute() {
        for (AttributeType attr : AttributeType.values()) {
            Parameter.put(attr, 0);
        }
        AttributePoint = (playerData.Level-1)*5;
        AttributePoint += playerData.titleManager.TitleList.size();
    }

    public ItemStack attributeView(AttributeType type) {
        ItemStack item = new ItemStack(type.Icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(type.Display + " §e§l[" + Parameter.get(type) + "] "));
        List<String> Lore = new ArrayList<>(type.Lore);
        Lore.add(decoText("§3§l追加ステータス"));
        final String format = "%.1f";
        if (type == AttributeType.STR) {
            Lore.add(decoLore("物理与ダメージ") + "+" + String.format(format, Parameter.get(type) * 0.5) + "%");
            Lore.add(decoLore("攻撃力") + "+" + String.format(format, Parameter.get(type) * 0.5) + "%");
        } else if (type == AttributeType.INT) {
            Lore.add(decoLore("魔法与ダメージ") + "+" + String.format(format, Parameter.get(type) * 0.4) + "%");
            Lore.add(decoLore("魔法被ダメージ軽減") + "+" + String.format(format, Parameter.get(type) * 0.1) + "%");
            Lore.add(decoLore("攻撃力") + "+" + String.format(format, Parameter.get(type) * 0.5) + "%");
        } else if (type == AttributeType.DEX) {
            Lore.add(decoLore("回避") + "+" + String.format(format, Parameter.get(type) * 0.8) + "%");
            Lore.add(decoLore("クリティカルダメージ") + "+" + String.format(format, Parameter.get(type) * 0.8) + "%");
        } else if (type == AttributeType.TEC) {
            Lore.add(decoLore("命中") + "+" + String.format(format, Parameter.get(type) * 0.8) + "%");
            Lore.add(decoLore("クリティカル発生") + "+" + String.format(format, Parameter.get(type) * 0.8) + "%");
        } else if (type == AttributeType.SPI) {
            Lore.add(decoLore("最大マナ") + "+" + String.format(format, Parameter.get(type) * 0.8) + "%");
            Lore.add(decoLore("マナ自動回復") + "+" + String.format(format, Parameter.get(type) * 0.05) + "%");
            Lore.add(decoLore("治癒力") + "+" + String.format(format, Parameter.get(type) * 0.5) + "%");
            Lore.add(decoLore("クリティカル耐性") + "+" + String.format(format, Parameter.get(type) * 0.2) + "%");
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

    private Inventory AttributeMenuCache;

    public void AttributeMenuView() {
        AttributeMenuCache = decoInv(AttributeMenuDisplay, 1);
        AttributeMenuLoad();
        player.openInventory(AttributeMenuCache);
    }

    public void AttributeMenuLoad() {
        Attribute attribute = playerData.Attribute;
        int slot = 0;
        for (AttributeType attr : AttributeType.values()) {
            AttributeMenuCache.setItem(slot, attribute.attributeView(attr));
            slot++;
        }
        List<String> lore = new ArrayList<>();
        lore.add(decoLore("ポイント") + attribute.getAttributePoint());
        lore.add("");
        lore.add("§c§l※クリックでアトリビュートをリセット");
        ItemStack point = new ItemStackData(Material.EXPERIENCE_BOTTLE, decoText("アトリビュート"), lore).view();
        AttributeMenuCache.setItem(8, point);
    }

    public void AttributeMenuClick(InventoryView view, ClickType clickType, ItemStack currentItem) {
        if (equalInv(view, AttributeMenuDisplay)) {
            Attribute attr = playerData.Attribute;
            for (AttributeType attrType : AttributeType.values()) {
                if (currentItem.getType() == attrType.Icon) {
                    int x = clickType.isShiftClick() ? 10 : 1;
                    if (clickType.isLeftClick()) {
                        attr.addAttribute(attrType, x);
                    } else if (clickType.isRightClick()) {
                        if (playerData.Map.Safe) {
                            attr.revAttribute(attrType, x);
                        } else Function.sendMessage(player, "§eセーフゾーン§aでのみ使用可能です", SoundList.Nope);
                    }
                }
            }
            if (currentItem.getType() == Material.EXPERIENCE_BOTTLE) {
                if (playerData.Map.Safe) {
                    attr.resetAttribute();
                } else Function.sendMessage(player, "§eセーフゾーン§aでのみ使用可能です", SoundList.Nope);
            }
            AttributeMenuLoad();
            playSound(player, SoundList.Click);
        }
    }
}
