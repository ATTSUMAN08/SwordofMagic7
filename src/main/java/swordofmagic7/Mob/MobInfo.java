package swordofmagic7.Mob;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Item.ItemStackData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.DataBase.DownScrollItem;
import static swordofmagic7.Data.DataLoader.MaxTitleSlot;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.TitleMenu.nonSlotVertical;
import static swordofmagic7.Sound.CustomSound.playSound;

public class MobInfo {

    private final PlayerData playerData;
    private final Player player;
    private final MobData[] MobArray = new MobData[54];
    private int scroll =  0;
    private final String MobInfoDisplay = "§lエネミーリスト";

    public MobInfo(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.player;
    }

    public void MobInfoView() {
        Inventory inv = decoInv(MobInfoDisplay, 6);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
        MultiThread.TaskRunSynchronizedLater(() -> MobInfoView(0), 1, "MobInfoView");
    }

    public void MobInfoView(int scroll) {
        this.scroll = scroll;
        int slot = 0;
        int index = scroll * 8;
        List<MobData> list = new ArrayList<>(DataBase.MobList.values());
        list.removeIf(mob -> mob.isHide);
        list.sort(new MobInfoSort());
        ItemStack[] itemStacks = new ItemStack[54];
        for (int i = 0; i < 48; i++) {
            if (list.size() > index) {
                MobData mobData = list.get(index);
                itemStacks[slot] = new ItemStackData(mobData.Icon, decoText(mobData.Display), toStringList(mobData)).view();
                MobArray[slot] = mobData;
                index++;
                slot++;
                if (nonSlotVertical(slot)) {
                    itemStacks[slot] = BrownItemFlame;
                    slot++;
                }
            } else break;
        }
        if (scroll > 0) itemStacks[8] = UpScrollItem;
        if (MaxTitleSlot/9-5 > 0) itemStacks[53] = DownScrollItem;
        player.getOpenInventory().getTopInventory().setContents(itemStacks);
    }

    public void MobInfoClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, MobInfoDisplay)) {
            if (currentItem != null) {
                if (!nonSlotVertical(Slot) && MobArray[Slot] != null) {

                } else if (equalItem(currentItem, DownScrollItem)) {
                    scroll++;
                    MobInfoView(scroll);
                } else if (equalItem(currentItem, UpScrollItem)) {
                    scroll--;
                    MobInfoView(scroll);
                }
            }
        }
    }

    public List<String> toStringList(MobData mobData) {
        List<String> list = new ArrayList<>();
        for (String str : mobData.Lore) {
            list.add("§a§l" + str);
        }
        list.add(decoText("§3§lドロップ情報"));
        for (DropItemData itemData : mobData.DropItemTable) {
            list.add("§7・§e§l" + itemData.itemParameter.Display + "§a§lx" + itemData.MinAmount + "-" + itemData.MaxAmount + "§b§l -> §e§l" + String.format(playerData.ViewFormat(), itemData.Percent*100) + "%");
        }
        for (DropRuneData runeData : mobData.DropRuneTable) {
            list.add("§7・§e§l" + runeData.runeParameter.Display + "§b§l -> §e§l" + String.format(playerData.ViewFormat(), runeData.Percent*100) + "%");
        }
        list.add(getPetList().containsKey(mobData.Id) ? "§b§l懐柔可能" : "§c§l懐柔不可");
        return list;
    }
}

class MobInfoSort implements Comparator<MobData> {
    public int compare(MobData mobData, MobData mobData2) {
        return mobData.Display.compareTo(mobData2.Display);
    }
}
