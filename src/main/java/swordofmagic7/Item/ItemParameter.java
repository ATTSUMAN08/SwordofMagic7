package swordofmagic7.Item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Equipment.EquipmentCategory;
import swordofmagic7.Equipment.EquipmentSlot;
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
    public ItemCategory Category;
    public EquipmentCategory EquipmentCategory;
    public EquipmentSlot EquipmentSlot;
    public int CustomModelData = 0;
    public HashMap<StatusParameter, Double> Parameter = new HashMap<>();
    public int Durable = 0;
    public int MaxDurable = 0;
    public int ReqLevel = 0;
    public int Sell = 0;
    public int Plus = 0;
    public int RuneSlot = 0;
    private List<RuneParameter> Rune = new ArrayList<>();
    public String PetId;
    public int PetMaxLevel;
    public int PetLevel;

    public List<RuneParameter> getRune() {
        return new ArrayList<>(Rune);
    }

    public int getRuneSize() {
        return getRune().size();
    }

    public RuneParameter getRune(int i) {
        return getRune().get(i);
    }

    Material getIcon() {
        if (Icon == null) Icon = Material.BARRIER;
        return Icon;
    }

    public void addRune(RuneParameter rune) {
        List<RuneParameter> List = getRune();
        List.add(rune);
        Rune = List;
    }

    public void removeRune(int i) {
        List<RuneParameter> List = getRune();
        List.remove(i);
        Rune = List;
    }

    public boolean isEmpty() {
        return this.Icon == Material.BARRIER || Icon == null;
    }
    public ItemStack viewItem(int amount, String format) {
        final HashMap<StatusParameter, Double> Parameter = Parameter();
        final ItemStack item = new ItemStack(getIcon());
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(Display));
        meta.setCustomModelData(CustomModelData);
        List<String> Lore = loreText(this.Lore);
        Lore.add(itemInformation);
        Lore.add(decoLore("カテゴリ") + Category.Display);
        Lore.add(decoLore("売値") + Sell);
        if (Category == ItemCategory.Equipment) {
            Lore.add(itemParameter);
            Lore.add(decoLore("装備部位") + EquipmentSlot.Display);
            Lore.add(decoLore("装備種") + EquipmentCategory.Display);
            for (StatusParameter param : StatusParameter.values()) {
                if (isZero(Parameter.get(param))) {
                    Lore.add(param.DecoDisplay + String.format(format, Parameter.get(param) * (1+Plus*0.02)) + " (" +String.format(format, this.Parameter.get(param)) + ")");
                }
            }
            Lore.add(decoLore("強化値") + Plus);
            Lore.add(decoLore("耐久値") + Durable + "/" + MaxDurable);
            Lore.add(decoLore("必要レベル") + ReqLevel);
            Lore.add(itemRune);
            for (int i = 0; i < RuneSlot; i++) {
                if (i < Rune.size()) {
                    RuneParameter runeParameter = Rune.get(i);
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

    public HashMap<StatusParameter, Double> Parameter() {
        HashMap<StatusParameter, Double> Parameter = new HashMap<>();
        for (StatusParameter statusParameter : StatusParameter.values()) {
            double parameter = this.Parameter.get(statusParameter);
            for (RuneParameter rune : Rune) {
                parameter += rune.Parameter(statusParameter);
            }
            Parameter.put(statusParameter, parameter);
        }
        return Parameter;
    }

    public ItemParameter() {
        for (StatusParameter param : StatusParameter.values()) {
            Parameter.put(param, 0d);
        }
    }

    @Override
    public ItemParameter clone() {
        try {
            ItemParameter clone = (ItemParameter) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}