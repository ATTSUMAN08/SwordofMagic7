package swordofmagic7.HotBar;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemExtend.ItemPotion;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.SkillData;

import java.util.UUID;

import static swordofmagic7.Data.DataBase.*;

public class HotBarData implements Cloneable {
    public HotBarCategory category = HotBarCategory.None;
    public String Icon;

    public HotBarData() {
    }

    public HotBarData(ItemParameter itemParameter) {
        category = HotBarCategory.Item;
        Icon = itemParameter.Id;
    }

    public HotBarData(SkillData skillData) {
        category = HotBarCategory.Skill;
        Icon = skillData.Id;
    }

    public HotBarData(PetParameter pet) {
        category = HotBarCategory.Pet;
        Icon = pet.petUUID.toString();
    }

    void clear() {
        category = HotBarCategory.None;
    }

    boolean isEmpty() {
        return category == HotBarCategory.None;
    }

    public ItemStack view(PlayerData playerData, boolean glow) {
        ItemStack item;
        int amount = 1;
        String format = playerData.ViewFormat();
        switch (category) {
            case Skill -> {
                SkillData skillData = getSkillData(Icon);
                item = skillData.view().clone();
                int cooltime = playerData.Skill.getSkillCoolTime(skillData);
                if (cooltime > 0) item.setType(Material.NETHER_STAR);
                amount = (int) Math.ceil(cooltime / 20f);
            }
            case Item -> {
                ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(getItemParameter(Icon));
                item = stack.itemParameter.viewItem(1, format).clone();
                amount = stack.Amount;
            }
            case Pet -> item = playerData.PetInventory.getHashMap().get(UUID.fromString(Icon)).viewPet(format);
            default -> item = FlameItem.clone();
        }
        if (glow) {
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 0);
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setAmount(Math.max(Math.min(127, amount), 1));
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

    @Override
    public String toString() {
        String data = "None";
        if (category != HotBarCategory.None) {
            data = Icon + "," + category;
        }
        return data;
    }

    public static HotBarData fromString(String data) {
        HotBarData hotBarData = new HotBarData();
        if (!data.equals("None")) {
            String[] split = data.split(",");
            hotBarData.Icon = split[0];
            hotBarData.category = HotBarCategory.valueOf(split[1]);
        }
        return hotBarData;
    }
}
