package swordofmagic7;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Item.isZero;
import static swordofmagic7.StatusParameter.*;

public class Item {
    static boolean isZero(int a) {
        return a != 0;
    }

    static boolean isZero(double a) {
        return a != 0;
    }
}

enum ItemCategory {
    Item("アイテム"),
    Material("素材"),
    PetEgg("ペットエッグ"),
    Equipment("装備"),
    ;
    String Display;

    ItemCategory(String Display) {
        this.Display = Display;
    }

    ItemCategory getItemCategory(String str) {
        for (ItemCategory loop : ItemCategory.values()) {
            if (loop.toString().equalsIgnoreCase(str)) {
                return loop;
            }
        }
        return ItemCategory.Item;
    }
}

enum EquipmentCategory {
    Blade("刃剣", Material.STONE_SWORD),
    Hammer("大槌", Material.STONE_AXE),
    Rod("法杖", Material.STONE_HOE),
    ActGun("法銃", Material.GOLDEN_HOE),
    Shield("盾", Material.SHIELD),
    Baton("指揮杖", Material.BLAZE_ROD),
    Armor("アーマー", Material.IRON_CHESTPLATE),
    ;
    String Display;
    Material material;

    EquipmentCategory(String Display, Material material) {
        this.Display = Display;
        this.material = material;
    }

    public EquipmentCategory getEquipmentCategory(String str) {
        for (EquipmentCategory loop : EquipmentCategory.values()) {
            if (loop.toString().equalsIgnoreCase(str)) {
                return loop;
            }
        }
        return EquipmentCategory.Blade;
    }
}

class ItemParameter implements Cloneable {
    String Id;
    String Display = "Display";
    List<String> Lore = new ArrayList<>();
    Material Icon = Material.BARRIER;
    ItemCategory Category;
    EquipmentCategory EquipmentCategory;
    EquipmentSlot EquipmentSlot;
    int CustomModelData = 0;
    HashMap<StatusParameter, Double> Parameter = new HashMap<>();
    int Durable = 0;
    int MaxDurable = 0;
    int ReqLevel = 0;
    int Sell = 0;
    int Plus = 0;
    int RuneSlot = 0;
    private List<RuneParameter> Rune = new ArrayList<>();
    String PetId;
    int PetMaxLevel;
    int PetLevel;

    List<RuneParameter> getRune() {
        return new ArrayList<>(Rune);
    }

    int getRuneSize() {
        return getRune().size();
    }

    RuneParameter getRune(int i) {
        return getRune().get(i);
    }

    Material getIcon() {
        if (Icon == null) Icon = Material.BARRIER;
        return Icon;
    }

    void addRune(RuneParameter rune) {
        List<RuneParameter> List = getRune();
        List.add(rune);
        Rune = List;
    }

    void removeRune(int i) {
        List<RuneParameter> List = getRune();
        List.remove(i);
        Rune = List;
    }

    boolean isEmpty() {
        return this.Icon == Material.BARRIER || Icon == null;
    }
    ItemStack viewItem(int amount, String format) {
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

    HashMap<StatusParameter, Double> Parameter() {
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

    ItemParameter() {
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

class RuneParameter implements Cloneable{
    String Id;
    String Display = "ルーン";
    List<String> Lore = new ArrayList<>();
    double Quality = 0.5;
    int Level = 0;
    HashMap<StatusParameter, Double> Parameter = new HashMap<>();

    RuneParameter() {
        for (StatusParameter param : StatusParameter.values()) {
            Parameter.put(param, 0d);
        }
    }

    boolean isEmpty() {
        return Level == 0;
    }

    double Parameter(StatusParameter param) {
        return (Parameter.get(param)/2 + (Quality * Parameter.get(param))) * (Math.pow(Level, 1.4) / Level);
    }

    ItemStack viewRune(String format) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(Display));
        List<String> Lore = loreText(this.Lore);
        Lore.add(decoText("§3§lパラメーター"));
        Lore.add(decoLore("§e§lレベル") + Level);
        Lore.add(decoLore("§e§l品質") + String.format(format, Quality*100) + "%");
        for (StatusParameter param : StatusParameter.values()) {
            if (isZero(Parameter.get(param))) Lore.add(param.DecoDisplay +  String.format(format, Parameter(param)));
        }
        meta.setLore(Lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public RuneParameter clone() {
        try {
            RuneParameter clone = (RuneParameter) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}