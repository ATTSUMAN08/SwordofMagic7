package swordofmagic7.Menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.TitleData;
import swordofmagic7.MultiThread.MultiThread;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static swordofmagic7.Data.DataBase.BrownItemFlame;
import static swordofmagic7.Function.decoInv;
import static swordofmagic7.Function.equalInv;
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

    public static List<Integer> nonSlot() {
        List<Integer> list = new ArrayList<>();
        list.add(8);
        list.add(17);
        list.add(26);
        list.add(35);
        list.add(44);
        return list;
    }

    public void TitleMenuView() {
        playerData.statistics.checkTitle();
        Inventory inv = decoInv(TitleMenuDisplay, 6);
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
        MultiThread.TaskRunLater(() -> TitleMenuView(0), 1, "TitleMenuView: " + player.getName());
    }

    public void TitleMenuView(int scroll) {
        if (equalInv(player.getOpenInventory(), TitleMenuDisplay)) {
            Scroll = scroll;
            ItemStack[] itemStacks = new ItemStack[54];
            List<TitleData> titleList = new ArrayList<>(DataBase.TitleDataList.values());
            titleList.sort(new TitleSort());
            int slot = 0;
            int index = scroll * 8;
            for (int i = 0; i < 48; i++) {
                if (titleList.size() > index) {
                    TitleData titleData = titleList.get(index);
                    itemStacks[slot] = titleData.view(playerData.titleManager.TitleList.contains(titleData.Id));
                    TitleArray[slot] = titleList.get(index);
                    index++;
                    slot++;
                    if (nonSlot().contains(slot)) slot++;
                } else break;
            }
            for (int i : nonSlot()) {
                itemStacks[i] = BrownItemFlame;
            }
            player.getOpenInventory().getTopInventory().setStorageContents(itemStacks);
            playSound(player, SoundList.Tick);
        }
    }

    public void TitleMenuClick(InventoryView view, ItemStack currentItem, int Slot) {
        if (equalInv(view, TitleMenuDisplay)) {
            if (currentItem != null) {
                if (!nonSlot().contains(Slot) && TitleArray[Slot] != null) {
                    playerData.titleManager.setTitle(TitleArray[Slot]);
                } else if (Slot == 8 && Scroll < Math.max(0, (DataBase.TitleDataList.size()/8-5))) {
                    Scroll++;
                    TitleMenuView(Scroll);
                } else if (Slot == 45 && Scroll > 1) {
                    Scroll--;
                    TitleMenuView(Scroll);
                }
            }
        }
    }
}

class TitleSort implements Comparator<TitleData> {
    public int compare(TitleData title, TitleData title2) {
        return title.Id.compareTo(title2.Id);
    }
}