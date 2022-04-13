package swordofmagic7.Menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.TitleData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.DataLoader.MaxTitleSlot;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.Data.TitleMenuDisplay;
import static swordofmagic7.Sound.CustomSound.playSound;

public class TitleMenu {

    private final Player player;
    private final PlayerData playerData;
    private final TitleData[] TitleArray = new TitleData[54];
    private int Scroll = 0;

    public TitleMenu(PlayerData playerData) {
        this.player = playerData.player;
        this.playerData = playerData;
    }

    public static boolean nonSlotVertical(int slot) {
        return Math.floorMod(slot+1, 9) == 0;
    }

    public void TitleMenuView() {
        playerData.statistics.checkTitle();
        Inventory inv = decoInv(TitleMenuDisplay, 6);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
        MultiThread.TaskRunSynchronizedLater(() -> TitleMenuView(0), 1, "TitleMenuView");
    }

    public void TitleMenuView(int scroll) {
        if (equalInv(player.getOpenInventory(), TitleMenuDisplay)) {
            Scroll = scroll;
            ItemStack[] itemStacks = new ItemStack[54];
            int slot = 0;
            int index = scroll * 9;
            for (int i = 0; i < 48; i++) {
                if (TitleGUIMap.containsKey(index)) {
                    TitleData titleData = TitleDataList.get(TitleGUIMap.get(index));
                    itemStacks[slot] = titleData.view(playerData.titleManager.TitleList.contains(titleData.Id));
                    TitleArray[slot] = titleData;
                }
                index++;
                slot++;
                if (nonSlotVertical(slot)) {
                    itemStacks[slot] = BrownItemFlame;
                    slot++;
                    index++;
                }
            }
            if (Scroll > 0) itemStacks[8] = UpScrollItem;
            if (MaxTitleSlot/9-5 > 0) itemStacks[53] = DownScrollItem;
            player.getOpenInventory().getTopInventory().setContents(itemStacks);
            playSound(player, SoundList.Tick);
        }
    }

    public void TitleMenuClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, TitleMenuDisplay)) {
            if (currentItem != null) {
                if (!nonSlotVertical(Slot) && TitleArray[Slot] != null) {
                    playerData.titleManager.setTitle(TitleArray[Slot]);
                } else if (equalItem(currentItem, UpScrollItem)) {
                    Scroll--;
                    TitleMenuView(Scroll);
                } else if (equalItem(currentItem, DownScrollItem)) {
                    Scroll++;
                    TitleMenuView(Scroll);
                }
            }
        }
    }
}