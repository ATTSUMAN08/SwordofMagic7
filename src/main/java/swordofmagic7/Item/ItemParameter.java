package swordofmagic7.Item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import swordofmagic7.Function;
import swordofmagic7.Item.ItemExtend.*;
import swordofmagic7.Status.StatusParameter;
import swordofmagic7.TextView.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;

public class ItemParameter implements Cloneable {
    public String Id;
    public String Display = "Display";
    public List<String> Lore = new ArrayList<>();
    public Material Icon = Material.BARRIER;
    public String IconData;
    public Color color = Color.BLACK;
    public ItemCategory Category = ItemCategory.None;
    public int CustomModelData = 0;
    public int Sell = 0;
    public ItemEquipmentData itemEquipmentData = new ItemEquipmentData();
    public ItemPetEgg itemPetEgg = new ItemPetEgg();
    public ItemPotion itemPotion = new ItemPotion();
    public ItemPetFood itemPetFood = new ItemPetFood();
    public ItemCook itemCook = new ItemCook();
    public String Materialization;
    public boolean isHide = false;
    public boolean isLoreHide = false;
    public java.io.File File;

    Material getIcon() {
        if (Icon == null) Icon = Material.BARRIER;
        return Icon;
    }

    public ItemParameter() {
        for (StatusParameter param : StatusParameter.values()) {
            itemEquipmentData.Parameter.put(param, 0d);
        }
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
            String suffix = "";
            if (itemPotion.PotionType.isElixir()) suffix = "%";
            for (double d : itemPotion.Value) {
                if (d != 0) {
                    Lore.add(decoLore("効果値[" + i + "]") + d + suffix);
                }
                i++;
            }
            Lore.add(decoLore("再使用時間") + itemPotion.CoolTime + "秒");
        }
        if (Category.isCook()) {
            Lore.add(decoText("§3§l料理効果"));
            if (itemCook.Health > 0) Lore.add(decoLore("体力回復") + itemCook.Health);
            if (itemCook.Mana > 0) Lore.add(decoLore("マナ回復") + itemCook.Mana);
            for (StatusParameter param : StatusParameter.values()) {
                if (itemCook.Fixed.containsKey(param)) {
                    Lore.add(decoLore(param.Display) + Function.decoDoubleToString(Math.round(itemCook.Fixed.get(param)), "%.0f"));
                }
                if (itemCook.Multiply.containsKey(param)) {
                    Lore.add(decoLore(param.Display) + Function.decoDoubleToString(Math.round(itemCook.Multiply.get(param)), "%.0f") + "%");
                }
            }
            if (itemCook.isBuff) Lore.add(decoLore("効果時間") + itemCook.BuffTime + "秒");
            Lore.add(decoLore("再使用時間") + itemCook.CoolTime + "秒");
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
            //if (itemEquipmentData.RuneMultiply != 1)
            Lore.add(decoLore("ルーン性能") + String.format("%.0f", itemEquipmentData.RuneMultiply*100) + "%");
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
        if (Icon == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) meta;
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", IconData));
            try {
                Field field = skullMeta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(skullMeta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Log("プレイヤへッドのロード時にエラーが発生しました -> " + Display);
            }

            item.setItemMeta(skullMeta);
        } else {
            item.setItemMeta(meta);
        }
        if (Category.isTool()) {
            //NBTItem nbtItem = new NBTItem(item);
        }

        if (amount > MaxStackAmount) {
            amount = MaxStackAmount;
        }

        item.setAmount(amount);
        return item;
    }

    public TextView getTextView(int amount, String format) {
        ItemStack item = viewItem(amount, format);
        String suffix = "";
        if (amount > 1) suffix = "§ax" + amount;
        if (Category.isEquipment()) suffix = "§b+" + itemEquipmentData.Plus;
        StringBuilder hoverText = new StringBuilder(item.getItemMeta().getDisplayName());
        for (String str : item.getLore()) {
            hoverText.append("\n").append(str);
        }
        return new TextView().addText("§e[" + Display + suffix + "§e]").addHover(hoverText.toString()).reset();
    }

    @Override
    public ItemParameter clone() {
        try {
            ItemParameter clone = (ItemParameter) super.clone();
            clone.itemEquipmentData = this.itemEquipmentData.clone();
            clone.itemPotion = this.itemPotion.clone();
            clone.itemPetEgg = this.itemPetEgg.clone();
            clone.itemPetFood = this.itemPetFood.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}