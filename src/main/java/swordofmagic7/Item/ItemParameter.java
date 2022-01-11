package swordofmagic7.Item;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import swordofmagic7.Item.ItemExtend.ItemEquipmentData;
import swordofmagic7.Item.ItemExtend.ItemPetEgg;
import swordofmagic7.Item.ItemExtend.ItemPetFood;
import swordofmagic7.Item.ItemExtend.ItemPotion;
import swordofmagic7.Status.StatusParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;

public class ItemParameter implements Cloneable {
    public String Id;
    public String Display = "Display";
    public List<String> Lore = new ArrayList<>();
    public Material Icon = Material.BARRIER;
    public Color color = Color.BLACK;
    public ItemCategory Category;
    public int CustomModelData = 0;
    public int Sell = 0;
    public ItemEquipmentData itemEquipmentData = new ItemEquipmentData();
    public ItemPetEgg itemPetEgg = new ItemPetEgg();
    public ItemPotion itemPotion = new ItemPotion();
    public ItemPetFood itemPetFood = new ItemPetFood();

    Material getIcon() {
        if (Icon == null) Icon = Material.BARRIER;
        return Icon;
    }

    public boolean isEmpty() {
        return this.Icon == Material.BARRIER || Icon == null;
    }
    public ItemStack viewItem(int amount, String format) {
        final HashMap<StatusParameter, Double> Parameter = itemEquipmentData.Parameter();
        final ItemStack item = new ItemStack(getIcon());
        final ItemMeta meta = item.getItemMeta();
        if (item.getType() == Material.POTION) {
            PotionMeta potion = (PotionMeta) meta;
            potion.setColor(color);
        }
        meta.setDisplayName(decoText(Display));
        meta.setCustomModelData(CustomModelData);
        List<String> Lore = loreText(this.Lore);
        Lore.add(itemInformation);
        Lore.add(decoLore("カテゴリ") + Category.Display);
        Lore.add(decoLore("売値") + Sell);
        if (Category.isPotion()) {
            Lore.add(decoText("§3§lポーション"));
            Lore.add(decoLore("タイプ") + itemPotion.PotionType.Display);
            int i = 1;
            for (double d : itemPotion.Value) {
                if (d != 0) {
                    Lore.add(decoLore("効果値[" + i + "]") + d);
                }
                i++;
            }
            Lore.add(decoLore("再使用時間") + itemPotion.CoolTime + "秒");
        }
        if (Category.isEquipment()) {
            Lore.add(itemParameter);
            Lore.add(decoLore("装備部位") + itemEquipmentData.EquipmentSlot.Display);
            Lore.add(decoLore("装備種") + itemEquipmentData.EquipmentCategory.Display);
            for (StatusParameter param : StatusParameter.values()) {
                if (isZero(Parameter.get(param))) {
                    Lore.add(param.DecoDisplay + String.format(format, Parameter.get(param)) + " (" +String.format(format, this.itemEquipmentData.Parameter.get(param)) + ")");
                }
            }
            Lore.add(decoLore("強化値") + itemEquipmentData.Plus);
            Lore.add(decoLore("耐久値") + itemEquipmentData.Durable + "/" + itemEquipmentData.MaxDurable);
            Lore.add(decoLore("必要レベル") + itemEquipmentData.ReqLevel);
            Lore.add(itemRune);
            for (int i = 0; i < itemEquipmentData.RuneSlot; i++) {
                if (i < itemEquipmentData.Rune.size()) {
                    RuneParameter runeParameter = itemEquipmentData.Rune.get(i);
                    Lore.add("§7・§e§l" + runeParameter.Display + " Lv" + runeParameter.Level + " (" + String.format(format, runeParameter.Quality*100) + "%)");
                } else {
                    Lore.add("§7・§lルーン未装着");
                }
            }
        }
        meta.setUnbreakable(true);
        meta.setLore(Lore);
        for (ItemFlag flag : ItemFlag.values()) {
            meta.addItemFlags(flag);
        }
        item.setItemMeta(meta);

        if (amount > 127) {
            amount = 127;
        }

        item.setAmount(amount);
        return item;
    }

    public ItemParameter() {
        for (StatusParameter param : StatusParameter.values()) {
            itemEquipmentData.Parameter.put(param, 0d);
        }
    }

    @Override
    public ItemParameter clone() {
        try {
            ItemParameter clone = (ItemParameter) super.clone();
            clone.itemEquipmentData = itemEquipmentData.clone();
            clone.itemPotion = itemPotion.clone();
            clone.itemPetEgg = itemPetEgg.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}