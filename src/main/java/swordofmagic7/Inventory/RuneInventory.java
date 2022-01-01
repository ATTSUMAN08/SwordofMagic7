package swordofmagic7.Inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Data.Type.ViewInventoryType;
import swordofmagic7.Item.ItemParameter;
import swordofmagic7.Item.RuneParameter;
import swordofmagic7.Sound.SoundList;

import java.util.ArrayList;
import java.util.List;

import static swordofmagic7.Data.DataBase.AirItem;
import static swordofmagic7.Function.decoInv;
import static swordofmagic7.Function.equalInv;
import static swordofmagic7.Menu.Data.RuneMenuDisplay;
import static swordofmagic7.Menu.Menu.ignoreSlot;
import static swordofmagic7.Sound.CustomSound.playSound;

public class RuneInventory extends BasicInventory {
    private final java.util.List<RuneParameter> List = new ArrayList<>();

    public RuneInventory(Player player, PlayerData playerData) {
        super(player, playerData);
    }

    public List<RuneParameter> getList() {
        return List;
    }

    public void clear() {
        List.clear();
    }

    public void addRuneParameter(RuneParameter runeParameter) {
        if (List.size() < 300) {
            List.add(runeParameter.clone());
            if (List.size() >= 295) {
                player.sendMessage("§e[ルーンインベントリ]§aが§c残り" + (300 - List.size()) +"スロット§aです");
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

    public void viewRune() {
        playerData.ViewInventory = ViewInventoryType.RuneInventory;
        int index = ScrollTick*8;
        int slot = 9;
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