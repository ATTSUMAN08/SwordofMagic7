package swordofmagic7.Skill;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Equipment.EquipmentCategory;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;

public class SkillData implements Cloneable {
    public String Id;
    public Material Icon;
    public String Display;
    public SkillType SkillType;
    public List<String> Lore = new ArrayList<>();
    public List<SkillParameter> Parameter = new ArrayList<>();
    public int Mana = 0;
    public int CastTime = 0;
    public int RigidTime = 0;
    public int CoolTime = 0;
    public int ReqLevel = 1;
    public List<EquipmentCategory> ReqMainHand = new ArrayList<>();

    public ItemStack view() {
        if (Icon == null) Icon = Material.END_CRYSTAL;
        ItemStack item = new ItemStack(Icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(decoText(Display));
        List<String> Lore = new ArrayList<>(this.Lore);
        Lore.add(decoText("§3§lスキルステータス"));
        for (SkillParameter param : Parameter) {
            Lore.add(decoLore(param.Display) + param.Prefix + param.valueView() + param.Suffix);
        }
        Lore.add(decoText("§3§lスキル情報"));
        Lore.add(decoLore("スキルタイプ") + SkillType.Display);
        if (SkillType.isActive()) {
            Lore.add(decoLore("消費マナ") + Mana);
            Lore.add(decoLore("詠唱時間") + (double) CastTime / 20 + "秒");
            Lore.add(decoLore("硬直時間") + (double) RigidTime / 20 + "秒");
            Lore.add(decoLore("再使用時間") + (double) CoolTime / 20 + "秒");
        }
        Lore.add(decoText("§3§l使用条件"));
        Lore.add(decoLore("クラスレベル") + ReqLevel);
        for (EquipmentCategory category : ReqMainHand) {
            Lore.add("§7・§e§l" + category.Display);
        }
        meta.setLore(Lore);
        for (ItemFlag flag : ItemFlag.values()) {
            meta.addItemFlags(flag);
        }
        item.setItemMeta(meta);
        return item;
    }

    public SkillParameter Parameter(int i) {
        if (Parameter.size() > i) {
            return Parameter.get(i);
        } else {
            return new SkillParameter();
        }
    }

    public double ParameterValue(int i) {
        if (Parameter.size() > i) {
            return Parameter.get(i).Value;
        } else {
            return -1;
        }
    }

    @Override
    public SkillData clone() {
        try {
            SkillData clone = (SkillData) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}