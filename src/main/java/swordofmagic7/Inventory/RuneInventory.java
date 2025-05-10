package swordofmagic7.Inventory;

import net.somrpg.swordofmagic7.SomCore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.DataBase;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Function.sendMessage;
import static swordofmagic7.Sound.CustomSound.playSound;

public class RuneInventory extends BasicInventory {
    public int MaxSlot;
    private final java.util.List<RuneParameter> List = new ArrayList<>();

    public RuneInventory(Player player, PlayerData playerData) {
        super(player, playerData);
        if (player.hasPermission(DataBase.Som7Premium)) {
            MaxSlot = 800;
        } else if (player.hasPermission(DataBase.Som7VIP)) {
            MaxSlot = 600;
        } else {
            MaxSlot = 500;
        }
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
            if (List.size() >= MaxSlot-10) {
                sendMessage(player, "§e[ルーンインベントリ]§aが§c残り" + (MaxSlot - List.size()) +"スロット§aです", SoundList.TICK);
            }
        } else {
            sendMessage(player, "§e[ルーンインベントリ]§aが§c満杯§aです", SoundList.NOPE);
            SomCore.instance.spawnPlayer(player);
        }
        List.add(runeParameter.clone());
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
        playSound(player, SoundList.CLICK);
        playerData.viewUpdate();
    }

    public void RuneInventorySortReverse() {
        SortReverse = !SortReverse;
        String msg = "§e[ルーンインベントリ]§aの§e[ソート順]§aを";
        if (!SortReverse) msg += "§b[昇順]";
        else msg += "§c[降順]";
        msg += "§aにしました";
        player.sendMessage(msg);
        playSound(player, SoundList.CLICK);
        playerData.viewUpdate();
    }

    public synchronized void viewRune() {
        playerData.ViewInventory = ViewInventoryType.RuneInventory;
        int index = ScrollTick*8;
        int slot;
        try {
            if (!List.isEmpty()) switch (Sort) {
                case Name -> List.sort(new RuneSortName());
                case Level -> List.sort(new RuneSortLevel());
                case Quality -> List.sort(new RuneSortQuality());
            }
            if (SortReverse) Collections.reverse(List);
        } catch (Exception e) {
            sendMessage(player, "§eソート処理中§aに§cエラー§aが発生したため§eソート処理§aを§e中断§aしました §c" + e.getMessage());
        }
        int i = index;
        for (slot = 9; slot < 36; slot++) {
            if (i < List.size()) {
                while (i < List.size()) {
                    RuneParameter rune = List.get(i);
                    if (wordSearch == null || rune.Id.contains(wordSearch)) {
                        ItemStack item = rune.viewRune(playerData.ViewFormat(), false);
                        ItemMeta meta = item.getItemMeta();
                        List<String> Lore = new ArrayList<>(meta.getLore());
                        Lore.add("§8SlotID:" + i);
                        meta.setLore(Lore);
                        item.setItemMeta(meta);
                        player.getInventory().setItem(slot, item);
                        i++;
                        break;
                    }
                    i++;
                }
            } else {
                player.getInventory().setItem(slot, AirItem);
            }
            if (slot == 16 || slot == 25) slot++;
        }
    }
}