package swordofmagic7;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import static swordofmagic7.DataBase.*;
import static swordofmagic7.Function.Log;
import static swordofmagic7.Function.colored;

enum HotBarCategory {
    Skill,
    Item,
    None
}

class HotBarData implements Cloneable{
    HotBarCategory category = HotBarCategory.None;
    String Icon;

    HotBarData() {}

    HotBarData(ItemParameter itemParameter) {
        category = HotBarCategory.Item;
        Icon = itemParameter.Display;
    }

    HotBarData(SkillData skillData) {
        category = HotBarCategory.Skill;
        Icon = skillData.Id;
    }

    void clear() {
        category = HotBarCategory.None;
    }

    boolean isEmpty() {
        return category == HotBarCategory.None;
    }

    ItemStack view(String format, boolean glow) {
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

public class HotBar {
    private final Player player;
    private final PlayerData playerData;
    private int SelectSlot = -1;
    private HotBarData[] HotBarData = new HotBarData[32];

    public HotBar(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;

        for(int i = 0; i < 32; i++) {
            HotBarData[i] = new HotBarData();
        }
    }

    void use(int index) {
        switch (HotBarData[index].category) {
            case Skill -> {
                if (playerData.Skill.isCastReady()) {
                    playerData.Skill.CastSkill(getSkillData(HotBarData[index].Icon));
                }
            }
            default -> player.sendMessage("§e[ホットバー" + (index+1) + "]§aは§eセット§aされていません");
        }
    }

    void setHotBar(HotBarData[] HotBarData) {
        this.HotBarData = HotBarData.clone();
    }

    void setHotBar(int index, HotBarData HotBarData) {
        this.HotBarData[index] = HotBarData.clone();
    }

    void setSelectSlot(int slot) {
        SelectSlot = slot;
    }

    int getSelectSlot() {
        return SelectSlot;
    }

    void unSelectSlot() {
        SelectSlot = -1;
    }

    void addHotbar(HotBarData hotBarData) {
        for (int i = 0; i < 32; i++) {
            if (HotBarData[i] != null) {
                if (HotBarData[i].isEmpty()) {
                    HotBarData[i] = hotBarData.clone();
                    return;
                }
            } else {
                HotBarData[i] = hotBarData.clone();
                return;
            }
        }
        player.sendMessage("§e[ホットバー]§aに空きがありません");
    }

    HotBarData[] getHotBar() {
        return HotBarData;
    }

    HotBarData getHotBar(int index) {
        return HotBarData[index];
    }

    void viewBottom() {
        for (int i = 0; i < 8; i++) {
            if (HotBarData[i] == null) HotBarData[i] = new HotBarData();
            player.getInventory().setItem(i, HotBarData[i].view(playerData.ViewFormat(), SelectSlot == i));
        }
    }

    void viewTop() {
        playerData.ViewInventory = ViewInventory.HotBar;
        int slot = 9;
        for (int i = 8; i < 32; i++) {
            player.getInventory().setItem(slot, HotBarData[i].view(playerData.ViewFormat(), SelectSlot == i));
            slot++;
            if (slot == 17 || slot == 26 || slot == 35) slot++;
        }
    }
}
