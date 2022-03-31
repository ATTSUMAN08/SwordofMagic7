package swordofmagic7.HotBar;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.SkillData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.decoLore;
import static swordofmagic7.Function.decoText;

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

    public ItemStack view(PlayerData playerData, int slot, boolean glow) {
        ItemStack item;
        int amount = 1;
        String format = playerData.ViewFormat();
        switch (category) {
            case Skill -> {
                SkillData skillData = getSkillData(Icon);
                item = skillData.view(playerData).clone();
                if (playerData.Skill.SkillStack(skillData) > 0) {
                    amount = playerData.Skill.SkillStack(skillData);
                } else {
                    item.setType(Material.NETHER_STAR);
                    amount = (int) Math.ceil(playerData.Skill.getSkillCoolTime(skillData) / 20f);
                }
            }
            case Item -> {
                ItemParameter itemParameter = playerData.ItemInventory.getItemParameter(Icon);
                List<String> addLore = new ArrayList<>();
                if (itemParameter != null) {
                    ItemParameterStack stack = playerData.ItemInventory.getItemParameterStack(itemParameter);
                    amount = stack.Amount;
                    if (itemParameter.Category.isPotion() || itemParameter.Category.isCook()) {
                        addLore.add(decoText("§3§lアイテムスタック"));
                        addLore.add(decoLore("個数") + stack.Amount);
                    }
                } else {
                    itemParameter = getItemParameter(Icon);
                }
                item = itemParameter.viewItem(1, format).clone();
                ItemMeta meta = item.getItemMeta();
                if (addLore.size() > 0) {
                    List<String> Lore = new ArrayList<>(meta.getLore());
                    Lore.addAll(addLore);
                    meta.setLore(Lore);
                    item.setItemMeta(meta);
                }
                if (itemParameter.Category.isPotion()) {
                    if (playerData.PotionCoolTime.containsKey(itemParameter.itemPotion.PotionType)) {
                        item.setType(Material.GLASS_BOTTLE);
                        amount = playerData.PotionCoolTime.get(itemParameter.itemPotion.PotionType);
                    }
                }
            }
            case Pet -> {
                if (playerData.PetInventory.getHashMap().containsKey(UUID.fromString(Icon))) {
                    item = playerData.PetInventory.getHashMap().get(UUID.fromString(Icon)).viewPet(format);
                } else {
                    category = HotBarCategory.None;
                    Icon = null;
                    item = FlameItem(slot).clone();
                }
            }
            default -> {
                item = FlameItem(slot).clone();
                amount = slot;
            }
        }
        if (glow) {
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 0);
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setAmount(Math.max(Math.min(99, amount), 1));
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
