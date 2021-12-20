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
    Rapier("突剣", Material.GOLDEN_SWORD),
    Rod("法杖", Material.BLAZE_ROD),
    ActGun("法銃", Material.GOLDEN_HOE),
    Shield("盾", Material.SHIELD),
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
    int ModuleSlot = 0;
    private List<ModuleParameter> Module = new ArrayList<>();

    List<ModuleParameter> getModule() {
        return new ArrayList<>(Module);
    }

    int getModuleSize() {
        return getModule().size();
    }

    ModuleParameter getModule(int i) {
        return getModule().get(i);
    }

    Material getIcon() {
        if (Icon == null) Icon = Material.BARRIER;
        return Icon;
    }

    void addModule(ModuleParameter module) {
        List<ModuleParameter> List = getModule();
        List.add(module);
        Module = List;
    }

    void removeModule(int i) {
        List<ModuleParameter> List = getModule();
        List.remove(i);
        Module = List;
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
            if (isZero(Parameter.get(MaxMana))) Lore.add(StatusParameter.MaxMana.DecoDisplay + String.format(format, Parameter.get(MaxMana)) + " (" +String.format(format, this.Parameter.get(MaxMana)) + ")");
            if (isZero(Parameter.get(ManaRegen))) Lore.add(StatusParameter.ManaRegen.DecoDisplay + String.format(format, Parameter.get(ManaRegen)) + " (" +String.format(format, this.Parameter.get(ManaRegen)) + ")");
            if (isZero(Parameter.get(ATK))) Lore.add(StatusParameter.ATK.DecoDisplay + String.format(format, Parameter.get(ATK)) + " (" +String.format(format, this.Parameter.get(ATK)) + ")");
            if (isZero(Parameter.get(DEF))) Lore.add(StatusParameter.DEF.DecoDisplay + String.format(format, Parameter.get(DEF)) + " (" +String.format(format, this.Parameter.get(DEF)) + ")");
            if (isZero(Parameter.get(SkillCastTime))) Lore.add(StatusParameter.SkillCastTime.DecoDisplay +  String.format(format, Parameter.get(SkillCastTime)) + " (" +String.format(format, this.Parameter.get(SkillCastTime)) + ")");
            if (isZero(Parameter.get(SkillRigidTime))) Lore.add(StatusParameter.SkillRigidTime.DecoDisplay +  String.format(format, Parameter.get(SkillRigidTime)) + " (" +String.format(format, this.Parameter.get(SkillRigidTime)) + ")");
            if (isZero(Parameter.get(SkillCooltime))) Lore.add(StatusParameter.SkillCooltime.DecoDisplay + String.format(format, Parameter.get(SkillCooltime)) + " (" +String.format(format, this.Parameter.get(SkillCooltime)) + ")");
            Lore.add(decoLore("強化値") + Plus);
            Lore.add(decoLore("耐久値") + Durable + "/" + MaxDurable);
            Lore.add(decoLore("必要レベル") + ReqLevel);
            Lore.add(itemModule);
            for (int i = 0; i < ModuleSlot; i++) {
                if (i < Module.size()) {
                    ModuleParameter moduleParameter = Module.get(i);
                    Lore.add(colored("&7・&e&l" + moduleParameter.Display + " Lv" + moduleParameter.Level + " (" + String.format(format, moduleParameter.Quality*100) + "%)"));
                } else {
                    Lore.add(colored("&7・&lモジュール未装着"));
                }
            }
        }
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
            for (ModuleParameter module : Module) {
                parameter += module.Parameter(statusParameter);
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

class ModuleParameter implements Cloneable{
    String Display = "モジュール";
    List<String> Lore = new ArrayList<>();
    double Quality = 0.5;
    int Level = 0;
    HashMap<StatusParameter, Double> Parameter = new HashMap<>();

    ModuleParameter() {
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

    ItemStack viewModule(String format) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(colored(Display)));
        List<String> Lore = loreText(this.Lore);
        Lore.add(decoText("&3&lパラメーター"));
        Lore.add(decoLore("&e&l品質") + String.format(format, Quality*100) + "%");
        if (isZero(Parameter.get(MaxMana))) Lore.add(StatusParameter.MaxMana.DecoDisplay +  String.format(format, Parameter(MaxMana)));
        if (isZero(Parameter.get(ManaRegen))) Lore.add(StatusParameter.ManaRegen.DecoDisplay +  String.format(format, Parameter(ManaRegen)));
        if (isZero(Parameter.get(ATK))) Lore.add(StatusParameter.ATK.DecoDisplay +  String.format(format, Parameter(ATK)));
        if (isZero(Parameter.get(DEF))) Lore.add(StatusParameter.DEF.DecoDisplay +  String.format(format, Parameter(DEF)));
        if (isZero(Parameter.get(SkillCastTime))) Lore.add(StatusParameter.SkillCastTime.DecoDisplay +  String.format(format, Parameter(SkillCastTime)));
        if (isZero(Parameter.get(SkillRigidTime))) Lore.add(StatusParameter.SkillRigidTime.DecoDisplay +  String.format(format, Parameter(SkillRigidTime)));
        if (isZero(Parameter.get(SkillCooltime))) Lore.add(StatusParameter.SkillCooltime.DecoDisplay +  String.format(format, Parameter(SkillCooltime)));
        meta.setLore(Lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public ModuleParameter clone() {
        try {
            ModuleParameter clone = (ModuleParameter) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}