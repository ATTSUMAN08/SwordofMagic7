package swordofmagic7.Menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.HotBar.HotBarCategory;
import swordofmagic7.HotBar.HotBarData;
import swordofmagic7.Inventory.ItemParameterStack;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Sound.SoundList;

import java.util.HashMap;

import static swordofmagic7.Data.DataBase.getSkillData;
import static swordofmagic7.Function.decoInv;
import static swordofmagic7.Function.equalInv;
import static swordofmagic7.Menu.Data.TriggerMenuDisplay;
import static swordofmagic7.Menu.Data.TriggerMenu_Reset;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Trigger {

    private final Player player;
    private final PlayerData playerData;

    Trigger(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    private final HashMap<Integer, HotBarData> TriggerMenuCache = new HashMap<>();
    public void TriggerMenuView() {
        Inventory inv = decoInv(TriggerMenuDisplay, 5);
        playerData.Menu.ViewInventoryCache = playerData.ViewInventory;
        playerData.setView(ViewInventoryType.HotBar, false);
        int slot = 0;
        int tier = 0;
        while (playerData.Classes.classTier[tier] != null) {
            for (SkillData skill : playerData.Classes.classTier[tier].SkillList) {
                if (skill.SkillType.isActive()) {
                    inv.setItem(slot, skill.view());
                    TriggerMenuCache.put(slot, new HotBarData(skill));
                    slot++;
                }
            }
            for (ItemParameterStack stack : playerData.ItemInventory.getList()) {
                if (stack.itemParameter.Category.isPotion()) {
                    inv.setItem(slot, stack.itemParameter.viewItem(1, playerData.ViewFormat()));
                    TriggerMenuCache.put(slot, new HotBarData(stack.itemParameter));
                    slot++;
                }
            }
            for (PetParameter pet : playerData.PetInventory.getList()) {
                inv.setItem(slot, pet.viewPet(playerData.ViewFormat()));
                TriggerMenuCache.put(slot, new HotBarData(pet));
                slot++;
            }
            tier++;
        }
        inv.setItem(44, TriggerMenu_Reset);
        player.openInventory(inv);
    }

    public void TriggerMenuClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, TriggerMenuDisplay)) {
            if (currentItem != null) {
                if (playerData.HotBar.getSelectSlot() != -1) {
                    if (currentItem.getType() == Material.BARRIER) {
                        playerData.HotBar.setHotBar(playerData.HotBar.getSelectSlot(), new HotBarData());
                    } else if (TriggerMenuCache.containsKey(Slot)) {
                        HotBarData hotBar = TriggerMenuCache.get(Slot);
                        if (hotBar.category == HotBarCategory.Skill && getSkillData(hotBar.Icon).SkillType.isPassive()) {
                            player.sendMessage("§e[" + getSkillData(hotBar.Icon).Display + "]§aは§eパッシブスキル§aです");
                            playSound(player, SoundList.Nope);
                        } else {
                            playerData.HotBar.setHotBar(playerData.HotBar.getSelectSlot(), TriggerMenuCache.get(Slot));
                        }
                    }
                    playerData.HotBar.unSelectSlot();
                    TriggerMenuCache.clear();
                    TriggerMenuView();
                    playSound(player, SoundList.Click);
                } else {
                    player.sendMessage("§eスロット§aを§e選択§aしてください");
                    playSound(player, SoundList.Nope);
                }
            }
        }
    }
}
