package swordofmagic7.HotBar;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Skill.SkillData;

import static swordofmagic7.Data.DataBase.*;

public class HotBarData implements Cloneable {
    public HotBarCategory category = HotBarCategory.None;
    public String Icon;

    public HotBarData() {
    }

    public HotBarData(ItemParameter itemParameter) {
        category = HotBarCategory.Item;
        Icon = itemParameter.Display;
    }

    public HotBarData(SkillData skillData) {
        category = HotBarCategory.Skill;
        Icon = skillData.Id;
    }

    void clear() {
        category = HotBarCategory.None;
    }

    boolean isEmpty() {
        return category == HotBarCategory.None;
    }

    public ItemStack view(String format, boolean glow) {
        ItemStack item;
        switch (category) {
            case Skill -> item = getSkillData(Icon).view().clone();
            case Item -> item = getItemParameter(Icon).viewItem(1, format).clone();
            default -> item = FlameItem.clone();
        }
        if (glow) {
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 0);
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return item;
    }

    @Override
    public HotBarData clone() {
        try {
            HotBarData clone = (HotBarData) super.clone();
            // TODO: このクローンが元の内部を変更できないようにミュータブルな状態をここにコピーします
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
