package swordofmagic7.Inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Sound.CustomSound.playSound;

public class RuneInventory extends BasicInventory {
    public final int MaxSlot = 500;
    private final java.util.List<RuneParameter> List = new ArrayList<>();

    public RuneInventory(Player player, PlayerData playerData) {
        super(player, playerData);
    }

    public RuneSortType Sort = RuneSortType.Name;
    public boolean SortReverse = false;

    public List<RuneParameter> getList() {
        return List;
    }

    public void clear() {
        List.clear();
    }

    public void addRuneParameter(RuneParameter runeParameter) {
        if (List.size() < MaxSlot) {
            List.add(runeParameter.clone());
            if (List.size() >= MaxSlot-5) {
                player.sendMessage("§e[ルーンインベントリ]§aが§c残り" + (MaxSlot - List.size()) +"スロット§aです");
            }
        } else {
            player.sendMessage("§e[ルーンインベントリ]§aが§c満杯§aです");
            playSound(player, SoundList.Nope);
        }

    }
    public RuneParameter getRuneParameter(int i) {
        if (i < List.size()) {
            return List.get(i).clone();
        }
        return null;
    }

    public void removeRuneParameter(int i) {
        List.remove(i);
    }

    public void RuneInventorySort() {
        switch (Sort) {
            case Name -> Sort = RuneSortType.Level;
            case Level -> Sort = RuneSortType.Quality;
            case Quality -> Sort = RuneSortType.Name;
        }
        player.sendMessage("§e[ルーンインベントリ]§aの§e[ソート方法]§aを§e[" + Sort.Display + "]§aにしました");
        playSound(player, SoundList.Click);
        playerData.viewUpdate();
    }

    public void RuneInventorySortReverse() {
        SortReverse = !SortReverse;
        String msg = "§e[ルーンインベントリ]§aの§e[ソート順]§aを";
        if (!SortReverse) msg += "§b[昇順]";
        else msg += "§c[降順]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.Click);
        playerData.viewUpdate();
    }

    public void viewRune() {
        playerData.ViewInventory = ViewInventoryType.RuneInventory;
        int index = ScrollTick*8;
        int slot = 9;
        if (List.size() > 0) switch (Sort) {
            case Name -> List.sort(new RuneSortName());
            case Level -> List.sort(new RuneSortLevel());
            case Quality -> List.sort(new RuneSortQuality());
        }
        if (SortReverse) Collections.reverse(List);
        for (int i = index; i < index+24; i++) {
            if (i < List.size()) {
                ItemStack item = List.get(i).viewRune(playerData.ViewFormat());
                ItemMeta meta = item.getItemMeta();
                List<String> Lore = new ArrayList<>(meta.getLore());
                Lore.add("§8" + i);
                meta.setLore(Lore);
                item.setItemMeta(meta);
                player.getInventory().setItem(slot, item);
            } else {
                player.getInventory().setItem(slot, AirItem);
            }
            slot++;
            if (slot == 17 || slot == 26) slot++;
        }
    }
}