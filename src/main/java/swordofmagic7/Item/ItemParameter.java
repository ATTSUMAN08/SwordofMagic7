package swordofmagic7.Item;

import com.google.common.collect.MultimapBuilder;
import me.attsuman08.abysslib.shade.nbtapi.NBT;
import me.attsuman08.abysslib.shade.nbtapi.NbtApiException;
import me.attsuman08.abysslib.shade.nbtapi.iface.ReadWriteNBT;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import swordofmagic7.Function;
import swordofmagic7.Item.ItemExtend.*;
import swordofmagic7.Item.ItemUseList.RewardBox;
import swordofmagic7.Item.ItemUseList.RewardBoxData;
import swordofmagic7.Status.StatusParameter;
import swordofmagic7.TextView.TextView;

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
    public boolean isNonTrade = false;
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
        return viewItem(amount, format, true);
    }

    public ItemStack viewItem(int amount, String format, boolean isLoreHide) {
        final HashMap<StatusParameter, Double> Parameter = itemEquipmentData.Parameter();
        final ItemStack item = new ItemStack(getIcon());
        final ItemMeta meta = item.getItemMeta();
        if (item.getType() == Material.POTION) {
            PotionMeta potion = (PotionMeta) meta;
            potion.setColor(color);
        }
        meta.displayName(Component.text(decoText(Display)));
        meta.setCustomModelData(CustomModelData);
        List<String> Lore = new ArrayList<>();
        if (isLoreHide && this.isLoreHide) Lore.add("§c§lこの情報へのアクセス権限がありません");
        else Lore.addAll(loreText(this.Lore));
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
                    Lore.add(decoLore(param.Display) + "+" + Function.decoDoubleToString(Math.round(itemCook.Fixed.get(param)), format));
                }
                if (itemCook.Multiply.containsKey(param)) {
                    Lore.add(decoLore(param.Display) + "+" + Function.decoDoubleToString(Math.round(itemCook.Multiply.get(param)*100), format) + "%");
                }
            }
            if (itemCook.isBuff) Lore.add(decoLore("効果時間") + itemCook.BuffTime + "秒");
            Lore.add(decoLore("再使用時間") + itemCook.CoolTime + "秒");
        }
        if (Category.isEquipment()) {
            Lore.add(itemParameter);
            Lore.add(decoLore("装備部位") + itemEquipmentData.EquipmentSlot.Display);
            Lore.add(decoLore("装備種") + itemEquipmentData.equipmentCategory.Display);
            for (StatusParameter param : StatusParameter.values()) {
                if (isZero(Parameter.get(param))) {
                    if (itemEquipmentData.isAccessory()) {
                        Lore.add(param.DecoDisplay + String.format(format, Parameter.get(param)) + " (" + String.format(format, itemEquipmentData.itemAccessory.Base.get(param)) + "±" + String.format("%.0f", itemEquipmentData.itemAccessory.Range.get(param)*100) + "%)");
                    } else {
                        Lore.add(param.DecoDisplay + String.format(format, Parameter.get(param)) + " (" +String.format(format, itemEquipmentData.Parameter.get(param)) + ")");
                    }
                }
            }
            //if (itemEquipmentData.RuneMultiply != 1)
            Lore.add(decoLore("ルーン性能") + String.format("%.0f", itemEquipmentData.RuneMultiply*100) + "%");
            Lore.add(decoLore("強化値") + itemEquipmentData.Plus);
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
        if (RewardBoxList.containsKey(Id)) {
            Lore.add(decoText("§3§l内容物"));
            RewardBox rewardBox = RewardBoxList.get(Id);
            for (RewardBoxData rewardBoxData : rewardBox.List) {
                Lore.add("§7・§e§l" + rewardBoxData.id + "§ax" + rewardBoxData.amount + " §b§l-> §a§l" + String.format(format, rewardBoxData.percent*100) + "%");
            }
            Lore.add(rewardBox.isPartition ? "§b§l抽選" : "§b§lテーブル");
        }
        meta.setUnbreakable(true);
        meta.setLore(Lore);
        meta.setAttributeModifiers(MultimapBuilder.hashKeys().hashSetValues().build());
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        if (Icon == Material.PLAYER_HEAD) {
            try {
                NBT.modifyComponents(item, nbt -> {
                    ReadWriteNBT profileNbt = nbt.getOrCreateCompound("minecraft:profile");
                    profileNbt.setUUID("id", UUID.randomUUID());
                    ReadWriteNBT propertiesNbt = profileNbt.getCompoundList("properties").addCompound();
                    propertiesNbt.setString("name", "textures");
                    propertiesNbt.setString("value", IconData);
                });
            } catch (NbtApiException e) {
                Log("プレイヤへッドのロード時にエラーが発生しました -> " + Display + " | " + e.getMessage());
            }
        } else {
            item.setItemMeta(meta);
        }
        /*if (Category.isTool()) {
            NBTItem nbtItem = new NBTItem(item);
        }*/

        if (amount > MaxStackAmount) {
            amount = MaxStackAmount;
        }

        item.setAmount(amount);
        return item;
    }

    public TextView getTextView(int amount, String format) {
        return getTextView(amount, format, true);
    }

    public TextView getTextView(int amount, String format, boolean isLoreHide) {
        ItemStack item = viewItem(amount, format, isLoreHide);
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