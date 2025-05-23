package swordofmagic7.Skill;

import com.google.common.collect.MultimapBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Equipment.EquipmentCategory;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Function.*;

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
    public int Stack = 1;
    public List<EquipmentCategory> ReqMainHand = new ArrayList<>();
    public List<EquipmentCategory> ReqOffHand = new ArrayList<>();

    public ItemStack view(PlayerData playerData) {
        if (Icon == null) Icon = Material.END_CRYSTAL;
        ItemStack item = new ItemStack(Icon);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(decoText(Display)));
        List<String> lore = new ArrayList<>(this.Lore);
        lore.add(decoText("§3§lスキルステータス"));
        for (SkillParameter param : Parameter) {
            lore.add(decoLore(param.Display) + param.valueView());
        }
        lore.add(decoText("§3§lスキル情報"));
        lore.add(decoLore("スキルタイプ") + SkillType.Display);
        if (SkillType.isActive()) {
            lore.add(decoLore("消費マナ") + IncreasedConsumptionMana(Mana, playerData.Level));
            lore.add(decoLore("詠唱時間") + (double) CastTime / 20 + "秒");
            lore.add(decoLore("硬直時間") + (double) RigidTime / 20 + "秒");
            lore.add(decoLore("再使用時間") + (double) CoolTime / 20 + "秒");
        }
        lore.add(decoText("§3§l使用条件"));
        lore.add(decoLore("クラスレベル") + ReqLevel);
        for (EquipmentCategory category : ReqMainHand) {
            lore.add("§7・§e§l" + category.Display);
        }
        for (EquipmentCategory category : ReqOffHand) {
            lore.add("§7・§e§l" + category.Display);
        }
        meta.setLore(lore);
        meta.setAttributeModifiers(MultimapBuilder.hashKeys().hashSetValues().build());
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
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

    public int ParameterValueInt(int i) {
        return Math.toIntExact(Math.round(ParameterValue(i)));
    }

    @Override
    public SkillData clone() {
        try {
            SkillData clone = (SkillData) super.clone();
            List<SkillParameter> params = new ArrayList<>();
            for (SkillParameter param : Parameter) {
                params.add(param.clone());
            }
            clone.Parameter = params;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}