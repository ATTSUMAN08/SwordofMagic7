package swordofmagic7.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import swordofmagic7.Data.PlayerData;
import swordofmagic7.Sound.SoundList;
import swordofmagic7.TagGame;

import java.util.HashMap;
import java.util.Map;

import static swordofmagic7.Data.DataBase.*;
import static swordofmagic7.Function.*;
import static swordofmagic7.Sound.CustomSound.playSound;

public class MapManager {

    private final Player player;
    private final PlayerData playerData;
    public String lastTeleportGate;

    public MapManager(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public void WarpGateSelector() {
        Location pLoc = player.getLocation();
        for (Map.Entry<String, WarpGateParameter> entry : WarpGateList.entrySet()) {
            if (entry.getValue().Location.distance(pLoc) < 2) {
                if (WarpGateList.containsKey(entry.getValue().Target) || entry.getValue().TargetLocation != null) {
                    entry.getValue().usePlayer(player);
                } else {
                    Log("§cError NotFundWarpGate: " + entry.getValue().Target + " at " + entry.getKey());
                }
            }
        }
    }

    public void TeleportGateSelector() {
        Location pLoc = player.getLocation();
        for (Map.Entry<String, TeleportGateParameter> entry : TeleportGateList.entrySet()) {
            TeleportGateParameter teleport = entry.getValue();
            if (teleport.Location.distance(pLoc) < 2) {
                if (!teleport.DefaultActive && !playerData.ActiveTeleportGate.contains(teleport.Id)) {
                    playerData.ActiveTeleportGate.add(teleport.Id);
                    player.sendMessage("§e[" + teleport.Display + "]§aを§b[有効化]§aしました");
                    playSound(player, SoundList.LevelUp);
                }
                lastTeleportGate = teleport.Id;
                if (playerData.PvPMode) {
                    player.sendMessage("§c[PvP中]§aは使用できません");
                    playSound(player, SoundList.Nope);
                } else if (TagGame.isPlayer(player)) {

                } else {
                    TeleportGateMenuView();
                }
            }
        }
    }

    public static final String TeleportGateMenuDisplay = "§lテレポート";
    private final HashMap<Integer, TeleportGateParameter> TeleportGateMenuCache = new HashMap<>();
    public void TeleportGateMenuView() {
        Inventory inv = decoInv(TeleportGateMenuDisplay, 3);
        for (Map.Entry<Integer, String> gui : TeleportGateMenu.entrySet()) {
            TeleportGateParameter teleport = TeleportGateList.get(gui.getValue());
            TeleportGateMenuCache.put(gui.getKey(), teleport);
            inv.setItem(gui.getKey(), teleport.view());
        }
        player.openInventory(inv);
        playSound(player, SoundList.MenuOpen);
    }

    public void TeleportGateMenuClick(InventoryView view, int Slot) {
        if (equalInv(view, TeleportGateMenuDisplay)) {
            if (TeleportGateMenuCache.containsKey(Slot)) {
                TeleportGateUse(TeleportGateMenuCache.get(Slot));
            }
        }
    }

    public void TeleportGateUse(TeleportGateParameter teleport) {
        if (teleport.DefaultActive || playerData.ActiveTeleportGate.contains(teleport.Id)) {
            player.teleportAsync(teleport.Location);
            player.sendTitle(teleport.Title, teleport.Subtitle, 20, 40, 20);
            playSound(player, SoundList.LevelUp);
            lastTeleportGate = teleport.Id;
        } else {
            player.sendMessage("§e[転移門]§aが§b[有効化]§aされていません");
            playSound(player, SoundList.Nope);
        }
    }

}


