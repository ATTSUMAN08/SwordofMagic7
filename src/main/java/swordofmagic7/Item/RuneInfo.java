package swordofmagic7.Item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Data.DataLoader.MaxTitleSlot;
import static swordofmagic7.Function.*;
import static swordofmagic7.Menu.TitleMenu.nonSlotVertical;
import static swordofmagic7.Sound.CustomSound.playSound;

public class RuneInfo {

    private final PlayerData playerData;
    private final Player player;
    private final RuneParameter[] RuneArray = new RuneParameter[54];
    private int scroll =  0;
    private final String RuneInfoDisplay = "§lルーンリスト";

    public RuneInfo(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.player;
    }

    public void RuneInfoView() {
        Inventory inv = decoInv(RuneInfoDisplay, 6);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
        MultiThread.TaskRunSynchronizedLater(() -> RuneInfoView(0), 1, "RuneInfoView");
    }

    public void RuneInfoView(int scroll) {
        this.scroll = scroll;
        int slot = 0;
        int index = scroll * 8;
        List<RuneParameter> list = new ArrayList<>(RuneList.values());
        List<RuneParameter> list2 = new ArrayList<>(RuneList.values());
        list.removeIf(rune -> rune.isHide || rune.isSpecial);
        list2.removeIf(rune -> !rune.isSpecial);
        list.addAll(list2);
        ItemStack[] itemStacks = new ItemStack[54];
        for (int i = 0; i < 48; i++) {
            if (list.size() > index) {
                RuneParameter runeData = list.get(index);
                itemStacks[slot] = new ItemStackData(runeData.Icon, decoText(runeData.Display), RuneInfoData.get(runeData.Id)).view();
                RuneArray[slot] = runeData;
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

    public void RuneInfoClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, RuneInfoDisplay)) {
            if (currentItem != null) {
                if (!nonSlotVertical(Slot) && RuneArray[Slot] != null) {

                } else if (equalItem(currentItem, DownScrollItem)) {
                    scroll++;
                    RuneInfoView(scroll);
                } else if (equalItem(currentItem, UpScrollItem)) {
                    scroll--;
                    RuneInfoView(scroll);
                }
            }
        }
    }
}

class RuneInfoSort implements Comparator<RuneParameter> {
    public int compare(RuneParameter runeData, RuneParameter runeData2) {
        return runeData.Display.compareTo(runeData2.Display);
    }
}
