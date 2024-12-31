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
import swordofmagic7.Item.ItemCategory;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Pet.PetParameter;
import swordofmagic7.Skill.SkillData;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.Tutorial;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static swordofmagic7.classes.Classes.maxSlot;
import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.TriggerMenuDisplay;
import static swordofmagic7.Menu.Data.TriggerMenu_Reset;
import static swordofmagic7.Menu.TitleMenu.nonSlotVertical;
import static swordofmagic7.Sound.CustomSound.playSound;

public class Trigger {

    private final Player player;
    private final PlayerData playerData;
    private int scroll = 0;

    Trigger(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    private final HashMap<Integer, HotBarData> TriggerMenuCache = new HashMap<>();
    public void TriggerMenuView() {
        if (!equalInv(player.getOpenInventory(), TriggerMenuDisplay)) {
            Inventory inv = decoInv(TriggerMenuDisplay, 6);
            player.openInventory(inv);
            MultiThread.TaskRunSynchronizedLater(() -> TriggerMenuView(0), 1);
        } else {
            TriggerMenuView(scroll);
        }
    }
    public void TriggerMenuView(int scroll) {
        this.scroll = scroll;
        playerData.setView(ViewInventoryType.HotBar, false);
        int slot = 0;
        for (int i = 0; i < maxSlot; i++) {
            if (playerData.Classes.classSlot[i] != null) {
                for (SkillData skill : playerData.Classes.classSlot[i].SkillList) {
                    if (skill.SkillType.isActive()) {
                        TriggerMenuCache.put(slot, new HotBarData(skill));
                        slot++;
                    }
                    if (nonSlotVertical(slot)) slot++;
                }
            }
        }
        slot = (int) Math.ceil(slot/9f)*9;
        Set<String> itemSet = new HashSet<>();
        for (ItemParameterStack stack : playerData.ItemInventory.getList()) {
            if (slot > 52) break;
            ItemCategory itemCategory = stack.itemParameter.Category;
            if (!itemSet.contains(stack.itemParameter.Display) && itemCategory.isTriggerAble()) {
                itemSet.add(stack.itemParameter.Display);
                TriggerMenuCache.put(slot, new HotBarData(stack.itemParameter));
                slot++;
                if (nonSlotVertical(slot)) slot++;
            }
        }
        for (PetParameter pet : playerData.PetInventory.getList()) {
            TriggerMenuCache.put(slot, new HotBarData(pet));
            slot++;
            if (nonSlotVertical(slot)) slot++;
        }
        ItemStack[] itemStack = new ItemStack[54];
        int index = scroll*9;
        for (int i = 0; i < 53; i++) {
            if (TriggerMenuCache.containsKey(index)) {
                itemStack[i] = TriggerMenuCache.get(index).view(playerData, i, false);
            }
            if (nonSlotVertical(i)) itemStack[i] = ItemFlame;
            index++;
        }
        itemStack[53] = TriggerMenu_Reset;
        if (scroll > 0) itemStack[8] = UpScrollItem;
        if (slot/9-5-scroll > 0) itemStack[44] = DownScrollItem;
        player.getOpenInventory().getTopInventory().setContents(itemStack);
    }

    public void TriggerMenuClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, TriggerMenuDisplay)) {
            if (currentItem != null) {
                if (equalItem(currentItem, UpScrollItem)) {
                    scroll--;
                    TriggerMenuView(scroll);
                } else if (equalItem(currentItem, DownScrollItem)) {
                    scroll++;
                    TriggerMenuView(scroll);
                } else {
                    int index = Slot+scroll*9;
                    if (playerData.HotBar.getSelectSlot() != -1) {
                        if (currentItem.getType() == Material.BARRIER) {
                            playerData.HotBar.setHotBar(playerData.HotBar.getSelectSlot(), new HotBarData());
                        } else if (TriggerMenuCache.containsKey(index)) {
                            HotBarData hotBar = TriggerMenuCache.get(index);
                            if (hotBar.category == HotBarCategory.Skill && getSkillData(hotBar.Icon).SkillType.isPassive()) {
                                player.sendMessage("§e[" + getSkillData(hotBar.Icon).Display + "]§aは§eパッシブスキル§aです");
                                playSound(player, SoundList.Nope);
                            } else {
                                playerData.HotBar.setHotBar(playerData.HotBar.getSelectSlot(), TriggerMenuCache.get(index));
                                Tutorial.tutorialTrigger(player, 6);
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
}
